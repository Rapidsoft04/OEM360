package com.sbi.oem.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.service.ValidationService;

@Service
public class ValidationServiceImpl implements ValidationService {

	@Override
	public Response<?> checkForRecommendationAddPayload(RecommendationAddRequestDto recommendationAddRequestDto) {
		if (recommendationAddRequestDto.getComponentId() == null) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please select the component.", null);
		} else if (recommendationAddRequestDto.getDepartmentIds() == null
				&& recommendationAddRequestDto.getDepartmentIds().size() <= 0) {
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
		} else if (recommendationDetailsRequestDto.getDevelopmentStartDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate())) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development start date should be before the deployment date.", null);
		} else if (recommendationDetailsRequestDto.getDevelopementEndDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate())) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development end date should be before the deployment date.", null);
		} else if (recommendationDetailsRequestDto.getTestCompletionDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate())) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Test completion date should be before the deployment date.", null);
		} else if (recommendationDetailsRequestDto.getDevelopmentStartDate()
				.after(recommendationDetailsRequestDto.getDevelopementEndDate())) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development start date should be before the development end date.", null);
		} else if (recommendationDetailsRequestDto.getDevelopementEndDate()
				.after(recommendationDetailsRequestDto.getTestCompletionDate())) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development end date should be before the test completion date.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}

	}

	@Override
	public Response<?> checkForAppOwnerRecommendationRejectedPayload(RecommendationRejectionRequestDto recommendation) {
		if (recommendation.getRecommendRefId() == null || recommendation.getRecommendRefId().equals("")) {
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

	@Override
	public Response<?> checkForDepartmentApproverAddPayload(DepartmentApprover departmentApprover) {
		try {
			if (departmentApprover.getDepartment() == null || departmentApprover.getDepartment().getId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the department id", null);
			} else if (departmentApprover.getAgm() == null || departmentApprover.getAgm().getId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the agm id", null);
			} else if (departmentApprover.getApplicationOwner() == null
					|| departmentApprover.getApplicationOwner().getId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Pleae provide the app owner id", null);
			} else if (departmentApprover.getDgm() == null || departmentApprover.getDgm().getId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the dgm id", null);
			} else if (departmentApprover.getAgm().getId().longValue() == departmentApprover.getApplicationOwner()
					.getId().longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "AGM and Appowner cannot be the same user", null);
			} else if (departmentApprover.getAgm().getId().longValue() == departmentApprover.getDgm().getId()
					.longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "AGM and DGM cannot be the same user", null);
			} else if (departmentApprover.getApplicationOwner().getId().longValue() == departmentApprover.getDgm()
					.getId().longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Appowner and DGM cannot be the same user", null);
			} else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

}
