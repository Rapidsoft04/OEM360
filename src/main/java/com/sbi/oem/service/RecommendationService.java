package com.sbi.oem.service;

import org.springframework.web.multipart.MultipartFile;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;

public interface RecommendationService {

	Response<?> getRecommendationPageData(Long companyId);

	Response<?> addRecommendation(RecommendationAddRequestDto recommendationAddRequestDto);

	Response<?> viewRecommendation(String refId);

	Response<?> getAllRecommendedStatus();

	Response<?> getAllRecommendations();

	Response<?> setRecommendationDeploymentDetails(RecommendationDetailsRequestDto recommendationDetailsRequestDto);

	Response<?> rejectRecommendationByAppOwner(RecommendationRejectionRequestDto recommendation);

	Response<?> revertApprovalRequestToAppOwnerForApproval(
			RecommendationDetailsRequestDto recommendationRejectionRequestDto);

	Response<?> rejectRecommendationByAgm(RecommendationRejectionRequestDto recommendationRejectionRequestDto);

	Response<?> acceptRecommendationRequestByAgm(RecommendationRejectionRequestDto recommendationRejectionRequestDto);

	Response<?> updateDeploymentDetails(RecommendationDetailsRequestDto recommendationDetailsRequestDto);

	Response<?> addRecommendationThroughExcel(MultipartFile file);

	Response<?> pendingRecommendationRequestForAppOwner(SearchDto searchDto);

	Response<?> approvedRecommendationRequestForAppOwner(SearchDto searchDto);

	Response<?> viewRecommendationDetailsForOemAndAgmAndGmPagination(SearchDto searchDto, long pageNumber,
			long pageSize);

	Response<?> pendingRecommendationRequestForAppOwnerThroughPagination(SearchDto newSearchDto, Integer pageNumber,
			Integer pageSize);

	Response<?> approvedRecommendationRequestForAppOwnerThroughPagination(SearchDto searchDto, Integer pageNumber,
			Integer pageSize);

	Response<?> viewRecommendationDetailsForOemAndAgmAndGm(SearchDto searchDto);

	Response<?> updateRecommendationStatus(RecommendationDetailsRequestDto recommendationRequestDto);

	Response<?> getAllStatusListToBeImplement();
	
	Response<?> updateRecommendation(RecommendationAddRequestDto recommendationAddRequestDto);
}
