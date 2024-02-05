package com.sbi.oem.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.service.ApprovedRecommendationService;
import com.sbi.oem.service.PendingRecommendationService;

@RestController
@RequestMapping("/recommendation")
public class RecommendationDataController {
	
	@Autowired
	private PendingRecommendationService pendingRecommendationService;
	
	@Autowired
	private ApprovedRecommendationService approvedRecommendationService;
	
	@GetMapping("/pending/details/for/appowner")
	public ResponseEntity<?> pendingRecommendationDetailsOfAppOwner(
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt,
			@RequestParam(name = "searchKey", required = false) String searchKey,
			@RequestParam(name = "chartSearchKey", required = false) String chartSearchKey,
			@RequestParam(name = "dateFilterKey", required = false) String dateFilterKey) {

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
		newSearchDto.setSearchKey(searchKey);
		newSearchDto.setChartSearchKey(chartSearchKey);
		newSearchDto.setDateFilterKey(dateFilterKey);
		Response<?> response = pendingRecommendationService.pendingRecommendationRequestForAppOwner(newSearchDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/approved/details/for/appowner")
	public ResponseEntity<?> approvedRecommendationDetailsOfAppOwner(
			@RequestParam(name = "recommendationType", required = false) Long recommendationType,
			@RequestParam(name = "priorityId", required = false) Long priorityId,
			@RequestParam(name = "referenceId", required = false) String referenceId,
			@RequestParam(name = "departmentId", required = false) Long departmentId,
			@RequestParam(name = "statusId", required = false) Long statusId,
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt,
			@RequestParam(name = "searchKey", required = false) String searchKey,
			@RequestParam(name = "chartSearchKey", required = false) String chartSearchKey,
			@RequestParam(name = "dateFilterKey", required = false) String dateFilterKey) {
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
		newSearchDto.setSearchKey(searchKey);
		newSearchDto.setChartSearchKey(chartSearchKey);
		newSearchDto.setDateFilterKey(dateFilterKey);
		Response<?> response = approvedRecommendationService.approvedRecommendationRequestForAppOwner(newSearchDto);
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
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
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

		Response<?> response = pendingRecommendationService
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
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
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

		Response<?> response = approvedRecommendationService
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
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
			@RequestParam(name = "createdBy", required = false) Long createdBy,
			@RequestParam(name = "updatedAt", required = false) Date updatedAt,
			@RequestParam(name = "searchKey", required = false) String searchKey,
			@RequestParam(name = "chartSearchKey", required = false) String chartSearchKey,
			@RequestParam(name = "dateFilterKey", required = false) String dateFilterKey) {

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
		searchDto.setSearchKey(searchKey);
		searchDto.setChartSearchKey(chartSearchKey);
		searchDto.setDateFilterKey(dateFilterKey);
		Response<?> response = approvedRecommendationService.viewRecommendationDetailsForOemAndAgmAndGm(searchDto);
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
			@RequestParam(name = "fromDate", required = false) String fromDate,
			@RequestParam(name = "toDate", required = false) String toDate,
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

		Response<?> response = approvedRecommendationService.viewRecommendationDetailsForOemAndAgmAndGmPagination(searchDto,
				pageNumber, pageSize);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));

	}

}