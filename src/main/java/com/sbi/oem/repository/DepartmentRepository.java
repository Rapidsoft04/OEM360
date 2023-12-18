package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

	@Query(value = "SELECT * FROM department where company_id=?1 and is_active=1", nativeQuery = true)
	List<Department> findAllByCompanyId(Long companyId);

}
