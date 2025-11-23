package com.iusjc.weschedule.repositories;

import com.iusjc.weschedule.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}