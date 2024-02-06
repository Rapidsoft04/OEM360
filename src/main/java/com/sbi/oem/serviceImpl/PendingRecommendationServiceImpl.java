package com.sbi.oem.serviceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.constant.Constant;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.RecommendationTrailResponseDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.RecommendationDeplyomentDetailsRepository;
import com.sbi.oem.repository.RecommendationMessagesRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationStatusRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.PendingRecommendationService;
import com.sbi.oem.util.Pagination;

@Service
public class PendingRecommendationServiceImpl implements PendingRecommendationService {

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private RecommendationTrailRepository recommendationTrailRepository;

	@Autowired
	private RecommendationStatusRepository recommendationStatusRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Autowired
	private RecommendationMessagesRepository recommendationMessagesRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Value("${application.name}")
	private String applicationName;

	@SuppressWarnings("rawtypes")
	@Lookup
	public Response getResponse() {
		return null;
	}

	public static Map<Long, String> priorityMap = new HashMap<>();

	public static void setPriorityMap(Map<Long, String> priorityMap) {
		Map<Long, String> newMap = new HashMap<>();
		newMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.getName());
		newMap.put(PriorityEnum.Medium.getId().longValue(), PriorityEnum.Medium.getName());
		newMap.put(PriorityEnum.Low.getId().longValue(), PriorityEnum.Low.getName());
		RecommendationServiceImpl.priorityMap = newMap;
	}

	@Override
	public Response<?> pendingRecommendationRequestForAppOwner(SearchDto searchDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if ((searchDto.getDateFilterKey() != null) && (!searchDto.getDateFilterKey().isBlank())) {
					String fromDate = "";
					String toDate = "";
					String addedFromTime = "00:00:00";
					String addedToTime = "23:59:59";
					if (searchDto.getDateFilterKey().equals(Constant.TODAY)) {
						Date todayDate = new Date();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String formattedDate = formatter.format(todayDate);
						fromDate = formattedDate + " " + addedFromTime;
						toDate = formattedDate + " " + addedToTime;
					} else if (searchDto.getDateFilterKey().equals(Constant.YESTERDAY)) {
						Calendar today = Calendar.getInstance();
						Calendar yesterday = (Calendar) today.clone();
						yesterday.add(Calendar.DAY_OF_MONTH, -1);
						Date utilYesterday = yesterday.getTime();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String formattedDate = formatter.format(utilYesterday);
						fromDate = formattedDate + " " + addedFromTime;
						toDate = formattedDate + " " + addedToTime;
					} else if (searchDto.getDateFilterKey().equals(Constant.THIS_MONTH)) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.DAY_OF_MONTH, 1);
						Date startDate = calendar.getTime();
						calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
						Date endDate = calendar.getTime();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String formattedStartDate = dateFormat.format(startDate);
						String formattedEndDate = dateFormat.format(endDate);
						fromDate = formattedStartDate + " " + addedFromTime;
						toDate = formattedEndDate + " " + addedToTime;
					} else if (searchDto.getDateFilterKey().equals(Constant.THIS_WEEK)) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
						Date startDate = calendar.getTime();
						calendar.add(Calendar.DAY_OF_WEEK, 6);
						Date endDate = calendar.getTime();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String formattedStartDate = dateFormat.format(startDate);
						String formattedEndDate = dateFormat.format(endDate);
						fromDate = formattedStartDate + " " + addedFromTime;
						toDate = formattedEndDate + " " + addedToTime;
					} else if (searchDto.getDateFilterKey().equals(Constant.LAST_MONTH)) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.DAY_OF_MONTH, 1);
						calendar.add(Calendar.MONTH, -1);
						Date lastMonthStartDate = calendar.getTime();
						calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
						Date lastMonthEndDate = calendar.getTime();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String formattedLastMonthStartDate = dateFormat.format(lastMonthStartDate);
						String formattedLastMonthEndDate = dateFormat.format(lastMonthEndDate);
						fromDate = formattedLastMonthStartDate + " " + addedFromTime;
						toDate = formattedLastMonthEndDate + " " + addedToTime;
					} else if (searchDto.getDateFilterKey().equals(Constant.TILL_TODAY)) {
						fromDate = null;
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						String formattedToDate = dateFormat.format(date);
						toDate = formattedToDate + " " + addedFromTime;

					} else {
						Date todayDate = new Date();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String formattedDate = formatter.format(todayDate);
						fromDate = formattedDate + " " + addedFromTime;
						toDate = formattedDate + " " + addedToTime;
					}
					searchDto.setFromDate(fromDate);
					searchDto.setToDate(toDate);
				}
				RecommendationResponseDto responseDtos = new RecommendationResponseDto();
				List<RecommendationResponseDto> recommendations = new ArrayList<>();
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
					RecommendationResponseDto pendingRecommendationResponseDto = new RecommendationResponseDto();
					List<RecommendationResponseDto> pendingRecommendation = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllPendingRecommendationsBySearchDto(searchDto);
							for (Recommendation rcmnd : recommendationList) {
								if (searchDto.getStatusId() != null
										&& (searchDto.getStatusId() != StatusEnum.Delayed.getId()
												|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
												|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());

									responseDto.setMessageList(messageList);
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									List<RecommendationTrail> trailList = recommendationTrailRepository
											.findAllByReferenceId(responseDto.getReferenceId());
									Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
									for (RecommendationTrail trail : trailList) {
										recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
									}
									Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet()
											.stream().sorted(Map.Entry.comparingByKey())
											.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
													(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

									List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
									if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
										for (Long key : sortedMap.keySet()) {
											RecommendationTrail trail = sortedMap.get(key);
											RecommendationTrailResponseDto response = trail.convertToDto();
											response.setIsStatusDone(true);
											trailResponseList.add(response);
										}
									} else {
										for (RecommendationStatus status : statusList) {
											if (sortedMap.containsKey(status.getId().longValue())) {
												RecommendationTrail trail = sortedMap.get(status.getId().longValue());
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(true);
												trailResponseList.add(response);
											} else {
												RecommendationTrail trail = new RecommendationTrail();
												trail.setRecommendationStatus(status);
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(false);
												trailResponseList.add(response);
											}
										}
									}
									responseDto.setTrailResponse(null);
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
									if (rcmnd.getRecommendationStatus().getId()
											.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {
										responseDto
												.setStatus(new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
									}
									if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Review_process
											.getId().longValue() && rcmnd.getIsAgmRejected() != null
											&& rcmnd.getIsAgmRejected().booleanValue() == true) {
										responseDto.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REJECTED));
									}
									if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Review_process
											.getId().longValue() && rcmnd.getIsAgmRejected() != null
											&& rcmnd.getIsAgmRejected().booleanValue() == false) {
										responseDto.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REVERTED));
									}
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									pendingRecommendation.add(responseDto);
								} else if (searchDto.getStatusId() != null
										&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.Approved.getName())) {
										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());

										responseDto.setMessageList(messageList);
										if (messageList != null && messageList.size() > 0) {
											List<RecommendationMessages> updatedMessageList = messageList.stream()
													.filter(e -> e.getCreatedBy() != null
															&& e.getCreatedBy().getId().longValue() == master.get()
																	.getUserId().getId().longValue())
													.collect(Collectors.toList());
											Collections.sort(updatedMessageList, Comparator
													.comparing(RecommendationMessages::getCreatedAt).reversed());
											if (updatedMessageList != null && updatedMessageList.size() > 0) {
												String message = "";
												if (updatedMessageList.get(0).getRejectionReason() != null) {
													message = updatedMessageList.get(0).getRejectionReason();
												} else {
													message = updatedMessageList.get(0).getAdditionalMessage();
												}
												responseDto.setPastExperienceComment(message);
											}
											responseDto.setMessageList(messageList);
										} else {
											responseDto.setMessageList(null);
										}
										List<RecommendationTrail> trailList = recommendationTrailRepository
												.findAllByReferenceId(responseDto.getReferenceId());
										Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
										for (RecommendationTrail trail : trailList) {
											recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
										}
										Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet()
												.stream().sorted(Map.Entry.comparingByKey())
												.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
														(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

										List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
										if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
											for (Long key : sortedMap.keySet()) {
												RecommendationTrail trail = sortedMap.get(key);
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(true);
												trailResponseList.add(response);
											}
										} else {
											for (RecommendationStatus status : statusList) {
												if (sortedMap.containsKey(status.getId().longValue())) {
													RecommendationTrail trail = sortedMap
															.get(status.getId().longValue());
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(true);
													trailResponseList.add(response);
												} else {
													RecommendationTrail trail = new RecommendationTrail();
													trail.setRecommendationStatus(status);
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(false);
													trailResponseList.add(response);
												}
											}
										}
										responseDto.setTrailResponse(null);
										if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
											responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
										} else {
											String priority = "";
											if (rcmnd.getPriorityId().longValue() == 1) {
												priority = PriorityEnum.High.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else if (rcmnd.getPriorityId().longValue() == 2) {
												priority = PriorityEnum.Medium.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else {
												priority = PriorityEnum.Low.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											}
										}
										Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
												.findByRecommendRefId(rcmnd.getReferenceId());
										if (deploymentDetails != null && deploymentDetails.isPresent()) {
											responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
										} else {
											responseDto.setRecommendationDeploymentDetails(null);
										}
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {
											responseDto.setStatus(
													new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == true) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REJECTED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == false) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REVERTED));
										}
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										pendingRecommendation.add(responseDto);
									}

								} else if (searchDto.getStatusId() != null
										&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {

									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());

										responseDto.setMessageList(messageList);
										if (messageList != null && messageList.size() > 0) {
											List<RecommendationMessages> updatedMessageList = messageList.stream()
													.filter(e -> e.getCreatedBy() != null
															&& e.getCreatedBy().getId().longValue() == master.get()
																	.getUserId().getId().longValue())
													.collect(Collectors.toList());
											Collections.sort(updatedMessageList, Comparator
													.comparing(RecommendationMessages::getCreatedAt).reversed());
											if (updatedMessageList != null && updatedMessageList.size() > 0) {
												String message = "";
												if (updatedMessageList.get(0).getRejectionReason() != null) {
													message = updatedMessageList.get(0).getRejectionReason();
												} else {
													message = updatedMessageList.get(0).getAdditionalMessage();
												}
												responseDto.setPastExperienceComment(message);
											}
											responseDto.setMessageList(messageList);
										} else {
											responseDto.setMessageList(null);
										}
										List<RecommendationTrail> trailList = recommendationTrailRepository
												.findAllByReferenceId(responseDto.getReferenceId());
										Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
										for (RecommendationTrail trail : trailList) {
											recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
										}
										Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet()
												.stream().sorted(Map.Entry.comparingByKey())
												.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
														(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

										List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
										if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
											for (Long key : sortedMap.keySet()) {
												RecommendationTrail trail = sortedMap.get(key);
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(true);
												trailResponseList.add(response);
											}
										} else {
											for (RecommendationStatus status : statusList) {
												if (sortedMap.containsKey(status.getId().longValue())) {
													RecommendationTrail trail = sortedMap
															.get(status.getId().longValue());
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(true);
													trailResponseList.add(response);
												} else {
													RecommendationTrail trail = new RecommendationTrail();
													trail.setRecommendationStatus(status);
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(false);
													trailResponseList.add(response);
												}
											}
										}
										responseDto.setTrailResponse(null);
										if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
											responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
										} else {
											String priority = "";
											if (rcmnd.getPriorityId().longValue() == 1) {
												priority = PriorityEnum.High.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else if (rcmnd.getPriorityId().longValue() == 2) {
												priority = PriorityEnum.Medium.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else {
												priority = PriorityEnum.Low.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											}
										}
										Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
												.findByRecommendRefId(rcmnd.getReferenceId());
										if (deploymentDetails != null && deploymentDetails.isPresent()) {
											responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
										} else {
											responseDto.setRecommendationDeploymentDetails(null);
										}
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {
											responseDto.setStatus(
													new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == true) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REJECTED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == false) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REVERTED));
										}
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										pendingRecommendation.add(responseDto);
									}

								}

								else if (searchDto.getStatusId() != null
										&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {

									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.Released.getName())) {
										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());

										responseDto.setMessageList(messageList);
										if (messageList != null && messageList.size() > 0) {
											List<RecommendationMessages> updatedMessageList = messageList.stream()
													.filter(e -> e.getCreatedBy() != null
															&& e.getCreatedBy().getId().longValue() == master.get()
																	.getUserId().getId().longValue())
													.collect(Collectors.toList());
											Collections.sort(updatedMessageList, Comparator
													.comparing(RecommendationMessages::getCreatedAt).reversed());
											if (updatedMessageList != null && updatedMessageList.size() > 0) {
												String message = "";
												if (updatedMessageList.get(0).getRejectionReason() != null) {
													message = updatedMessageList.get(0).getRejectionReason();
												} else {
													message = updatedMessageList.get(0).getAdditionalMessage();
												}
												responseDto.setPastExperienceComment(message);
											}
											responseDto.setMessageList(messageList);
										} else {
											responseDto.setMessageList(null);
										}
										List<RecommendationTrail> trailList = recommendationTrailRepository
												.findAllByReferenceId(responseDto.getReferenceId());
										Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
										for (RecommendationTrail trail : trailList) {
											recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
										}
										Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet()
												.stream().sorted(Map.Entry.comparingByKey())
												.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
														(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

										List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
										if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
											for (Long key : sortedMap.keySet()) {
												RecommendationTrail trail = sortedMap.get(key);
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(true);
												trailResponseList.add(response);
											}
										} else {
											for (RecommendationStatus status : statusList) {
												if (sortedMap.containsKey(status.getId().longValue())) {
													RecommendationTrail trail = sortedMap
															.get(status.getId().longValue());
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(true);
													trailResponseList.add(response);
												} else {
													RecommendationTrail trail = new RecommendationTrail();
													trail.setRecommendationStatus(status);
													RecommendationTrailResponseDto response = trail.convertToDto();
													response.setIsStatusDone(false);
													trailResponseList.add(response);
												}
											}
										}
										responseDto.setTrailResponse(null);
										if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
											responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
										} else {
											String priority = "";
											if (rcmnd.getPriorityId().longValue() == 1) {
												priority = PriorityEnum.High.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else if (rcmnd.getPriorityId().longValue() == 2) {
												priority = PriorityEnum.Medium.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											} else {
												priority = PriorityEnum.Low.getName();
												priorityMap.put(PriorityEnum.High.getId().longValue(),
														PriorityEnum.High.name());
												responseDto.setPriority(priority);
											}
										}
										Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
												.findByRecommendRefId(rcmnd.getReferenceId());
										if (deploymentDetails != null && deploymentDetails.isPresent()) {
											responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
										} else {
											responseDto.setRecommendationDeploymentDetails(null);
										}
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {
											responseDto.setStatus(
													new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == true) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REJECTED));
										}
										if (rcmnd.getRecommendationStatus().getId()
												.longValue() == StatusEnum.Review_process.getId().longValue()
												&& rcmnd.getIsAgmRejected() != null
												&& rcmnd.getIsAgmRejected().booleanValue() == false) {
											responseDto
													.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REVERTED));
										}
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										pendingRecommendation.add(responseDto);
									}

								} else {
									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());

									responseDto.setMessageList(messageList);
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									List<RecommendationTrail> trailList = recommendationTrailRepository
											.findAllByReferenceId(responseDto.getReferenceId());
									Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
									for (RecommendationTrail trail : trailList) {
										recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
									}
									Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet()
											.stream().sorted(Map.Entry.comparingByKey())
											.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
													(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

									List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
									if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
										for (Long key : sortedMap.keySet()) {
											RecommendationTrail trail = sortedMap.get(key);
											RecommendationTrailResponseDto response = trail.convertToDto();
											response.setIsStatusDone(true);
											trailResponseList.add(response);
										}
									} else {
										for (RecommendationStatus status : statusList) {
											if (sortedMap.containsKey(status.getId().longValue())) {
												RecommendationTrail trail = sortedMap.get(status.getId().longValue());
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(true);
												trailResponseList.add(response);
											} else {
												RecommendationTrail trail = new RecommendationTrail();
												trail.setRecommendationStatus(status);
												RecommendationTrailResponseDto response = trail.convertToDto();
												response.setIsStatusDone(false);
												trailResponseList.add(response);
											}
										}
									}
									responseDto.setTrailResponse(null);
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
									if (rcmnd.getRecommendationStatus().getId()
											.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {
										responseDto
												.setStatus(new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
									}
									if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Review_process
											.getId().longValue() && rcmnd.getIsAgmRejected() != null
											&& rcmnd.getIsAgmRejected().booleanValue() == true) {
										responseDto.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REJECTED));
									}
									if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Review_process
											.getId().longValue() && rcmnd.getIsAgmRejected() != null
											&& rcmnd.getIsAgmRejected().booleanValue() == false) {
										responseDto.setStatus(new RecommendationStatus(Constant.AGM_OR_DGM_REVERTED));
									}
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(Constant.RECOMMENDATION_CREATED)) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									pendingRecommendation.add(responseDto);

								}
							}
						}
					}

					pendingRecommendationResponseDto.setPendingRecommendation(pendingRecommendation);
					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation of App Owner",
							pendingRecommendationResponseDto);
				}
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
					recommendations = getAllPendingRecommendationForAgm(master, searchDto, statusList);
					responseDtos.setPendingRecommendation(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation List AGM.", responseDtos);
				}
				if (master.get().getUserTypeId().name().equals(UserType.DGM.name())) {

					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					searchDto.setDepartmentIds(departmentIds);
					List<Recommendation> recommendationList = recommendationRepository
							.findAllPendingRecommendationsForAgmBySearchDto(searchDto);

					List<Recommendation> recommendationListHighPriority = recommendationList.stream()
							.filter(x -> x.getPriorityId() == PriorityEnum.High.getId().longValue())
							.filter(x -> x.getIsAppOwnerRejected().booleanValue() == true).collect(Collectors.toList());

					List<DepartmentApprover> departmentApproverList = departmentApproverRepository
							.findAllByDepartmentIdIn(departmentIds);
					Map<Long, DepartmentApprover> departmentApproverMap = new HashMap<>();
					if (departmentApproverList != null && departmentApproverList.size() > 0) {
						for (DepartmentApprover approver : departmentApproverList) {
							if (!departmentApproverMap.containsKey(approver.getDepartment().getId().longValue())) {
								departmentApproverMap.put(approver.getDepartment().getId(), approver);
							}
						}
					}
					recommendations = getAllPendingRecommendationForDgm(master, searchDto, departmentApproverMap,
							recommendationListHighPriority);

					responseDtos.setPendingRecommendation(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation List DGM.", responseDtos);

				}

				else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> pendingRecommendationRequestForAppOwnerThroughPagination(SearchDto searchDto, Integer pageNumber,
			Integer pageSize) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
					RecommendationResponseDto pendingRecommendationResponseDto = new RecommendationResponseDto();
					List<RecommendationResponseDto> pendingRecommendation = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).collect(Collectors.toList());

					Page<Recommendation> recommendationPage = null;
					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							recommendationPage = recommendationRepository.findAllPendingRequestByPagination(searchDto,
									pageNumber, pageSize);
							List<Recommendation> recommendationList = recommendationPage.getContent();
							for (Recommendation rcmnd : recommendationList) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);
								List<RecommendationTrail> trailList = recommendationTrailRepository
										.findAllByReferenceId(responseDto.getReferenceId());
								Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
								for (RecommendationTrail trail : trailList) {
									recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
								}
								Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
										.sorted(Map.Entry.comparingByKey())
										.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
												(e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

								List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
								if (sortedMap.containsKey(StatusEnum.Rejected.getId().longValue())) {
									for (Long key : sortedMap.keySet()) {
										RecommendationTrail trail = sortedMap.get(key);
										RecommendationTrailResponseDto response = trail.convertToDto();
										response.setIsStatusDone(true);
										trailResponseList.add(response);
									}
								} else {
									for (RecommendationStatus status : statusList) {
										if (sortedMap.containsKey(status.getId().longValue())) {
											RecommendationTrail trail = sortedMap.get(status.getId().longValue());
											RecommendationTrailResponseDto response = trail.convertToDto();
											response.setIsStatusDone(true);
											trailResponseList.add(response);
										} else {
											RecommendationTrail trail = new RecommendationTrail();
											trail.setRecommendationStatus(status);
											RecommendationTrailResponseDto response = trail.convertToDto();
											response.setIsStatusDone(false);
											trailResponseList.add(response);
										}
									}
								}
								responseDto.setTrailResponse(null);
								responseDto.setStatus(null);
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								pendingRecommendation.add(responseDto);
							}
						}
					}
					pendingRecommendationResponseDto.setPendingRecommendation(pendingRecommendation);
					Pagination<RecommendationResponseDto> paginate = new Pagination<>();
					paginate.setData(pendingRecommendationResponseDto);
					paginate.setPageNumber(pageNumber);
					paginate.setPageSize(pageSize);
					paginate.setNumberOfElements(recommendationPage.getNumberOfElements());
					paginate.setTotalPages(recommendationPage.getTotalPages());
					int totalElements = (int) recommendationPage.getTotalElements();
					paginate.setTotalElements(totalElements);
					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation of App Owner", paginate);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	public List<RecommendationResponseDto> getAllPendingRecommendationForDgm(Optional<CredentialMaster> master,
			SearchDto searchDto, Map<Long, DepartmentApprover> departmentApproverMap,
			List<Recommendation> recommendationListHighPriority) throws ParseException {
		List<RecommendationResponseDto> recommendations = new ArrayList<>();

		for (Recommendation rcmnd : recommendationListHighPriority) {
			if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Completed) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Released.getId().longValue())) {

				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Pending_For_Approval) && (rcmnd
							.getRecommendationStatus().getId().longValue() < StatusEnum.Approved.getId().longValue())) {

				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			}

			else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Implementation) && rcmnd.getIsAgmApproved() != null
					&& rcmnd.getIsAgmApproved().booleanValue() == true
					&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved.getId().longValue()
							&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released.getId()
									.longValue())) {

				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Rejected) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Rejected.getId().longValue())) {

				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Planned)
					&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
							&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved.getId()
									.longValue()
									&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released.getId()
											.longValue()))) {

				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.On_Time) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Released.getId().longValue())) {
				Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
						.findByRecommendRefId(rcmnd.getReferenceId());
				Optional<RecommendationTrail> trailObj = recommendationTrailRepository
						.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
				if (trailObj.get().getCreatedAt().before(recommendationDeploymentDetails.get().getDeploymentDate())) {
					if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
							|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
							|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Approved.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Released.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}

				}
				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Delayed) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Released.getId().longValue())) {
				Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
						.findByRecommendRefId(rcmnd.getReferenceId());
				Optional<RecommendationTrail> trailObj = recommendationTrailRepository
						.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
				if (trailObj.get().getCreatedAt().after(recommendationDeploymentDetails.get().getDeploymentDate())) {
					if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
							|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
							|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Approved.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Released.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}

				}
				if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
						|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
						|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
							.equals(StatusEnum.OEM_recommendation.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null && (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else if (searchDto.getStatusId() != null
						&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
					Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
					if (recommendDate.before(new Date())
							&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}
				} else {

					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					List<RecommendationMessages> messageList = recommendationMessagesRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					if (messageList != null && messageList.size() > 0) {
						List<RecommendationMessages> updatedMessageList = messageList
								.stream().filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
										.longValue() == master.get().getUserId().getId().longValue())
								.collect(Collectors.toList());
						Collections.sort(updatedMessageList,
								Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
						if (updatedMessageList != null && updatedMessageList.size() > 0) {
							String message = updatedMessageList.get(0).getRejectionReason();
							responseDto.setPastExperienceComment(message);
						}
						responseDto.setMessageList(messageList);
					} else {
						responseDto.setMessageList(null);
					}
					if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
						responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
					} else {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						} else {
							priority = PriorityEnum.Low.getName();
							priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
							responseDto.setPriority(priority);
						}
					}
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
						DepartmentApprover approverObj = departmentApproverMap
								.get(rcmnd.getDepartment().getId().longValue());
						responseDto.setAppOwner(approverObj.getApplicationOwner());
						responseDto.setApprover(approverObj.getAgm());
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true
							&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						recommendations.add(responseDto);
					}
					if (rcmnd.getIsAppOwnerRejected() != null && rcmnd.getIsAppOwnerRejected().booleanValue() == true
							&& (rcmnd.getIsAgmRejected() == null || rcmnd.getIsAgmRejected().booleanValue() != true)) {
						responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
						if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.No_Action.getId());
							status.setStatusName(StatusEnum.No_Action.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Delayed.getId());
							status.setStatusName(StatusEnum.Delayed.getName());
							responseDto.setStatus(status);
						}
						if (rcmdDate.before(new Date())
								&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
							RecommendationStatus status = new RecommendationStatus();
							status.setId(StatusEnum.Released_With_Delay.getId());
							status.setStatusName(StatusEnum.Released_With_Delay.getName());
							responseDto.setStatus(status);
						}
						recommendations.add(responseDto);
					}

				}

			} else {
				if (searchDto.getChartSearchKey() == null || searchDto.getChartSearchKey().isBlank()) {
					if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
							|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
							|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.OEM_recommendation.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Approved.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else if (searchDto.getStatusId() != null
							&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
						Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
						if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
								.equals(StatusEnum.Released.getName())) {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = updatedMessageList.get(0).getRejectionReason();
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}
					} else {

						RecommendationResponseDto responseDto = rcmnd.convertToDto();
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						if (messageList != null && messageList.size() > 0) {
							List<RecommendationMessages> updatedMessageList = messageList.stream()
									.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
											.longValue() == master.get().getUserId().getId().longValue())
									.collect(Collectors.toList());
							Collections.sort(updatedMessageList,
									Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
							if (updatedMessageList != null && updatedMessageList.size() > 0) {
								String message = updatedMessageList.get(0).getRejectionReason();
								responseDto.setPastExperienceComment(message);
							}
							responseDto.setMessageList(messageList);
						} else {
							responseDto.setMessageList(null);
						}
						if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
							responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
						} else {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							} else {
								priority = PriorityEnum.Low.getName();
								priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
								responseDto.setPriority(priority);
							}
						}
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
							DepartmentApprover approverObj = departmentApproverMap
									.get(rcmnd.getDepartment().getId().longValue());
							responseDto.setAppOwner(approverObj.getApplicationOwner());
							responseDto.setApprover(approverObj.getAgm());
						}
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
								&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
							recommendations.add(responseDto);
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
								&& (rcmnd.getIsAgmRejected() == null
										|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
							Date rcmdDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(responseDto.getRecommendDate());
							if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.No_Action.getId());
								status.setStatusName(StatusEnum.No_Action.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Delayed.getId());
								status.setStatusName(StatusEnum.Delayed.getName());
								responseDto.setStatus(status);
							}
							if (rcmdDate.before(new Date())
									&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
								RecommendationStatus status = new RecommendationStatus();
								status.setId(StatusEnum.Released_With_Delay.getId());
								status.setStatusName(StatusEnum.Released_With_Delay.getName());
								responseDto.setStatus(status);
							}
							recommendations.add(responseDto);
						}

					}

				}

			}

		}
		return recommendations;
	}

	public List<RecommendationResponseDto> getAllPendingRecommendationForAgm(Optional<CredentialMaster> master,
			SearchDto searchDto, List<RecommendationStatus> statusList) throws ParseException {
		List<RecommendationResponseDto> recommendations = new ArrayList<>();
		List<DepartmentApprover> departmentList = departmentApproverRepository
				.findAllByUserId(master.get().getUserId().getId());

		List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
				.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

		if (departmentIds != null && departmentIds.size() > 0) {

			for (Long departmentId : departmentIds) {
				searchDto.setDepartmentId(departmentId);
				List<Recommendation> recommendationList = recommendationRepository
						.findAllPendingRecommendationsForAgmBySearchDto(searchDto);

				List<Recommendation> updatedRecommendationList = recommendationList.stream().filter(
						x -> x.getIsAppOwnerApproved() != null && ((x.getIsAppOwnerApproved().booleanValue() == true
								&& (x.getPriorityId() == PriorityEnum.Medium.getId()
										|| x.getPriorityId() == PriorityEnum.Low.getId()
										|| x.getPriorityId() == PriorityEnum.High.getId()))
								|| (x.getIsAppOwnerRejected().booleanValue() == true
										&& (x.getPriorityId() == PriorityEnum.Medium.getId()
												|| x.getPriorityId() == PriorityEnum.Low.getId()))))
						.collect(Collectors.toList());
				List<DepartmentApprover> departmentApproverList = departmentApproverRepository
						.findAllByDepartmentIdIn(departmentIds);
				Map<Long, DepartmentApprover> departmentApproverMap = new HashMap<>();
				if (departmentApproverList != null && departmentApproverList.size() > 0) {
					for (DepartmentApprover approver : departmentApproverList) {
						if (!departmentApproverMap.containsKey(approver.getDepartment().getId().longValue())) {
							departmentApproverMap.put(approver.getDepartment().getId(), approver);
						}
					}
				}
				for (Recommendation rcmnd : updatedRecommendationList) {
					if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Completed)
							&& (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId()
									.longValue())) {

						if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
								|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
								|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Pending_For_Approval)
							&& (rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Approved.getId()
									.longValue())) {

						if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
								|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
								|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Implementation)
							&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
									&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved
											.getId().longValue()
											&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released
													.getId().longValue()))) {

						if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
								|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
								|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Rejected)
							&& (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Rejected.getId()
									.longValue())) {

						if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
								|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
								|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Planned)
							&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
									&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved
											.getId().longValue()
											&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released
													.getId().longValue()))) {

						if (searchDto.getStatusId() != null && (searchDto.getStatusId() != StatusEnum.Delayed.getId()
								|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
								|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						} else {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							if (messageList != null && messageList.size() > 0) {
								List<RecommendationMessages> updatedMessageList = messageList.stream()
										.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
												.longValue() == master.get().getUserId().getId().longValue())
										.collect(Collectors.toList());
								Collections.sort(updatedMessageList,
										Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
								if (updatedMessageList != null && updatedMessageList.size() > 0) {
									String message = "";
									if (updatedMessageList.get(0).getRejectionReason() != null) {
										message = updatedMessageList.get(0).getRejectionReason();
									} else {
										message = updatedMessageList.get(0).getAdditionalMessage();
									}
									responseDto.setPastExperienceComment(message);
								}
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
							}
							if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
								responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
							} else {
								String priority = "";
								if (rcmnd.getPriorityId().longValue() == 1) {
									priority = PriorityEnum.High.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else if (rcmnd.getPriorityId().longValue() == 2) {
									priority = PriorityEnum.Medium.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								} else {
									priority = PriorityEnum.Low.getName();
									priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
									responseDto.setPriority(priority);
								}
							}
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
							if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
								DepartmentApprover approverObj = departmentApproverMap
										.get(rcmnd.getDepartment().getId().longValue());
								responseDto.setAppOwner(approverObj.getApplicationOwner());
								responseDto.setApprover(approverObj.getAgm());
							}
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
									&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
								recommendations.add(responseDto);
							}
							if (rcmnd.getIsAppOwnerRejected() != null
									&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
									&& (rcmnd.getIsAgmRejected() == null
											|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
								responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
								Date rcmdDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(responseDto.getRecommendDate());
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.No_Action.getId());
									status.setStatusName(StatusEnum.No_Action.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Delayed.getId());
									status.setStatusName(StatusEnum.Delayed.getName());
									responseDto.setStatus(status);
								}
								if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									RecommendationStatus status = new RecommendationStatus();
									status.setId(StatusEnum.Released_With_Delay.getId());
									status.setStatusName(StatusEnum.Released_With_Delay.getName());
									responseDto.setStatus(status);
								}
								recommendations.add(responseDto);
							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.On_Time)
							&& (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId()
									.longValue())) {
						Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						Optional<RecommendationTrail> trailObj = recommendationTrailRepository
								.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
						if ((trailObj.get().getCreatedAt()
								.before(recommendationDeploymentDetails.get().getDeploymentDate()))) {
							if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() != StatusEnum.Delayed.getId()
											|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
											|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Delayed)
							&& (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId()
									.longValue())) {
						Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						Optional<RecommendationTrail> trailObj = recommendationTrailRepository
								.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
						if ((trailObj.get().getCreatedAt()
								.after(recommendationDeploymentDetails.get().getDeploymentDate()))) {
							if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() != StatusEnum.Delayed.getId()
											|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
											|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}
						}

					} else {
						if (searchDto.getChartSearchKey() == null || searchDto.getChartSearchKey().isBlank()) {
							if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() != StatusEnum.Delayed.getId()
											|| searchDto.getStatusId() != StatusEnum.No_Action.getId()
											|| searchDto.getStatusId() != StatusEnum.Released_With_Delay.getId())) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {
									if (messageList != null && messageList.size() > 0) {
										List<RecommendationMessages> updatedMessageList = messageList.stream()
												.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
														.longValue() == master.get().getUserId().getId().longValue())
												.collect(Collectors.toList());
										Collections.sort(updatedMessageList,
												Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
										if (updatedMessageList != null && updatedMessageList.size() > 0) {
											String message = "";
											if (updatedMessageList.get(0).getRejectionReason() != null) {
												message = updatedMessageList.get(0).getRejectionReason();
											} else {
												message = updatedMessageList.get(0).getAdditionalMessage();
											}
											responseDto.setPastExperienceComment(message);
										}
										responseDto.setMessageList(messageList);
									} else {
										responseDto.setMessageList(null);
									}
									if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
										responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
									} else {
										String priority = "";
										if (rcmnd.getPriorityId().longValue() == 1) {
											priority = PriorityEnum.High.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else if (rcmnd.getPriorityId().longValue() == 2) {
											priority = PriorityEnum.Medium.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										} else {
											priority = PriorityEnum.Low.getName();
											priorityMap.put(PriorityEnum.High.getId().longValue(),
													PriorityEnum.High.name());
											responseDto.setPriority(priority);
										}
									}
									Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
											.findByRecommendRefId(rcmnd.getReferenceId());
									if (deploymentDetails != null && deploymentDetails.isPresent()) {
										responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
									} else {
										responseDto.setRecommendationDeploymentDetails(null);
									}
									if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
										DepartmentApprover approverObj = departmentApproverMap
												.get(rcmnd.getDepartment().getId().longValue());
										responseDto.setAppOwner(approverObj.getApplicationOwner());
										responseDto.setApprover(approverObj.getAgm());
									}
									if (rcmnd.getIsAppOwnerApproved() != null
											&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
											&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
										recommendations.add(responseDto);
									}
									if (rcmnd.getIsAppOwnerRejected() != null
											&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
											&& (rcmnd.getIsAgmRejected() == null
													|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
										responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
										Date rcmdDate = com.sbi.oem.util.DateUtil
												.convertDateToNigh12AM(responseDto.getRecommendDate());
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.OEM_recommendation.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.No_Action.getId());
											status.setStatusName(StatusEnum.No_Action.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Approved.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Delayed.getId());
											status.setStatusName(StatusEnum.Delayed.getName());
											responseDto.setStatus(status);
										}
										if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
												.equals(StatusEnum.Released.getName())) {
											RecommendationStatus status = new RecommendationStatus();
											status.setId(StatusEnum.Released_With_Delay.getId());
											status.setStatusName(StatusEnum.Released_With_Delay.getName());
											responseDto.setStatus(status);
										}
										recommendations.add(responseDto);
									}

								}

							} else {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								if (messageList != null && messageList.size() > 0) {
									List<RecommendationMessages> updatedMessageList = messageList.stream()
											.filter(e -> e.getCreatedBy() != null && e.getCreatedBy().getId()
													.longValue() == master.get().getUserId().getId().longValue())
											.collect(Collectors.toList());
									Collections.sort(updatedMessageList,
											Comparator.comparing(RecommendationMessages::getCreatedAt).reversed());
									if (updatedMessageList != null && updatedMessageList.size() > 0) {
										String message = "";
										if (updatedMessageList.get(0).getRejectionReason() != null) {
											message = updatedMessageList.get(0).getRejectionReason();
										} else {
											message = updatedMessageList.get(0).getAdditionalMessage();
										}
										responseDto.setPastExperienceComment(message);
									}
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
								}
								if (priorityMap != null && priorityMap.containsKey(rcmnd.getPriorityId())) {
									responseDto.setPriority(priorityMap.get(rcmnd.getPriorityId()));
								} else {
									String priority = "";
									if (rcmnd.getPriorityId().longValue() == 1) {
										priority = PriorityEnum.High.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else if (rcmnd.getPriorityId().longValue() == 2) {
										priority = PriorityEnum.Medium.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									} else {
										priority = PriorityEnum.Low.getName();
										priorityMap.put(PriorityEnum.High.getId().longValue(),
												PriorityEnum.High.name());
										responseDto.setPriority(priority);
									}
								}
								Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
										.findByRecommendRefId(rcmnd.getReferenceId());
								if (deploymentDetails != null && deploymentDetails.isPresent()) {
									responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
								} else {
									responseDto.setRecommendationDeploymentDetails(null);
								}
								if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
									DepartmentApprover approverObj = departmentApproverMap
											.get(rcmnd.getDepartment().getId().longValue());
									responseDto.setAppOwner(approverObj.getApplicationOwner());
									responseDto.setApprover(approverObj.getAgm());
								}
								if (rcmnd.getIsAppOwnerApproved() != null
										&& rcmnd.getIsAppOwnerApproved().booleanValue() == true
										&& (rcmnd.getIsAgmApproved() == null || rcmnd.getIsAgmApproved() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
									recommendations.add(responseDto);
								}
								if (rcmnd.getIsAppOwnerRejected() != null
										&& rcmnd.getIsAppOwnerRejected().booleanValue() == true
										&& (rcmnd.getIsAgmRejected() == null
												|| rcmnd.getIsAgmRejected().booleanValue() != true)) {
									responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
									Date rcmdDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(responseDto.getRecommendDate());
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.OEM_recommendation.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.No_Action.getId());
										status.setStatusName(StatusEnum.No_Action.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Approved.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Delayed.getId());
										status.setStatusName(StatusEnum.Delayed.getName());
										responseDto.setStatus(status);
									}
									if (rcmdDate.before(new Date()) && responseDto.getStatus().getStatusName()
											.equals(StatusEnum.Released.getName())) {
										RecommendationStatus status = new RecommendationStatus();
										status.setId(StatusEnum.Released_With_Delay.getId());
										status.setStatusName(StatusEnum.Released_With_Delay.getName());
										responseDto.setStatus(status);
									}
									recommendations.add(responseDto);
								}

							}
						}

					}

				}
			}

		}
		return recommendations;

	}

}