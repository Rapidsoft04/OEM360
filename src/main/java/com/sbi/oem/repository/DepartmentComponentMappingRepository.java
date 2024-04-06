package com.sbi.oem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.DepartmentComponentMapping;

@Repository
public interface DepartmentComponentMappingRepository extends JpaRepository<DepartmentComponentMapping, Long> {

	@Query(value = "SELECT * FROM department_component WHERE department_id = ?1 ORDER BY updated_at DESC", nativeQuery = true)
	List<DepartmentComponentMapping> findAllByDepartmentId(Long departmentId);

	@Query(value = "SELECT * FROM department_component WHERE component_id = ?1 ORDER BY updated_at DESC", nativeQuery = true)
	List<DepartmentComponentMapping> findAllByComponentId(Long componentId);

	@Query(value = "SELECT component FROM DepartmentComponentMapping where department_id in :departmentIds group by component_id having count(department_id) = :departmentCount")
	List<Component> findCommonComponents(@Param("departmentIds") List<Long> departmentIds,
			@Param("departmentCount") Long departmentCount);

}
