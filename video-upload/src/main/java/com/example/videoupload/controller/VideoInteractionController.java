package com.example.videoupload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.videoupload.service.VideoInteractionService;

@RestController
public class VideoInteractionController {

    @Autowired
    VideoInteractionService videoInteractionService;

    @CrossOrigin
    @PutMapping("/like/{username}/{videoUUID}")
    public ResponseEntity<String> likeVideo(@PathVariable String username, @PathVariable String videoUUID) {
        videoInteractionService.addLike(username, videoUUID);
        return ResponseEntity.ok(username + " liked a video");
    }

    @CrossOrigin
    @DeleteMapping("/removeLike/{username}/{videoUUID}")
    public ResponseEntity<String> removeLikeFromVideo(@PathVariable String username, @PathVariable String videoUUID) {
        videoInteractionService.removeLike(username, videoUUID);
        return ResponseEntity.ok(username + " unliked a video");
    }
}
