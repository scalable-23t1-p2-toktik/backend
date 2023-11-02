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

    @CrossOrigin(origins = "http://localhost:3000")
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

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/stream/{key}")
    public ResponseEntity<String> getStream(@PathVariable String key) {
        try {
            S3TransferManager transferManager = S3TransferManager.create();

            // Download the M3U8 file from S3 to a temp folder
            Long contentLength = downloadM3u8File(transferManager, key);
            if (contentLength == null) {
                return ResponseEntity.status(500).body("Error occurred during file download.");
            }

            // Modify the M3U8 file by getting the presigned url of the chuncks
            boolean modificationResult = modifyM3u8File(key);
            if (!modificationResult) {
                return ResponseEntity.status(500).body("Error occurred during file modification.");
            }

            // Upload the modified M3U8 file back to S3
            String uploadResult = uploadModifiedM3u8File(transferManager, key);
            if (uploadResult == null) {
                return ResponseEntity.status(500).body("Error occurred during file upload.");
            }

            // Delete the m3u8 file from the temp folder
            boolean deletionResult = deleteM3u8File(key);
            if (!deletionResult) {
                return ResponseEntity.status(500).body("Error occurred while deleting the file.");
            }

            // Return the presigned url of the modified m3u8 file
            String result = videoStreamService.getPresignUrl("playlist.m3u8").toString();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    // TODO: Parse in the UUID name of the folder of the chuncks (Still hard coded)
    private Long downloadM3u8File(S3TransferManager transferManager, String key) {
        return videoStreamService.downloadFile(transferManager, "toktik-bucket", "hls/" + key + "/playlist.m3u8", "playlist/playlist.m3u8");
    }

    private boolean modifyM3u8File(String key) {
        try {
            videoStreamService.modifyM3u8("playlist/playlist.m3u8", key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String uploadModifiedM3u8File(S3TransferManager transferManager, String key) {
        return videoStreamService.uploadFile(transferManager, "toktik-bucket", "playlist.m3u8", "playlist/playlist.m3u8");
    }

    private boolean deleteM3u8File(String key) {
        File file = new File("playlist/playlist.m3u8");
        return file.delete();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/thumbnail/{key}")
    public ResponseEntity<String> getThumbnail(@PathVariable String key) {

        return ResponseEntity.ok(videoStreamService.getPresignUrl("hls/" + key + "/thumbnail.jpg").toString());
    }

}
