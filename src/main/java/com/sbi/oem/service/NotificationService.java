package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Notification;

public interface NotificationService {

	public Response<?> save(Notification notification);
	
	public Response<?> getNotificationByUserId(Long userId);
	
	public Response<?> markAsSeen(Long userId);
}
