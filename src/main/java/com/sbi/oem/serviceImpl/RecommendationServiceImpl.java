package com.sbi.oem.serviceImpl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.internal.build.AllowSysOut;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.sbi.oem.constant.Constant;
import com.sbi.oem.dto.PriorityResponseDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationPageDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.RecommendationTrailResponseDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SearchDto;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.enums.RecommendationStatusEnum;
import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.repository.RecommendationDeplyomentDetailsRepository;
import com.sbi.oem.repository.RecommendationMessagesRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationStatusRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.EmailTemplateService;
import com.sbi.oem.service.NotificationService;
import com.sbi.oem.service.RecommendationService;
import com.sbi.oem.util.Pagination;

@Service
public class RecommendationServiceImpl implements RecommendationService {

	@Autowired
	private RecommendationTypeRepository recommendationTypeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private ComponentRepository componentRepository;

	@Autowired
	private FileSystemStorageService fileSystemStorageService;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private RecommendationTrailRepository recommendationTrailRepository;

	@Autowired
	private RecommendationStatusRepository recommendationStatusRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@Autowired
	private NotificationService notificationService;
	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Autowired
	private RecommendationMessagesRepository recommendationMessagesRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

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
	public Response<?> getRecommendationPageData(Long companyId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				RecommendationPageDto recommendationPageDto = new RecommendationPageDto();
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {

					List<RecommendationType> recommendationList = recommendationTypeRepository
							.findAllByCompanyId(companyId);
					recommendationPageDto.setRecommendationTypeList(recommendationList);
					List<Department> departmentList = departmentRepository.findAllByCompanyId(companyId);
					recommendationPageDto.setDepartmentList(departmentList);
					List<Component> componentList = componentRepository.findAllByCompanyId(companyId);
					recommendationPageDto.setComponentList(componentList);
					List<PriorityEnum> priorityEnumList = Arrays.asList(PriorityEnum.values());
					List<PriorityResponseDto> priorityResponse = new ArrayList<>();
					for (PriorityEnum enums : priorityEnumList) {
						PriorityResponseDto dto = new PriorityResponseDto();
						dto.setId(enums.getId());
						dto.setName(enums.getName());
						priorityResponse.add(dto);
					}
					recommendationPageDto.setPriorityList(priorityResponse);
					return new Response<>(HttpStatus.OK.value(), "Recommendation page data.", recommendationPageDto);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found.", recommendationPageDto);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}

	}

	@Override
	public Response<?> addRecommendation(RecommendationAddRequestDto recommendationAddRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {

					String fileUrl = null;
					if (recommendationAddRequestDto.getFile() != null
							&& recommendationAddRequestDto.getFile().getSize() > 1048576) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "File size can't be above 1MB.", null);
					} else {
						if (recommendationAddRequestDto.getFile() != null) {
							fileUrl = fileSystemStorageService
									.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
						}
						String responseText = "Recommendation created successfully. An email will be sent to the application owner";
						Recommendation savedRecommendation = new Recommendation();
						List<Recommendation> recommendationList = new ArrayList<>();
						List<RecommendationTrail> recommendatioTrailList = new ArrayList<>();
						if (recommendationAddRequestDto.getDepartmentIds() != null
								&& recommendationAddRequestDto.getDepartmentIds().size() > 0) {
							for (Long id : recommendationAddRequestDto.getDepartmentIds()) {
								Recommendation recommendation = new Recommendation();
								recommendation.setFileUrl(fileUrl);
								recommendation.setDocumentUrl(recommendationAddRequestDto.getUrlLink());
								recommendation.setDescriptions(recommendationAddRequestDto.getDescription());
								recommendation.setCreatedAt(new Date());
								recommendation.setRecommendDate(recommendationAddRequestDto.getRecommendDate());
								recommendation.setCreatedBy(new User(recommendationAddRequestDto.getCreatedBy()));
								recommendation.setDepartment(new Department(id));
								recommendation
										.setComponent(new Component(recommendationAddRequestDto.getComponentId()));
								recommendation.setPriorityId(recommendationAddRequestDto.getPriorityId());
								recommendation.setRecommendationType(
										new RecommendationType(recommendationAddRequestDto.getTypeId()));
								recommendation.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.OEM_recommendation.getId()));
								recommendation.setExpectedImpact(recommendationAddRequestDto.getExpectedImpact());
								List<Recommendation> recommendList = recommendationRepository.findAll();
								String refId = generateReferenceId(recommendList.size());
								recommendation.setIsAppOwnerApproved(false);
								recommendation.setIsAppOwnerRejected(false);
								recommendation.setIsAgmApproved(false);
								recommendation.setReferenceId(refId);
								recommendation.setUpdatedAt(new Date());
								recommendationList.add(recommendation);
								RecommendationTrail trailData = new RecommendationTrail();
								trailData.setCreatedAt(new Date());
								trailData.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.OEM_recommendation.getId()));
								trailData.setReferenceId(refId);
								recommendatioTrailList.add(trailData);
								savedRecommendation = recommendationRepository.save(recommendation);
								recommendationTrailRepository.save(trailData);
							}
						}

						Department rcmdDepartment = savedRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());

//						notificationService.save(savedRecommendation, RecommendationStatusEnum.CREATED);
						notificationService.saveAllNotification(recommendationList, RecommendationStatusEnum.CREATED);
