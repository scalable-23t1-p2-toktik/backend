package com.example.videoupload.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.videoupload.model.Video;
import com.example.videoupload.repository.VideoRepository;

@Service
public class VideoInteractionService {

    @Autowired
    VideoRepository videoRepository;
    
    public void addLike(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> likes = getVideo.getLikes();

        if (likes.contains(username)) {
            return;
        }

        likes.add(username);
        getVideo.setLikes(likes);
        videoRepository.save(getVideo);
    }

    public void removeLike(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> likes = getVideo.getLikes();

        if (likes.contains(username)) {
            likes.remove(username);
        } else {
            return;
        }

        getVideo.setLikes(likes);
        videoRepository.save(getVideo);
    }

    public void increaseViewCount(String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        getVideo.setViews(getVideo.getViews() + 1);
        videoRepository.save(getVideo);
    }

}
