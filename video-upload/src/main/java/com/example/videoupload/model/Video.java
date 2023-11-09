package com.example.videoupload.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="Video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String status;
    private String username;
    private String originalName;
    @Column(unique = true)
    private String uuidName;
    private Long views = 0L;

    @ElementCollection
    @CollectionTable(name = "video_likes", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "like_username")
    private List<String> likes = new ArrayList<>();

    public Video() {}

    public Video(String username, String originalName, String uuidName) {
        this.username = username;
        this.originalName = originalName;
        this.uuidName = uuidName;
    }

    public Video(String status) {
        this.status = status;
    }

    public Video(List<String> likes) {
        this.likes = likes;
    }
}
