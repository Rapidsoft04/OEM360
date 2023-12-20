package com.sbi.oem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationMessages;

@Repository
public interface RecommendationMessagesRepository extends JpaRepository<RecommendationMessages, Long> {

}
