package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.service.RecommendationService;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

	@Autowired
	private RecommendationService recommendationService;

	@GetMapping("/page/data")
	public ResponseEntity<?> getRecommendationPageData(@RequestParam("companyId") Long companyId) {
		Response<?> response = recommendationService.getRecommendationPageData(companyId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
