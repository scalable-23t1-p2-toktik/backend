package com.example.videoupload.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.videoupload.model.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long>{
    List<Notification> findByUsername(String username);

    @Query("SELECT n FROM Notification n WHERE n.username = :username AND n.is_read = false")
    List<Notification> findByUsernameAndStatus(String username);
}
