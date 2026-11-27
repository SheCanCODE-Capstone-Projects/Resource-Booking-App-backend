package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}