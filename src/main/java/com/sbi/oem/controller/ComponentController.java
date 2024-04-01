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

import com.sbi.oem.dto.AddComponentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.service.ComponentService;

@RestController
@RequestMapping("/component")
public class ComponentController {

	@Autowired
	private ComponentService componentService;

	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody AddComponentDto componentDto) {
		try {
			Response<?> response = componentService.saveComponent(componentDto);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/departmentId")
	public ResponseEntity<?> getAllByDepartmentId(@RequestParam("departmentId") Long departmentId) {
		try {
			Response<?> response = componentService.getAllByDepartmentId(departmentId);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get/all")
	public ResponseEntity<?> getAllComponents() {
		try {
			Response<?> response = componentService.getAllComponents();
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get/all/with/department")
	public ResponseEntity<?> getAllComponentsWithDepartment() {
		try {
			Response<?> response = componentService.getAllComponentWithDepartment();
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}
}
