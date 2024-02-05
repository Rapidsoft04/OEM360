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
import com.sbi.oem.service.ApprovedRecommendationService;
import com.sbi.oem.util.Pagination;

@Service
public class ApprovedRecommendationServiceImpl implements ApprovedRecommendationService {

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
	public Response<?> approvedRecommendationRequestForAppOwner(SearchDto searchDto) {

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
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();
					List<RecommendationResponseDto> approvedRecommendations = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());
					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());
					Long statusId = searchDto.getStatusId();
					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = new ArrayList<>();
							if (searchDto.getStatusId() != null && ((searchDto.getStatusId()
									.longValue() == StatusEnum.No_Action.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {

								searchDto.setStatusId(null);
								recommendationList = recommendationRepository
										.findAllApprovedRecommendationsBySearchDto(searchDto);
							} else {
								recommendationList = recommendationRepository
										.findAllApprovedRecommendationsBySearchDto(searchDto);
							}
							if (statusId != null) {
								searchDto.setStatusId(statusId);
							}

							for (Recommendation rcmnd : recommendationList) {
								if (searchDto.getStatusId() != null && ((searchDto.getStatusId()
										.longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);
									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								} else if (searchDto.getStatusId() != null && (searchDto.getStatusId()
										.longValue() == StatusEnum.No_Action.getId().longValue())) {
									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {

										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());
										responseDto.setMessageList(messageList);
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
										responseDto.setTrailResponse(trailResponseList);
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
										approvedRecommendations.add(responseDto);

									}
								} else if (searchDto.getStatusId() != null && (searchDto.getStatusId()
										.longValue() == StatusEnum.Delayed.getId().longValue())) {
									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.Approved.getName())) {

										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());
										responseDto.setMessageList(messageList);
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
										responseDto.setTrailResponse(trailResponseList);
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
										approvedRecommendations.add(responseDto);

									}
								} else if (searchDto.getStatusId() != null
										&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
									Date recommendDate = com.sbi.oem.util.DateUtil
											.convertDateToNigh12AM(rcmnd.getRecommendDate());
									if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus()
											.getStatusName().equals(StatusEnum.Released.getName())) {

										RecommendationResponseDto responseDto = rcmnd.convertToDto();
										List<RecommendationMessages> messageList = recommendationMessagesRepository
												.findAllByReferenceId(rcmnd.getReferenceId());
										responseDto.setMessageList(messageList);
										Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
												.findAllByDepartmentId(rcmnd.getDepartment().getId());
										responseDto.setApprover(departmentApprover.get().getAgm());
										responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
										responseDto.setTrailResponse(trailResponseList);
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
										approvedRecommendations.add(responseDto);

									}
								} else {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);
									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							}
						}
					}
					approvedRecommendationResponseDto.setApprovedRecommendation(approvedRecommendations);
					return new Response<>(HttpStatus.OK.value(), "Approved Recommendation of App Owner",
							approvedRecommendationResponseDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();

					List<RecommendationResponseDto> approvedRecommendations = getAllApprovedRecommendationForAgm(master,
							searchDto, statusList);

					approvedRecommendationResponseDto.setApprovedRecommendation(approvedRecommendations);
					return new Response<>(HttpStatus.OK.value(), "Approved Recommendation of App Owner",
							approvedRecommendationResponseDto);
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

	@Override
	public Response<?> approvedRecommendationRequestForAppOwnerThroughPagination(SearchDto searchDto,
			Integer pageNumber, Integer pageSize) {

		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();
					List<RecommendationResponseDto> approvedRecommendation = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).collect(Collectors.toList());

					Page<Recommendation> recommendationPage = null;
					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							recommendationPage = recommendationRepository.findAllApprovedRequestByPagination(searchDto,
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
								approvedRecommendation.add(responseDto);
							}
						}
					}
					approvedRecommendationResponseDto.setApprovedRecommendation(approvedRecommendation);
					Pagination<RecommendationResponseDto> paginate = new Pagination<>();
					paginate.setData(approvedRecommendationResponseDto);
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

	@Override
	public Response<?> viewRecommendationDetailsForOemAndAgmAndGm(SearchDto searchDto) {

		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			RecommendationResponseDto responseDtos = new RecommendationResponseDto();
			List<RecommendationResponseDto> recommendations = new ArrayList<>();

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
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {

					Long OemId = master.get().getUserId().getId();
					Long statusId = searchDto.getStatusId();
					List<Recommendation> recomendationListOem = new ArrayList<>();
					if (searchDto.getStatusId() != null
							&& ((searchDto.getStatusId().longValue() == StatusEnum.No_Action.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {

						searchDto.setStatusId(null);
						recomendationListOem = recommendationRepository
								.findAllRecommendationsOemAndAgmBySearchDto(OemId, searchDto);
					} else {
						recomendationListOem = recommendationRepository
								.findAllRecommendationsOemAndAgmBySearchDto(OemId, searchDto);
					}
					if (statusId != null) {
						searchDto.setStatusId(statusId);
					}

					for (Recommendation rcmnd : recomendationListOem) {
						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();

							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
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
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();

								List<RecommendationTrail> trailList = recommendationTrailRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
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
								responseDto.setTrailResponse(trailResponseList);
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
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
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
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();

								List<RecommendationTrail> trailList = recommendationTrailRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
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
								responseDto.setTrailResponse(trailResponseList);
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
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
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
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();

								List<RecommendationTrail> trailList = recommendationTrailRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
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
								responseDto.setTrailResponse(trailResponseList);
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
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());

								if (messageList != null && messageList.size() > 0) {
									responseDto.setMessageList(messageList);
								} else {
									responseDto.setMessageList(null);
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
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();

							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());

							if (messageList != null && messageList.size() > 0) {
								responseDto.setMessageList(messageList);
							} else {
								responseDto.setMessageList(null);
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
					responseDtos.setRecommendations(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Recomendation List OEM_SI", responseDtos);

				} else if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {
					List<Recommendation> recomendationListGm = new ArrayList<>();
					Long statusId = searchDto.getStatusId();
					if (searchDto.getStatusId() != null
							&& ((searchDto.getStatusId().longValue() == StatusEnum.No_Action.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									|| (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {

						searchDto.setStatusId(null);
						recomendationListGm = recommendationRepository
								.findAllRecommendationsForGmBySearchDto(searchDto);
					} else {
						recomendationListGm = recommendationRepository
								.findAllRecommendationsForGmBySearchDto(searchDto);
					}
					if (statusId != null) {
						searchDto.setStatusId(statusId);
					}
					List<Long> departmentIds = recomendationListGm.stream()
							.filter(e -> e.getDepartment().getId() != null).map(e -> e.getDepartment().getId())
							.distinct().collect(Collectors.toList());
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
					recommendations = getAllRecommendationForGM(master, statusList, searchDto, departmentApproverMap,
							recomendationListGm);
					responseDtos.setRecommendations(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Recommendation List GM.", responseDtos);

				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);

		}

		return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);

	}

	@Override
	public Response<?> viewRecommendationDetailsForOemAndAgmAndGmPagination(SearchDto searchDto, long pageNumber,
			long pageSize) {

		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			RecommendationResponseDto responseDtos = new RecommendationResponseDto();
			List<RecommendationResponseDto> recommendations = new ArrayList<>();

			if (master != null && master.isPresent()) {
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {

					Long OemId = master.get().getUserId().getId();

					Page<Recommendation> recommendationPage = recommendationRepository
							.findAllRecommendationsOemAndAgmPagination(OemId, searchDto, pageNumber, pageSize);

					List<Recommendation> RecomendationListOem = recommendationPage.getContent();

					for (Recommendation rcmnd : RecomendationListOem) {
						RecommendationResponseDto responseDto = rcmnd.convertToDto();

						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(rcmnd.getDepartment().getId());
						responseDto.setApprover(departmentApprover.get().getAgm());
						responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
						recommendations.add(responseDto);
					}
					responseDtos.setRecommendations(recommendations);

					Pagination<RecommendationResponseDto> paginate = new Pagination<>();
					paginate.setData(responseDtos);
					paginate.setPageNumber((int) pageNumber);
					paginate.setPageSize((int) pageSize);
					paginate.setNumberOfElements(recommendationPage.getNumberOfElements());
					paginate.setTotalPages(recommendationPage.getTotalPages());
					int totalElements = (int) recommendationPage.getTotalElements();
					paginate.setTotalElements(totalElements);

					return new Response<>(HttpStatus.OK.value(), "Recomendation List OEM_SI", responseDtos);

				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {

					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							Page<Recommendation> recommendationPage = recommendationRepository
									.findAllPendingRecommendationsForAgmBySearchDtoPagination(searchDto, pageNumber,
											pageSize);

							List<Recommendation> RecomendationListAgm = recommendationPage.getContent();

							List<DepartmentApprover> departmentApproverList = departmentApproverRepository
									.findAllByDepartmentIdIn(departmentIds);
							Map<Long, DepartmentApprover> departmentApproverMap = new HashMap<>();
							if (departmentApproverList != null && departmentApproverList.size() > 0) {
								for (DepartmentApprover approver : departmentApproverList) {
									if (!departmentApproverMap
											.containsKey(approver.getDepartment().getId().longValue())) {
										departmentApproverMap.put(approver.getDepartment().getId(), approver);
									}
								}
							}
							for (Recommendation rcmnd : RecomendationListAgm) {
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
									recommendations.add(responseDto);
								}

							}

							responseDtos.setRecommendations(recommendations);

							Pagination<RecommendationResponseDto> paginate = new Pagination<>();
							paginate.setData(responseDtos);
							paginate.setPageNumber((int) pageNumber);
							paginate.setPageSize((int) pageSize);
							paginate.setNumberOfElements(recommendationPage.getNumberOfElements());
							paginate.setTotalPages(recommendationPage.getTotalPages());
							int totalElements = (int) recommendationPage.getTotalElements();
							paginate.setTotalElements(totalElements);

							return new Response<>(HttpStatus.OK.value(), "Recommendation List AGM.", paginate);
						}

					}

				} else if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {

					Page<Recommendation> recommendationPage = recommendationRepository
							.findAllRecommendationsForGmBySearchDtoPagination(searchDto, pageNumber, pageSize);

					List<Recommendation> recomendationListGm = recommendationPage.getContent();

					List<Long> departmentIds = recomendationListGm.stream()
							.filter(e -> e.getDepartment().getId() != null).map(e -> e.getDepartment().getId())
							.distinct().collect(Collectors.toList());
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
					for (Recommendation rcmnd : recomendationListGm) {
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						recommendations.add(responseDto);
					}
					responseDtos.setRecommendations(recommendations);

					Pagination<RecommendationResponseDto> paginate = new Pagination<>();
					paginate.setData(responseDtos);
					paginate.setPageNumber((int) pageNumber);
					paginate.setPageSize((int) pageSize);
					paginate.setNumberOfElements(recommendationPage.getNumberOfElements());
					paginate.setTotalPages(recommendationPage.getTotalPages());
					int totalElements = (int) recommendationPage.getTotalElements();
					paginate.setTotalElements(totalElements);

					return new Response<>(HttpStatus.OK.value(), "Recommendation List GM.", paginate);

				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);

		}

		return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);

	}

	public List<RecommendationResponseDto> getAllApprovedRecommendationForAgm(Optional<CredentialMaster> master,
			SearchDto searchDto, List<RecommendationStatus> statusList) throws ParseException {

		RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();

		List<RecommendationResponseDto> approvedRecommendations = new ArrayList<>();
		List<DepartmentApprover> departmentList = departmentApproverRepository
				.findAllByUserId(master.get().getUserId().getId());
		List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
				.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

		if (departmentIds != null && departmentIds.size() > 0) {
			for (Long departmentId : departmentIds) {
				searchDto.setDepartmentId(departmentId);
				List<Recommendation> recommendationList = new ArrayList<>();
				Long statusId = searchDto.getStatusId();
				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() == StatusEnum.No_Action.getId().longValue())
								|| (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								|| (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {

					searchDto.setStatusId(null);
					recommendationList = recommendationRepository
							.findAllApprovedRecommendationsOfAgmBySearchDto(searchDto);
				} else {
					recommendationList = recommendationRepository
							.findAllApprovedRecommendationsOfAgmBySearchDto(searchDto);
				}
				if (statusId != null) {
					searchDto.setStatusId(statusId);
				}

				for (Recommendation rcmnd : recommendationList) {
					if (searchDto.getChartSearchKey() != null
							&& (!searchDto.getChartSearchKey().isBlank()
									&& searchDto.getChartSearchKey().equals(Constant.Pending_For_Approval))
							&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Approved.getId()
									.longValue()) {

						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Completed)
							&& rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId()
									.longValue()) {

						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Implementation)
							&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
									&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved
											.getId().longValue()
											&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released
													.getId().longValue()))) {

						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Rejected)
							&& (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Rejected.getId()
									.longValue())) {

						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);

						}

					} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
							&& searchDto.getChartSearchKey().equals(Constant.Planned)
							&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
									&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved
											.getId().longValue()
											&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released
													.getId().longValue()))) {

						if (searchDto.getStatusId() != null
								&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId()
												.longValue())
										&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay
												.getId().longValue()))) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Released.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.OEM_recommendation.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else if (searchDto.getStatusId() != null
								&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
							Date recommendDate = com.sbi.oem.util.DateUtil
									.convertDateToNigh12AM(rcmnd.getRecommendDate());
							if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
									.equals(StatusEnum.Approved.getName())) {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						} else {

							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							responseDto.setMessageList(messageList);

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							approvedRecommendations.add(responseDto);

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
							if (searchDto.getStatusId() != null && ((searchDto.getStatusId()
									.longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

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
							if (searchDto.getStatusId() != null && ((searchDto.getStatusId()
									.longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}

						}

					} else {
						if (searchDto.getChartSearchKey() != null && searchDto.getChartSearchKey().isBlank()) {
							if (searchDto.getStatusId() != null && ((searchDto.getStatusId()
									.longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Released_With_Delay.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Released.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.No_Action.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.OEM_recommendation.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else if (searchDto.getStatusId() != null
									&& (searchDto.getStatusId() == StatusEnum.Delayed.getId())) {
								Date recommendDate = com.sbi.oem.util.DateUtil
										.convertDateToNigh12AM(rcmnd.getRecommendDate());
								if (recommendDate.before(new Date()) && rcmnd.getRecommendationStatus().getStatusName()
										.equals(StatusEnum.Approved.getName())) {

									RecommendationResponseDto responseDto = rcmnd.convertToDto();
									List<RecommendationMessages> messageList = recommendationMessagesRepository
											.findAllByReferenceId(rcmnd.getReferenceId());
									responseDto.setMessageList(messageList);

									Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
											.findAllByDepartmentId(rcmnd.getDepartment().getId());
									responseDto.setApprover(departmentApprover.get().getAgm());
									responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
									responseDto.setTrailResponse(trailResponseList);
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
									approvedRecommendations.add(responseDto);

								}
							} else {

								RecommendationResponseDto responseDto = rcmnd.convertToDto();
								List<RecommendationMessages> messageList = recommendationMessagesRepository
										.findAllByReferenceId(rcmnd.getReferenceId());
								responseDto.setMessageList(messageList);

								Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
										.findAllByDepartmentId(rcmnd.getDepartment().getId());
								responseDto.setApprover(departmentApprover.get().getAgm());
								responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
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
								responseDto.setTrailResponse(trailResponseList);
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
								approvedRecommendations.add(responseDto);

							}
						}

					}

				}

			}
		}

		return approvedRecommendations;

	}

	public List<RecommendationResponseDto> getAllRecommendationForGM(Optional<CredentialMaster> master,
			List<RecommendationStatus> statusList, SearchDto searchDto,
			Map<Long, DepartmentApprover> departmentApproverMap, List<Recommendation> recomendationListGm)
			throws ParseException {

		List<RecommendationResponseDto> recommendations = new ArrayList<>();
		for (Recommendation rcmnd : recomendationListGm) {
			if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Completed)
					&& rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {

				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Pending_For_Approval) && (rcmnd
							.getRecommendationStatus().getId().longValue() < StatusEnum.Approved.getId().longValue())) {

				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Implementation)
					&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
							&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved.getId()
									.longValue()
									&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released.getId()
											.longValue()))) {

				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Rejected) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Rejected.getId().longValue())) {

				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.Planned)
					&& (rcmnd.getIsAgmApproved() != null && rcmnd.getIsAgmApproved().booleanValue() == true
							&& (rcmnd.getRecommendationStatus().getId().longValue() >= StatusEnum.Approved.getId()
									.longValue()
									&& rcmnd.getRecommendationStatus().getId().longValue() < StatusEnum.Released.getId()
											.longValue()))) {

				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else if (searchDto.getChartSearchKey() != null && (!searchDto.getChartSearchKey().isBlank())
					&& searchDto.getChartSearchKey().equals(Constant.On_Time) && (rcmnd.getRecommendationStatus()
							.getId().longValue() == StatusEnum.Released.getId().longValue())) {
				Optional<RecommendationTrail> trailObj = recommendationTrailRepository
						.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
				Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
						.findByRecommendRefId(rcmnd.getReferenceId());
				if ((trailObj.get().getCreatedAt().before(recommendationDeploymentDetails.get().getDeploymentDate()))) {
					if (searchDto.getStatusId() != null
							&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
				Optional<RecommendationTrail> trailObj = recommendationTrailRepository
						.findAllByReferenceIdAndStatusId(rcmnd.getReferenceId(), StatusEnum.Released.getId());
				Optional<RecommendationDeplyomentDetails> recommendationDeploymentDetails = deplyomentDetailsRepository
						.findByRecommendRefId(rcmnd.getReferenceId());
				if ((trailObj.get().getCreatedAt().after(recommendationDeploymentDetails.get().getDeploymentDate()))) {
					if (searchDto.getStatusId() != null
							&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
				if (searchDto.getStatusId() != null
						&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
								&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
										.longValue()))) {
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
					List<RecommendationTrail> trailList = recommendationTrailRepository
							.findAllByReferenceId(rcmnd.getReferenceId());
					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
					for (RecommendationTrail trail : trailList) {
						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
					}
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

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
					responseDto.setTrailResponse(trailResponseList);
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

			} else {
				if (searchDto.getChartSearchKey() == null || searchDto.getChartSearchKey().isBlank()) {
					if (searchDto.getStatusId() != null
							&& ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Delayed.getId().longValue())
									&& (searchDto.getStatusId().longValue() == StatusEnum.Released_With_Delay.getId()
											.longValue()))) {
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(rcmnd.getReferenceId());
							Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
							for (RecommendationTrail trail : trailList) {
								recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
							}
							Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
									.sorted(Map.Entry.comparingByKey())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
											LinkedHashMap<Long, RecommendationTrail>::new));

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
							responseDto.setTrailResponse(trailResponseList);
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
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(rcmnd.getReferenceId());
						Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
						for (RecommendationTrail trail : trailList) {
							recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
						}
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

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
						responseDto.setTrailResponse(trailResponseList);
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

		}
		return recommendations;

	}

}
