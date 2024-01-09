package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationTrail;

@Repository
public interface RecommendationTrailRepository extends JpaRepository<RecommendationTrail, Long> {

	List<RecommendationTrail> findAllByReferenceId(String referenceId);

	@Query(value = "SELECT * FROM recommendation_trail where ref_id=?1 and status_id=?2", nativeQuery = true)
	Optional<RecommendationTrail> findAllByReferenceIdAndStatusId(String referenceId, Long id);

}