//						emailTemplateService.sendMailRecommendation(recommendation, RecommendationStatusEnum.CREATED);
						emailTemplateService.sendAllMailForRecommendation(recommendationList,
								RecommendationStatusEnum.CREATED);

						if (approver != null && approver.isPresent()) {
							if (approver.get().getApplicationOwner() != null
									&& !approver.get().getApplicationOwner().getEmail().isBlank()) {
								responseText += "(" + approver.get().getApplicationOwner().getEmail() + ")";
								return new Response<>(HttpStatus.CREATED.value(), responseText, null);
							}
						}
						return new Response<>(HttpStatus.CREATED.value(), responseText, null);
					}

				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

	public static String generateReferenceId(int size) {
		int year = Year.now().getValue();
		String refId = "REF" + year + (size + 1);
		return refId;
	}

	@Override
	public Response<?> viewRecommendation(String refId) {
		try {
			Optional<Recommendation> recommendation = recommendationRepository.findByReferenceId(refId);
			if (recommendation != null && recommendation.isPresent()) {
				RecommendationResponseDto responseDto = recommendation.get().convertToDto();
				if (recommendation.get().getPriorityId() != null) {
					String priority = "";
					if (recommendation.get().getPriorityId().longValue() == 1) {
						priority = PriorityEnum.High.getName();
					} else if (recommendation.get().getPriorityId().longValue() == 2) {
						priority = PriorityEnum.Medium.getName();
					} else {
						priority = PriorityEnum.Low.getName();
					}
					responseDto.setPriority(priority);
				}
				Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
						.findAllByDepartmentId(recommendation.get().getDepartment().getId());
				responseDto.setApprover(departmentApprover.get().getAgm());
				List<RecommendationTrail> trailList = recommendationTrailRepository
						.findAllByReferenceId(responseDto.getReferenceId());
				responseDto.setTrailData(trailList);
				List<RecommendationMessages> messageList = recommendationMessagesRepository.findAllByReferenceId(refId);
				responseDto.setMessageList(messageList);
				return new Response<>(HttpStatus.OK.value(), "Recommendation data.", responseDto);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Data not exist.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}

	}

	@Override
	public Response<?> getAllRecommendedStatus() {
		try {
			List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
			return new Response<>(HttpStatus.OK.value(), "Recommend status list.", statusList);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}

	}

	@Override
	public Response<?> getAllRecommendations() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					RecommendationResponseDto responseDtos = new RecommendationResponseDto();
					List<RecommendationResponseDto> pendingRecommendation = new ArrayList<>();
					List<RecommendationResponseDto> approvedRecommendation = new ArrayList<>();
					List<RecommendationResponseDto> recommendations = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());
					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						List<Recommendation> recommendationList = recommendationRepository
								.findAllByDepartmentIdIn(departmentIds);

						for (Recommendation rcmnd : recommendationList) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
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

							Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
									.findAllByDepartmentId(rcmnd.getDepartment().getId());
							responseDto.setApprover(departmentApprover.get().getAgm());
							responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
							List<RecommendationTrail> trailList = recommendationTrailRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							responseDto.setTrailData(trailList);
							List<RecommendationMessages> messageList = recommendationMessagesRepository
									.findAllByReferenceId(responseDto.getReferenceId());
							responseDto.setMessageList(messageList);
							Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
									.findByRecommendRefId(rcmnd.getReferenceId());
							if (deploymentDetails != null && deploymentDetails.isPresent()) {
								responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
							} else {
								responseDto.setRecommendationDeploymentDetails(null);
							}
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
							if (rcmnd.getIsAppOwnerApproved() != null
									&& rcmnd.getIsAppOwnerApproved().booleanValue() == true) {
								approvedRecommendation.add(responseDto);
								recommendations.add(responseDtos);
							} else {
								responseDto.setTrailResponse(null);
								responseDto.setStatus(null);
								pendingRecommendation.add(responseDto);
								recommendations.add(responseDtos);
							}

						}
						responseDtos.setApprovedRecommendation(approvedRecommendation);
						responseDtos.setPendingRecommendation(pendingRecommendation);
						return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
					} else {
						responseDtos.setApprovedRecommendation(new ArrayList<>());
						responseDtos.setPendingRecommendation(new ArrayList<>());
						return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
					}
				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					RecommendationResponseDto responseDtos = new RecommendationResponseDto();
					List<RecommendationResponseDto> recommendations = new ArrayList<>();
					List<Recommendation> recommendationList = recommendationRepository
							.findAllByUserId(master.get().getUserId().getId());
					for (Recommendation rcmnd : recommendationList) {
						RecommendationResponseDto responseDto = rcmnd.convertToDto();
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
						if (rcmnd.getIsAppOwnerApproved() != null
								&& rcmnd.getIsAppOwnerApproved().booleanValue() == true) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_ACCEPTED));
						}
						if (rcmnd.getIsAppOwnerRejected() != null
								&& rcmnd.getIsAppOwnerRejected().booleanValue() == true) {
							responseDto.setStatus(new RecommendationStatus(Constant.APPLICATION_REJECTED));
						}
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(rcmnd.getDepartment().getId());
						responseDto.setApprover(departmentApprover.get().getAgm());
						responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(responseDto.getReferenceId());
						responseDto.setMessageList(messageList);
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}

						recommendations.add(responseDto);
					}
					responseDtos.setRecommendations(recommendations);
					return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
				} else if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {
					RecommendationResponseDto responseDtos = new RecommendationResponseDto();
					List<RecommendationResponseDto> recommendations = new ArrayList<>();
					List<Recommendation> recommendationList = recommendationRepository
							.findAllByUserId(master.get().getUserId().getId());

					for (Recommendation rcmnd : recommendationList) {
						RecommendationResponseDto responseDto = rcmnd.convertToDto();
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
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(rcmnd.getDepartment().getId());
						responseDto.setApprover(departmentApprover.get().getAgm());
						responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
						List<RecommendationTrail> trailList = recommendationTrailRepository
								.findAllByReferenceId(responseDto.getReferenceId());
						responseDto.setTrailData(trailList);
						List<RecommendationMessages> messageList = recommendationMessagesRepository
								.findAllByReferenceId(responseDto.getReferenceId());
						responseDto.setMessageList(messageList);
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
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
						recommendations.add(responseDto);
					}
					responseDtos.setRecommendations(recommendations);
					return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);

				} else {
					RecommendationResponseDto responseDtos = new RecommendationResponseDto();
					List<RecommendationResponseDto> recommendations = new ArrayList<>();
					List<Recommendation> recommendationList = recommendationRepository
							.findAllByUserId(master.get().getUserId().getId());
					for (Recommendation rcmnd : recommendationList) {
						RecommendationResponseDto responseDto = rcmnd.convertToDto();
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
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
						}
						recommendations.add(responseDto);

					}
					responseDtos.setRecommendations(recommendations);
					return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
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
	public Response<?> setRecommendationDeploymentDetails(
			RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					Optional<RecommendationDeplyomentDetails> recommendDeployDetails = deplyomentDetailsRepository
							.findByRecommendRefId(recommendationDetailsRequestDto.getRecommendRefId());
					if (recommendDeployDetails != null && recommendDeployDetails.isPresent()) {
						String responseText = "Deployment details updated successfully. An email will be sent to the AGM";
						RecommendationDeplyomentDetails details = recommendationDetailsRequestDto.convertToEntity();
						details.setId(recommendDeployDetails.get().getId());
						RecommendationDeplyomentDetails savedDeploymentDetails = deplyomentDetailsRepository
								.save(details);
						Optional<Recommendation> recommendation = recommendationRepository
								.findByReferenceId(details.getRecommendRefId());
						recommendation.get().setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
						recommendation.get().setIsAppOwnerApproved(true);
						recommendation.get().setUpdatedAt(new Date());
						Recommendation updateRecommendation = recommendationRepository.save(recommendation.get());
						if (recommendationDetailsRequestDto.getDescription() != null
								&& (recommendationDetailsRequestDto.getDescription() != "")
								&& (!recommendationDetailsRequestDto.getDescription().equals(""))) {
							RecommendationMessages messages = new RecommendationMessages();
							messages.setAdditionalMessage(recommendationDetailsRequestDto.getDescription());
							messages.setCreatedBy(recommendationDetailsRequestDto.getCreatedBy());
							messages.setCreatedAt(new Date());
							messages.setReferenceId(recommendationDetailsRequestDto.getRecommendRefId());
							recommendationMessagesRepository.save(messages);
						}
						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());

						notificationService.save(recommendation.get(),
								RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS, null, null);

						emailTemplateService.sendMailRecommendationDeplyomentDetails(recommendationDetailsRequestDto,
								RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS);
						if (approver != null && approver.isPresent()) {
							if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isBlank()) {
								responseText += "(" + approver.get().getAgm().getEmail() + ")";
								return new Response<>(HttpStatus.OK.value(), responseText, null);
							}
						}
						return new Response<>(HttpStatus.OK.value(), responseText, null);

					} else {
						String responseText = "Deployment details added successfully. An email will be sent to the AGM";
						RecommendationDeplyomentDetails details = recommendationDetailsRequestDto.convertToEntity();
						details.setCreatedAt(new Date());
						deplyomentDetailsRepository.save(details);
						Optional<Recommendation> recommendation = recommendationRepository
								.findByReferenceId(details.getRecommendRefId());
						recommendation.get().setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Review_process.getId().longValue()));
						recommendation.get().setIsAppOwnerApproved(true);
						recommendation.get().setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
						recommendation.get()
								.setImpactedDepartment(recommendationDetailsRequestDto.getImpactedDepartment());
						recommendation.get().setUpdatedAt(new Date());
						Recommendation updateRecommendation = recommendationRepository.save(recommendation.get());
						RecommendationTrail trail = new RecommendationTrail();
						trail.setCreatedAt(new Date());
						trail.setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Review_process.getId().longValue()));
						trail.setReferenceId(details.getRecommendRefId());
						recommendationTrailRepository.save(trail);

						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());

						notificationService.save(recommendation.get(), RecommendationStatusEnum.APPROVED_BY_APPOWNER,
								null, null);
						emailTemplateService.sendMailRecommendationDeplyomentDetails(recommendationDetailsRequestDto,
								RecommendationStatusEnum.APPROVED_BY_APPOWNER);
						if (approver != null && approver.isPresent()) {
							if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isBlank()) {
								responseText += "(" + approver.get().getAgm().getEmail() + ")";
								return new Response<>(HttpStatus.CREATED.value(), responseText, null);
							}
						}
						return new Response<>(HttpStatus.CREATED.value(), responseText, null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"You have no access to provide deployment details.", null);
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
	public Response<?> rejectRecommendationByAppOwner(RecommendationRejectionRequestDto recommendation) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					String responseText = "Recommendation rejected successfully. An email will be sent to the AGM";

					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendation.getRecommendRefId());
					RecommendationMessages messages = recommendation.convertToEntity();
					messages.setCreatedAt(new Date());
					recommendationMessagesRepository.save(messages);
					recommendObj.get().setIsAppOwnerApproved(false);
					recommendObj.get().setIsAppOwnerRejected(true);
					recommendObj.get()
							.setRecommendationStatus(new RecommendationStatus(StatusEnum.Review_process.getId()));
					Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
					RecommendationTrail recommendTrail = new RecommendationTrail();
					recommendTrail.setCreatedAt(new Date());
					recommendTrail.setRecommendationStatus(new RecommendationStatus(StatusEnum.Review_process.getId()));
					recommendTrail.setReferenceId(recommendation.getRecommendRefId());
					recommendationTrailRepository.save(recommendTrail);
					Optional<RecommendationDeplyomentDetails> recommendDeploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(recommendation.getRecommendRefId());
					if (recommendDeploymentDetails != null && recommendDeploymentDetails.isPresent()) {
						deplyomentDetailsRepository.delete(recommendDeploymentDetails.get());
					}

					Department rcmdDepartment = updateRecommendation.getDepartment();
					Optional<DepartmentApprover> approver = departmentApproverRepository
							.findAllByDepartmentId(rcmdDepartment.getId());
					notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_APPOWNER,
							recommendation.getRejectionMessage(), recommendation.getAddtionalInformation());
					emailTemplateService.sendMailRecommendationMessages(messages,
							RecommendationStatusEnum.REJECTED_BY_APPOWNER);
					if (approver != null && approver.isPresent()) {
						if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isBlank()) {
							responseText += "(" + approver.get().getAgm().getEmail() + ")";
							return new Response<>(HttpStatus.OK.value(), responseText, null);
						}
					}
					return new Response<>(HttpStatus.OK.value(), responseText, null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access to reject.", null);
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
	public Response<?> revertApprovalRequestToAppOwnerForApproval(
			RecommendationDetailsRequestDto recommendationRejectionRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					String responseText = "Approval request reverted successfully. An email will be sent to the Appowner";
					RecommendationMessages messages = new RecommendationMessages();
					messages.setCreatedBy(recommendationRejectionRequestDto.getCreatedBy());
					messages.setAdditionalMessage(recommendationRejectionRequestDto.getDescription());
					messages.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					messages.setCreatedAt(new Date());
					recommendationMessagesRepository.save(messages);
					notificationService.getRecommendationByReferenceId(messages.getReferenceId(),
							RecommendationStatusEnum.REVERTED_BY_AGM, null,
							recommendationRejectionRequestDto.getDescription());
					emailTemplateService.sendMailRecommendationMessages(messages,
							RecommendationStatusEnum.REVERTED_BY_AGM);
					Optional<Recommendation> recommendationObj = recommendationRepository
							.findByReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					recommendationObj.get().setUpdatedAt(new Date());
					recommendationObj.get().setIsAppOwnerRejected(false);
					recommendationObj.get().setIsAppOwnerApproved(false);
					recommendationObj.get().setIsAgmRejected(false);
					Recommendation updateRecommendation = recommendationRepository.save(recommendationObj.get());
					Department rcmdDepartment = updateRecommendation.getDepartment();
					Optional<DepartmentApprover> approver = departmentApproverRepository
							.findAllByDepartmentId(rcmdDepartment.getId());
					if (approver != null && approver.isPresent()) {
						if (approver.get().getApplicationOwner() != null
								&& !approver.get().getApplicationOwner().getEmail().isBlank()) {
							responseText += "(" + approver.get().getApplicationOwner().getEmail() + ")";
							return new Response<>(HttpStatus.OK.value(), responseText, null);
						}
					}
					return new Response<>(HttpStatus.OK.value(), responseText, null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"You have no access revert recommendation request.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}

	}

	@Override
	public Response<?> rejectRecommendationByAgm(RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			String responseText = "";
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					if (recommendObj != null && recommendObj.isPresent() && recommendObj.get().getPriorityId() != PriorityEnum.High.getId()) {
						if (recommendObj.get().getIsAppOwnerApproved() != null
								&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
							responseText = "Recommendation reject request sent successfully. An email will be sent to the Appowner";
							RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
							messages.setCreatedAt(new Date());
							recommendationMessagesRepository.save(messages);
							notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_AGM, null,
									null);
							emailTemplateService.sendMailRecommendationMessages(messages,
									RecommendationStatusEnum.REJECTED_BY_AGM);
							recommendObj.get().setIsAppOwnerApproved(false);
							recommendObj.get().setIsAgmRejected(true);
							recommendObj.get().setUpdatedAt(new Date());
							recommendationRepository.save(recommendObj.get());
							return new Response<>(HttpStatus.OK.value(),
									"Recommendation reject request sent successfully.", null);
						} else {
							if (recommendObj.get().getRecommendationStatus().getId() != StatusEnum.Rejected.getId()
									.longValue()) {
								recommendObj.get().setIsAgmApproved(false);
								recommendObj.get().setRecommendationStatus(new RecommendationStatus(4L));
								recommendObj.get().setIsAgmRejected(true);
								recommendationRepository.save(recommendObj.get());
								RecommendationTrail trailData = new RecommendationTrail();
								trailData.setCreatedAt(new Date());
								trailData.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.Rejected.getId().longValue()));
								trailData.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
								recommendationTrailRepository.save(trailData);
								RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
								messages.setCreatedAt(new Date());
								recommendationMessagesRepository.save(messages);

								notificationService.save(recommendObj.get(),
										RecommendationStatusEnum.RECCOMENDATION_REJECTED, null, null);

								emailTemplateService.sendMailRecommendationMessages(messages,
										RecommendationStatusEnum.RECCOMENDATION_REJECTED);
								return new Response<>(HttpStatus.OK.value(), "Recommendation rejected successfully.",
										null);
							} else {
								return new Response<>(HttpStatus.OK.value(), "Recommendation already rejected.", null);
							}
						}
					} else if (recommendObj != null && recommendObj.isPresent()
							&& recommendObj.get().getPriorityId() == PriorityEnum.High.getId()) {

						if (recommendObj.get().getIsAppOwnerApproved() != null
								&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
							RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
							messages.setCreatedAt(new Date());
							recommendationMessagesRepository.save(messages);
							notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_AGM, null,
									recommendationRejectionRequestDto.getAddtionalInformation());
							emailTemplateService.sendMailRecommendationMessages(messages,
									RecommendationStatusEnum.REJECTED_BY_DGM);
							recommendObj.get().setIsAppOwnerApproved(false);
							recommendObj.get().setIsAgmRejected(true);
							recommendObj.get().setUpdatedAt(new Date());
							recommendationRepository.save(recommendObj.get());
							Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
							Department rcmdDepartment = updateRecommendation.getDepartment();
							Optional<DepartmentApprover> approver = departmentApproverRepository
									.findAllByDepartmentId(rcmdDepartment.getId());
							if (approver != null && approver.isPresent()) {
								if (approver.get().getApplicationOwner() != null
										&& !approver.get().getApplicationOwner().getEmail().isBlank()) {
									responseText += "(" + approver.get().getApplicationOwner().getEmail() + ")";
									return new Response<>(HttpStatus.OK.value(), responseText, null);
								}
							}
							return new Response<>(HttpStatus.OK.value(), responseText, null);
						} else {
							if (recommendObj.get().getRecommendationStatus().getId() != StatusEnum.Rejected.getId()
									.longValue()) {
								responseText = "Recommendation rejected successfully. An email will be sent to the oem";
								recommendObj.get().setIsAgmApproved(false);
								recommendObj.get().setRecommendationStatus(new RecommendationStatus(4L));
								recommendObj.get().setIsAgmRejected(true);
								Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
								RecommendationTrail trailData = new RecommendationTrail();
								trailData.setCreatedAt(new Date());
								trailData.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.Rejected.getId().longValue()));
								trailData.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
								recommendationTrailRepository.save(trailData);
								RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
								messages.setCreatedAt(new Date());
								recommendationMessagesRepository.save(messages);
								notificationService.save(recommendObj.get(),
										RecommendationStatusEnum.RECCOMENDATION_REJECTED, null, null);
								emailTemplateService.sendMailRecommendationMessages(messages,
										RecommendationStatusEnum.RECCOMENDATION_REJECTED);
								if (recommendObj != null && recommendObj.isPresent()) {
									if (recommendObj.get().getCreatedBy() != null
											&& !recommendObj.get().getCreatedBy().getEmail().isBlank()) {
										responseText += "(" + recommendObj.get().getCreatedBy().getEmail() + ")";
										return new Response<>(HttpStatus.OK.value(), responseText, null);
									}
								}
								return new Response<>(HttpStatus.OK.value(), responseText, null);
							} else {
								return new Response<>(HttpStatus.OK.value(), "Recommendation already rejected.", null);
							}
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"You have no access to reject recommendation request.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

	@Override
	public Response<?> acceptRecommendationRequestByAgm(
			RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					if (recommendObj.get().getIsAppOwnerApproved() != null
							&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
						String responseText = "Recommendation request accepted";
						recommendObj.get().setIsAgmApproved(true);
						recommendObj.get().setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Approved.getId().longValue()));
						Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
						RecommendationTrail trailData = new RecommendationTrail();
						trailData.setCreatedAt(new Date());
						trailData.setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Approved.getId().longValue()));
						trailData.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
						recommendationTrailRepository.save(trailData);
						if (recommendationRejectionRequestDto.getAddtionalInformation() != null
								&& recommendationRejectionRequestDto.getAddtionalInformation() != ""
								&& !(recommendationRejectionRequestDto.getAddtionalInformation().isEmpty())) {
							RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
							messages.setCreatedAt(new Date());
							recommendationMessagesRepository.save(messages);
						}

						notificationService.save(recommendObj.get(), RecommendationStatusEnum.APPROVED_BY_AGM, null,
								null);
						emailTemplateService.sendMailRecommendation(recommendObj.get(),
								RecommendationStatusEnum.APPROVED_BY_AGM);

						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());
						if (approver != null && approver.isPresent()) {
							if (approver.get().getApplicationOwner() != null
									&& !approver.get().getApplicationOwner().getEmail().isBlank()) {
								responseText += "Email will be sent to both Appowner("
										+ approver.get().getApplicationOwner().getEmail() + ") and OEM("
										+ recommendObj.get().getCreatedBy().getEmail() + ")";
								return new Response<>(HttpStatus.OK.value(), responseText, null);
							}
						}

						return new Response<>(HttpStatus.OK.value(), responseText, null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Recommendation is not yet approved by app owner.", null);
					}
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
	public Response<?> updateDeploymentDetails(RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					Optional<RecommendationDeplyomentDetails> recommendDeployDetails = deplyomentDetailsRepository
							.findByRecommendRefId(recommendationDetailsRequestDto.getRecommendRefId());
					if (recommendDeployDetails != null && recommendDeployDetails.isPresent()) {
						String responseText = "Deployment details updated successfully";
						RecommendationDeplyomentDetails details = recommendationDetailsRequestDto.convertToEntity();
						details.setId(recommendDeployDetails.get().getId());
						RecommendationDeplyomentDetails savedDeploymentDetails = deplyomentDetailsRepository
								.save(details);
						Optional<Recommendation> recommendation = recommendationRepository
								.findByReferenceId(details.getRecommendRefId());
						recommendation.get().setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
						Recommendation updateRecommendation = recommendationRepository.save(recommendation.get());
						if (recommendationDetailsRequestDto.getDescription() != null
								|| !recommendationDetailsRequestDto.getDescription().equals("")) {
							RecommendationMessages messages = new RecommendationMessages();
							messages.setAdditionalMessage(recommendationDetailsRequestDto.getDescription());
							messages.setCreatedBy(recommendationDetailsRequestDto.getCreatedBy());
							messages.setCreatedAt(new Date());
							messages.setReferenceId(recommendationDetailsRequestDto.getRecommendRefId());
							recommendationMessagesRepository.save(messages);
						}
						notificationService.save(recommendation.get(),
								RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS, null, null);

						System.out.println("mail send !!");

						emailTemplateService.sendMailRecommendationDeplyomentDetails(recommendationDetailsRequestDto,
								RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS);
						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());
						if (approver != null && approver.isPresent()) {
							if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isBlank()) {
								responseText += "(" + approver.get().getAgm().getEmail() + ")";
								return new Response<>(HttpStatus.OK.value(), responseText, null);
							}
						}
						return new Response<>(HttpStatus.OK.value(), responseText, null);

					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found.", null);
					}
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
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {

					Long OemId = master.get().getUserId().getId();

					List<Recommendation> RecomendationListOem = recommendationRepository
							.findAllRecommendationsOemAndAgmBySearchDto(OemId, searchDto);

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
						recommendations.add(responseDto);
					}
					responseDtos.setRecommendations(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Recomendation List OEM_SI", responseDtos);

				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {

					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllPendingRecommendationsForAgmBySearchDto(searchDto);
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
							for (Recommendation rcmnd : recommendationList) {
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
						}

					}
					responseDtos.setRecommendations(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Recommendation List AGM.", responseDtos);

				} else if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())) {

					List<Recommendation> recomendationListGm = recommendationRepository
							.findAllRecommendationsForGmBySearchDto(searchDto);
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
	public Response<?> addRecommendationThroughExcel(MultipartFile file) {
		try {
			Workbook workbook = WorkbookFactory.create(file.getInputStream());
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
					int numberOfSheets = workbook.getNumberOfSheets();
					List<String> headerList = new ArrayList<>();
					List<String> cellValueString = new ArrayList<>();
					List<JsonObject> objectList = new ArrayList<>();
					Boolean isValidFile = false;
					String[] expectedColumnNames = { "Descriptions", "Type", "Priority", "Recommend end date",
							"Department", "Component name", "Expected Impact", "Document link" };
					List<RecommendationType> recommendationTypeList = recommendationTypeRepository.findAll();
					List<Department> departmentList = departmentRepository.findAll();
					List<Component> componentList = componentRepository.findAll();
					Map<String, RecommendationType> recommendationTypeMap = new HashMap<>();
					for (RecommendationType type : recommendationTypeList) {
						recommendationTypeMap.put(type.getName().trim().toUpperCase(), type);
					}
					Map<String, Department> departmentMap = new HashMap<>();
					for (Department department : departmentList) {
						departmentMap.put(department.getName().trim().toUpperCase(), department);
					}
					Map<String, Component> componentMap = new HashMap<>();
					for (Component component : componentList) {
						componentMap.put(component.getName().trim().toUpperCase(), component);
					}
					List<String> stringList = Arrays.asList(expectedColumnNames);
					Boolean isBlankSheet = false;
					for (int i = 0; i < numberOfSheets; i++) {
						Sheet sheet = workbook.getSheetAt(i);
						Row topRowData = sheet.getRow(0);
						int noOfTopData = 0;
						for (Cell topCell : topRowData) {
							headerList.add(topCell.toString().trim());
							noOfTopData += 1;
						}

						if (headerList.equals(stringList)) {
							isValidFile = true;
						}
						if (isValidFile) {
							if (!(sheet.getPhysicalNumberOfRows() > 1)) {
								isBlankSheet = true;
							} else {
								for (Row row : sheet) {

									String str = "";
									if (row != null && !isRowEmpty(row)) {
										for (int j = 0; j < noOfTopData; j++) {
											Cell cel = row.getCell(j);

											String cellName = "";
											if (cel == null) {
												if (str == "") {
													str = str + "" + "/n";
												} else {
													str = str + " " + "/n";
												}
											} else {
												cellName = cel.toString();
												if (cel.toString().contains(".") && cel.toString().contains("E")) {
													String[] stringArray = cel.toString().split("E");
													List<String> wordList = Arrays.asList(stringArray);
													String firstString = wordList.get(0);
													String lastString = wordList.get(1);
													if (firstString != null && !firstString.isEmpty()
															&& lastString != null && !lastString.isEmpty()) {
														try {
															cellName = new DecimalFormat("#.##")
																	.format(Double.parseDouble(firstString) * Math
																			.pow(10, Double.parseDouble(lastString)));
														} catch (Exception e) {
														}
													}
												}
												if (cel.getCellType() == CellType.NUMERIC
														&& DateUtil.isCellDateFormatted(cel)) {
													Date javaDate = cel.getDateCellValue();
													SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
													cellName = formatter.format(javaDate);
												}
												if (str == "") {
													str = str + cellName + " " + "/n";
												} else {
													str = str + cellName + " " + "/n";
												}

											}

										}
										if (!str.isEmpty() && str != "") {
											cellValueString.add(str);
											str = "";
										} else {
											str = "";
										}
									}

								}
							}
						}

					}
					if (isValidFile) {
						if (!isBlankSheet) {
							List<String> updatedList = new ArrayList<>();
							for (int i = 1; i < cellValueString.size(); i++) {
								updatedList.add(cellValueString.get(i));
							}
							for (String str : updatedList) {
								String[] commaSeparatedArray = str.split("/n");
								List<String> wordList = Arrays.asList(commaSeparatedArray);
								JsonObject obj = new JsonObject();

								for (int i = 0; i < headerList.size(); i++) {
									obj.addProperty(headerList.get(i), wordList.get(i));
								}
								objectList.add(obj);

							}
							List<Recommendation> recommendationList = new ArrayList<>();
							for (Object obj : objectList) {
								Recommendation recommendation = new Recommendation();
								JSONObject object = new JSONObject(obj.toString());
								if (object.has("Descriptions")) {
									if (object.get("Descriptions") == null || object.get("Descriptions").equals(" ")
											|| object.get("Descriptions").equals("")) {
										recommendation.setDescriptions(null);
									} else {
										recommendation.setDescriptions(object.getString("Descriptions").trim());
									}
								}
								if (object.has("Type")) {
									if (object.get("Type") == null || object.get("Type").equals(" ")
											|| object.get("Type").equals("")) {
										recommendation.setDescriptions(null);
									} else {
										if (recommendationTypeMap
												.containsKey(object.get("Type").toString().trim().toUpperCase())) {
											recommendation.setRecommendationType(recommendationTypeMap
													.get(object.get("Type").toString().trim().toUpperCase()));
										} else {
											recommendation.setRecommendationType(null);
										}
									}
								}
								if (object.has("Priority")) {
									if (object.get("Priority") == null || object.get("Priority").equals(" ")
											|| object.get("Priority").equals("")) {
										recommendation.setPriorityId(null);
									} else {
										if (object.get("Priority").toString().trim().toUpperCase().equals("HIGH")) {
											recommendation.setPriorityId(PriorityEnum.High.getId().longValue());
										} else if (object.get("Priority").toString().trim().toUpperCase()
												.equals("MEDIUM")) {
											recommendation.setPriorityId(PriorityEnum.Medium.getId().longValue());
										} else if (object.get("Priority").toString().trim().toUpperCase()
												.equals("LOW")) {
											recommendation.setPriorityId(PriorityEnum.Low.getId().longValue());
										} else {
											recommendation.setPriorityId(null);
										}
									}
								}
								if (object.has("Recommend end date")) {
									if (object.get("Recommend end date") == null
											|| object.get("Recommend end date").equals(" ")
											|| object.get("Recommend end date").equals("")) {
										recommendation.setRecommendDate(null);
									} else {
										DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
										Date date = formatter.parse(object.get("Recommend end date").toString().trim());
										recommendation.setRecommendDate(date);
									}
								}
								if (object.has("Department")) {
									if (object.get("Department") == null || object.get("Department").equals(" ")
											|| object.get("Department").equals("")) {
										recommendation.setDepartment(null);
									} else {
										if (departmentMap.containsKey(
												object.get("Department").toString().trim().toUpperCase())) {
											recommendation.setDepartment(departmentMap
													.get(object.get("Department").toString().trim().toUpperCase()));
										} else {
											recommendation.setDepartment(null);
										}
									}
								}
								if (object.has("Component name")) {
									if (object.get("Component name") == null || object.get("Component name").equals(" ")
											|| object.get("Component name").equals("")) {
										recommendation.setComponent(null);
									} else {
										if (componentMap.containsKey(
												object.get("Component name").toString().trim().toUpperCase())) {
											recommendation.setComponent(componentMap
													.get(object.get("Component name").toString().trim().toUpperCase()));
										} else {
											recommendation.setComponent(null);
										}
									}
								}
								if (object.has("Expected Impact")) {
									if (object.get("Expected Impact") == null
											|| object.get("Expected Impact").equals(" ")
											|| object.get("Expected Impact").equals("")) {
										recommendation.setExpectedImpact(null);
									} else {
										recommendation
												.setExpectedImpact(object.get("Expected Impact").toString().trim());
									}
								}
								if (object.has("Document link")) {
									if (object.get("Document link") == null || object.get("Document link").equals(" ")
											|| object.get("Document link").equals("")) {
										recommendation.setDocumentUrl(null);
									} else {
										recommendation.setDocumentUrl(object.get("Document link").toString().trim());
									}
								}
								recommendation.setCreatedAt(new Date());
								recommendation.setUpdatedAt(new Date());
								List<Recommendation> recommendationListObj = recommendationRepository.findAll();
								recommendation.setReferenceId(generateReferenceId(recommendationListObj.size()));
								recommendation.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.OEM_recommendation.getId().longValue()));
								RecommendationTrail trailData = new RecommendationTrail();
								trailData.setCreatedAt(new Date());
								trailData.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.OEM_recommendation.getId().longValue()));
								trailData.setReferenceId(recommendation.getReferenceId());
								recommendationTrailRepository.save(trailData);
								recommendation.setCreatedBy(master.get().getUserId());
								recommendationList.add(recommendation);
							}
							Response<List<String>> response = new Response<>();
							if (recommendationList != null && recommendationList.size() > 0) {
								for (Recommendation recommendation : recommendationList) {
									if (recommendation.getDescriptions() == null
											|| recommendation.getDescriptions().equals("")) {
										response.setMessage("Descriptions  can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else if (recommendation.getRecommendationType() == null) {
										response.setMessage("Type can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else if (recommendation.getPriorityId() == null) {
										response.setMessage("Priority can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else if (recommendation.getRecommendDate() == null) {
										response.setMessage("Recommended end date can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else if (recommendation.getDepartment() == null) {
										response.setMessage("Department can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else if (recommendation.getComponent() == null) {
										response.setMessage("Component name can't be blank.");
										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
										response.setData(null);
										break;
									} else {
										response.setMessage("OK");
										response.setResponseCode(HttpStatus.OK.value());
									}
								}
							}
							if (response.getResponseCode() == HttpStatus.OK.value()) {
								recommendationRepository.saveAll(recommendationList);
								return new Response<>(HttpStatus.OK.value(), "Recommendation list added successfully.",
										null);
							} else {
								return response;
							}
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Wrong File.", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
		}
	}

	private static boolean isRowEmpty(Row row) {
		for (Cell cell : row) {
			if (cell.getCellType() != CellType.BLANK) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Response<?> pendingRecommendationRequestForAppOwner(SearchDto searchDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
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
										String message = updatedMessageList.get(0).getRejectionReason();
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
								if (rcmnd.getRecommendationStatus().getId().longValue() == StatusEnum.OEM_recommendation
										.getId().longValue()) {
									responseDto.setStatus(new RecommendationStatus(Constant.RECOMMENDATION_CREATED));
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
								pendingRecommendation.add(responseDto);
							}
						}
					}
					pendingRecommendationResponseDto.setPendingRecommendation(pendingRecommendation);
					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation of App Owner",
							pendingRecommendationResponseDto);
				}
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllPendingRecommendationsForAgmBySearchDto(searchDto);
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
							for (Recommendation rcmnd : recommendationList) {
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
						}

					}
					responseDtos.setPendingRecommendation(recommendations);

					return new Response<>(HttpStatus.OK.value(), "Pending Recommendation List AGM.", responseDtos);
				}
				if (master.get().getUserTypeId().name().equals(UserType.DGM.name())) {

					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());

					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);

							List<Recommendation> recommendationList = recommendationRepository
									.findAllPendingRecommendationsForAgmBySearchDto(searchDto);

							List<Recommendation> recommendationListHighPriority = recommendationList.stream()
									.filter(x -> x.getPriorityId() == PriorityEnum.High.getId().longValue())
									.filter(x -> x.getIsAppOwnerRejected().booleanValue() == true)
									.collect(Collectors.toList());

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
							for (Recommendation rcmnd : recommendationListHighPriority) {
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
						}

					}
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
	public Response<?> approvedRecommendationRequestForAppOwner(SearchDto searchDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();
					List<RecommendationResponseDto> approvedRecommendations = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());
					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllApprovedRecommendationsBySearchDto(searchDto);

							for (Recommendation rcmnd : recommendationList) {
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
								approvedRecommendations.add(responseDto);
							}
						}
					}
					approvedRecommendationResponseDto.setApprovedRecommendation(approvedRecommendations);
					return new Response<>(HttpStatus.OK.value(), "Approved Recommendation of App Owner",
							approvedRecommendationResponseDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {

					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();

					List<RecommendationResponseDto> approvedRecommendations = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());
					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllApprovedRecommendationsOfAgmBySearchDto(searchDto);

							if (searchDto.getStatusId() != null
									&& searchDto.getStatusId() >= StatusEnum.Planned.getId()) {

								if (searchDto.getStatusId() != null
										&& searchDto.getStatusId() == StatusEnum.Planned.getId()) {

									List<Recommendation> recommendationListFilter = recommendationList.stream()
											.filter(rcmnd -> rcmnd.getRecommendationStatus()
													.getId() == StatusEnum.Approved.getId())
											.filter(rcmnd -> {

												Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
														.findByRecommendRefId(rcmnd.getReferenceId());

												List<RecommendationTrail> trail = recommendationTrailRepository
														.findAllByReferenceId(rcmnd.getReferenceId());

												if (deploymentDetails != null && trail != null
														&& searchDto.getStatusId() == StatusEnum.Planned.getId()) {

													Date endDate = deploymentDetails.get().getDevelopementEndDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													RecommendationTrail latestTrailEntry = collect.get(0);
													Date trailDate = latestTrailEntry.getCreatedAt();

													return trailDate != null && trailDate.after(endDate);

												}
												return false;

											}).collect(Collectors.toList());

									for (Recommendation rcmnd : recommendationListFilter) {
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
										approvedRecommendations.add(responseDto);

									}

								} else {

									List<Recommendation> recommendationListFilter = recommendationList.stream()
											.filter(rcmnd -> rcmnd.getRecommendationStatus()
													.getId() >= StatusEnum.Department_implementation.getId()
													&& rcmnd.getRecommendationStatus().getId() < StatusEnum.Released
															.getId())
											.filter(rcmnd -> {
												Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
														.findByRecommendRefId(rcmnd.getReferenceId());

												List<RecommendationTrail> trail = recommendationTrailRepository
														.findAllByReferenceId(rcmnd.getReferenceId());

												if (deploymentDetails.isPresent() && trail != null
														&& searchDto.getStatusId() == StatusEnum.On_time.getId()) {
													boolean checkDate = false;
													Date trailDate = null;

													Date developementEndDate = deploymentDetails.get()
															.getDevelopementEndDate();
													Date testCompletionDate = deploymentDetails.get()
															.getTestCompletionDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													System.out.println(
															"Ref Id:" + rcmnd.getReferenceId() + "    status ID ="
																	+ rcmnd.getRecommendationStatus().getStatusName());

													for (RecommendationTrail x : collect) {
														if (x.getReferenceId().equals(rcmnd.getReferenceId())) {
															if (x.getRecommendationStatus().getId() == x
																	.getRecommendationStatus().getId()) {
																trailDate = x.getCreatedAt();

																if (x.getRecommendationStatus()
																		.getId() == StatusEnum.Department_implementation
																				.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.before(developementEndDate);
																	System.out.println(
																			"devCompletionDate =" + developementEndDate
																					+ " " + "trailDate =" + trailDate);

																} else if (x.getRecommendationStatus()
																		.getId() == StatusEnum.UAT_testing.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.before(testCompletionDate);
																	System.out.println(
																			"testCompletionDate =" + testCompletionDate
																					+ " " + "trailDate =" + trailDate);

																}

															}
														}

														System.out.println("value = " + checkDate);

														return checkDate;

													}

												} else if (deploymentDetails.isPresent() && trail != null
														&& searchDto.getStatusId() == StatusEnum.Delayed.getId()) {
													boolean checkDate = false;
													Date trailDate = null;

													Date developementEndDate = deploymentDetails.get()
															.getDevelopementEndDate();
													Date testCompletionDate = deploymentDetails.get()
															.getTestCompletionDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													System.out.println(
															"Ref Id:" + rcmnd.getReferenceId() + "    status ID ="
																	+ rcmnd.getRecommendationStatus().getStatusName());

													for (RecommendationTrail x : collect) {
														if (x.getReferenceId().equals(rcmnd.getReferenceId())) {
															if (x.getRecommendationStatus().getId() == x
																	.getRecommendationStatus().getId()) {
																trailDate = x.getCreatedAt();

																if (x.getRecommendationStatus()
																		.getId() == StatusEnum.Department_implementation
																				.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.after(developementEndDate);
																	System.out.println(
																			"devCompletionDate =" + developementEndDate
																					+ " " + "trailDate =" + trailDate);

																} else if (x.getRecommendationStatus()
																		.getId() == StatusEnum.UAT_testing.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.after(testCompletionDate);
																	System.out.println(
																			"testCompletionDate =" + testCompletionDate
																					+ " " + "trailDate =" + trailDate);

																}

															}
														}
													}

													System.out.println("value = " + checkDate);

													return checkDate;

												}
												return false;

											}).collect(Collectors.toList());

									for (Recommendation rcmnd : recommendationListFilter) {
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
										approvedRecommendations.add(responseDto);

									}

								}

							} else {

								for (Recommendation rcmnd : recommendationList) {
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
									approvedRecommendations.add(responseDto);
								}

							}

						}
					}
					approvedRecommendationResponseDto.setApprovedRecommendation(approvedRecommendations);
					return new Response<>(HttpStatus.OK.value(), "Approved Recommendation of App Owner",
							approvedRecommendationResponseDto);
				} else if (master.get().getUserTypeId().name().equals(UserType.DGM.name())) {

					RecommendationResponseDto approvedRecommendationResponseDto = new RecommendationResponseDto();

					List<RecommendationResponseDto> approvedRecommendations = new ArrayList<>();
					List<DepartmentApprover> departmentList = departmentApproverRepository
							.findAllByUserId(master.get().getUserId().getId());
					List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
							.map(e -> e.getDepartment().getId()).distinct().collect(Collectors.toList());

					if (departmentIds != null && departmentIds.size() > 0) {
						for (Long departmentId : departmentIds) {
							searchDto.setDepartmentId(departmentId);
							List<Recommendation> recommendationList = recommendationRepository
									.findAllApprovedRecommendationsOfDgmBySearchDto(searchDto);

							if (searchDto.getStatusId() != null
									&& searchDto.getStatusId() >= StatusEnum.Planned.getId()) {

								if (searchDto.getStatusId() != null
										&& searchDto.getStatusId() == StatusEnum.Planned.getId()) {

									List<Recommendation> recommendationListFilter = recommendationList.stream()
											.filter(rcmnd -> rcmnd.getRecommendationStatus()
													.getId() == StatusEnum.Approved.getId())
											.filter(rcmnd -> {

												Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
														.findByRecommendRefId(rcmnd.getReferenceId());

												List<RecommendationTrail> trail = recommendationTrailRepository
														.findAllByReferenceId(rcmnd.getReferenceId());

												if (deploymentDetails != null && trail != null
														&& searchDto.getStatusId() == StatusEnum.Planned.getId()) {

													Date endDate = deploymentDetails.get().getDevelopementEndDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													RecommendationTrail latestTrailEntry = collect.get(0);
													Date trailDate = latestTrailEntry.getCreatedAt();

													return trailDate != null && trailDate.after(endDate);

												}
												return false;

											}).collect(Collectors.toList());

									for (Recommendation rcmnd : recommendationListFilter) {
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
										approvedRecommendations.add(responseDto);

									}

								} else {

									List<Recommendation> recommendationListFilter = recommendationList.stream()
											.filter(rcmnd -> rcmnd.getRecommendationStatus()
													.getId() >= StatusEnum.Department_implementation.getId()
													&& rcmnd.getRecommendationStatus().getId() < StatusEnum.Released
															.getId())
											.filter(rcmnd -> {
												Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
														.findByRecommendRefId(rcmnd.getReferenceId());

												List<RecommendationTrail> trail = recommendationTrailRepository
														.findAllByReferenceId(rcmnd.getReferenceId());

												if (deploymentDetails.isPresent() && trail != null
														&& searchDto.getStatusId() == StatusEnum.On_time.getId()) {
													boolean checkDate = false;
													Date trailDate = null;

													Date developementEndDate = deploymentDetails.get()
															.getDevelopementEndDate();
													Date testCompletionDate = deploymentDetails.get()
															.getTestCompletionDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													System.out.println(
															"Ref Id:" + rcmnd.getReferenceId() + "    status ID ="
																	+ rcmnd.getRecommendationStatus().getStatusName());

													for (RecommendationTrail x : collect) {
														if (x.getReferenceId().equals(rcmnd.getReferenceId())) {
															if (x.getRecommendationStatus().getId() == x
																	.getRecommendationStatus().getId()) {
																trailDate = x.getCreatedAt();

																if (x.getRecommendationStatus()
																		.getId() == StatusEnum.Department_implementation
																				.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.before(developementEndDate);
																	System.out.println(
																			"devCompletionDate =" + developementEndDate
																					+ " " + "trailDate =" + trailDate);

																} else if (x.getRecommendationStatus()
																		.getId() == StatusEnum.UAT_testing.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.before(testCompletionDate);
																	System.out.println(
																			"testCompletionDate =" + testCompletionDate
																					+ " " + "trailDate =" + trailDate);

																}

															}
														}

														System.out.println("value = " + checkDate);

														return checkDate;

													}

												} else if (deploymentDetails.isPresent() && trail != null
														&& searchDto.getStatusId() == StatusEnum.Delayed.getId()) {
													boolean checkDate = false;
													Date trailDate = null;

													Date developementEndDate = deploymentDetails.get()
															.getDevelopementEndDate();
													Date testCompletionDate = deploymentDetails.get()
															.getTestCompletionDate();

													List<RecommendationTrail> collect = trail.stream().filter(
															rt -> rt.getReferenceId().equals(rcmnd.getReferenceId()))
															.sorted(Comparator
																	.comparing(RecommendationTrail::getCreatedAt)
																	.reversed())
															.collect(Collectors.toList());

													System.out.println(
															"Ref Id:" + rcmnd.getReferenceId() + "    status ID ="
																	+ rcmnd.getRecommendationStatus().getStatusName());

													for (RecommendationTrail x : collect) {
														if (x.getReferenceId().equals(rcmnd.getReferenceId())) {
															if (x.getRecommendationStatus().getId() == x
																	.getRecommendationStatus().getId()) {
																trailDate = x.getCreatedAt();

																if (x.getRecommendationStatus()
																		.getId() == StatusEnum.Department_implementation
																				.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.after(developementEndDate);
																	System.out.println(
																			"devCompletionDate =" + developementEndDate
																					+ " " + "trailDate =" + trailDate);

																} else if (x.getRecommendationStatus()
																		.getId() == StatusEnum.UAT_testing.getId()) {

																	checkDate = trailDate != null
																			&& trailDate.after(testCompletionDate);
																	System.out.println(
																			"testCompletionDate =" + testCompletionDate
																					+ " " + "trailDate =" + trailDate);

																}

															}
														}
													}

													System.out.println("value = " + checkDate);

													return checkDate;

												}
												return false;

											}).collect(Collectors.toList());

									for (Recommendation rcmnd : recommendationListFilter) {
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
										approvedRecommendations.add(responseDto);

									}

								}

							} else {

								for (Recommendation rcmnd : recommendationList) {
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
									approvedRecommendations.add(responseDto);
								}

							}

						}
					}
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

	@Override
	public Response<?> updateRecommendationStatus(RecommendationDetailsRequestDto recommendationRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					Optional<Recommendation> recommendationObj = recommendationRepository
							.findByReferenceId(recommendationRequestDto.getRecommendRefId());
					if (recommendationObj != null && recommendationObj.isPresent()) {
						String responseText = "Recommendation status updated successfully";
						if (recommendationObj.get().getRecommendationStatus() != null && recommendationObj.get()
								.getRecommendationStatus().getId() == StatusEnum.Rejected.getId()) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation already rejected.",
									null);
						} else if (recommendationObj.get().getRecommendationStatus() != null && recommendationObj.get()
								.getRecommendationStatus().getId()
								.longValue() > recommendationRequestDto.getRecommendationStatus().getId().longValue()) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid status.",
									null);
						} else if (recommendationObj.get().getRecommendationStatus() != null
								&& recommendationObj.get().getRecommendationStatus().getId()
										.longValue() == StatusEnum.Approved.getId().longValue()
								&& recommendationRequestDto.getRecommendationStatus().getId()
										.longValue() != StatusEnum.Department_implementation.getId().longValue()) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid status.",
									null);
						} else if (recommendationObj.get().getRecommendationStatus() != null
								&& recommendationObj.get().getRecommendationStatus().getId()
										.longValue() != StatusEnum.Approved.getId().longValue()
								&& recommendationObj.get().getRecommendationStatus().getId().longValue()
										+ 1 != recommendationRequestDto.getRecommendationStatus().getId().longValue()) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid status.",
									null);
						} else {
							recommendationObj.get()
									.setRecommendationStatus(recommendationRequestDto.getRecommendationStatus());
							recommendationObj.get().setUpdatedAt(new Date());
							RecommendationTrail trailData = new RecommendationTrail();
							trailData.setCreatedAt(new Date());
							trailData.setRecommendationStatus(recommendationRequestDto.getRecommendationStatus());
							trailData.setReferenceId(recommendationRequestDto.getRecommendRefId());
							recommendationTrailRepository.save(trailData);
							Recommendation updatedRecommendation = recommendationRepository
									.save(recommendationObj.get());
							Department rcmdDepartment = updatedRecommendation.getDepartment();
							Optional<DepartmentApprover> approver = departmentApproverRepository
									.findAllByDepartmentId(rcmdDepartment.getId());
							if (updatedRecommendation.getRecommendationStatus().getId() == StatusEnum.Released
									.getId()) {
								if (approver != null && approver.isPresent()) {
									if (approver.get().getAgm() != null
											&& !approver.get().getAgm().getEmail().isBlank()) {
										responseText += "Email will be sent to both AGM("
												+ approver.get().getAgm().getEmail() + ") and OEM("
												+ updatedRecommendation.getCreatedBy().getEmail() + ")";
									}
								}
								notificationService.save(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_RELEASED, null, null);

								emailTemplateService.sendMailRecommendation(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_RELEASED);
							} else {
								if (approver != null && approver.isPresent()) {
									if (approver.get().getAgm() != null
											&& !approver.get().getAgm().getEmail().isBlank()) {
										responseText += ". Email will be sent to AGM("
												+ approver.get().getAgm().getEmail() + ")" + " " + "also send to OEM("
												+ recommendationObj.get().getCreatedBy().getEmail() + ")";
									}
								}
								notificationService.save(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_STATUS_CHANGED, null, null);

								emailTemplateService.sendMailRecommendation(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_STATUS_CHANGED);
							}
							return new Response<>(HttpStatus.OK.value(), responseText, null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found.", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access to update status.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

	@Override
	public Response<?> getAllStatusListToBeImplement() {
		List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
		List<RecommendationStatus> updatedStatusList = statusList.stream()
				.filter(e -> e.getStatusName().equals(StatusEnum.Department_implementation.getName())
						|| e.getStatusName().equals(StatusEnum.UAT_testing.getName())
						|| e.getStatusName().equals(StatusEnum.Released.getName()))
				.collect(Collectors.toList());

		return new Response<>(HttpStatus.OK.value(), "Status List.", updatedStatusList);
	}

	@Override
	public Response<?> updateRecommendation(RecommendationAddRequestDto recommendationAddRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
					if (recommendationAddRequestDto.getReferenceId() != null
							&& !recommendationAddRequestDto.getReferenceId().toString().isBlank()
							&& !recommendationAddRequestDto.getReferenceId().toString().isEmpty()) {

						Optional<Recommendation> rcmd = recommendationRepository
								.findByReferenceId(recommendationAddRequestDto.getReferenceId());

						if (rcmd.get() != null && rcmd.isPresent()) {
							if (rcmd.get().getRecommendationStatus().getId()
									.longValue() == StatusEnum.OEM_recommendation.getId().longValue()) {

								String fileUrl = null;
								if (recommendationAddRequestDto.getFile() != null
										&& recommendationAddRequestDto.getFile().getSize() > 1048576) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"File size can't be above 1MB.", null);
								} else {
									if (recommendationAddRequestDto.getFile() != null) {
										fileUrl = fileSystemStorageService
												.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
									}
									Recommendation recommendation = rcmd.get();
									recommendation.setFileUrl(fileUrl);
									recommendation.setDocumentUrl(recommendationAddRequestDto.getUrlLink());
									recommendation.setDescriptions(recommendationAddRequestDto.getDescription());
									recommendation.setRecommendDate(recommendationAddRequestDto.getRecommendDate());
									recommendation
											.setComponent(new Component(recommendationAddRequestDto.getComponentId()));
									recommendation.setPriorityId(recommendationAddRequestDto.getPriorityId());
									recommendation.setRecommendationType(
											new RecommendationType(recommendationAddRequestDto.getTypeId()));
									recommendation.setUpdatedAt(new Date());
									recommendationRepository.save(recommendation);
									return new Response<>(HttpStatus.OK.value(), "Recommendation updated successfully.",
											null);
								}
							} else {
								return new Response<>(HttpStatus.BAD_REQUEST.value(),
										"Recommendation cannot be updated now.", null);
							}
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation not found", null);
						}

					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Reference ID is required.", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

}
