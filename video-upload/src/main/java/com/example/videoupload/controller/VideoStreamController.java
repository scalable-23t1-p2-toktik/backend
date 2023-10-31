package com.example.videoupload.controller;

import java.io.File;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonServiceException;
import com.example.videoupload.service.VideoStreamService;

import software.amazon.awssdk.transfer.s3.S3TransferManager;

@RestController
public class VideoStreamController {

    @Autowired
    VideoStreamService videoStreamService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/stream/{key}")
    public ResponseEntity<URL> getPresignedUrl(@PathVariable String key) {
        try {
            URL playlistUrl = videoStreamService.getPresignUrl(key);

            return ResponseEntity.ok(playlistUrl);
        } catch (AmazonServiceException e) {
            System.err.println("Amazon S3 service exception: " + e.getErrorMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    // @CrossOrigin(origins = "http://localhost:3000")
    // @GetMapping("/video/{key}")
    // public ResponseEntity<URL> modifyM3u8(@PathVariable String key) {
        
    // }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/download/{key}")
    public ResponseEntity<String> download(@PathVariable String key) {
        S3TransferManager transferManager = S3TransferManager.create();
        try {
            Long contentLength = videoStreamService.downloadFile(transferManager,"toktik-bucket", key + ".m3u8", "playlist/" + key + ".m3u8");
            return ResponseEntity.ok("File downloaded. Content length: " + contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file download.");
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/upload/{key}")
    public ResponseEntity<String> upload(@PathVariable String key) {
        S3TransferManager transferManager = S3TransferManager.create();
        try {
            String res = videoStreamService.uploadFile(transferManager, "toktik-bucket", key, "playlist/" + key);
            return ResponseEntity.ok("File Uploaded. " + res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file upload.");
        }
    }

    @GetMapping("/modify/{key}")
    public ResponseEntity<String> modify(@PathVariable String key) {
        S3TransferManager transferManager = S3TransferManager.create();
        try {
            Long contentLength = videoStreamService.downloadFile(transferManager,"toktik-bucket", "test/" + key + ".m3u8", "playlist/" + key + ".m3u8");
            ResponseEntity.ok("File downloaded. Content length: " + contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file download.");
        }

        try {
            videoStreamService.modifyM3u8("playlist/" + key + ".m3u8");
            ResponseEntity.ok("Finish modifying file.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file download.");
        }

        try {
            String res = videoStreamService.uploadFile(transferManager, "toktik-bucket", key + ".m3u8", "playlist/" + key + ".m3u8");
            ResponseEntity.ok("Finish modifying and uploading file." + res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file upload.");
        }

        File file = new File("playlist/" + key + ".m3u8");
        if (file.delete()) {
            return ResponseEntity.ok("Successfully modify and upload file.");
        } else {
            return ResponseEntity.status(500).body("Error occurred while deleting the file.");
        }
    }

}
