package com.sbi.oem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;

@Service
public interface EmailTemplateService {

	Response<?> sendMailRecommendation(Recommendation recommendation, RecommendationStatusEnum status);

	void sendMailRecommendationDeplyomentDetails(RecommendationDetailsRequestDto recommendationDetailsRequestDto,
			RecommendationStatusEnum recommendation);

	void sendMailRecommendationMessages(RecommendationMessages messages,
			RecommendationStatusEnum rejectedByAppowner);

	void sendMailBuldRecommendation(List<Recommendation> recommendationList);

	void sendAllMailForRecommendation(List<Recommendation> recommendationList, RecommendationStatusEnum created);

}
