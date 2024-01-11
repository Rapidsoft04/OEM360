package com.sbi.oem.serviceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Notification;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.NotificationRepository;
import com.sbi.oem.repository.RecommendationDeplyomentDetailsRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.service.NotificationService;

@Service
@EnableScheduling
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private RecommendationDeplyomentDetailsRepository recommendationdeplyomentDetailsRepository;

	@Override
	public void save(Recommendation recommendation, RecommendationStatusEnum status, String rejectionMessage,
			String additionalInformation) {
		try {
			Optional<Recommendation> recommendationObj = recommendationRepository
					.findByReferenceId(recommendation.getReferenceId());
			if (recommendation != null && status != null) {
				Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
						.findAllByDepartmentId(recommendation.getDepartment().getId());

				Optional<RecommendationDeplyomentDetails> deplyomentDetails = recommendationdeplyomentDetailsRepository
						.findByRecommendRefId(recommendation.getReferenceId());
				
				 rejectionMessage = (rejectionMessage != null) ? rejectionMessage : "NA";
			   additionalInformation = (additionalInformation != null) ? additionalInformation : "NA";
				
				String RecommendationStatus = "";
				if (recommendation.getRecommendationStatus().getId() == 1) {
					RecommendationStatus = StatusEnum.OEM_recommendation.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 2) {
					RecommendationStatus = StatusEnum.Review_process.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 3) {
					RecommendationStatus = StatusEnum.Approved.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 4) {
					RecommendationStatus = StatusEnum.Rejected.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 5) {
					RecommendationStatus = StatusEnum.Department_implementation.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 6) {
					RecommendationStatus = StatusEnum.UAT_testing.getName();
				} else if (recommendation.getRecommendationStatus().getId() == 7) {
					RecommendationStatus = StatusEnum.Released.getName();
				}

				if (departmentApprover != null && !departmentApprover.isEmpty()) {
					if (status.equals(RecommendationStatusEnum.CREATED)) {
						List<User> userList = Arrays.asList(departmentApprover.get().getAgm(),
								departmentApprover.get().getApplicationOwner());
						String text = "New recommendation request has been created.";

						String descriptions = "New recommendation has been created with referenceId = "
								+ recommendation.getReferenceId() + " created on "
								+ formatDate(recommendation.getCreatedAt()) != null
										? formatDate(recommendation.getCreatedAt())
										: "NA" + ". with recommended date as - "
												+ formatDate(recommendation.getRecommendDate()) != null
														? formatDate(recommendation.getRecommendDate())
														: "NA" + " RecommendationStatus is "+RecommendationStatus +
								                           ". The expected impact and affected department are succinctly conveyed "
																+ recommendation.getExpectedImpact() != null
																		? recommendation.getExpectedImpact()
																		: "NA" 
							+ "These are the systemic overview of the new Recommendation.";

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, descriptions, user,
									recommendationStatus);
						}
					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {
						User agm = departmentApprover.get().getAgm();
						String text = "App owner has accepted a new recommendation.";

						String descriptions = "Your recommendation with referenceId = "
								+ recommendation.getReferenceId()
								+ ". Recommendation deployment details has been updated as " + "Development Dates - "
								+ (deplyomentDetails.get().getDevelopmentStartDate() != null
										? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
										: "NA")
								+ "-"
								+ (deplyomentDetails.get().getDevelopementEndDate() != null
										? formatDate(deplyomentDetails.get().getDevelopementEndDate())
										: "NA")
								+ " , with Test Completion Date - "
								+ (deplyomentDetails.get().getTestCompletionDate() != null
										? formatDate(deplyomentDetails.get().getTestCompletionDate())
										: "NA")
								+ ", with Development Complete Date - "
								+ (deplyomentDetails.get().getDeploymentDate() != null
										? formatDate(deplyomentDetails.get().getDeploymentDate())
										: "NA")
								+ ". The expected impact and affected department are succinctly conveyed "
								+ (deplyomentDetails.get().getImpactedDepartment() != null
										? deplyomentDetails.get().getImpactedDepartment()
										: "NA")
								+ " These are the updated overview of the Recommendation.";

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						createNotification(recommendation.getReferenceId(), text, descriptions, agm,
								recommendationStatus);
					} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {
						User agm = new User();
						if (recommendationObj.get().getPriorityId().longValue() == PriorityEnum.High.getId()
								.longValue()) {
							agm = departmentApprover.get().getDgm();
						} else {
							agm = departmentApprover.get().getAgm();
						}
						String text = "App owner has rejected a recommendation.";
						String descriptions = "";
						if (deplyomentDetails != null && deplyomentDetails.isPresent()) {
							descriptions = "Your recommendation with referenceId = " + recommendation.getReferenceId()
									+ ". Recommendation deployment details has been updated as "
									+ "Development Dates - "
									+ (deplyomentDetails.get().getDevelopmentStartDate() != null
											? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
											: "NA")
									+ "-"
									+ (deplyomentDetails.get().getDevelopementEndDate() != null
											? formatDate(deplyomentDetails.get().getDevelopementEndDate())
											: "NA")
									+ " , with Test Completion Date - "
									+ (deplyomentDetails.get().getTestCompletionDate() != null
											? formatDate(deplyomentDetails.get().getTestCompletionDate())
											: "NA")
									+ ", with Development Complete Date - "
									+ (deplyomentDetails.get().getDeploymentDate() != null
											? formatDate(deplyomentDetails.get().getDeploymentDate())
											: "NA")
									+ ". The expected impact and affected department are succinctly conveyed "
									+ (deplyomentDetails.get().getImpactedDepartment() != null
											? deplyomentDetails.get().getImpactedDepartment()
											: "NA")
									+ " These are the updated overview of the Recommendation.";
						} else {
							descriptions = "Your recommendation with referenceId = " + recommendation.getReferenceId() + " with Current Status as (" +RecommendationStatus+
									") Recommendation has been REJECTED , Rejection message as- " + rejectionMessage + " and additional Information -" + additionalInformation;
						}
						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						createNotification(recommendation.getReferenceId(), text, descriptions, agm,
								recommendationStatus);
					} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {
						List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
								departmentApprover.get().getApplicationOwner());
						String text = "Your recommendation request has been approved by AGM.";

						String descriptions = "Your recommendation with referenceId = "
								+ recommendation.getReferenceId()
								+ ". Recommendation deployment details has been Approved by AGM " + "Development Dates - "
								+ (deplyomentDetails.get().getDevelopmentStartDate() != null
										? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
										: "NA")
								+ "-"
								+ (deplyomentDetails.get().getDevelopementEndDate() != null
										? formatDate(deplyomentDetails.get().getDevelopementEndDate())
										: "NA")
								+ " , with Test Completion Date - "
								+ (deplyomentDetails.get().getTestCompletionDate() != null
										? formatDate(deplyomentDetails.get().getTestCompletionDate())
										: "NA")
								+ ", with Development Complete Date - "
								+ (deplyomentDetails.get().getDeploymentDate() != null
										? formatDate(deplyomentDetails.get().getDeploymentDate())
										: "NA")
								+ ". The expected impact and affected department are succinctly conveyed "
								+ (deplyomentDetails.get().getImpactedDepartment() != null
										? deplyomentDetails.get().getImpactedDepartment()
										: "NA")
								+ " These are the updated overview of the Recommendation.";

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, descriptions, user,
									recommendationStatus);
						}
					} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {
						User appOwner = departmentApprover.get().getApplicationOwner();
						String text = "";
						if (recommendationObj.get().getPriorityId().longValue() == PriorityEnum.High.getId()
								.longValue()) {
							text = "DGM has commented on your recommendation";
						} else {
							text = "AGM has commented on your recommendation";
						}
						String descriptions = "";
						if (deplyomentDetails != null && deplyomentDetails.isPresent()) {
							descriptions = "Your recommendation with referenceId = " + recommendation.getReferenceId()
									+ ". Recommendation deployment details has been REVERTED as "
									+ "Development Dates - "
									+ (deplyomentDetails.get().getDevelopmentStartDate() != null
											? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
											: "NA")
									+ "-"
									 + "Rejection message as " + rejectionMessage + " and additionalInformation -" + additionalInformation
									+ " These are the updated overview of the Recommendation.";
						} else {
							descriptions = "Your recommendation with referenceId = " + recommendation.getReferenceId() +
									" Recommendation has been REVERTED . Added information as- " + additionalInformation ;
						}

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						createNotification(recommendation.getReferenceId(), text, descriptions, appOwner,
								recommendationStatus);
					} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {
						User appOwner = departmentApprover.get().getApplicationOwner();
						String text = "";
						if (recommendationObj.get().getPriorityId().longValue() == PriorityEnum.High.getId()
								.longValue()) {
							text = "Your recommendation request has been rejected by DGM.";
						} else {
							text = "Your recommendation request has been rejected by AGM.";
						}

						String descriptions = "Your recommendation with referenceId = "
								+ recommendation.getReferenceId()
								+ ". Recommendation deployment details been rejected by AGM as- " + "Development Dates - "
								+ (deplyomentDetails.get().getDevelopmentStartDate() != null
										? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
										: "NA")
								+ "-"
						        + "Rejection message as " + rejectionMessage + " and additionalInformation -" + additionalInformation
								+ " These are the updated overview of the Recommendation.";

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();
						createNotification(recommendation.getReferenceId(), text, descriptions, appOwner,
								recommendationStatus);
					} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {
						User oem = recommendation.getCreatedBy();
						User appOwner = departmentApprover.get().getApplicationOwner();
						List<User> userList = new ArrayList<>();
						userList.add(appOwner);
						userList.add(oem);
						String text = "";
						if (recommendationObj.get().getPriorityId().longValue() == PriorityEnum.High.getId()
								.longValue()) {
							text = "DGM has Rejected the recommendation";
						} else {
							text = "AGM has Rejected the recommendation";
						}
						String descriptions = "";
						if (deplyomentDetails != null && deplyomentDetails.isPresent()) {
						  descriptions = "Your recommendation with referenceId = "
									+ recommendation.getReferenceId()
									+ ". Recommendation deployment details been rejected  as- " + "Development Dates - "
									+ (deplyomentDetails.get().getDevelopmentStartDate() != null
											? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
											: "NA")
									+ "-"
							        + "Rejection message as " + rejectionMessage + " and additionalInformation -" + additionalInformation
									+ " These are the updated overview of the Recommendation.";
						} else {
							descriptions = "Your recommendation with referenceId = " + recommendation.getReferenceId() +
									" Recommendation has been REJECTED , Rejection message as- " + rejectionMessage + " and additionalInformation -" + additionalInformation;
						}

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();
						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, descriptions, user,
									recommendationStatus);
						}

					} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {
						User agm = departmentApprover.get().getAgm();
						String text = "Recommendation deployment details has been updated";

						String descriptions = "Your recommendation with referenceId = "
								+ recommendation.getReferenceId()
								+ ". Recommendation deployment details has been updated as " + "Development Dates - "
								+ (deplyomentDetails.get().getDevelopmentStartDate() != null
										? formatDate(deplyomentDetails.get().getDevelopmentStartDate())
										: "NA")
								+ "-"
								+ (deplyomentDetails.get().getDevelopementEndDate() != null
										? formatDate(deplyomentDetails.get().getDevelopementEndDate())
										: "NA")
								+ " , with Test Completion Date - "
								+ (deplyomentDetails.get().getTestCompletionDate() != null
										? formatDate(deplyomentDetails.get().getTestCompletionDate())
										: "NA")
								+ ", with Development Complete Date - "
								+ (deplyomentDetails.get().getDeploymentDate() != null
										? formatDate(deplyomentDetails.get().getDeploymentDate())
										: "NA")
								+ ". The expected impact and affected department are succinctly conveyed "
								+ (deplyomentDetails.get().getImpactedDepartment() != null
										? deplyomentDetails.get().getImpactedDepartment()
										: "NA")
								+ ". These are the updated overview of the Recommendation.";

						System.out.println("descriptions = " + descriptions);

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						createNotification(recommendation.getReferenceId(), text, descriptions, agm,
								recommendationStatus);
					} else if (status.equals(RecommendationStatusEnum.RECOMMENDATION_STATUS_CHANGED)) {
						User agm = departmentApprover.get().getAgm();
						User oem = recommendation.getCreatedBy();
						List<User> userList = new ArrayList<>();
						userList.add(agm);
						userList.add(oem);
						String text = "Recommendation status has been changed";

						String descriptions = "Your recommendation with referenceId = " +
						        recommendation.getReferenceId() +
						        ". Recommendation deployment details has been updated as " +
						        "Development Dates - " +
						        (deplyomentDetails.get().getDevelopmentStartDate() != null ?
						                formatDate(deplyomentDetails.get().getDevelopmentStartDate()) :
						                "NA") +
						        "-" +
						        "Recommendation status has been changed to " +
						        (recommendation.getRecommendationStatus() != null ?
						        		RecommendationStatus :
						                "NA") +
						        ". These are the updated overview of the Recommendation.";


						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();
						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, descriptions, user,
									recommendationStatus);
						}

					} else if (status.equals(RecommendationStatusEnum.RECOMMENDATION_RELEASED)) {
						List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
								departmentApprover.get().getAgm());
						String text = "Recommendation has been released.";

						String descriptions = "Your recommendation with referenceId = " +
						        recommendation.getReferenceId() +
						        ". Recommendation has been released , deployment details has been updated as " +
						        "Development Dates - " +
						        (deplyomentDetails.get().getDevelopmentStartDate() != null ?
						                formatDate(deplyomentDetails.get().getDevelopmentStartDate()) :
						                "NA") +
						        "-" +
						        "Recommendation status has been changed to " +
						        (recommendation.getRecommendationStatus() != null ?
						        		RecommendationStatus :
						                "NA") +
						        ". These are the updated overview of the Recommendation.";

						RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

						for (User user : userList) {
							createNotification(recommendation.getReferenceId(), text, descriptions, user,
									recommendationStatus);
						}
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

	public void createNotification(String referenceId, String notificationText, String descriptions, User user,
			RecommendationStatus status) {
		try {
			Notification notification = new Notification();
			notification.setReferenceId(referenceId);
			notification.setMessage(notificationText);
			notification.setDescriptions(descriptions);
			notification.setRecommendationStatus(new RecommendationStatus(status.getId()));
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
	public void getRecommendationByReferenceId(String referenceId, RecommendationStatusEnum status,
			String rejectionMesasge, String additionalInformation) {
		try {
			Optional<Recommendation> recommendation = recommendationRepository.findByReferenceId(referenceId);
			if (recommendation != null && recommendation.isPresent()) {
				System.out.println(" hhhh " +status.toString());
				save(recommendation.get(), status, rejectionMesasge, additionalInformation);
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
					
					String RecommendationStatus = "";
					if (recommendation.getRecommendationStatus().getId() == 1) {
						RecommendationStatus = StatusEnum.OEM_recommendation.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 2) {
						RecommendationStatus = StatusEnum.Review_process.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 3) {
						RecommendationStatus = StatusEnum.Approved.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 4) {
						RecommendationStatus = StatusEnum.Rejected.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 5) {
						RecommendationStatus = StatusEnum.Department_implementation.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 6) {
						RecommendationStatus = StatusEnum.UAT_testing.getName();
					} else if (recommendation.getRecommendationStatus().getId() == 7) {
						RecommendationStatus = StatusEnum.Released.getName();
					}

					if (departmentApprover != null && !departmentApprover.isEmpty()) {
						if (status.equals(RecommendationStatusEnum.CREATED)) {

							User appOwner = departmentApprover.get().getApplicationOwner();

							String text = "New recommendation request has been created.";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									 + ((formatDate(recommendation.getCreatedAt()) != null)
							                ? formatDate(recommendation.getCreatedAt())
							                : "NA") + ". with recommended date as - "
							        + ((formatDate(recommendation.getRecommendDate()) != null)
							                ? formatDate(recommendation.getRecommendDate())
							                : "NA") + " RecommendationStatus is " + RecommendationStatus +
							        ". The expected impact and affected department are succinctly conveyed "
							if (recommendation.getExpectedImpact() == null
									|| recommendation.getExpectedImpact().isBlank()
									|| recommendation.getExpectedImpact().equals("")) {
								descriptions = descriptions + "NA";
							} else {
								descriptions = descriptions + recommendation.getExpectedImpact();
							}
							descriptions = descriptions + " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, appOwner,
									recommendationStatus);

						} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_APPOWNER)) {

							User agm = departmentApprover.get().getAgm();

							String text = "App owner has accepted a new recommendation.";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, agm,
									recommendationStatus);

						} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_APPOWNER)) {
							User agm = departmentApprover.get().getAgm();

							String text = "App owner has rejected a recommendation.";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, agm,
									recommendationStatus);
						} else if (status.equals(RecommendationStatusEnum.APPROVED_BY_AGM)) {
							List<User> userList = Arrays.asList(recommendation.getCreatedBy(),
									departmentApprover.get().getApplicationOwner());
							String text = "Your recommendation request has been approved by AGM.";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							for (User user : userList) {
								createNotification(recommendation.getReferenceId(), text, descriptions, user,
										recommendationStatus);
							}
						} else if (status.equals(RecommendationStatusEnum.REVERTED_BY_AGM)) {
							User appOwner = departmentApprover.get().getApplicationOwner();
							String text = "AGM has commented on your recommendation";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, appOwner,
									recommendationStatus);
						} else if (status.equals(RecommendationStatusEnum.REJECTED_BY_AGM)) {
							User appOwner = departmentApprover.get().getApplicationOwner();
							String text = "Your recommendation request has been rejected by AGM.";
							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, appOwner,
									recommendationStatus);
						} else if (status.equals(RecommendationStatusEnum.RECCOMENDATION_REJECTED)) {
							User oem = recommendation.getCreatedBy();
							String text = "AGM has Rejected the recommendation";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();
							createNotification(recommendation.getReferenceId(), text, descriptions, oem,
									recommendationStatus);

						} else if (status.equals(RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS)) {
							User agm = departmentApprover.get().getAgm();
							String text = "Deployment Details have been updated";

							String descriptions = "New recommendation has been created with referenceId = "
									+ recommendation.getReferenceId() + " created on "
									+ formatDate(recommendation.getCreatedAt()) + ". with recommended date as - "
									+ formatDate(recommendation.getRecommendDate())
									+ ". The expected impact and affected department are succinctly conveyed "
									+ recommendation.getExpectedImpact()
									+ " along with accessible documentation through URLs "
									+ "These are the systemic overview of the new Recommendation.";

							RecommendationStatus recommendationStatus = recommendation.getRecommendationStatus();

							createNotification(recommendation.getReferenceId(), text, descriptions, agm,
									recommendationStatus);
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

	@Scheduled(cron = "0 00 18 * * *", zone = "UTC")
	public void deleteSeenNotifications() {
		notificationRepository.deleteByIsSeenTrue();
	}

	public static String formatDate(Date date) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.format(dateFormatter);
	}

}