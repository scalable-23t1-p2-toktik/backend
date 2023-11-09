package com.example.videoupload.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.example.videoupload.model.Video;
import com.example.videoupload.repository.VideoRepository;

import io.github.cdimascio.dotenv.Dotenv;

public class HandlerService implements Runnable{
    
    private VideoRepository videoRepository;

    public HandlerService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    Dotenv dotenv = Dotenv.configure().load();

    String hostname = dotenv.get("REDIS_HOST");
    int port = Integer.parseInt(dotenv.get("REDIS_PORT"));
    
    public void run() {
        
        Jedis jedis = new JedisPool(hostname, port).getResource();

        while (true) {
            String message = jedis.brpop(0, "ffmpeg_response_channel").get(1);
            System.out.println(message);

            try {
                String[] splitted = message.split(":");
                String status = splitted[0];
                // String username = splitted[1];
                String directory = splitted[2];
                
                if (status.equals("200")) {
                    String videoUUID = directory.split("/")[1];
                    // System.out.println(Arrays.toString(directory.split("/")));
                    // System.out.println(videoUUID);
                    Video completedVideo = videoRepository.findByUuidName(videoUUID);
                    completedVideo.setStatus(status);
                    videoRepository.save(completedVideo);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
    }
}
