package com.example.videoupload.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.videoupload.model.Notification;
import com.example.videoupload.repository.NotificationRepository;
import com.example.videoupload.service.VideoInteractionService;

@RestController
public class VideoInteractionController {

    @Autowired
    VideoInteractionService videoInteractionService;

    @Autowired
    NotificationRepository notificationRepository;

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

    @CrossOrigin
    @PostMapping("/comment/{username}/{videoUUID}")
    public ResponseEntity<String> addCommentToVideo(
        @PathVariable String username, @PathVariable String videoUUID, @RequestParam String text) {
            videoInteractionService.addComment(username, videoUUID, text);
            return ResponseEntity.ok(username + " has added commented: " + text + " to " + videoUUID);
        }

    @CrossOrigin
    @DeleteMapping("/removeComment/{username}/{videoUUID}")
    public ResponseEntity<String> removeCommentFromVideo(
        @PathVariable String username, @PathVariable String videoUUID, @RequestParam String text) {
            videoInteractionService.removeComment(username, videoUUID, text);
            return ResponseEntity.ok(username + " has removed their comment: " + text + " from " + videoUUID);
        }

    @CrossOrigin
    @GetMapping("/notification/{username}")
    public ResponseEntity<List<Notification>> getNotification(@PathVariable String username) {
        return ResponseEntity.ok(videoInteractionService.getNotifications(username));
    }

    @CrossOrigin
    @PutMapping("/readNotification/{id}")
    public ResponseEntity<String> readNotification(@PathVariable Long id) {
        videoInteractionService.readNotification(id);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping("/getUnReadNotification/{username}")
    public ResponseEntity<String> getUnReadNotification(@PathVariable String username) {
        videoInteractionService.getUnReadNotifications(username);
        return ResponseEntity.ok().build();
    }
}
