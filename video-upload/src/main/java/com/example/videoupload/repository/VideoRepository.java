package com.example.videoupload.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.videoupload.model.Video;
import java.util.List;


@Repository
public interface VideoRepository extends CrudRepository<Video, Long> {

    Video findByUuidName(String uuidName);

    List<Video> findByStatus(String status);
}
