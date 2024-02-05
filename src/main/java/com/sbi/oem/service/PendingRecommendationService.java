package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;

public interface PendingRecommendationService {
	
	Response<?> pendingRecommendationRequestForAppOwner(SearchDto searchDto);
	
	Response<?> pendingRecommendationRequestForAppOwnerThroughPagination(SearchDto newSearchDto, Integer pageNumber,
			Integer pageSize);
	
}