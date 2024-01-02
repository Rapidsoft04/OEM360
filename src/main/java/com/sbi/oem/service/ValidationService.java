package com.sbi.oem.service;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;

public interface ValidationService {

	Response<?> checkForRecommendationAddPayload(RecommendationAddRequestDto recommendationAddRequestDto);

	Response<?> checkForDeploymentDetailsAddPayload(RecommendationDetailsRequestDto recommendationDetailsRequestDto);

	Response<?> checkForAppOwnerRecommendationRejectedPayload(RecommendationRejectionRequestDto recommendation);

	Response<?> checkForUpdateRecommendationStatusPayload(RecommendationDetailsRequestDto recommendationRequestDto);

}
