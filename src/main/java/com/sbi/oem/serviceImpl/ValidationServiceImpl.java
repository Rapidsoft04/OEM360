package com.sbi.oem.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationAddRequestDto;
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

}
