package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Notification;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;

public class NotificationResponseDto {

	private Notification notification;
	private Recommendation recommendation;
	private List<RecommendationMessages> recommendationMessages;
	private RecommendationDeplyomentDetails deplyomentDetails;

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public Recommendation getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(Recommendation recommendation) {
		this.recommendation = recommendation;
	}

	public List<RecommendationMessages> getRecommendationMessages() {
		return recommendationMessages;
	}

	public void setRecommendationMessages(List<RecommendationMessages> recommendationMessages) {
		this.recommendationMessages = recommendationMessages;
	}

	public RecommendationDeplyomentDetails getDeplyomentDetails() {
		return deplyomentDetails;
	}

	public void setDeplyomentDetails(RecommendationDeplyomentDetails deplyomentDetails) {
		this.deplyomentDetails = deplyomentDetails;
	}

}
