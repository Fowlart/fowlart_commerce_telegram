package com.fowlart.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class FileAccepterController {

    private final static Logger logger = LoggerFactory.getLogger(FileAccepterController.class);

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
        var filename = file.getOriginalFilename();

        var fileInStore = new File("/botstore/item_imgs/" + filename);

        logger.info("image received: {}", filename);
        logger.info("accepted userId {}, itemIs {}", userID, itemID);

        if (!file.getContentType().contains("image")) {
            return "Rejected! Not an image.";
        }

        try {
            file.transferTo(fileInStore);
        } catch (IOException e) {
            return e.getMessage();
        }

        return "File uploaded successfully";
    }
}
