package com.example.videoupload.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.example.videoupload.model.Video;
import com.example.videoupload.repository.VideoRepository;

public class HandlerService implements Runnable{
    
    private VideoRepository videoRepository;

    public HandlerService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }
    
    public void run() {
        
        Jedis jedis = new JedisPool("localhost", 6379).getResource();

        System.out.println("hi");
        String message = jedis.brpop(0, "ffmpeg_response_channel").get(1);
        System.out.println(message);

        try {
            String[] splitted = message.split(":");
            String status = splitted[0];
            // String username = splitted[1];
            String directory = splitted[2];

            String videoUUID = directory.split("/")[1];
            // System.out.println(Arrays.toString(directory.split("/")));
            // System.out.println(videoUUID);

            if (status.equals("200")) {
                Video completedVideo = videoRepository.findByUuidName(videoUUID);
                completedVideo.setStatus(status);
                videoRepository.save(completedVideo);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }
}
