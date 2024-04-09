package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.service.MisReportService;

@RestController
public class MisReportController {

	@Autowired
	private MisReportService misReportService;

	@GetMapping("/export/mis/report")
	public ResponseEntity<?> exportMisReport(@RequestParam("value") String value) {
		Response<?> response = misReportService.exportMisReportData(value);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
}
