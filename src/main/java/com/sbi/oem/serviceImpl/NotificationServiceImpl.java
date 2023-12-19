package com.sbi.oem.serviceImpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Notification;
import com.sbi.oem.repository.NotificationRepository;
import com.sbi.oem.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Override
	public Response<?> save(Notification notification) {
		try {
			if (notification.getMessage() == null || notification.getReferenceId() == null
					|| notification.getUser() == null) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Message, ReferenceId and User ID are compulsory",
						null);
			}
			notification.setIsSeen(false);
			notification.setCreatedAt(new Date());
			notification.setUpdatedAt(new Date());
			Notification savedNotification = notificationRepository.save(notification);
			return new Response<>(HttpStatus.CREATED.value(), "Success", savedNotification);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}

	}

	@Override
	public Response<?> getNotificationByUserId(Long userId) {
		try {
			List<Notification> list = notificationRepository.findByUserId(userId);
			return new Response<>(HttpStatus.OK.value(), "success", list);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> markAsSeen(Long userId) {
		try {
			notificationRepository.markAsSeen(userId);
			return new Response<>(HttpStatus.OK.value(), "success", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

}
