package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.Recommendation;

public interface NotificationService {

	public void save(Recommendation recommendation, RecommendationStatusEnum status);

	public Response<?> getNotificationByUserId(Long userId);

	public void markAsSeen(Long userId);

	public void markAsSeenV2(Long notificationId);

	public void getRecommendationByReferenceId(String referenceId, RecommendationStatusEnum status);
}
