package com.example.videoupload.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String status;
    private String username;
    private String originalName;
    @Column(unique = true)
    private String uuidName;

    public Video(String username, String originalName, String uuidName) {
        this.username = username;
        this.originalName = originalName;
        this.uuidName = uuidName;
    }

    public Video(String status) {
        this.status = status;
    }
}
