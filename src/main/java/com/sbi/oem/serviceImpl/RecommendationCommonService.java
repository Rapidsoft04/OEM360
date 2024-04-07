package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
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

import com.sbi.oem.backup.data.DataRetrievalService;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.RecommendationTrailResponseDto;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.StatusEnum;
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
import com.sbi.oem.repository.RecommendationTrailRepository;

public class RecommendationCommonService {

	@Autowired
	private RecommendationTrailRepository recommendationTrailRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Autowired
	private RecommendationMessagesRepository recommendationMessagesRepository;

	@Autowired
	private DataRetrievalService dataRetrievalService;

	public RecommendationResponseDto getNoActionOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap) {

		try {
			RecommendationResponseDto responseDto = rcmnd.convertToDto();

			List<RecommendationTrail> trailList = recommendationTrailRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
			for (RecommendationTrail trail : trailList) {
				recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
			}
			Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
			responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
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
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
			if (rcmdDate.before(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
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
			return responseDto;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}

	public RecommendationResponseDto getApprovedWithDelayOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap) {
		try {

			Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
			RecommendationResponseDto responseDto = new RecommendationResponseDto();
			if (recommendDate.before(new Date())
					&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

				responseDto = rcmnd.convertToDto();

				List<RecommendationTrail> trailList = recommendationTrailRepository
						.findAllByReferenceId(rcmnd.getReferenceId());
				Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
				for (RecommendationTrail trail : trailList) {
					recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
				}
				Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
						.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey,
								Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

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
				responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
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
				Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
				if (rcmdDate.before(new Date())
						&& responseDto.getStatus().getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
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

				if (responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
					RecommendationTrail trailObj = trailList.stream().filter(e -> e.getRecommendationStatus() != null
							&& e.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName()))
							.findFirst().get();
					if (rcmdDate.before(trailObj.getCreatedAt())) {
						RecommendationStatus status = new RecommendationStatus();
						status.setId(StatusEnum.Released_With_Delay.getId());
						status.setStatusName(StatusEnum.Released_With_Delay.getName());
						responseDto.setStatus(status);
					}

				}

			}
			return responseDto;

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getReleasedWithDelayOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap) {
		try {
			RecommendationResponseDto responseDto = new RecommendationResponseDto();
			if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.Released.getId().longValue()) {
				List<RecommendationTrail> trailList = recommendationTrailRepository
						.findAllByReferenceId(rcmnd.getReferenceId());
				RecommendationTrail trailObj = trailList.stream()
						.filter(e -> e.getRecommendationStatus() != null
								&& e.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName()))
						.findFirst().get();
				Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());

				responseDto = rcmnd.convertToDto();

				Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
				for (RecommendationTrail trails : trailList) {
					recommendationTrailMap.put(trails.getRecommendationStatus().getId(), trails);
				}
				Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
						.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey,
								Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

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
				responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
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
				if (recommendDate.before(trailObj.getCreatedAt())) {
					RecommendationStatus status = new RecommendationStatus();
					status.setId(StatusEnum.Released_With_Delay.getId());
					status.setStatusName(StatusEnum.Released_With_Delay.getName());
					responseDto.setStatus(status);

				}

			}

			return responseDto;
		} catch (Exception e2) {
			// TODO: handle exception
			return null;
		}

	}

	public RecommendationResponseDto getReleasedOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap) {
		try {

			List<RecommendationTrail> trailList = recommendationTrailRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			RecommendationTrail trailObj = trailList.stream()
					.filter(e -> e.getRecommendationStatus() != null
							&& e.getRecommendationStatus().getStatusName().equals(StatusEnum.Released.getName()))
					.findFirst().get();
			Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());

			RecommendationResponseDto responseDto = rcmnd.convertToDto();

			Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
			for (RecommendationTrail trails : trailList) {
				recommendationTrailMap.put(trails.getRecommendationStatus().getId(), trails);
			}
			Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
			responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
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
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
			if (rcmdDate.before(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
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
			if (recommendDate.before(trailObj.getCreatedAt())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
				RecommendationStatus status = new RecommendationStatus();
				status.setId(StatusEnum.Released_With_Delay.getId());
				status.setStatusName(StatusEnum.Released_With_Delay.getName());
				responseDto.setStatus(status);
			}
			return responseDto;

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getApprovedButDelayOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap, Optional<CredentialMaster> master,
			Map<Long, DepartmentApprover> departmentApproverMap) {
		try {
			RecommendationResponseDto responseDto = new RecommendationResponseDto();
			Date recommendDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(rcmnd.getRecommendDate());
			if (recommendDate.before(new Date())
					&& rcmnd.getRecommendationStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

				responseDto = rcmnd.convertToDto();
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
						.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey,
								Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap<Long, RecommendationTrail>::new));

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
				responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
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

				if (rcmdDate.before(new Date())
						&& responseDto.getStatus().getStatusName().equals(StatusEnum.Released.getName())) {
					RecommendationStatus status = new RecommendationStatus();
					status.setId(StatusEnum.Released_With_Delay.getId());
					status.setStatusName(StatusEnum.Released_With_Delay.getName());
					responseDto.setStatus(status);
				}

			}
			return responseDto;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getNoActionOrReleaseOrReleaseWithDelayOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap, Optional<CredentialMaster> master,
			Map<Long, DepartmentApprover> departmentApproverMap) {
		try {

			RecommendationResponseDto responseDto = rcmnd.convertToDto();
			List<RecommendationMessages> messageList = recommendationMessagesRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			if (messageList != null && messageList.size() > 0) {
				List<RecommendationMessages> updatedMessageList = messageList.stream()
						.filter(e -> e.getCreatedBy() != null
								&& e.getCreatedBy().getId().longValue() == master.get().getUserId().getId().longValue())
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
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
			responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
			Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
					.findByRecommendRefId(rcmnd.getReferenceId());
			if (deploymentDetails != null && deploymentDetails.isPresent()) {
				responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
			} else {
				responseDto.setRecommendationDeploymentDetails(null);
			}
			if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
				DepartmentApprover approverObj = departmentApproverMap.get(rcmnd.getDepartment().getId().longValue());
				responseDto.setAppOwner(approverObj.getApplicationOwner());
				responseDto.setApprover(approverObj.getAgm());
			}
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
			if (rcmdDate.before(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
				RecommendationStatus status = new RecommendationStatus();
				status.setId(StatusEnum.No_Action.getId());
				status.setStatusName(StatusEnum.No_Action.getName());
				responseDto.setStatus(status);
			}

			return responseDto;

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getApprovedOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap, Optional<CredentialMaster> master,
			Map<Long, DepartmentApprover> departmentApproverMap) {
		try {
			RecommendationResponseDto responseDto = rcmnd.convertToDto();
			List<RecommendationMessages> messageList = recommendationMessagesRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			if (messageList != null && messageList.size() > 0) {
				List<RecommendationMessages> updatedMessageList = messageList.stream()
						.filter(e -> e.getCreatedBy() != null
								&& e.getCreatedBy().getId().longValue() == master.get().getUserId().getId().longValue())
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
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
			responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
			Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
					.findByRecommendRefId(rcmnd.getReferenceId());
			if (deploymentDetails != null && deploymentDetails.isPresent()) {
				responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
			} else {
				responseDto.setRecommendationDeploymentDetails(null);
			}
			if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
				DepartmentApprover approverObj = departmentApproverMap.get(rcmnd.getDepartment().getId().longValue());
				responseDto.setAppOwner(approverObj.getApplicationOwner());
				responseDto.setApprover(approverObj.getAgm());
			}
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());

			if (rcmdDate.after(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {

				return responseDto;
			} else {
				return null;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getApprovedNotReleasedOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap, SearchDto searchDto,
			Map<Long, DepartmentApprover> departmentApproverMap, Optional<CredentialMaster> master) {
		try {

			RecommendationResponseDto responseDto = rcmnd.convertToDto();
			List<RecommendationMessages> messageList = recommendationMessagesRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			if (messageList != null && messageList.size() > 0) {
				List<RecommendationMessages> updatedMessageList = messageList.stream()
						.filter(e -> e.getCreatedBy() != null
								&& e.getCreatedBy().getId().longValue() == master.get().getUserId().getId().longValue())
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
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
			responseDto.setPriority(dataRetrievalService.getPriority(rcmnd.getPriorityId()));
			Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
					.findByRecommendRefId(rcmnd.getReferenceId());
			if (deploymentDetails != null && deploymentDetails.isPresent()) {
				responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
			} else {
				responseDto.setRecommendationDeploymentDetails(null);
			}
			if (departmentApproverMap.containsKey(rcmnd.getDepartment().getId().longValue())) {
				DepartmentApprover approverObj = departmentApproverMap.get(rcmnd.getDepartment().getId().longValue());
				responseDto.setAppOwner(approverObj.getApplicationOwner());
				responseDto.setApprover(approverObj.getAgm());
			}
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());

			if (rcmdDate.before(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.Approved.getName())) {
				RecommendationStatus status = new RecommendationStatus();
				status.setId(StatusEnum.Delayed.getId());
				status.setStatusName(StatusEnum.Delayed.getName());
				responseDto.setStatus(status);
			}
			if (searchDto.getStatusId() == null
					|| ((searchDto.getStatusId().longValue() != StatusEnum.No_Action.getId().longValue())
							&& (searchDto.getStatusId().longValue() != StatusEnum.Released_With_Delay.getId()
									.longValue())
							&& (searchDto.getStatusId().longValue() != StatusEnum.Delayed.getId().longValue()))) {
				return responseDto;
			} else {
				return null;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public RecommendationResponseDto getElseDataOemRecommendation(Recommendation rcmnd,
			List<RecommendationStatus> statusList, Map<Long, String> priorityMap,
			Map<Long, DepartmentApprover> departmentApproverMap, Optional<CredentialMaster> master) {
		try {

			RecommendationResponseDto responseDto = rcmnd.convertToDto();
			List<RecommendationMessages> messageList = recommendationMessagesRepository
					.findAllByReferenceId(rcmnd.getReferenceId());
			if (messageList != null && messageList.size() > 0) {
				List<RecommendationMessages> updatedMessageList = messageList.stream()
						.filter(e -> e.getCreatedBy() != null
								&& e.getCreatedBy().getId().longValue() == master.get().getUserId().getId().longValue())
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
					.sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
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
				DepartmentApprover approverObj = departmentApproverMap.get(rcmnd.getDepartment().getId().longValue());
				responseDto.setAppOwner(approverObj.getApplicationOwner());
				responseDto.setApprover(approverObj.getAgm());
			}
			Date rcmdDate = com.sbi.oem.util.DateUtil.convertDateToNigh12AM(responseDto.getRecommendDate());
			if (rcmdDate.before(new Date())
					&& responseDto.getStatus().getStatusName().equals(StatusEnum.OEM_recommendation.getName())) {
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
			return responseDto;

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}
}
