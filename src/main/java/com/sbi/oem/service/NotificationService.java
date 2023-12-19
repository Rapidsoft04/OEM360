package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Recommendation;

public interface NotificationService {

	public void save(Recommendation recommendation);
	
	public Response<?> getNotificationByUserId(Long userId);
	
	public void markAsSeen(Long userId);
}
