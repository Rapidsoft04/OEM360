package com.sbi.oem.service;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.Response;

public interface RecommendationService {

	Response<?> getRecommendationPageData(Long companyId);

	Response<?> addRecommendation(RecommendationAddRequestDto recommendationAddRequestDto);

	Response<?> viewRecommendation(String refId);

	Response<?> getAllRecommendedStatus();

	Response<?> getAllRecommendations();

}
