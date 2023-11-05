package com.fowlart.main.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fowlart.main.state.Catalog;
import com.fowlart.main.state.cosmos.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiPredicate;

@RestController
public class AdminController {

    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final Catalog catalog;

    private final String containerName;

    private final String connectionString;

    private final BlobContainerClient containerClient;

    public AdminController(@Autowired Catalog catalog,
                           @Value("${azure.storage.container.name}") String containerName,
                           @Value("${azure.storage.connection.string}") String connectionString) {
        this.catalog = catalog;
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.containerClient = getBlobContainerClient();
    }

    private BlobContainerClient getBlobContainerClient() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(Objects.requireNonNull(connectionString)).buildClient();
        return blobServiceClient.createBlobContainerIfNotExists(containerName);
    }

    @PostMapping("/accept-img")
    public String handleImageUpload(@RequestParam("file") MultipartFile receivedFile,
                                    @RequestParam String itemID) {

        var fileExtension = receivedFile.getOriginalFilename().substring(Math.max(receivedFile.getOriginalFilename().length() - 3, 0));

        var itemName = catalog.getItemList().stream().filter(it -> itemID.equals(it.id())).map(Item::name).findFirst().orElse("_none_");

        var fileInContainer = itemName.replaceAll("/","_") + "." + fileExtension;

        if (!Objects.requireNonNull(receivedFile.getContentType()).contains("image")) {
            return "Rejected! Not an image.";
        }

        // Todo: check if user is admin
        if (false) {
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
