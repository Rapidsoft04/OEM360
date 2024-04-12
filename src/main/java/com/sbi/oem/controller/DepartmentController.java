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

import com.sbi.oem.dto.AddDepartmentComponentMapDto;
import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.UpdateDepartmentComponentMapRequestDto;
import com.sbi.oem.model.DepartmentComponentMapping;
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

	@GetMapping("/get/common/components")
	public ResponseEntity<?> getCommonComponentsOfDepartments(@RequestParam("departmentList") String departmentList) {
		Response<?> response = departmentService.getCommonComponents(departmentList);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/component/data")
	public ResponseEntity<?> getDepartmentComponentDataToMapNewComponents(
			@RequestParam("departmentId") Long departmentId) {
		Response<?> response = departmentService.getDepartmentComponentDataToMapNewComponents(departmentId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@PostMapping("/component/map/update")
	public ResponseEntity<?> updateDepartmentComponent(@RequestBody UpdateDepartmentComponentMapRequestDto requestDto) {
		Response<?> response = departmentService.updateDepartmentComponent(requestDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@PostMapping("/component/map/save")
	public ResponseEntity<?> saveDepartmentComponent(@RequestBody AddDepartmentComponentMapDto requestDto) {
		Response<?> response = departmentService.departmentComponentMapSave(requestDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

}
