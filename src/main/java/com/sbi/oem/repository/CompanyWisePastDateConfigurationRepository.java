package com.sbi.oem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sbi.oem.model.CompanyWisePastDateConfiguration;

@Repository
public interface CompanyWisePastDateConfigurationRepository
		extends JpaRepository<CompanyWisePastDateConfiguration, Long> {

	@Query(value = "SELECT * FROM company_wise_user_date_configuration where company_id=?1 and is_active=1", nativeQuery = true)
	Optional<CompanyWisePastDateConfiguration> findByCompany(Long id);

}
