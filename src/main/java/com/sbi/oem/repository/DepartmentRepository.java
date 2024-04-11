package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

	@Query(value = "SELECT * FROM department WHERE company_id = ?1 AND is_active = 1 ORDER BY updated_at DESC", nativeQuery = true)
	List<Department> findAllByCompanyId(Long companyId);

	@Query(value = "SELECT * FROM department where name = ?1 OR code = ?2", nativeQuery = true)
	Optional<Department> findDepartmentByName(String departmentName, String departmentCode);
	
//	@Query(value = "SELECT * FROM department where name = ?1", nativeQuery = true)
//	Optional<Department> findDepartmentByName(String departmentName);
	
	@Query(value = "SELECT * FROM department WHERE name = ?1", nativeQuery = true)
	Optional<Department> findDepartmentByName(String departmentName);

}
