package com.example.videoupload.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.videoupload.model.Video;
import com.example.videoupload.repository.VideoRepository;

public class SocketIOEventListenser {

    @Autowired
    VideoRepository videoRepository;
    
    @OnEvent("like")
    public void handleLikeEvent(SocketIOClient client, AckRequest ackRequest, Map<String, String> eventData) {
        String username = eventData.get("username");
        String uuidName = eventData.get("uuidName");

        System.out.println("Received like event for video: " + uuidName + " from user: " + username);

        Video getVideo = videoRepository.findByUuidName(uuidName);
        List<String> likes = getVideo.getLikes();

        if (likes.contains(username)) {
            return;
        }

        likes.add(username);
        getVideo.setLikes(likes);
        videoRepository.save(getVideo);

        client.getNamespace().getBroadcastOperations().sendEvent("like", eventData);
    }
}
