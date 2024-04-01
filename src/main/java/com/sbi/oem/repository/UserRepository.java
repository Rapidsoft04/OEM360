package com.sbi.oem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String name);

//	@Query(value = "SELECT * FROM user " + "WHERE (department_id = :departmentId AND user_type_id = '6') "
//			+ "OR (user_type_id = '6' AND department_id IS NULL) ", nativeQuery = true)
//	List<User> findAllUnAssignedUsers(@Param("departmentId") Long departmentId);

	@Query(value = "SELECT * FROM user " + "WHERE (department_id = :departmentId AND user_type_id = '6') "
			+ "OR (user_type_id = '6' AND department_id IS NULL)", nativeQuery = true)
	List<User> findAllUnAssignedUsers(@Param("departmentId") Long departmentId);

}
