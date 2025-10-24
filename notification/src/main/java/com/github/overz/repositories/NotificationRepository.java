package com.github.overz.repositories;

import com.github.overz.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
	Optional<Notification> findByCdOrder(String cdOrder);
}
