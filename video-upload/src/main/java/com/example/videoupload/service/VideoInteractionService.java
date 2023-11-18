package com.example.videoupload.service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.videoupload.model.Comment;
import com.example.videoupload.model.Notification;
import com.example.videoupload.model.NotificationMessage;
import com.example.videoupload.model.Video;
import com.example.videoupload.repository.NotificationRepository;
import com.example.videoupload.repository.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.Jedis;

@Service
public class VideoInteractionService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    NotificationRepository notificationRepository;
    
    public void addLike(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> likes = getVideo.getLikes();

        if (likes.contains(username)) {
            return;
        }

        // Send notification on like
        List<String> vips = getVideo.getVip();
        for (String vip : vips) {
            if (vip.equals(username)) {
                continue;
            }
            sendLikeNotification(vip, username, videoUUID);   
        }
        
        addVip(username, videoUUID);

        likes.add(username);
        getVideo.setLikes(likes);
        videoRepository.save(getVideo);
    }

    public void removeLike(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> likes = getVideo.getLikes();
        List<Comment> comments = getVideo.getComments();

        if (likes.contains(username)) {
            likes.remove(username);
        } else {
            return;
        }
        
        // Check whether the user has a comment
        boolean hasCommentWithUsername = comments.stream().anyMatch(comment -> comment.getUsername().equals(username));
        // Remove from vip if not
        if (!hasCommentWithUsername) {
            removeVip(username, videoUUID);
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

        // Send comment notification
        List<String> vips = getVideo.getVip();
        for (String vip : vips) {
            if (vip.equals(username)) {
                continue;
            }
            sendCommentNotification(vip, username, videoUUID, text);
        }

        addVip(username, videoUUID);

        getVideo.getComments().add(comment);
        videoRepository.save(getVideo);
    }

    public void removeComment(String username, String videoUUID, String text) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> likes = getVideo.getLikes();
        Iterator<Comment> iterator = getVideo.getComments().listIterator();

        while (iterator.hasNext()) {
            Comment comment = iterator.next();
            if (comment.getText().equals(text) && comment.getUsername().equals(username)) {
                iterator.remove();
                break;
            }
        }
        // Check whether the user is in like list and remove from vip if they're not
        if (!likes.contains(username)) {
            removeVip(username, videoUUID);
        }

        videoRepository.save(getVideo);
    }

    public void addVip(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> vips = getVideo.getVip();

        if (getVideo.getUsername().equals(username)) {
            return;
        }

        if (!vips.contains(username)) {
            vips.add(username);
            getVideo.setVip(vips);
        } else {
            return;
        }

        videoRepository.save(getVideo);
    }

    public void removeVip(String username, String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        List<String> vips = getVideo.getVip();

        if (getVideo.getUsername().equals(username)) {
            return;
        }

        if (vips.contains(username)) {
            vips.remove(username);
            getVideo.setVip(vips);
        } else {
            return;
        }

        videoRepository.save(getVideo);
    }

    // TODO: remove from the S3 bucket too
    public void removeVideo(String videoUUID) {
        Video getVideo = videoRepository.findByUuidName(videoUUID);
        videoRepository.delete(getVideo);
    }

    Dotenv dotenv = Dotenv.configure().load();

	String hostname = dotenv.get("REDIS_HOST");
	int port = Integer.parseInt(dotenv.get("REDIS_PORT"));

    Jedis jedis = new Jedis(hostname, port);

    // Handle like notification
    public void sendLikeNotification(String vip, String username, String videoUUID) {
        try {
            Video getVideo = videoRepository.findByUuidName(videoUUID);
            String likeMessage = username + " has liked " + getVideo.getOriginalName();

            // add notification to database
            Notification notification = new Notification(vip, likeMessage, false);
            Long getId = notificationRepository.save(notification).getId();

            NotificationMessage notificationMessage = new NotificationMessage(getId, likeMessage);
            String message = objectMapper.writeValueAsString(notificationMessage);

            // jedis.lpush(vip + "-notification", message);
            jedis.publish(vip + "-notification", message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    // Handle comment notification
    public void sendCommentNotification(String vip, String username, String videoUUID, String text) {
        try {
            Video getVideo = videoRepository.findByUuidName(videoUUID);
            String commentMessage = username + " has commented: " + text + ", in " + getVideo.getOriginalName();

            // add notification to the database
            Notification notification = new Notification(vip, commentMessage, false);
            Long getId = notificationRepository.save(notification).getId();

            NotificationMessage notificationMessage = new NotificationMessage(getId, commentMessage);
            String message = objectMapper.writeValueAsString(notificationMessage);

            // jedis.lpush(vip + "-notification", message);
            jedis.publish(vip + "-notification", message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    // Change is_read to true
    public void readNotification(Long id) {
        Notification getNotification = notificationRepository.findById(id).get();
        getNotification.set_read(true);
        notificationRepository.save(getNotification);
    }

    public List<Notification> getNotifications(String username) {
        List<Notification> getNotification = notificationRepository.findByUsername(username);

        // Handles when user open all notification, to set all the notifcationsto read
        for (Notification notification : getNotification) {
            readNotification(notification.getId());
        }

        return getNotification;
    }

    public void getUnReadNotifications(String username) {
        try {
            List<Notification> getNotifications = notificationRepository.findByUsernameAndStatus(username);
            for (Notification notification : getNotifications) {
                NotificationMessage notificationMessage = new NotificationMessage(notification.getId(), notification.getText());
                String message = objectMapper.writeValueAsString(notificationMessage);

                jedis.publish(username + "-notification", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }
}
