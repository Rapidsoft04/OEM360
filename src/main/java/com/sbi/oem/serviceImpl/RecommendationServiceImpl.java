package com.sbi.oem.serviceImpl;

import java.text.DecimalFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
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
import com.sbi.oem.repository.CredentialMasterRepository;
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
	private CredentialMasterRepository credentialMasterRepository;

	@Autowired
	private NotificationService notificationService;
	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Autowired
	private RecommendationMessagesRepository recommendationMessagesRepository;

	@SuppressWarnings("rawtypes")
	@Lookup
	public Response getResponse() {
		return null;
	}

	@Override
	public Response<?> getRecommendationPageData(Long companyId) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
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

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}

	}

	@Override
	public Response<?> addRecommendation(RecommendationAddRequestDto recommendationAddRequestDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
				Recommendation recommendation = new Recommendation();
				if (recommendationAddRequestDto.getFile() != null
						&& recommendationAddRequestDto.getFile().getSize() > 1048576) {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "File size can't be above 1MB.", null);
				} else {
					if (recommendationAddRequestDto.getFile() != null) {
						String fileUrl = fileSystemStorageService
								.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
						if (fileUrl != null && !fileUrl.isEmpty()) {
							recommendation.setFileUrl(fileUrl);
						}
					}
					recommendation.setDocumentUrl(recommendationAddRequestDto.getUrlLink());
					recommendation.setDescriptions(recommendationAddRequestDto.getDescription());
					recommendation.setCreatedAt(new Date());
					recommendation.setRecommendDate(recommendationAddRequestDto.getRecommendDate());
					recommendation.setCreatedBy(new User(recommendationAddRequestDto.getCreatedBy()));
					recommendation.setDepartment(new Department(recommendationAddRequestDto.getDepartmentId()));
					recommendation.setComponent(new Component(recommendationAddRequestDto.getComponentId()));
					recommendation.setPriorityId(recommendationAddRequestDto.getPriorityId());
					recommendation
							.setRecommendationType(new RecommendationType(recommendationAddRequestDto.getTypeId()));
					recommendation.setRecommendationStatus(new RecommendationStatus(1L));
					recommendation.setExpectedImpact(recommendationAddRequestDto.getExpectedImpact());
					List<Recommendation> recommendList = recommendationRepository.findAll();
					String refId = generateReferenceId(recommendList.size());
					recommendation.setReferenceId(refId);
					recommendation.setUpdatedAt(new Date());
					Recommendation savedRecommendation = recommendationRepository.save(recommendation);

					RecommendationTrail trailData = new RecommendationTrail();
					trailData.setCreatedAt(new Date());
					trailData.setRecommendationStatus(new RecommendationStatus(1L));
					trailData.setReferenceId(refId);
					recommendationTrailRepository.save(trailData);
					notificationService.save(savedRecommendation, RecommendationStatusEnum.CREATED);
					emailTemplateService.sendMailRecommendation(recommendation, RecommendationStatusEnum.CREATED);

					return new Response<>(HttpStatus.CREATED.value(), "Recommendation created successfully.", null);
				}

			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
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
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
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
						if (rcmnd.getPriorityId() != null) {
							String priority = "";
							if (rcmnd.getPriorityId().longValue() == 1) {
								priority = PriorityEnum.High.getName();
							} else if (rcmnd.getPriorityId().longValue() == 2) {
								priority = PriorityEnum.Medium.getName();
							} else {
								priority = PriorityEnum.Low.getName();
							}
							responseDto.setPriority(priority);
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
						// Sort the map by key
						Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
										LinkedHashMap<Long, RecommendationTrail>::new));

						List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
						if (sortedMap.containsKey(4L)) {
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
//						responseDtos.add(responseDto);

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
					if (rcmnd.getPriorityId() != null) {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
						} else {
							priority = PriorityEnum.Low.getName();
						}
						responseDto.setPriority(priority);
					}
					if (rcmnd.getIsAppOwnerApproved() != null && rcmnd.getIsAppOwnerApproved().booleanValue() == true) {
						responseDto.setStatus(
								new RecommendationStatus(RecommendationStatusEnum.RECOMMENDATION_APPROVED.name()));
					}
					if (rcmnd.getIsAppOwnerApproved() != null
							&& rcmnd.getIsAppOwnerApproved().booleanValue() == false) {
						responseDto.setStatus(
								new RecommendationStatus(RecommendationStatusEnum.RECCOMENDATION_REJECTED.name()));
					}
					Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
							.findAllByDepartmentId(rcmnd.getDepartment().getId());
					responseDto.setApprover(departmentApprover.get().getAgm());
					responseDto.setAppOwner(departmentApprover.get().getApplicationOwner());
