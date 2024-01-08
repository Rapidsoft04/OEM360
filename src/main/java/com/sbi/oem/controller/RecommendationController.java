package com.sbi.oem.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.service.RecommendationService;
import com.sbi.oem.service.ValidationService;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	private ValidationService validationService;

	@GetMapping("/page/data")
	public ResponseEntity<?> getRecommendationPageData(@RequestParam("companyId") Long companyId) {
		Response<?> response = recommendationService.getRecommendationPageData(companyId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@PostMapping("/create")
	public ResponseEntity<?> createRecommendation(
			@ModelAttribute RecommendationAddRequestDto recommendationAddRequestDto) {
		Response<?> validationResponse = validationService
				.checkForRecommendationAddPayload(recommendationAddRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.addRecommendation(recommendationAddRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}

	@GetMapping("/view")
	public ResponseEntity<?> viewRecommendationByRefId(@RequestParam("refId") String refId) {
		Response<?> response = recommendationService.viewRecommendation(refId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/get/all")
	public ResponseEntity<?> getAllRecommendations() {
		Response<?> response = recommendationService.getAllRecommendations();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/deplyoment/details/provide")
	public ResponseEntity<?> provideRecommendationDeplyomentDetails(
			@RequestBody RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		Response<?> validationResponse = validationService
				.checkForDeploymentDetailsAddPayload(recommendationDetailsRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService
					.setRecommendationDeploymentDetails(recommendationDetailsRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}

	@PostMapping("/rejected/by/appowner")
	public ResponseEntity<?> recommendationRejectByAppOwner(
			@RequestBody RecommendationRejectionRequestDto recommendation) {
		Response<?> validationResponse = validationService
				.checkForAppOwnerRecommendationRejectedPayload(recommendation);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.rejectRecommendationByAppOwner(recommendation);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}

	@PostMapping("/reject/request/revert/by/agm")
	public ResponseEntity<?> revertApprovalRequestToAppOwnerForApproval(
			@RequestBody RecommendationDetailsRequestDto recommendationRejectionRequestDto) {
		Response<?> response = recommendationService
				.revertApprovalRequestToAppOwnerForApproval(recommendationRejectionRequestDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@PostMapping("/reject/by/agm")
	public ResponseEntity<?> rejectRecommendationByAgm(
			@RequestBody RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		Response<?> validationResponse = validationService
				.checkForAppOwnerRecommendationRejectedPayload(recommendationRejectionRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.rejectRecommendationByAgm(recommendationRejectionRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}

	@PostMapping("/request/accept/by/agm")
	public ResponseEntity<?> acceptRecommendationByAgm(
			@RequestBody RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		Response<?> response = recommendationService
				.acceptRecommendationRequestByAgm(recommendationRejectionRequestDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));

	}

	@PostMapping("/deployment/details/update")
	public ResponseEntity<?> updateDeploymentDetails(
			@RequestBody RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		Response<?> validationResponse = validationService
				.checkForDeploymentDetailsAddPayload(recommendationDetailsRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.updateDeploymentDetails(recommendationDetailsRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}

	@PostMapping("/add/through/excel")
	public ResponseEntity<?> addRecommendationThroughExcel(@ModelAttribute MultipartFile file) {
		Response<?> response = recommendationService.addRecommendationThroughExcel(file);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/pending/details/for/appowner")
	public ResponseEntity<?> pendingRecommendationDetailsOfAppOwner(
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt) {

		SearchDto newSearchDto = new SearchDto();
		newSearchDto.setRecommendationType(recommendationType);
		newSearchDto.setPriorityId(priorityId);
		newSearchDto.setReferenceId(referenceId);
		newSearchDto.setDepartmentId(departmentId);
		newSearchDto.setStatusId(statusId);
		newSearchDto.setFromDate(fromDate);
		newSearchDto.setToDate(toDate);
		newSearchDto.setCreatedBy(createdBy);
		newSearchDto.setUpdatedAt(updatedAt);
		Response<?> response = recommendationService.pendingRecommendationRequestForAppOwner(newSearchDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/approved/details/for/appowner")
	public ResponseEntity<?> approvedRecommendationDetailsOfAppOwner(
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt
			) {
		SearchDto newSearchDto = new SearchDto();
		newSearchDto.setRecommendationType(recommendationType);
		newSearchDto.setPriorityId(priorityId);
		newSearchDto.setReferenceId(referenceId);
		newSearchDto.setDepartmentId(departmentId);
		newSearchDto.setStatusId(statusId);
		newSearchDto.setFromDate(fromDate);
		newSearchDto.setToDate(toDate);
		newSearchDto.setCreatedBy(createdBy);
		newSearchDto.setUpdatedAt(updatedAt);
		Response<?> response = recommendationService.approvedRecommendationRequestForAppOwner(newSearchDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/pending/details/for/appowner/paginate")
	public ResponseEntity<?> pendingRecommendationDetailsOfAppOwner(
			@RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
			@RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt) {

		SearchDto newSearchDto = new SearchDto();
		newSearchDto.setRecommendationType(recommendationType);
		newSearchDto.setPriorityId(priorityId);
		newSearchDto.setReferenceId(referenceId);
		newSearchDto.setDepartmentId(departmentId);
		newSearchDto.setStatusId(statusId);
		newSearchDto.setFromDate(fromDate);
		newSearchDto.setToDate(toDate);
		newSearchDto.setCreatedBy(createdBy);
		newSearchDto.setUpdatedAt(updatedAt);

		Response<?> response = recommendationService
				.pendingRecommendationRequestForAppOwnerThroughPagination(newSearchDto, pageNumber, pageSize);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/approved/details/for/appowner/paginate")
	public ResponseEntity<?> approvedRecommendationDetailsOfAppOwner(
			@RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
			@RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt, SearchDto searchDto) {

		SearchDto newSearchDto = new SearchDto();
		newSearchDto.setRecommendationType(recommendationType);
		newSearchDto.setPriorityId(priorityId);
		newSearchDto.setReferenceId(referenceId);
		newSearchDto.setDepartmentId(departmentId);
		newSearchDto.setStatusId(statusId);
		newSearchDto.setFromDate(fromDate);
		newSearchDto.setToDate(toDate);
		newSearchDto.setCreatedBy(createdBy);
		newSearchDto.setUpdatedAt(updatedAt);

		Response<?> response = recommendationService
				.approvedRecommendationRequestForAppOwnerThroughPagination(searchDto, pageNumber, pageSize);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/view/details/agmAndoem")
	public ResponseEntity<?> viewRecommendationDetailsForOemAndAgmAndGm(
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt) {

		SearchDto searchDto = new SearchDto();
		searchDto.setRecommendationType(recommendationType);
		searchDto.setPriorityId(priorityId);
		searchDto.setReferenceId(referenceId);
		searchDto.setDepartmentId(departmentId);
		searchDto.setStatusId(statusId);
		searchDto.setFromDate(fromDate);
		searchDto.setToDate(toDate);
		searchDto.setCreatedBy(createdBy);
		searchDto.setUpdatedAt(updatedAt);

		Response<?> response = recommendationService.viewRecommendationDetailsForOemAndAgmAndGm(searchDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));

	}

	// paginate

	@GetMapping("/view/all/recommendationsforagmoemandgm")
	public ResponseEntity<?> viewRecommendationDetailsForOemAndAgmAndGm(
			@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", required = false, defaultValue = "0") int pageSize,
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) Date fromDate,
			@RequestParam(name = "toDate", required = false) Date toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt) {

		SearchDto searchDto = new SearchDto();
		searchDto.setRecommendationType(recommendationType);
		searchDto.setPriorityId(priorityId);
		searchDto.setReferenceId(referenceId);
		searchDto.setDepartmentId(departmentId);
		searchDto.setStatusId(statusId);
		searchDto.setFromDate(fromDate);
		searchDto.setToDate(toDate);
		searchDto.setCreatedBy(createdBy);
		searchDto.setUpdatedAt(updatedAt);

		Response<?> response = recommendationService.viewRecommendationDetailsForOemAndAgmAndGmPagination(searchDto,
				pageNumber, pageSize);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));

	}

	@PostMapping("/status/update")
	public ResponseEntity<?> updateRecommendationStatus(
			@RequestBody RecommendationDetailsRequestDto recommendationRequestDto) {
		Response<?> validationResponse = validationService
				.checkForUpdateRecommendationStatusPayload(recommendationRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.updateRecommendationStatus(recommendationRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}
	
	@PostMapping("/update")
	public ResponseEntity<?> updateRecommendation(
			@ModelAttribute RecommendationAddRequestDto recommendationAddRequestDto) {
		Response<?> validationResponse = validationService
				.checkForRecommendationAddPayload(recommendationAddRequestDto);
		if (validationResponse.getResponseCode() == HttpStatus.OK.value()) {
			Response<?> response = recommendationService.updateRecommendation(recommendationAddRequestDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} else {
			return new ResponseEntity<>(validationResponse, HttpStatus.valueOf(validationResponse.getResponseCode()));
		}
	}
}
