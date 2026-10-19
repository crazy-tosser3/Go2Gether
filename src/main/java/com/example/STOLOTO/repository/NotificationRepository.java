package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Notification;

public interface NotificationRepository extends ListCrudRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
}
