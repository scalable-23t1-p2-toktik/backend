package com.example.videoupload.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// TODO: this
@Entity
@Getter
@Setter
@Table(name="Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String text;
    private boolean is_read;

    public Notification () {}

    public Notification (String username, String text, boolean is_read) {
        this.username = username;
        this.text = text;
        this.is_read = is_read;
    }

    public Notification (Long id, String username, String text, boolean is_read) {
        this(username, text, is_read);
        this.id = id;
    }
}

