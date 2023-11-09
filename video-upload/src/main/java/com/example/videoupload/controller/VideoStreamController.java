package com.example.videoupload.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.example.videoupload.service.VideoInteractionService;
import com.example.videoupload.service.VideoStreamService;

import software.amazon.awssdk.transfer.s3.S3TransferManager;

@RestController
public class VideoStreamController {

    @Autowired
    VideoStreamService videoStreamService;

    @Autowired
    VideoInteractionService videoInteractionService;

    @CrossOrigin
    @GetMapping("/getPresigned/{key}")
    public ResponseEntity<URL> getPresignedUrl(@PathVariable String key) {
        try {
            URL playlistUrl = videoStreamService.getPresignUrl(key);

            return ResponseEntity.ok(playlistUrl);
        } catch (AmazonServiceException e) {
            System.err.println("Amazon S3 service exception: " + e.getErrorMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @CrossOrigin
    @GetMapping("/download/{key}")
    public ResponseEntity<String> download(@PathVariable String key) {
        S3TransferManager transferManager = S3TransferManager.create();
        try {
            Long contentLength = videoStreamService.downloadFile(transferManager,"toktik-bucket", "hls/" + key + "/playlist.m3u8", "playlist/playlist.m3u8");
            return ResponseEntity.ok("File downloaded. Content length: " + contentLength);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error occurred during file download.");
        }
    }

    @CrossOrigin
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

    @CrossOrigin
    @GetMapping("/stream/{key}")
    public ResponseEntity<StreamingResponseBody> readFile(@PathVariable String key) {

        StreamingResponseBody responseBody = outputStream -> {
            try {
                S3Object data = videoStreamService.readFile(key);

                try (InputStream objectData = data.getObjectContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(objectData, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains(".ts")) {
                            String preSignedUrl = videoStreamService.getPresignUrl("hls/" + key + "/" + line).toString();
                            line = preSignedUrl; 
                        }
                        outputStream.write(line.getBytes("UTF-8"));
                        outputStream.write("\n".getBytes("UTF-8"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        videoInteractionService.increaseViewCount(key);

        return ResponseEntity.ok().body(responseBody);
    }

    @CrossOrigin
    @GetMapping("/thumbnail/{key}")
    public ResponseEntity<String> getThumbnail(@PathVariable String key) {

        return ResponseEntity.ok(videoStreamService.getPresignUrl("hls/" + key + "/thumbnail.jpg").toString());
    }

}
