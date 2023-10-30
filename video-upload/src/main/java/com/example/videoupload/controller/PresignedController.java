package com.example.videoupload.controller;

import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.example.videoupload.service.PresignedService;

@RestController
public class PresignedController {

    @Autowired
    public PresignedService presignedService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/presigned")
    public ResponseEntity<URL> getPresignedUrl() {
        URL url = presignedService.createSignedUrlForStringPut("toktik-bucket", UUID.randomUUID().toString());

        return ResponseEntity.ok(url);
    }
    
}
