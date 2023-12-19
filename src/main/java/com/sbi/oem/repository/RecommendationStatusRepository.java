package com.sbi.oem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.RecommendationStatus;

@Repository
public interface RecommendationStatusRepository extends JpaRepository<RecommendationStatus, Long> {

}
