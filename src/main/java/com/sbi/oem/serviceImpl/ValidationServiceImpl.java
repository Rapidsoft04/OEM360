package com.sbi.oem.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.service.ValidationService;

@Service
public class ValidationServiceImpl implements ValidationService {

	@Override
	public Response<?> checkForRecommendationAddPayload(RecommendationAddRequestDto recommendationAddRequestDto) {
		if (recommendationAddRequestDto.getComponentId() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the component.", null);
		} else if (recommendationAddRequestDto.getDepartmentId() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the department.", null);
		} else if (recommendationAddRequestDto.getDescription() == null
				|| recommendationAddRequestDto.getDescription() == "") {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the description.", null);
		} else if (recommendationAddRequestDto.getPriorityId() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the priority.", null);
		} else if (recommendationAddRequestDto.getRecommendDate() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the recommendation date.", null);
		} else if (recommendationAddRequestDto.getTypeId() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the type.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}
	}

	@Override
	public Response<?> checkForDeploymentDetailsAddPayload(
			RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		if (recommendationDetailsRequestDto.getDevelopmentStartDate() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the development start date.", null);
		} else if (recommendationDetailsRequestDto.getDevelopementEndDate() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the development end date.", null);
		} else if (recommendationDetailsRequestDto.getTestCompletionDate() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the test completion date.", null);
		} else if (recommendationDetailsRequestDto.getDeploymentDate() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the deployment date.", null);
		} else if (recommendationDetailsRequestDto.getImpactedDepartment() == null
				|| recommendationDetailsRequestDto.getImpactedDepartment() == "") {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the impacted department.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}

	}

	@Override
	public Response<?> checkForAppOwnerRecommendationRejectedPayload(RecommendationRejectionRequestDto recommendation) {
		if (recommendation.getReferenceId() == null || recommendation.getReferenceId().equals("")) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the reference id.", null);
		} else if (recommendation.getRejectionMessage() == null || recommendation.getRejectionMessage().equals("")) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide reason for rejection.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}
	}

	@Override
	public Response<?> checkForUpdateRecommendationStatusPayload(
			RecommendationDetailsRequestDto recommendationRequestDto) {
		if (recommendationRequestDto.getRecommendRefId() == null
				|| recommendationRequestDto.getRecommendRefId().isBlank()
				|| recommendationRequestDto.getRecommendRefId().equals("")) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the reference id.", null);
		} else if (recommendationRequestDto.getRecommendationStatus() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the status.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}
	}

}
