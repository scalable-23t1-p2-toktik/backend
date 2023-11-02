package com.example.videoupload.service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.example.videoupload.repository.VideoRepository;

@Component
public class StartUpService implements ApplicationListener < ApplicationReadyEvent > {

    @Autowired
    VideoRepository videoRepository;


    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new HandlerService(videoRepository));
        return;
    }
}