//					List<RecommendationTrail> trailList = recommendationTrailRepository
//							.findAllByReferenceId(responseDto.getReferenceId());
//					responseDto.setTrailData(trailList);
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
//					Map<Long, RecommendationTrail> recommendationTrailMap = new HashMap<>();
//					for (RecommendationTrail trail : trailList) {
//						recommendationTrailMap.put(trail.getRecommendationStatus().getId(), trail);
//					}
//					// Sort the map by key
//					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
//							.sorted(Map.Entry.comparingByKey())
//							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
//									LinkedHashMap<Long, RecommendationTrail>::new));
//
//					List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
//					if (sortedMap.containsKey(4L)) {
//						for (Long key : sortedMap.keySet()) {
//							RecommendationTrail trail = sortedMap.get(key);
//							RecommendationTrailResponseDto response = trail.convertToDto();
//							response.setIsStatusDone(true);
//							trailResponseList.add(response);
//						}
//					} else {
//						for (RecommendationStatus status : statusList) {
//							if (sortedMap.containsKey(status.getId().longValue())) {
//								RecommendationTrail trail = sortedMap.get(status.getId().longValue());
//								RecommendationTrailResponseDto response = trail.convertToDto();
//								response.setIsStatusDone(true);
//								trailResponseList.add(response);
//							} else {
//								RecommendationTrail trail = new RecommendationTrail();
//								trail.setRecommendationStatus(status);
//								RecommendationTrailResponseDto response = trail.convertToDto();
//								response.setIsStatusDone(false);
//								trailResponseList.add(response);
//							}
//						}
//					}
//					responseDto.setTrailResponse(trailResponseList);
					recommendations.add(responseDto);
