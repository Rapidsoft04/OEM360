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

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody Recommendation recommendation) {
		notificationService.save(recommendation, RecommendationStatusEnum.CREATED);
		return new ResponseEntity<>("success", HttpStatus.OK);
	}

	@GetMapping("/pending/request")
	public ResponseEntity<?> getByUserId(@RequestParam("userId") Long userId) {
		Response<?> response = notificationService.getNotificationByUserId(userId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
	
	@PostMapping("/mark-seen")
	public ResponseEntity<?> markAsSeen(@RequestParam("userId") Long userId) {
		notificationService.markAsSeen(userId);
		return new ResponseEntity<>("success", HttpStatus.OK);
	}
	
	@PostMapping("/mark/seen")
	public ResponseEntity<?> markAsSeenV2(@RequestBody Long id) {
		notificationService.markAsSeenV2(id);
		return new ResponseEntity<>("success", HttpStatus.OK);
	}
}
