package com.fowlart.main.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@RestController
public class FileAccepterController {

    private final static Logger logger = LoggerFactory.getLogger(FileAccepterController.class);

    private final Catalog catalog;

    private final String botAdminsList;

    private final String containerName;

    private final String connectionString;

    private final BlobContainerClient containerClient;

    public FileAccepterController(@Autowired Catalog catalog,
                                  @Value("${app.bot.admins}") String botAdminsList,
                                  @Value("${azure.storage.container.name}") String containerName,
                                  @Value("${azure.storage.connection.string}") String connectionString) {
        this.catalog = catalog;
        this.botAdminsList = botAdminsList;
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.containerClient = getBlobContainerClient();
    }

    private BlobContainerClient getBlobContainerClient() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(Objects.requireNonNull(connectionString)).buildClient();
        return blobServiceClient.createBlobContainerIfNotExists(containerName);
    }

    @PostMapping("/accept-file")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        var filename = file.getOriginalFilename();

        var fileInStore = new File("/botstore/received_files/" + filename);

        logger.info("file received: {}", filename);

        try {
            file.transferTo(fileInStore);
        } catch (IOException e) {
            return e.getMessage();
        }
        return "file uploaded successfully";
    }

    @PostMapping("/accept-img")
    public String handleImageUpload(@RequestParam("file") MultipartFile receivedFile,
                                    @RequestParam String userID,
                                    @RequestParam String itemID) {

        var fileExtension = receivedFile.getOriginalFilename().substring(Math.max(receivedFile.getOriginalFilename().length() - 3, 0));

        var itemName = catalog.getItemList().stream().filter(it -> itemID.equals(it.id())).map(Item::name).findFirst().orElse("_none_");

        var fileInContainer = itemName.replaceAll("/","_") + "." + fileExtension;

        if (!Objects.requireNonNull(receivedFile.getContentType()).contains("image")) {
            return "Rejected! Not an image.";
        }

        if (!Arrays.stream(botAdminsList.split(",")).anyMatch(userID::equals)) {
            return "Rejected! Not a Administrator.";
        }

        try {
            logger.info("Attempt to store file in container {}: {}", this.containerClient.getBlobContainerName(),fileInContainer);

            BlobClient blobClient = this.containerClient.getBlobClient(fileInContainer);

            blobClient.upload(receivedFile.getInputStream(),true);

            logger.info("Success!");
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "File uploaded successfully!";
    }
}
