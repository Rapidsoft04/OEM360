package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Component;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

	@Query(value = "SELECT * FROM components where company_id=?1 and is_active=1", nativeQuery = true)
	List<Component> findAllByCompanyId(Long companyId);

}
