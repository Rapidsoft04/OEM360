package com.sbi.oem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Functionality;

@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, Long> {
	
	@Query(value = "SELECT * FROM functionality WHERE title_id = :titleId", nativeQuery = true)
    Optional<Functionality> findByTitleId(Long titleId);
}