//					responseDtos.add(responseDto);
				}
				responseDtos.setRecommendations(recommendations);
				return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
			} else if (master.get().getUserTypeId().name().equals(UserType.SENIOR_MANAGEMENT.name())) {
				RecommendationResponseDto responseDtos = new RecommendationResponseDto();
				List<RecommendationResponseDto> recommendations = new ArrayList<>();
				List<Recommendation> recommendationList = recommendationRepository
						.findAllByUserId(master.get().getUserId().getId());

				for (Recommendation rcmnd : recommendationList) {
					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					if (rcmnd.getPriorityId() != null) {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
						} else {
							priority = PriorityEnum.Low.getName();
						}
						responseDto.setPriority(priority);
						Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(rcmnd.getReferenceId());
						if (deploymentDetails != null && deploymentDetails.isPresent()) {
							responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
						} else {
							responseDto.setRecommendationDeploymentDetails(null);
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
					// Sort the map by key
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

					List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
					if (sortedMap.containsKey(4L)) {
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
//					responseDtos.add(responseDto);

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
					if (rcmnd.getPriorityId() != null) {
						String priority = "";
						if (rcmnd.getPriorityId().longValue() == 1) {
							priority = PriorityEnum.High.getName();
						} else if (rcmnd.getPriorityId().longValue() == 2) {
							priority = PriorityEnum.Medium.getName();
						} else {
							priority = PriorityEnum.Low.getName();
						}
						responseDto.setPriority(priority);
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
					// Sort the map by key
					Map<Long, RecommendationTrail> sortedMap = recommendationTrailMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
									LinkedHashMap<Long, RecommendationTrail>::new));

					List<RecommendationTrailResponseDto> trailResponseList = new ArrayList<>();
					if (sortedMap.containsKey(4L)) {
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
//					List<RecommendationMessages> messageList = recommendationMessagesRepository
//							.findAllByReferenceId(responseDto.getReferenceId());
//					responseDto.setMessageList(messageList);
					Optional<RecommendationDeplyomentDetails> deploymentDetails = deplyomentDetailsRepository
							.findByRecommendRefId(rcmnd.getReferenceId());
					if (deploymentDetails != null && deploymentDetails.isPresent()) {
						responseDto.setRecommendationDeploymentDetails(deploymentDetails.get());
					} else {
						responseDto.setRecommendationDeploymentDetails(null);
					}
					recommendations.add(responseDto);
//					responseDtos.add(responseDto);

				}
				responseDtos.setRecommendations(recommendations);
				return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);

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
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
				Optional<RecommendationDeplyomentDetails> recommendDeployDetails = deplyomentDetailsRepository
						.findByRecommendRefId(recommendationDetailsRequestDto.getRecommendRefId());
				if (recommendDeployDetails != null && recommendDeployDetails.isPresent()) {
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"Deployment details already exist for the provided recommendation.", null);
				} else {
					RecommendationDeplyomentDetails details = recommendationDetailsRequestDto.convertToEntity();
					details.setCreatedAt(new Date());
					deplyomentDetailsRepository.save(details);
					Optional<Recommendation> recommendation = recommendationRepository
							.findByReferenceId(details.getRecommendRefId());
					recommendation.get().setRecommendationStatus(new RecommendationStatus(2L));
					recommendation.get().setIsAppOwnerApproved(true);
					recommendation.get().setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
					recommendation.get().setImpactedDepartment(recommendationDetailsRequestDto.getImpactedDepartment());
					recommendation.get().setUpdatedAt(new Date());
					recommendationRepository.save(recommendation.get());
					RecommendationTrail trail = new RecommendationTrail();
					trail.setCreatedAt(new Date());
					trail.setRecommendationStatus(new RecommendationStatus(2L));
					trail.setReferenceId(details.getRecommendRefId());
					recommendationTrailRepository.save(trail);

					notificationService.save(recommendation.get(), RecommendationStatusEnum.APPROVED_BY_APPOWNER);
					emailTemplateService.sendMailRecommendationDeplyomentDetails(details,
							RecommendationStatusEnum.APPROVED_BY_APPOWNER);
					return new Response<>(HttpStatus.CREATED.value(), "Deployment details added successfully.", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(),
						"You have no access to provide deployment details.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);

		}

	}

	@Override
	public Response<?> rejectRecommendationByAppOwner(RecommendationRejectionRequestDto recommendation) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {

				Optional<Recommendation> recommendObj = recommendationRepository
						.findByReferenceId(recommendation.getReferenceId());
				RecommendationMessages messages = recommendation.convertToEntity();
				messages.setCreatedAt(new Date());
				recommendationMessagesRepository.save(messages);
				recommendObj.get().setIsAppOwnerRejected(true);
				recommendObj.get().setRecommendationStatus(new RecommendationStatus(2L));
				recommendationRepository.save(recommendObj.get());
				RecommendationTrail recommendTrail = new RecommendationTrail();
				recommendTrail.setCreatedAt(new Date());
				recommendTrail.setRecommendationStatus(new RecommendationStatus(2L));
				recommendTrail.setReferenceId(recommendation.getReferenceId());
				recommendationTrailRepository.save(recommendTrail);
				Optional<RecommendationDeplyomentDetails> recommendDeploymentDetails = deplyomentDetailsRepository
						.findByRecommendRefId(recommendation.getReferenceId());
				if (recommendDeploymentDetails != null && recommendDeploymentDetails.isPresent()) {
					deplyomentDetailsRepository.delete(recommendDeploymentDetails.get());
				}
				notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_APPOWNER);
				emailTemplateService.sendMailRecommendationMessages(messages,
						RecommendationStatusEnum.REJECTED_BY_APPOWNER);
				return new Response<>(HttpStatus.OK.value(), "Recommendation rejected successfully.", null);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access to reject.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}

	}

	@Override
	public Response<?> revertApprovalRequestToAppOwnerForApproval(
			RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
				RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
				messages.setCreatedAt(new Date());
				recommendationMessagesRepository.save(messages);
				notificationService.getRecommendationByReferenceId(messages.getReferenceId(),
						RecommendationStatusEnum.REVERTED_BY_AGM);
				emailTemplateService.sendMailRecommendationMessages(messages, RecommendationStatusEnum.REVERTED_BY_AGM);
				Optional<Recommendation> recommendationObj = recommendationRepository
						.findByReferenceId(recommendationRejectionRequestDto.getReferenceId());
				recommendationObj.get().setUpdatedAt(new Date());
				recommendationRepository.save(recommendationObj.get());
				return new Response<>(HttpStatus.OK.value(), "Approval request reverted successfully.", null);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(),
						"You have no access revert recommendation request.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}

	}

	@Override
	public Response<?> rejectRecommendationByAgm(RecommendationRejectionRequestDto recommendationRejectionRequestDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
				Optional<Recommendation> recommendObj = recommendationRepository
						.findByReferenceId(recommendationRejectionRequestDto.getReferenceId());
				if (recommendObj != null && recommendObj.isPresent()) {
					if (recommendObj.get().getIsAppOwnerApproved() != null
							&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
						RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
						messages.setCreatedAt(new Date());
						recommendationMessagesRepository.save(messages);
						notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_AGM);
						emailTemplateService.sendMailRecommendationMessages(messages,
								RecommendationStatusEnum.REJECTED_BY_AGM);
						recommendObj.get().setUpdatedAt(new Date());
						recommendationRepository.save(recommendObj.get());
						return new Response<>(HttpStatus.OK.value(), "Recommendation reject request sent successfully.",
								null);
					} else {
						recommendObj.get().setIsAgmApproved(false);
						recommendationRepository.save(recommendObj.get());
						RecommendationTrail trailData = new RecommendationTrail();
						trailData.setCreatedAt(new Date());
						trailData.setRecommendationStatus(new RecommendationStatus(4L));
						trailData.setReferenceId(recommendationRejectionRequestDto.getReferenceId());
						recommendationTrailRepository.save(trailData);
						RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
						messages.setCreatedAt(new Date());
						recommendationMessagesRepository.save(messages);
						notificationService.save(recommendObj.get(), RecommendationStatusEnum.RECCOMENDATION_REJECTED);
						emailTemplateService.sendMailRecommendationMessages(messages,
								RecommendationStatusEnum.RECCOMENDATION_REJECTED);
						return new Response<>(HttpStatus.OK.value(), "Recommendation rejected successfully.", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(),
						"You have no access to reject recommendation request.", null);
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
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
				Optional<Recommendation> recommendObj = recommendationRepository
						.findByReferenceId(recommendationRejectionRequestDto.getReferenceId());
				if (recommendObj.get().getIsAppOwnerApproved() != null
						&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
					recommendObj.get().setIsAgmApproved(true);
					recommendObj.get().setRecommendationStatus(new RecommendationStatus(3L));
					recommendationRepository.save(recommendObj.get());
					RecommendationTrail trailData = new RecommendationTrail();
					trailData.setCreatedAt(new Date());
					trailData.setRecommendationStatus(new RecommendationStatus(3L));
					trailData.setReferenceId(recommendationRejectionRequestDto.getReferenceId());
					recommendationTrailRepository.save(trailData);
					if (recommendationRejectionRequestDto.getAddtionalInformation() != null
							|| !recommendationRejectionRequestDto.getAddtionalInformation().equals("")) {
						RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
						messages.setCreatedAt(new Date());
						recommendationMessagesRepository.save(messages);
					}

					notificationService.save(recommendObj.get(), RecommendationStatusEnum.APPROVED_BY_AGM);
					emailTemplateService.sendMailRecommendation(recommendObj.get(),
							RecommendationStatusEnum.APPROVED_BY_AGM);
					return new Response<>(HttpStatus.OK.value(), "Recommendation request accepted.", null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"Recommendation is not yet approved by app owner.", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> updateDeploymentDetails(RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
				Optional<RecommendationDeplyomentDetails> recommendDeployDetails = deplyomentDetailsRepository
						.findByRecommendRefId(recommendationDetailsRequestDto.getRecommendRefId());
				if (recommendDeployDetails != null && recommendDeployDetails.isPresent()) {
					RecommendationDeplyomentDetails details = recommendationDetailsRequestDto.convertToEntity();
					details.setId(recommendDeployDetails.get().getId());
					RecommendationDeplyomentDetails savedDeploymentDetails = deplyomentDetailsRepository.save(details);
					Optional<Recommendation> recommendation = recommendationRepository
							.findByReferenceId(details.getRecommendRefId());
					recommendation.get().setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
					recommendationRepository.save(recommendation.get());
					if (recommendationDetailsRequestDto.getDescription() != null
							|| !recommendationDetailsRequestDto.getDescription().equals("")) {
						RecommendationMessages messages = new RecommendationMessages();
						messages.setAdditionalMessage(recommendationDetailsRequestDto.getDescription());
						messages.setCreatedBy(recommendationDetailsRequestDto.getCreatedBy());
						messages.setCreatedAt(new Date());
						messages.setReferenceId(recommendationDetailsRequestDto.getRecommendRefId());
						recommendationMessagesRepository.save(messages);
					}
					notificationService.save(recommendation.get(), RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS);
					emailTemplateService.sendMailRecommendationDeplyomentDetails(savedDeploymentDetails,
							RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS);

					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Deployment details updated successfully.",
							null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "No data found.", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> pendingRecommendationRequestForAppOwner(SearchDto searchDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
				List<RecommendationResponseDto> pendingRecommendation = new ArrayList<>();
				List<DepartmentApprover> departmentList = departmentApproverRepository
						.findAllByUserId(master.get().getUserId().getId());

				List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
						.map(e -> e.getDepartment().getId()).collect(Collectors.toList());
				System.out.println(departmentIds.size());
				if (departmentIds != null && departmentIds.size() > 0) {
					for (Long departmentId : departmentIds) {
						searchDto.setDepartmentId(departmentId);
						// Get Pending recommendations by filtering
						List<Recommendation> recommendationList = recommendationRepository
								.findAllPendingRecommendationsBySearchDto(searchDto);
						System.out.println(recommendationList.size());
						// Converting each response to dto and setting trial response and status to false
						for (Recommendation rcmnd : recommendationList) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							responseDto.setTrailResponse(null);
							responseDto.setStatus(null);
							pendingRecommendation.add(responseDto);
						}
					}
				}
				return new Response<>(HttpStatus.OK.value(), "Pending Recommendation of App Owner",
						pendingRecommendation);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}
	
	@Override
	public Response<?> approvedRecommendationRequestForAppOwner(SearchDto searchDto) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
				List<RecommendationResponseDto> pendingRecommendation = new ArrayList<>();
				List<DepartmentApprover> departmentList = departmentApproverRepository
						.findAllByUserId(master.get().getUserId().getId());

				List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
						.map(e -> e.getDepartment().getId()).collect(Collectors.toList());
				System.out.println(departmentIds.size());
				if (departmentIds != null && departmentIds.size() > 0) {
					for (Long departmentId : departmentIds) {
						searchDto.setDepartmentId(departmentId);
						// Get Approved recommendations by filtering
						List<Recommendation> recommendationList = recommendationRepository
								.findAllApprovedRecommendationsBySearchDto(searchDto);
						System.out.println(recommendationList.size());
						// Converting each response to dto
						for (Recommendation rcmnd : recommendationList) {
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							pendingRecommendation.add(responseDto);
						}
					}
				}
				return new Response<>(HttpStatus.OK.value(), "Approved Recommendation of App Owner",
						pendingRecommendation);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}
	
	@Override
	public Response<?> viewRecommendationDetailsForOemAndAgmAndGm(SearchDto searchDto) {
		
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<CredentialMaster> master = credentialMasterRepository.findByEmail(auth.getName());
			if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
				
				RecommendationResponseDto responseDtos = new RecommendationResponseDto();
				
				List<RecommendationResponseDto> recommendations = new ArrayList<>();
				
				Long OemId = master.get().getUserId().getId();
			
				List<Recommendation> RecomendationListOem = recommendationRepository.findAllByUserIdFilter(OemId, searchDto);
				
				
				for (Recommendation rcmnd : RecomendationListOem) {
					System.out.println(rcmnd.getDepartment().getId());
					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					responseDto.setTrailResponse(null);
					responseDto.setStatus(null);
					recommendations.add(responseDto);
				 }
				
				return new Response<>(HttpStatus.OK.value(), "Recomendation List OEM_SI", recommendations);
	
			} else if(master.get().getUserTypeId().name().equals(UserType.AGM.name())){
				
				RecommendationResponseDto responseDtos = new RecommendationResponseDto();
				
				List<RecommendationResponseDto> recommendations = new ArrayList<>();
				
				List<DepartmentApprover> departmentList = departmentApproverRepository
						.findAllByUserId(master.get().getUserId().getId());
				
				List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
						.map(e -> e.getDepartment().getId()).collect(Collectors.toList());

				if (departmentIds != null && departmentIds.size() > 0) {
					for (Long departmentId : departmentIds) {
						searchDto.setDepartmentId(departmentId);
						List<Recommendation> recommendationListAgm = recommendationRepository
								.findAllByUserIdFilter(null, searchDto);
						
						System.out.println(recommendationListAgm.size());
						
						for (Recommendation rcmnd : recommendationListAgm) {
							
							RecommendationResponseDto responseDto = rcmnd.convertToDto();
							responseDto.setTrailResponse(null);
							responseDto.setStatus(null);
							recommendations.add(responseDto);
						}
					}
				}


				return new Response<>(HttpStatus.OK.value(), "Recommendation List AGM.", recommendations);
				
				
			}else if (master.get().getUserTypeId().name().equals(UserType.SENIOR_MANAGEMENT.name())) {
				
				RecommendationResponseDto responseDtos = new RecommendationResponseDto();
				
				List<RecommendationResponseDto> recommendations = new ArrayList<>();
				
				List<Recommendation> RecomendationListGm = recommendationRepository
						.findAll();
		
				for (Recommendation rcmnd : RecomendationListGm) {
					System.out.println(rcmnd.getDepartment().getId());
					RecommendationResponseDto responseDto = rcmnd.convertToDto();
					responseDto.setTrailResponse(null);
					responseDto.setStatus(null);
					recommendations.add(responseDto);
				 }
				
				return new Response<>(HttpStatus.OK.value(), "Recommendation List GM.", recommendations);
				
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
			String duplicateProductName = "";
			int numberOfSheets = workbook.getNumberOfSheets();
			List<String> headerList = new ArrayList<>();
			List<String> cellValueString = new ArrayList<>();
			List<JsonObject> objectList = new ArrayList<>();
			Boolean isValidFile = false;
			String[] expectedColumnNames = { "Descriptions", "Type", "Priority", "Recommend end date", "Department",
					"Component name", "Expected Impact", "Document link" };
			boolean isMismatchedData = false;
			String misMatchedColumnName = "";
			String misMatchedProductName = "";
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
											if (firstString != null && !firstString.isEmpty() && lastString != null
													&& !lastString.isEmpty()) {
												try {
													cellName = new DecimalFormat("#.##")
															.format(Double.parseDouble(firstString)
																	* Math.pow(10, Double.parseDouble(lastString)));
												} catch (Exception e) {
													// TODO: handle exception
												}
											}
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
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	// Function to check if a row is empty (contains only blank cells)
	private static boolean isRowEmpty(Row row) {
		for (Cell cell : row) {
			if (cell.getCellType() != CellType.BLANK) {
				return false; // Row is not empty
			}
		}
		return true; // Row is empty
	}
}
