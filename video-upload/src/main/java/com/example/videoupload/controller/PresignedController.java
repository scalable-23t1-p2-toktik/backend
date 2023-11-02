package com.example.videoupload.controller;

import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import redis.clients.jedis.Jedis;

import com.example.videoupload.model.UploadTicket;
import com.example.videoupload.model.Video;
import com.example.videoupload.repository.VideoRepository;
import com.example.videoupload.service.PresignedService;

@RestController
public class PresignedController {

    @Autowired
    public PresignedService presignedService;

    @Autowired
    VideoRepository videoRepository;

    Jedis jedis = new Jedis("localhost", 6379);

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/presigned")
    public ResponseEntity<UploadTicket> getPresignedUrl() {

        String videoUUID =  UUID.randomUUID().toString();

        URL url = presignedService.createSignedUrlForStringPut("toktik-bucket",  videoUUID);
        UploadTicket ticket = new UploadTicket(videoUUID, url.toString());

        return ResponseEntity.ok(ticket);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/notify/{username}/{originalVideo}/{videoUUID}")
    public ResponseEntity<String> notifyDoneUpload(
        @PathVariable String username, @PathVariable String originalVideo, @PathVariable String videoUUID) {
        try {
                String message = username + ":" + videoUUID;
                System.out.println(message);
                jedis.lpush("ffmpeg_channel", message);
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            } finally {
                jedis.close();
            }

        videoRepository.save(new Video(username, originalVideo, videoUUID));
        return ResponseEntity.ok().build();
    }
    
}
