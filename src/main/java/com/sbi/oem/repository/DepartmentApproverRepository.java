package com.sbi.oem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.DepartmentApprover;

@Repository
public interface DepartmentApproverRepository extends JpaRepository<DepartmentApprover, Long> {

	@Query(value = "SELECT * FROM department_approver WHERE department_id = :departmentId", nativeQuery = true)
	DepartmentApprover getByDepartmentId(Long departmentId);
}
