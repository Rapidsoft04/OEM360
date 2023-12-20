package com.sbi.oem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationDeplyomentDetails;

@Repository
public interface RecommendationDeplyomentDetailsRepository
		extends JpaRepository<RecommendationDeplyomentDetails, Long> {

	Optional<RecommendationDeplyomentDetails> findByRecommendRefId(String recommendRefId);

}
