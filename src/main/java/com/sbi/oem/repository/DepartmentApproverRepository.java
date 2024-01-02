package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.DepartmentApprover;

@Repository
public interface DepartmentApproverRepository extends JpaRepository<DepartmentApprover, Long> {

	@Query(value = "SELECT * FROM department_approver where department_id=?1", nativeQuery = true)
	Optional<DepartmentApprover> findAllByDepartmentId(Long id);

	@Query(value = "SELECT * FROM department_approver where app_owner_id=?1 or agm_id=?1", nativeQuery = true)
	List<DepartmentApprover> findAllByUserId(Long userId);

	@Query(value = "SELECT * FROM department_approver where department_id in(?1)", nativeQuery = true)
	List<DepartmentApprover> findAllByDepartmentIdIn(List<Long> departmentIds);

	@Query(value = "SELECT * FROM department_approver where agm_id=?1", nativeQuery = true)
	Optional<DepartmentApprover> findByAgmId(Long id);

}
