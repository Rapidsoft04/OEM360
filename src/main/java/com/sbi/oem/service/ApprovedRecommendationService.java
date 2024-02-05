package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;

public interface ApprovedRecommendationService {
	
	Response<?> approvedRecommendationRequestForAppOwner(SearchDto searchDto);
	
	Response<?> approvedRecommendationRequestForAppOwnerThroughPagination(SearchDto searchDto, Integer pageNumber,
			Integer pageSize);

	Response<?> viewRecommendationDetailsForOemAndAgmAndGm(SearchDto searchDto);
	
	Response<?> viewRecommendationDetailsForOemAndAgmAndGmPagination(SearchDto searchDto, long pageNumber, long pageSize);
}