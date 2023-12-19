package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationTrail;

@Repository
public interface RecommendationTrailRepository extends JpaRepository<RecommendationTrail, Long> {

	List<RecommendationTrail> findAllByReferenceId(String referenceId);

}
