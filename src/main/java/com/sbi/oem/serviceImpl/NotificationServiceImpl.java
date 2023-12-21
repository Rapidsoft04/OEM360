package com.sbi.oem.serviceImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Notification;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.NotificationRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Override
	public void save(Recommendation recommendation, RecommendationStatusEnum status) {
		try {

			Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
					.findAllByDepartmentId(recommendation.getDepartment().getId());

			if (status.equals(RecommendationStatusEnum.CREATED)) {
				List<User> userList = Arrays.asList(departmentApprover.get().getAgm(),
						departmentApprover.get().getApplicationOwner());
				for (User user : userList) {
					Notification newNotification = new Notification();
					newNotification.setMessage(recommendation.getDescriptions());
					newNotification.setReferenceId(recommendation.getReferenceId());
					newNotification.setMessage("New recommendation request has been created.");
					newNotification.setCreatedAt(new Date());
					newNotification.setUpdatedAt(new Date());
					newNotification.setIsSeen(false);
					newNotification.setUser(user);
					notificationRepository.save(newNotification);
				}
			} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {
				User agm = departmentApprover.get().getAgm();
				Notification newNotification = new Notification();
				newNotification.setMessage(recommendation.getDescriptions());
				newNotification.setReferenceId(recommendation.getReferenceId());
				newNotification.setMessage("App owner has accepted a new recommendation.");
				newNotification.setCreatedAt(new Date());
				newNotification.setUpdatedAt(new Date());
				newNotification.setIsSeen(false);
				newNotification.setUser(agm);
				notificationRepository.save(newNotification);
			} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {
				User agm = departmentApprover.get().getAgm();
				Notification newNotification = new Notification();
				newNotification.setMessage(recommendation.getDescriptions());
				newNotification.setReferenceId(recommendation.getReferenceId());
				newNotification.setMessage("App owner has rejected a recommendation.");
				newNotification.setCreatedAt(new Date());
				newNotification.setUpdatedAt(new Date());
				newNotification.setIsSeen(false);
				newNotification.setUser(agm);
				notificationRepository.save(newNotification);
			} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {
				List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
						departmentApprover.get().getApplicationOwner());
				for (User user : userList) {
					Notification newNotification = new Notification();
					newNotification.setMessage(recommendation.getDescriptions());
					newNotification.setReferenceId(recommendation.getReferenceId());
					newNotification.setMessage("Your recommendation request has been approved by AGM.");
					newNotification.setCreatedAt(new Date());
					newNotification.setUpdatedAt(new Date());
					newNotification.setIsSeen(false);
					newNotification.setUser(user);
					notificationRepository.save(newNotification);
				}
			} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {
				User appOwner = departmentApprover.get().getApplicationOwner();
				Notification newNotification = new Notification();
				newNotification.setMessage(recommendation.getDescriptions());
				newNotification.setReferenceId(recommendation.getReferenceId());
				newNotification.setMessage("AGM has commented on your recommendation");
				newNotification.setCreatedAt(new Date());
				newNotification.setUpdatedAt(new Date());
				newNotification.setIsSeen(false);
				newNotification.setUser(appOwner);
				notificationRepository.save(newNotification);
			} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {
				// Notification to OEM & APP OWNER
				List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
						departmentApprover.get().getApplicationOwner());
				for (User user : userList) {
					Notification newNotification = new Notification();
					newNotification.setMessage(recommendation.getDescriptions());
					newNotification.setReferenceId(recommendation.getReferenceId());
					newNotification.setMessage("Your recommendation request has been rejected by AGM.");
					newNotification.setCreatedAt(new Date());
					newNotification.setUpdatedAt(new Date());
					newNotification.setIsSeen(false);
					newNotification.setUser(user);
					notificationRepository.save(newNotification);
				}
			} else if(status.equals(RecommendationStatusEnum.REJECT_RECOMMENDATION)) {
				User oem = recommendation.getCreatedBy();
				Notification newNotification = new Notification();
				newNotification.setMessage(recommendation.getDescriptions());
				newNotification.setReferenceId(recommendation.getReferenceId());
				newNotification.setMessage("AGM has Rejected the recommendation");
				newNotification.setCreatedAt(new Date());
				newNotification.setUpdatedAt(new Date());
				newNotification.setIsSeen(false);
				newNotification.setUser(oem);
				notificationRepository.save(newNotification);
			} else if(status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {
				User agm = departmentApprover.get().getAgm();
				Notification newNotification = new Notification();
				newNotification.setMessage(recommendation.getDescriptions());
				newNotification.setReferenceId(recommendation.getReferenceId());
				newNotification.setMessage("Deployment Details have been updated");
				newNotification.setCreatedAt(new Date());
				newNotification.setUpdatedAt(new Date());
				newNotification.setIsSeen(false);
				newNotification.setUser(agm);
				notificationRepository.save(newNotification);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Response<?> getNotificationByUserId(Long userId) {
		try {
			List<Notification> list = notificationRepository.findByUserId(userId);
			return new Response<>(HttpStatus.OK.value(), "success", list);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public void markAsSeen(Long userId) {
		try {
			notificationRepository.markAsSeen(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void markAsSeenV2(Long id) {
		try {
			Optional<Notification> notification = notificationRepository.findById(id);
			if (notification != null && notification.isPresent()) {
				notification.get().setIsSeen(true);
				notification.get().setUpdatedAt(new Date());
				notificationRepository.save(notification.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getRecommendationByReferenceId(String referenceId, RecommendationStatusEnum status) {
		try {
			Optional<Recommendation> recommendation = recommendationRepository.findByReferenceId(referenceId);
			if (recommendation != null && recommendation.isPresent()) {
				save(recommendation.get(), status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
