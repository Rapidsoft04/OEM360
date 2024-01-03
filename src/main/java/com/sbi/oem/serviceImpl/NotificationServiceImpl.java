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
			if (recommendation != null && status != null) {
				Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
						.findAllByDepartmentId(recommendation.getDepartment().getId());
				if (departmentApprover != null && !departmentApprover.isEmpty()) {
					if (status.equals(RecommendationStatusEnum.CREATED)) {
						List<User> userList = Arrays.asList(departmentApprover.get().getAgm(),
								departmentApprover.get().getApplicationOwner());
						String text = "New recommendation request has been created.";
						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, user);
						}
					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {
						User agm = departmentApprover.get().getAgm();
						String text = "App owner has accepted a new recommendation.";
						createNotification(recommendation.getReferenceId(), text, agm);
					} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {
						User agm = departmentApprover.get().getAgm();
						String text = "App owner has rejected a recommendation.";
						createNotification(recommendation.getReferenceId(), text, agm);
					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {
						List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
								departmentApprover.get().getApplicationOwner());
						String text = "Your recommendation request has been approved by AGM.";
						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, user);
						}
					} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {
						User appOwner = departmentApprover.get().getApplicationOwner();
						String text = "AGM has commented on your recommendation";
						createNotification(recommendation.getReferenceId(), text, appOwner);
					} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {
						User appOwner = departmentApprover.get().getApplicationOwner();
						String text = "Your recommendation request has been rejected by AGM.";
						createNotification(recommendation.getReferenceId(), text, appOwner);
					} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {
						User oem = recommendation.getCreatedBy();
						String text = "AGM has Rejected the recommendation";
						createNotification(recommendation.getReferenceId(), text, oem);
					} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {
						User agm = departmentApprover.get().getAgm();
						String text = "Deployment Details have been updated";
						createNotification(recommendation.getReferenceId(), text, agm);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createNotification(String referenceId, String notificationText, User user) {
		try {
			Notification notification = new Notification();
			notification.setReferenceId(referenceId);
			notification.setMessage(notificationText);
			notification.setUser(user);
			notification.setIsSeen(false);
			notification.setCreatedAt(new Date());
			notification.setUpdatedAt(new Date());
			notificationRepository.save(notification);
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

		Thread notificationThread = new Thread(() -> {
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
		});
		notificationThread.start();

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

	@Override
	public void saveAllNotification(List<Recommendation> recommendationList, RecommendationStatusEnum status) {

		Thread notificationThread = new Thread(() -> {
			try {

				for (Recommendation recommendation : recommendationList) {

					Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
							.findAllByDepartmentId(recommendation.getDepartment().getId());
					if (departmentApprover != null && !departmentApprover.isEmpty()) {
						if (status.equals(RecommendationStatusEnum.CREATED)) {
							List<User> userList = Arrays.asList(departmentApprover.get().getAgm(),
									departmentApprover.get().getApplicationOwner());
							String text = "New recommendation request has been created.";
							for (User user : userList) {
								createNotification(recommendation.getReferenceId(), text, user);
							}
						} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {
							User agm = departmentApprover.get().getAgm();
							String text = "App owner has accepted a new recommendation.";
							createNotification(recommendation.getReferenceId(), text, agm);
						} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {
							User agm = departmentApprover.get().getAgm();
							String text = "App owner has rejected a recommendation.";
							createNotification(recommendation.getReferenceId(), text, agm);
						} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {
							List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
									departmentApprover.get().getApplicationOwner());
							String text = "Your recommendation request has been approved by AGM.";
							for (User user : userList) {
								createNotification(recommendation.getReferenceId(), text, user);
							}
						} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {
							User appOwner = departmentApprover.get().getApplicationOwner();
							String text = "AGM has commented on your recommendation";
							createNotification(recommendation.getReferenceId(), text, appOwner);
						} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {
							User appOwner = departmentApprover.get().getApplicationOwner();
							String text = "Your recommendation request has been rejected by AGM.";
							createNotification(recommendation.getReferenceId(), text, appOwner);
						} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {
							User oem = recommendation.getCreatedBy();
							String text = "AGM has Rejected the recommendation";
							createNotification(recommendation.getReferenceId(), text, oem);
						} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {
							User agm = departmentApprover.get().getAgm();
							String text = "Deployment Details have been updated";
							createNotification(recommendation.getReferenceId(), text, agm);
						}
					}

				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		});

		notificationThread.start();

	}

}
