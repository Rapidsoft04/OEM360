package com.sbi.oem.service;

import com.sbi.oem.dto.AddComponentDto;
import com.sbi.oem.dto.AddDepartmentApproverDto;
import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SignUpRequest;
import com.sbi.oem.model.DepartmentApprover;

public interface ValidationService {

	Response<?> checkForRecommendationAddPayload(RecommendationAddRequestDto recommendationAddRequestDto);

	Response<?> checkForDeploymentDetailsAddPayload(RecommendationDetailsRequestDto recommendationDetailsRequestDto);

	Response<?> checkForAppOwnerRecommendationRejectedPayload(RecommendationRejectionRequestDto recommendation);

	Response<?> checkForUpdateRecommendationStatusPayload(RecommendationDetailsRequestDto recommendationRequestDto);

	Response<?> checkForDepartmentApproverAddPayload(DepartmentApprover departmentApprover);
	
	Response<?> checkForDepartmentApproverAddPayloadV2(AddDepartmentApproverDto departmentApprover);

	Response<?> checkForRevertRequestByAgmOrDgm(RecommendationDetailsRequestDto recommendationRejectionRequestDto);

	Response<?> checkForComponentAddPayload(AddComponentDto componentDto);

	Response<?> checkForDepartmentAddPayload(AddDepartmentDto addDepartmentDto);

	Response<?> checkForUserAddPayload(SignUpRequest signUpRequest);

}
