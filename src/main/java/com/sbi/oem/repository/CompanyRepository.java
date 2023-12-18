package com.sbi.oem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}
