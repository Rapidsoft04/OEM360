package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.service.DashboardService;

@RestController
@RequestMapping("dashboard")
public class DashboardController {

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/details")
	public ResponseEntity<?> getDashboardDetails(@RequestParam("value") String value) {
		Response<?> response = dashboardService.getDashboardDetails(value);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
}
