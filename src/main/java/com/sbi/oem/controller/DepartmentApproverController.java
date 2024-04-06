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

import com.sbi.oem.dto.AddDepartmentApproverDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.service.DepartmentApproverService;
import com.sbi.oem.service.ValidationService;

@RestController
@RequestMapping("department/approver")
public class DepartmentApproverController {

	@Autowired
	private DepartmentApproverService departmentApproverService;

//	@PostMapping("/save")
//	public ResponseEntity<?> saveDepartmentApprover(@RequestBody DepartmentApprover departmentApprover) {
//		try {
//			Response<?> response = departmentApproverService.save(departmentApprover);
//			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
//					HttpStatus.BAD_REQUEST);
//		}
//	}

	@PostMapping("/save")
	public ResponseEntity<?> saveDepartmentApprover2(@RequestBody AddDepartmentApproverDto departmentApprover) {
		try {
			Response<?> response = departmentApproverService.save2(departmentApprover);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/departmentId")
	public ResponseEntity<?> getAllDataByDepartmentId(@RequestParam("departmentId") Long departmentId) {
		try {
			Response<?> response = departmentApproverService.getAllDataByDepartmentId(departmentId);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get/all")
	public ResponseEntity<?> getAllDepartmentApproverList() {
		try {
			Response<?> response = departmentApproverService.getAllDepartmentApproverList();
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/id")
	public ResponseEntity<?> getDepartmentApproverByDepartmentId(@RequestParam("departmentId") Long departmentId) {
		try {
			Response<?> response = departmentApproverService.getDepartmentApproverByDepartmentId(departmentId);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/department/id")
	public ResponseEntity<?> getUserTypeByDepartmentId(@RequestParam("departmentId") Long departmentId) {
		try {
			Response<?> response = departmentApproverService.getUserTypeByDepartmentId(departmentId);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}
}
