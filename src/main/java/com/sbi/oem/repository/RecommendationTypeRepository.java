package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationType;

@Repository
public interface RecommendationTypeRepository extends JpaRepository<RecommendationType, Long> {

	@Query(value = "SELECT * FROM recommendation_type where company_id=?1 and is_active=1", nativeQuery = true)
	List<RecommendationType> findAllByCompanyId(Long companyId);

}
