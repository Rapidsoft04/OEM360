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
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@PostMapping("/v1/save")
	public ResponseEntity<?> save(@RequestBody Recommendation recommendation) {
		notificationService.save(recommendation);
		return new ResponseEntity<>("success", HttpStatus.OK);
	}

	@GetMapping("/v1/notification-request")
	public ResponseEntity<?> getByUserId(@RequestParam("userId") Long userId) {
		Response<?> response = notificationService.getNotificationByUserId(userId);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}
	
	@PostMapping("/v1/mark-seen")
	public ResponseEntity<?> markAsSeen(@RequestParam("userId") Long userId) {
		notificationService.markAsSeen(userId);
		return new ResponseEntity<>("success", HttpStatus.OK);
	}
}
