package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationMessages;

@Repository
public interface RecommendationMessagesRepository extends JpaRepository<RecommendationMessages, Long> {

	List<RecommendationMessages> findAllByReferenceId(String refId);

}
