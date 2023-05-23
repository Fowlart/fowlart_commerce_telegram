package com.fowlart.main;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class FileAccepterController {

    @PostMapping("/accept-file")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {

        var filename = file.getOriginalFilename();
        var fileInStore = new File("/botstore/received_files/"+filename);
        try {
            file.transferTo(fileInStore);
        } catch (IOException e) {
            return e.getMessage();
        }
        return "file uploaded successfully";
    }
}
