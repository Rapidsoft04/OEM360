package com.sbi.oem.serviceImpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
<<<<<<< Updated upstream
import com.sbi.oem.model.Notification;
=======
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Notification;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.repository.DepartmentApproverRepository;
>>>>>>> Stashed changes
import com.sbi.oem.repository.NotificationRepository;
import com.sbi.oem.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;
<<<<<<< Updated upstream

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
=======
	
	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Override
	public Response<?> save(Recommendation recommendation) {
		try {
			DepartmentApprover departmentApprover = departmentApproverRepository.getByDepartmentId(recommendation.getDepartment().getId());
			Notification newNotification = new Notification();
			newNotification.setMessage(recommendation.getDescriptions());
			newNotification.setReferenceId(recommendation.getReferenceId());
			
			
			return new Response<>(HttpStatus.CREATED.value(), "Success", null);
>>>>>>> Stashed changes
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
