package com.sbi.oem.service;

import com.sbi.oem.dto.Response;

public interface RecommendationService {

	Response<?> getRecommendationPageData(Long companyId);

}
