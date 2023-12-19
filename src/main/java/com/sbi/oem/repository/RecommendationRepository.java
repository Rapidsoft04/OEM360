package com.sbi.oem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Recommendation;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

	Optional<Recommendation> findByReferenceId(String refId);

}
