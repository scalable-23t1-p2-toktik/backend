package com.example.videoupload.model;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
// @Table(name="comments")
@Getter
@Setter
public class Comment {

    private String username;

    private String text;

    private LocalDateTime dateTime;
}
