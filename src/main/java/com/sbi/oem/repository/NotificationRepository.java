package com.sbi.oem.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	@Query(value = "SELECT * FROM notification where user_id = :userId and is_seen = false ORDER BY created_at DESC", nativeQuery = true)
	List<Notification> findByUserId(Long userId);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE notification SET is_seen = true WHERE user_id = :userId", nativeQuery = true)
	int markAsSeen(Long userId);
	
	@Transactional
    @Modifying
    @Query(value = "DELETE FROM notification WHERE is_seen = true", nativeQuery = true)
    void deleteByIsSeenTrue();
}
