package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.service.RecommendationTypeService;

@RestController
@RequestMapping("/recommendation/type")
public class RecommendationTypeController {

	@Autowired
	private RecommendationTypeService recommendationTypeService;

	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody RecommendationType recommendationType) {
		Response<?> response = recommendationTypeService.save(recommendationType);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@GetMapping("/get/all")
	public ResponseEntity<?> getAll() {
		Response<?> response = recommendationTypeService.getAllByCompanyId();
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
	
	@PostMapping("/update")
	public ResponseEntity<?> update(@RequestBody RecommendationType recommendationType) {
		Response<?> response = recommendationTypeService.update(recommendationType);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
}
