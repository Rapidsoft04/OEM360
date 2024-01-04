package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.Response;
import com.sbi.oem.service.RecommendationService;

@RestController
@RequestMapping("/status")
public class RecommendationStatusController {

	@Autowired
	private RecommendationService recommendationService;
	
	@GetMapping("/get/all")
	public ResponseEntity<?> getAllRecommendationStatus(){
		Response<?> response=recommendationService.getAllRecommendedStatus();
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	@GetMapping("/list/for/implementation")
	public ResponseEntity<?> getAllStatusListToBeImplement(){
		Response<?> response=recommendationService.getAllStatusListToBeImplement();
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
}
