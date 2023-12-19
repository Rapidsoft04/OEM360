package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	@Query(value = "SELECT * FROM notification where user_id = :userId and is_seen = true", nativeQuery = true)
	List<Notification> findByUserId(Long userId);
	
	@Query(value = "UPDATE notification SET is_seen = true WHERE user_id = :userId", nativeQuery = true)
	int markAsSeen(Long userId);
}
