package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Component;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

	@Query(value = "SELECT * FROM components WHERE company_id = ?1 AND is_active = 1 ORDER BY updated_at DESC", nativeQuery = true)
	List<Component> findAllByCompanyId(Long companyId);

	@Query(value = "SELECT * FROM components where name=?1", nativeQuery = true)
	Optional<Component> findComponentByName(String componentName);

}
