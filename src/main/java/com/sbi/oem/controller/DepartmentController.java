package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.service.DepartmentService;

@RestController
@RequestMapping("/department")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	@PostMapping("/save")
	public ResponseEntity<?> saveDepartment(@RequestBody AddDepartmentDto departmentDto) {
		Response<?> response = departmentService.saveDepartment(departmentDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/get/all")
	public ResponseEntity<?> getDepartmentListByCompanyId(@RequestParam("companyId") Long companyId) {
		Response<?> response = departmentService.getAllDepartmentByCompanyId(companyId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
	
	@GetMapping("/get/all/with/component")
	public ResponseEntity<?> getDepartmentListWithComponent(@RequestParam("companyId") Long companyId) {
		Response<?> response = departmentService.getAllDepartmentWithComponent(companyId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

//	@PostMapping("/approver/save")
//	public ResponseEntity<?> saveDepartmentApprover(@RequestBody DepartmentApprover departmentApprover) {
//		Response<?> checkValidationResponse = validationService
//				.checkForDepartmentApproverAddPayload(departmentApprover);
//		if (checkValidationResponse.getResponseCode() == HttpStatus.OK.value()) {
//			Response<?> response = departmentService.saveDepartmentApprover(departmentApprover);
//			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
//		} else {
//			return new ResponseEntity<>(checkValidationResponse,
//					HttpStatus.valueOf(checkValidationResponse.getResponseCode()));
//		}
//	}
	
//	@PostMapping("/approver/save")
//	public ResponseEntity<?> saveDepartmentApproverV2(@RequestBody DepartmentApprover departmentApprover) {
//		Response<?> checkValidationResponse = validationService
//				.checkForDepartmentApproverAddPayload(departmentApprover);
//		if (checkValidationResponse.getResponseCode() == HttpStatus.OK.value()) {
//			Response<?> response = departmentService.saveDepartmentApprover(departmentApprover);
//			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
//		} else {
//			return new ResponseEntity<>(checkValidationResponse,
//					HttpStatus.valueOf(checkValidationResponse.getResponseCode()));
//		}
//	}
//
//	@GetMapping("/approver/get/all")
//	public ResponseEntity<?> getAllDepartmentApproverList() {
//		Response<?> response = departmentService.getAllDepartmentApproverList();
//		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
//	}

//	@GetMapping("/approver/id")
//	public ResponseEntity<?> getDepartmentApproverByDepartmentId(@RequestParam("departmentId") Long departmentId) {
//		Response<?> response = departmentService.getDepartmentApproverByDepartmentId(departmentId);
//		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
//	}

}
