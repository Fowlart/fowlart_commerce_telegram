package com.fowlart.main.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fowlart.main.state.Catalog;
import com.fowlart.main.state.inmem.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

@RestController
public class AdminController {

    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final Catalog catalog;

    private final String containerName;

    private final String connectionString;

    private final BlobContainerClient containerClient;

    private final String appHost;

    public AdminController(@Autowired Catalog catalog, @Value("${app.host}") String appHost, @Value("${azure.storage.container.name}") String containerName, @Value("${azure.storage.connection.string}") String connectionString) {
        this.appHost = appHost;
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
    public String handleImageUpload(@RequestParam("file") MultipartFile receivedFile, @RequestParam String itemID, HttpServletRequest request) {

        //todo: security check here

        var fileExtension = receivedFile.getOriginalFilename().substring(Math.max(receivedFile.getOriginalFilename().length() - 3, 0));

        var itemName = catalog.getItemList().stream().filter(it -> itemID.equals(it.id())).map(Item::name).findFirst().orElse("_none_");

        var fileInContainer = itemName.replaceAll("/", "_") + "." + fileExtension;

        if (!Objects.requireNonNull(receivedFile.getContentType()).contains("image")) {
            return "Rejected! Not an image.";
        }

        try {
            logger.info("Attempt to store file in container {}: {}", this.containerClient.getBlobContainerName(), fileInContainer);

            BlobClient blobClient = this.containerClient.getBlobClient(fileInContainer);

            blobClient.upload(receivedFile.getInputStream(), true);

            logger.info("Success!");
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "File uploaded successfully!";
    }
}
