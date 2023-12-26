package com.sbi.oem.service;


import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;

@Service
public interface EmailTemplateService {
	
	
	  Response<?> sendMailRecommendation(Recommendation recommendation, RecommendationStatusEnum status);

	  Response<?> sendMailRecommendationDeplyomentDetails(RecommendationDeplyomentDetails details ,RecommendationStatusEnum recommendation);

	  Response<?> sendMailRecommendationMessages(RecommendationMessages messages, RecommendationStatusEnum rejectedByAppowner);

}
