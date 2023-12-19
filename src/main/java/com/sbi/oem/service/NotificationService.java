package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Notification;
<<<<<<< Updated upstream

public interface NotificationService {

	public Response<?> save(Notification notification);
=======
import com.sbi.oem.model.Recommendation;

public interface NotificationService {

	public Response<?> save(Recommendation recommendation);
>>>>>>> Stashed changes
	
	public Response<?> getNotificationByUserId(Long userId);
	
	public Response<?> markAsSeen(Long userId);
}
