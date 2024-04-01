package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationType;

@Repository
public interface RecommendationTypeRepository extends JpaRepository<RecommendationType, Long> {

	@Query(value = "SELECT * FROM recommendation_type WHERE company_id = ?1 AND is_active = 1 ORDER BY updated_at DESC", nativeQuery = true)
	List<RecommendationType> findAllByCompanyId(Long companyId);

	@Query(value = "SELECT * FROM recommendation_type where name=?1", nativeQuery = true)
	Optional<RecommendationType> findRecommendationTypeByName(String name);

}
