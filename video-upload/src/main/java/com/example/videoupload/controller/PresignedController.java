package com.example.videoupload.controller;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import redis.clients.jedis.Jedis;

import com.example.videoupload.service.PresignedService;

@RestController
public class PresignedController {

    @Autowired
    public PresignedService presignedService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/presigned/{username}")
    public ResponseEntity<URL> getPresignedUrl(@PathVariable String username) {
        Jedis jedis = new Jedis("localhost", 6379);

        String uid =  UUID.randomUUID().toString();

        URL url = presignedService.createSignedUrlForStringPut("toktik-bucket",  uid);

        if (url != null) {
            try {
                String message = username + ":" + uid;
                jedis.lpush("ffmpeg_channel", message);
            } finally {
                jedis.close();
            }
            return ResponseEntity.ok(url);
        } else {
            jedis.close();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/redis")
    public ResponseEntity<String> showRedis() {
        Jedis jedis = new Jedis("localhost", 6379);
        List<String> messages = jedis.lrange("ffmpeg_channel", 0, -1);
        jedis.close();

        for (String msg : messages) {
            System.out.println(msg);
        }

        return ResponseEntity.ok("ok");
    }
    
}
