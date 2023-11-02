package com.example.videoupload.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.videoupload.model.Video;

@Repository
public interface VideoRepository extends CrudRepository<Video, Long> {

    Video findByUuidName(String uuidName);
}
