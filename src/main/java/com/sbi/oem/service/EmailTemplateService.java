package com.sbi.oem.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;

@Service
public interface EmailTemplateService {
	
	
	  Response<?> sendMail(Recommendation recommendation, RecommendationStatusEnum status);

	  Response<?> sendMail(RecommendationDeplyomentDetails details ,Optional<Recommendation> recommendation);

}
