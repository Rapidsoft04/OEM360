package com.sbi.oem.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sbi.oem.service.RecommendationService;

@Component
public class Scheduler {

	@Autowired
	private RecommendationService recommendationService;
	
	@Scheduled(cron = "00 00 18 * * *",zone = "UTC")
	public void sendEmailOnLessAvailStock() {
		recommendationService.changeRecommendationStatusByScheduler();
	}
}
