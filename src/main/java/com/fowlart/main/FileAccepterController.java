package com.fowlart.main;

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

    public FileAccepterController(@Autowired Catalog catalog,
                                  @Value("${app.bot.admins}") String botAdminsList) {
        this.catalog = catalog;
        this.botAdminsList = botAdminsList;
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
    public String handleImageUpload(@RequestParam("file") MultipartFile file,
                                    @RequestParam String userID,
                                    @RequestParam String itemID) {



        var fileExtension = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];

        var itemName = catalog
                .getItemList()
                .stream()
                .filter(it->itemID.equals(it.id()))
                .map(Item::name)
                .findFirst()
                .orElse("_none_");

        var fileInStore = new File("/botstore/item_imgs/" + itemName+"."+fileExtension);

        if (!Objects.requireNonNull(file.getContentType()).contains("image")) {
            return "Rejected! Not an image.";
        }

        if (!Arrays.stream(botAdminsList.split(",")).allMatch(userID::equals)) {
            return "Rejected! Not a Administrator.";
        }

        try {
            logger.info("Attempt to store file in storage: {}", fileInStore);
            file.transferTo(fileInStore);
            logger.info("Success!");
        } catch (IOException e) {
            return e.getMessage();
        }

        return "File uploaded successfully!";
    }
}
