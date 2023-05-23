package com.fowlart.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class FileAccepterController {

    private final static Logger logger = LoggerFactory.getLogger(FileAccepterController.class);
    @GetMapping("/accept-file")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        var filename = file.getOriginalFilename();
        var fileInStore = new File("/botstore/received_files/"+filename);
        logger.info("file received: {}", filename);
        try {
            file.transferTo(fileInStore);
        } catch (IOException e) {
            return e.getMessage();
        }
        return "file uploaded successfully";
    }
}
