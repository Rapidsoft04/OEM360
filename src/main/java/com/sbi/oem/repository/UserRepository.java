package com.sbi.oem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	
	Optional<User> findByEmail(String name);

}
