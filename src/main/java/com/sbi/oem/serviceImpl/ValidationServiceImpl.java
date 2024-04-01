package com.sbi.oem.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.AddComponentDto;
import com.sbi.oem.dto.AddDepartmentApproverDto;
import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SignUpRequest;
import com.sbi.oem.enums.UserType;
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
		} else if ((recommendationDetailsRequestDto.getDevelopmentStartDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate()))
				&& (!recommendationDetailsRequestDto.getDevelopmentStartDate()
						.equals(recommendationDetailsRequestDto.getDeploymentDate()))) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development start date should be before the deployment date.", null);
		} else if ((recommendationDetailsRequestDto.getDevelopementEndDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate()))
				&& (!recommendationDetailsRequestDto.getDevelopementEndDate()
						.equals(recommendationDetailsRequestDto.getDeploymentDate()))) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development end date should be before the deployment date.", null);
		} else if ((recommendationDetailsRequestDto.getTestCompletionDate()
				.after(recommendationDetailsRequestDto.getDeploymentDate()))
				&& (!recommendationDetailsRequestDto.getTestCompletionDate()
						.equals(recommendationDetailsRequestDto.getDeploymentDate()))) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Test completion date should be before the deployment date.", null);
		} else if ((recommendationDetailsRequestDto.getDevelopmentStartDate()
				.after(recommendationDetailsRequestDto.getDevelopementEndDate()))
				&& (!recommendationDetailsRequestDto.getDevelopmentStartDate()
						.equals(recommendationDetailsRequestDto.getDevelopementEndDate()))) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(),
					"Development start date should be before the development end date.", null);
		} else if ((recommendationDetailsRequestDto.getDevelopementEndDate()
				.after(recommendationDetailsRequestDto.getTestCompletionDate()))
				&& (!recommendationDetailsRequestDto.getDevelopementEndDate()
						.equals(recommendationDetailsRequestDto.getTestCompletionDate()))) {
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
				|| recommendationRequestDto.getRecommendRefId().isEmpty()
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
			} else if (departmentApprover.getAgm() == null && departmentApprover.getApplicationOwner() == null
					&& departmentApprover.getDgm() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please assign at least one approver.", null);
			} else if (departmentApprover.getAgm() != null && departmentApprover.getApplicationOwner() != null
					&& departmentApprover.getAgm().getId() != null
					&& departmentApprover.getApplicationOwner().getId() != null && departmentApprover.getAgm().getId()
							.longValue() == departmentApprover.getApplicationOwner().getId().longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "AGM and Appowner cannot be the same user", null);
			} else if (departmentApprover.getAgm() != null && departmentApprover.getDgm() != null
					&& departmentApprover.getAgm().getId() != null && departmentApprover.getDgm().getId() != null
					&& departmentApprover.getAgm().getId().longValue() == departmentApprover.getDgm().getId()
							.longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "AGM and DGM cannot be the same user", null);
			} else if (departmentApprover.getApplicationOwner() != null && departmentApprover.getDgm() != null
					&& departmentApprover.getApplicationOwner().getId() != null
					&& departmentApprover.getDgm().getId() != null && departmentApprover.getApplicationOwner().getId()
							.longValue() == departmentApprover.getDgm().getId().longValue()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Appowner and DGM cannot be the same user", null);
			} else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> checkForDepartmentApproverAddPayloadV2(AddDepartmentApproverDto departmentApprover) {
		try {
			if (departmentApprover.getDepartmentId() == null || departmentApprover.getDepartmentId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide the department", null);
			} else if (departmentApprover.getUserType().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid user type", null);
			} else if (departmentApprover.getUserId() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide user", null);
			} else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> checkForRevertRequestByAgmOrDgm(
			RecommendationDetailsRequestDto recommendationRejectionRequestDto) {
		if (recommendationRejectionRequestDto.getDescription() == null
				|| recommendationRejectionRequestDto.getDescription().isEmpty()
				|| recommendationRejectionRequestDto.getDescription().equals("")) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Plese provide the description details.", null);
		} else {
			return new Response<>(HttpStatus.OK.value(), "OK", null);
		}
	}

	@Override
	public Response<?> checkForComponentAddPayload(AddComponentDto componentDto) {
		try {
			if (componentDto.getName() == null || componentDto.getName().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid component name", null);
			} 
//			else if (componentDto.getDepartmentIds() == null || componentDto.getDepartmentIds().isEmpty()) {
//				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid department", null);
//			} 
			else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> checkForDepartmentAddPayload(AddDepartmentDto addDepartmentDto) {
		try {
			if (addDepartmentDto.getName() == null || addDepartmentDto.getName().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid department name", null);
			} else if (addDepartmentDto.getCode() == null || addDepartmentDto.getCode().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid code", null);
			} else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> checkForUserAddPayload(SignUpRequest signUpRequest) {
		try {
			if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid email", null);
			} else if (signUpRequest.getUserName() == null || signUpRequest.getUserName().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid username", null);
			} else if (signUpRequest.getPhoneNo() == null || signUpRequest.getPhoneNo().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid phone number", null);
			} else if (signUpRequest.getDesignation() == null || signUpRequest.getDesignation().trim().isEmpty()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid designation", null);
			} else if (signUpRequest.getDepartmentId() == null && signUpRequest.getUserType() != null
					&& !signUpRequest.getUserType().equalsIgnoreCase(UserType.USER.name())) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department to set the user type",
						null);
			} else {
				return new Response<>(HttpStatus.OK.value(), "OK", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

}
