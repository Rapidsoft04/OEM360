package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.service.FunctionalityService;

@RestController
@RequestMapping("/functionality")
public class FunctionalityController {

	@Autowired
	private FunctionalityService componentListSummaryService;

	@GetMapping("/all")
	public ResponseEntity<?> getAll() {
		Response<?> response = componentListSummaryService.getAllComponentsList();
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
}
