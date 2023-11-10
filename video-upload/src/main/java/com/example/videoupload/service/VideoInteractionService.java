package com.example.videoupload.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.videoupload.model.Comment;
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

    public void addComment(String username, String videoUUID, String text) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        Comment comment = new Comment();

        comment.setUsername(username);
        comment.setText(text);
        comment.setDateTime(LocalDateTime.now());

        getVideo.getComments().add(comment);
        videoRepository.save(getVideo);
    }

    public void removeComment(String username, String videoUUID, String text) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<Comment> userComments = getVideo.getComments();
        Comment comment = new Comment();

        for (Comment comments : userComments) {
            if (comments.getText().equals(text)) {  
                comment = comments;
                break;
            }
        }

        userComments.remove(comment);

        videoRepository.save(getVideo);
    }
}
