package com.sbi.oem.serviceImpl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.gson.JsonObject;
import com.sbi.oem.backup.data.DataRetrievalService;
import com.sbi.oem.dto.DepartmentListDto;
import com.sbi.oem.dto.PriorityResponseDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationPageDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.Response;
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
import com.sbi.oem.service.DepartmentService;
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
	private NotificationService notificationService;
	@Autowired
	private RecommendationDeplyomentDetailsRepository deplyomentDetailsRepository;

	@Autowired
	private RecommendationMessagesRepository recommendationMessagesRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private CredentialMasterRepository credentialMasterRepository;

	@Autowired
	private DataRetrievalService dataRetrievalService;

	@Autowired
	private DepartmentService departmentService;

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
	public Response<?> getRecommendationPageData(Long companyId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				RecommendationPageDto recommendationPageDto = new RecommendationPageDto();

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
				List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
				StatusEnum[] statusEnums = StatusEnum.values();
				for (StatusEnum enums : statusEnums) {
					if (enums.getName().equals(StatusEnum.No_Action.getName())) {
						RecommendationStatus status = new RecommendationStatus();
						status.setId(enums.getId());
						status.setStatusName(enums.getName());
						statusList.add(status);
					}
					if (enums.getName().equals(StatusEnum.Delayed.getName())) {
						RecommendationStatus status = new RecommendationStatus();
						status.setId(enums.getId());
						status.setStatusName(enums.getName());
						statusList.add(status);
					}
					if (enums.getName().equals(StatusEnum.Released_With_Delay.getName())) {
						RecommendationStatus status = new RecommendationStatus();
						status.setId(enums.getId());
						status.setStatusName(enums.getName());
						statusList.add(status);
					}
				}
				recommendationPageDto.setStatusList(statusList);
				return new Response<>(HttpStatus.OK.value(), "Recommendation page data.", recommendationPageDto);

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
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())
						|| (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name()))) {

					if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())
							&& master.get().getUserId().getDepartment() != null && !recommendationAddRequestDto
									.getDepartmentIds().contains(master.get().getUserId().getDepartment().getId())) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Your department must be included in the list of departments while adding recommendations.",
								null);
					}

					if (checkIfRecommendationAlreadyExist(recommendationAddRequestDto).equals(Boolean.valueOf(true))) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation already reported", null);
					}

					String fileUrl = null;
					if (recommendationAddRequestDto.getFile() != null) {

//						if (recommendationAddRequestDto.getFile() != null
//								&& recommendationAddRequestDto.getFile().getSize() > 1048576) {
//							return new Response<>(HttpStatus.BAD_REQUEST.value(), "File size can't be above 1MB.",
//									null);
//						}

						if (recommendationAddRequestDto.getFile() != null
								&& recommendationAddRequestDto.getFile().getSize() > 1048576) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "File size can't be above 1MB.",
									null);
						}

						fileUrl = fileSystemStorageService.storeFile(recommendationAddRequestDto.getFile(),
								new Date().getTime());
						fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
								.path(fileUrl).toUriString();
						if (fileUrl.contains("/" + applicationName + "/")) {

							fileUrl = fileUrl.replace("/" + applicationName + "/", "/backend/");
							fileUrl = fileUrl.replace("http", "https");
						}
//						if(fileExtension.equals("docx")) {
//							byte[] pdfBytes = FileValidation.convertDocxToPdf(recommendationAddRequestDto.getFile());
//							MultipartFile multipartFile = new ByteArrayMultipartFile(pdfBytes, "output.pdf", "output.pdf", "application/pdf");
//							fileUrl = fileSystemStorageService.getUserExpenseFileUrl(multipartFile);
//						}
//						if (recommendationAddRequestDto.getFile().getSize() > 1048576) {
//							FileOutputStream fos = new FileOutputStream("compressed.zip");
//							ZipOutputStream zipOut = new ZipOutputStream(fos);
//
//							InputStream fis = recommendationAddRequestDto.getFile().getInputStream();
//
//							File fileToZip = new File(recommendationAddRequestDto.getFile().getOriginalFilename());
//							ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
//							zipOut.putNextEntry(zipEntry);
//
//							byte[] bytes = new byte[1024];
//							int length;
//							while ((length = fis.read(bytes)) >= 0) {
//								zipOut.write(bytes, 0, length);
//							}
//
//							zipOut.close();
//							fis.close();
//							fos.close();
//
//							fileUrl = fileSystemStorageService
//									.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
//						} else {
//							fileUrl = fileSystemStorageService
//									.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
//						}

					}
					String responseText = "Recommendation created successfully.";
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
							recommendation.setCreatedBy(master.get().getUserId());
							recommendation.setDepartment(new Department(id));
							recommendation.setComponent(new Component(recommendationAddRequestDto.getComponentId()));
							recommendation.setPriorityId(recommendationAddRequestDto.getPriorityId());
							recommendation.setRecommendationType(
									new RecommendationType(recommendationAddRequestDto.getTypeId()));
							recommendation.setRecommendationStatus(
									new RecommendationStatus(StatusEnum.OEM_recommendation.getId()));
							recommendation.setExpectedImpact(recommendationAddRequestDto.getExpectedImpact());
							recommendation.setUserType(master.get().getUserTypeId());
							if (recommendationAddRequestDto.getFile() != null) {
								recommendation.setFileName(recommendationAddRequestDto.getFile().getOriginalFilename());
							}
							List<Recommendation> recommendList = recommendationRepository.findAll();

							Integer size = 0;
							if (recommendList != null && recommendList.size() > 0) {
								Collections.sort(recommendList, Comparator.comparing(Recommendation::getId).reversed());
								size = size + recommendList.get(0).getId().intValue();
							}
							String refId = generateReferenceId(size);
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
							Optional<DepartmentApprover> approver = departmentApproverRepository
									.findAllByDepartmentId(id);
							if (savedRecommendation != null && savedRecommendation.getCreatedBy() != null
									&& savedRecommendation.getCreatedBy().getUserType().name()
											.equals(UserType.APPLICATION_OWNER.name())) {
								responseText = responseText + "An email will be sent to the AGM ";
								if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isEmpty()) {
									responseText = responseText + approver.get().getAgm().getUserName() + "("
											+ approver.get().getDepartment().getName() + ")";
									responseText += "(" + approver.get().getAgm().getEmail() + ") ";

								}
							} else {
								responseText = responseText + "An email will be sent to the application owner ";
								if (approver != null && approver.isPresent()) {
									if (approver.get().getApplicationOwner() != null
											&& !approver.get().getApplicationOwner().getEmail().isEmpty()) {
										responseText = responseText + approver.get().getApplicationOwner().getUserName()
												+ "(" + approver.get().getDepartment().getName() + ")";
										responseText += "(" + approver.get().getApplicationOwner().getEmail() + ") ";

									}
								}
							}
						}
					}

//						notificationService.save(savedRecommendation, RecommendationStatusEnum.CREATED);
					notificationService.saveAllNotification(recommendationList, RecommendationStatusEnum.CREATED);
//						emailTemplateService.sendMailRecommendation(recommendation, RecommendationStatusEnum.CREATED);
					emailTemplateService.sendAllMailForRecommendation(recommendationList,
							RecommendationStatusEnum.CREATED);

					return new Response<>(HttpStatus.CREATED.value(), responseText, null);

				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public Response<?> setRecommendationDeploymentDetails(
			RecommendationDetailsRequestDto recommendationDetailsRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					Optional<Recommendation> recommendation = recommendationRepository
							.findByReferenceId(recommendationDetailsRequestDto.getRecommendRefId());

					List<CredentialMaster> seniorManagementList = credentialMasterRepository
							.findByUserTypeId(UserType.GM_IT_INFRA);
					List<User> seniorManagementUsers = new ArrayList<>();
					if (seniorManagementList != null && seniorManagementList.size() > 0) {
						seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
								.collect(Collectors.toList());
					}
					Date recommendationDate = recommendation.get().getRecommendDate();
					SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
					String dateString = date.format(recommendationDate);
					SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String dateStrings = dateString + " 23:59:00";
					Date updatedRecommendedDate = time.parse(dateStrings);

					recommendation.get().setRecommendDate(updatedRecommendedDate);

					if (recommendation.get().getRecommendDate().after(new Date())) {

						if (recommendation.get().getRecommendDate()
								.after(recommendationDetailsRequestDto.getDeploymentDate())) {

							Optional<RecommendationDeplyomentDetails> recommendDeployDetails = deplyomentDetailsRepository
									.findByRecommendRefId(recommendationDetailsRequestDto.getRecommendRefId());
							if (recommendDeployDetails != null && recommendDeployDetails.isPresent()) {
								String responseText = "Deployment details updated successfully. An email will be sent to the AGM ";
								RecommendationDeplyomentDetails details = recommendationDetailsRequestDto
										.convertToEntity();
								details.setId(recommendDeployDetails.get().getId());
								RecommendationDeplyomentDetails savedDeploymentDetails = deplyomentDetailsRepository
										.save(details);

//							recommendation.get()
//									.setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
								recommendation.get().setIsAppOwnerApproved(true);
								recommendation.get().setUpdatedAt(new Date());
								Recommendation updateRecommendation = recommendationRepository
										.save(recommendation.get());
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
										RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS, null,
										recommendationDetailsRequestDto.getDescription());

								emailTemplateService.sendMailRecommendationDeplyomentDetails(
										recommendationDetailsRequestDto,
										RecommendationStatusEnum.UPDATE_DEPLOYMENT_DETAILS);
								if (approver != null && approver.isPresent()) {
									if (approver.get().getAgm() != null
											&& !approver.get().getAgm().getEmail().isEmpty()) {
										responseText += "(" + approver.get().getAgm().getEmail() + ") and GM ";

										for (User user : seniorManagementUsers) {
											responseText += "(" + user.getEmail() + ") ";
										}

										return new Response<>(HttpStatus.OK.value(), responseText, null);
									}
								}
								return new Response<>(HttpStatus.OK.value(), responseText, null);

							} else {
								String responseText = "Deployment details added successfully. An email will be sent to the AGM";
								RecommendationDeplyomentDetails details = recommendationDetailsRequestDto
										.convertToEntity();
								details.setCreatedAt(new Date());
								deplyomentDetailsRepository.save(details);
								recommendation.get().setRecommendationStatus(
										new RecommendationStatus(StatusEnum.Review_process.getId().longValue()));
								recommendation.get().setIsAppOwnerApproved(true);
//							recommendation.get()
//									.setExpectedImpact(recommendationDetailsRequestDto.getImpactedDepartment());
								recommendation.get()
										.setImpactedDepartment(recommendationDetailsRequestDto.getImpactedDepartment());
								recommendation.get().setUpdatedAt(new Date());
								Recommendation updateRecommendation = recommendationRepository
										.save(recommendation.get());
								RecommendationTrail trail = new RecommendationTrail();
								trail.setCreatedAt(new Date());
								trail.setRecommendationStatus(
										new RecommendationStatus(StatusEnum.Review_process.getId().longValue()));
								trail.setReferenceId(details.getRecommendRefId());
								recommendationTrailRepository.save(trail);
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
										RecommendationStatusEnum.APPROVED_BY_APPOWNER, null, null);
								emailTemplateService.sendMailRecommendationDeplyomentDetails(
										recommendationDetailsRequestDto, RecommendationStatusEnum.APPROVED_BY_APPOWNER);
								if (approver != null && approver.isPresent()) {
									if (approver.get().getAgm() != null
											&& !approver.get().getAgm().getEmail().isEmpty()) {
										responseText += "(" + approver.get().getAgm().getEmail() + ")";

										return new Response<>(HttpStatus.OK.value(), responseText, null);
									}
								}
								return new Response<>(HttpStatus.CREATED.value(), responseText, null);
							}
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(),
									"Deployment date should be before the recommendation end date", null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Recommendation end date exceed unable to perform any action", null);
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

					List<CredentialMaster> seniorManagementList = credentialMasterRepository
							.findByUserTypeId(UserType.GM_IT_INFRA);
					List<User> seniorManagementUsers = new ArrayList<>();
					if (seniorManagementList != null && seniorManagementList.size() > 0) {
						seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
								.collect(Collectors.toList());
					}

					String responseText = "Recommendation rejected successfully. An email will be sent to the ";

					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendation.getRecommendRefId());
					Date recommendationDate = recommendObj.get().getRecommendDate();
					SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
					String dateString = date.format(recommendationDate);
					SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String dateStrings = dateString + " 23:59:00";
					Date updatedRecommendedDate = time.parse(dateStrings);

					recommendObj.get().setRecommendDate(updatedRecommendedDate);

					if (recommendObj.get().getRecommendDate().after(new Date())) {
						RecommendationMessages messages = recommendation.convertToEntity();
						messages.setCreatedAt(new Date());
						recommendationMessagesRepository.save(messages);
						recommendObj.get().setIsAppOwnerApproved(false);
						recommendObj.get().setIsAppOwnerRejected(true);
						recommendObj.get().setIsAgmRejected(false);
						recommendObj.get()
								.setRecommendationStatus(new RecommendationStatus(StatusEnum.Review_process.getId()));
						Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
						RecommendationTrail recommendTrail = new RecommendationTrail();
						recommendTrail.setCreatedAt(new Date());
						recommendTrail
								.setRecommendationStatus(new RecommendationStatus(StatusEnum.Review_process.getId()));
						recommendTrail.setReferenceId(recommendation.getRecommendRefId());
						recommendationTrailRepository.save(recommendTrail);
						Optional<RecommendationDeplyomentDetails> recommendDeploymentDetails = deplyomentDetailsRepository
								.findByRecommendRefId(recommendation.getRecommendRefId());
//						if (recommendDeploymentDetails != null && recommendDeploymentDetails.isPresent()) {
//							deplyomentDetailsRepository.delete(recommendDeploymentDetails.get());
//						}

						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());
						notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_APPOWNER,
								recommendation.getRejectionMessage(), recommendation.getAddtionalInformation());
						emailTemplateService.sendMailRecommendationMessages(messages,
								RecommendationStatusEnum.REJECTED_BY_APPOWNER);
						if (approver != null && approver.isPresent() && (recommendObj.get().getPriorityId()
								.longValue() != PriorityEnum.High.getId().longValue())) {
							if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isEmpty()) {
								responseText += "AGM (" + approver.get().getAgm().getEmail() + ") and GM ";
								for (User user : seniorManagementUsers) {
									responseText += "(" + user.getEmail() + ") ";
								}

								return new Response<>(HttpStatus.OK.value(), responseText, null);
							}
						} else {
							if (approver.get().getDgm() != null && !approver.get().getDgm().getEmail().isEmpty()) {
								responseText += "DGM (" + approver.get().getDgm().getEmail() + ") and GM ";
								for (User user : seniorManagementUsers) {
									responseText += "(" + user.getEmail() + ") ";
								}
								return new Response<>(HttpStatus.OK.value(), responseText, null);
							}
						}
						return new Response<>(HttpStatus.OK.value(), responseText, null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Recommendation end date exceed unable to perform any action", null);
					}

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

					List<CredentialMaster> seniorManagementList = credentialMasterRepository
							.findByUserTypeId(UserType.GM_IT_INFRA);
					List<User> seniorManagementUsers = new ArrayList<>();
					if (seniorManagementList != null && seniorManagementList.size() > 0) {
						seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
								.collect(Collectors.toList());
					}

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
								&& !approver.get().getApplicationOwner().getEmail().isEmpty()) {
							responseText += "(" + approver.get().getApplicationOwner().getEmail() + ") and GM";
							for (User user : seniorManagementUsers) {
								responseText += "(" + user.getEmail() + ") ";
							}
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

					List<CredentialMaster> seniorManagementList = credentialMasterRepository
							.findByUserTypeId(UserType.GM_IT_INFRA);
					List<User> seniorManagementUsers = new ArrayList<>();
					if (seniorManagementList != null && seniorManagementList.size() > 0) {
						seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
								.collect(Collectors.toList());
					}

					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					if (recommendObj != null && recommendObj.isPresent()) {
						if (recommendObj.get().getIsAppOwnerApproved() != null
								&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {
							responseText = "Recommendation reject request sent successfully. An email will be sent to the Appowner";
							RecommendationMessages messages = recommendationRejectionRequestDto.convertToEntity();
							messages.setCreatedAt(new Date());
							recommendationMessagesRepository.save(messages);
							notificationService.save(recommendObj.get(), RecommendationStatusEnum.REJECTED_BY_AGM,
									recommendationRejectionRequestDto.getRejectionMessage(),
									recommendationRejectionRequestDto.getAddtionalInformation());
							emailTemplateService.sendMailRecommendationMessages(messages,
									RecommendationStatusEnum.REJECTED_BY_AGM);
							recommendObj.get().setIsAppOwnerApproved(false);
							recommendObj.get().setIsAgmRejected(true);
							recommendObj.get().setUpdatedAt(new Date());
							Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
							Department rcmdDepartment = updateRecommendation.getDepartment();
							Optional<DepartmentApprover> approver = departmentApproverRepository
									.findAllByDepartmentId(rcmdDepartment.getId());
							if (approver != null && approver.isPresent()) {
								if (approver.get().getApplicationOwner() != null
										&& !approver.get().getApplicationOwner().getEmail().isEmpty()) {
									responseText += "(" + approver.get().getApplicationOwner().getEmail() + ") and GM ";
									for (User user : seniorManagementUsers) {
										responseText += "(" + user.getEmail() + ") ";
									}
									return new Response<>(HttpStatus.OK.value(), responseText, null);
								}
							}
							return new Response<>(HttpStatus.OK.value(), responseText, null);
						} else {
							responseText = "Recommendation reject request sent successfully. ";
							if (recommendObj.get().getRecommendationStatus().getId() != StatusEnum.Rejected.getId()
									.longValue()) {
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
										RecommendationStatusEnum.RECCOMENDATION_REJECTED,
										recommendationRejectionRequestDto.getRejectionMessage(),
										recommendationRejectionRequestDto.getAddtionalInformation());

								emailTemplateService.sendMailRecommendationMessages(messages,
										RecommendationStatusEnum.RECCOMENDATION_REJECTED);

								Department rcmdDepartment = updateRecommendation.getDepartment();
								Optional<DepartmentApprover> approver = departmentApproverRepository
										.findAllByDepartmentId(rcmdDepartment.getId());
								if (approver != null && approver.isPresent()) {
									if (approver.get().getApplicationOwner() != null
											&& !approver.get().getApplicationOwner().getEmail().isEmpty()) {
										responseText += "Email will be sent to Appowner("
												+ approver.get().getApplicationOwner().getEmail() + "), OEM("
												+ recommendObj.get().getCreatedBy().getEmail() + ") and GM ";
										for (User user : seniorManagementUsers) {
											responseText += "(" + user.getEmail() + ") ";
										}
										return new Response<>(HttpStatus.OK.value(), responseText, null);
									}
								}

								return new Response<>(HttpStatus.OK.value(), "Recommendation rejected successfully.",
										null);
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
			RecommendationDetailsRequestDto recommendationRejectionRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.AGM.name())) {
					Optional<Recommendation> recommendObj = recommendationRepository
							.findByReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
					if (recommendObj.get().getIsAppOwnerApproved() != null
							&& recommendObj.get().getIsAppOwnerApproved().booleanValue() == true) {

						List<CredentialMaster> seniorManagementList = credentialMasterRepository
								.findByUserTypeId(UserType.GM_IT_INFRA);
						List<User> seniorManagementUsers = new ArrayList<>();
						if (seniorManagementList != null && seniorManagementList.size() > 0) {
							seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
									.collect(Collectors.toList());
						}

						String responseText = "Recommendation request accepted. ";
						recommendObj.get().setIsAgmApproved(true);
						recommendObj.get().setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Approved.getId().longValue()));
						recommendObj.get().setUpdatedAt(new Date());
						Recommendation updateRecommendation = recommendationRepository.save(recommendObj.get());
						RecommendationTrail trailData = new RecommendationTrail();
						trailData.setCreatedAt(new Date());
						trailData.setRecommendationStatus(
								new RecommendationStatus(StatusEnum.Approved.getId().longValue()));
						trailData.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
						recommendationTrailRepository.save(trailData);
						if (recommendationRejectionRequestDto.getDescription() != null
								&& recommendationRejectionRequestDto.getDescription() != ""
								&& !(recommendationRejectionRequestDto.getDescription().isEmpty())) {
							RecommendationMessages messages = new RecommendationMessages();
							messages.setAdditionalMessage(recommendationRejectionRequestDto.getDescription());
							messages.setCreatedBy(recommendationRejectionRequestDto.getCreatedBy());
							messages.setReferenceId(recommendationRejectionRequestDto.getRecommendRefId());
							messages.setCreatedAt(new Date());
							recommendationMessagesRepository.save(messages);
						}

						notificationService.save(recommendObj.get(), RecommendationStatusEnum.APPROVED_BY_AGM, null,
								recommendationRejectionRequestDto.getDescription());

						emailTemplateService.sendMailRecommendation(recommendObj.get(),
								RecommendationStatusEnum.APPROVED_BY_AGM);

						Department rcmdDepartment = updateRecommendation.getDepartment();
						Optional<DepartmentApprover> approver = departmentApproverRepository
								.findAllByDepartmentId(rcmdDepartment.getId());
						if (recommendObj.get().getCreatedBy() != null && recommendObj.get().getCreatedBy().getUserType()
								.name().equals(UserType.APPLICATION_OWNER.name())) {
							responseText += "Email will be sent to Appowner("
									+ approver.get().getApplicationOwner().getEmail() + ") and GM ";
							for (User user : seniorManagementUsers) {
								responseText += "(" + user.getEmail() + ") ";
							}
						} else {
							if (approver != null && approver.isPresent()) {
								if (approver.get().getApplicationOwner() != null
										&& !approver.get().getApplicationOwner().getEmail().isEmpty()) {
									responseText += "Email will be sent to Appowner("
											+ approver.get().getApplicationOwner().getEmail() + "), OEM("
											+ recommendObj.get().getCreatedBy().getEmail() + ") and GM ";
									for (User user : seniorManagementUsers) {
										responseText += "(" + user.getEmail() + ") ";
									}
									return new Response<>(HttpStatus.OK.value(), responseText, null);
								}
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

						List<CredentialMaster> seniorManagementList = credentialMasterRepository
								.findByUserTypeId(UserType.GM_IT_INFRA);
						List<User> seniorManagementUsers = new ArrayList<>();
						if (seniorManagementList != null && seniorManagementList.size() > 0) {
							seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
									.collect(Collectors.toList());
						}

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
							if (approver.get().getAgm() != null && !approver.get().getAgm().getEmail().isEmpty()) {
								responseText += "(" + approver.get().getAgm().getEmail() + ") and GM ";
								for (User user : seniorManagementUsers) {
									responseText += "(" + user.getEmail() + ") ";
								}
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

//	@Override
//	public Response<?> addRecommendationThroughExcel(MultipartFile file) {
//		try {
//			Workbook workbook = WorkbookFactory.create(file.getInputStream());
//			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
//			if (master != null && master.isPresent()) {
//				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
//					int numberOfSheets = workbook.getNumberOfSheets();
//					List<String> headerList = new ArrayList<>();
//					List<String> cellValueString = new ArrayList<>();
//					List<JsonObject> objectList = new ArrayList<>();
//					Boolean isValidFile = false;
//					String[] expectedColumnNames = { "Descriptions", "Type", "Priority", "Recommend end date",
//							"Department", "Component name", "Expected Impact", "Document link" };
//					List<RecommendationType> recommendationTypeList = recommendationTypeRepository.findAll();
//					List<Department> departmentList = departmentRepository.findAll();
//					List<Component> componentList = componentRepository.findAll();
//					Map<String, RecommendationType> recommendationTypeMap = new HashMap<>();
//					for (RecommendationType type : recommendationTypeList) {
//						recommendationTypeMap.put(type.getName().trim().toUpperCase(), type);
//					}
//					Map<String, Department> departmentMap = new HashMap<>();
//					for (Department department : departmentList) {
//						departmentMap.put(department.getName().trim().toUpperCase(), department);
//					}
//					Map<String, Component> componentMap = new HashMap<>();
//					for (Component component : componentList) {
//						componentMap.put(component.getName().trim().toUpperCase(), component);
//					}
//					List<String> stringList = Arrays.asList(expectedColumnNames);
//					Boolean isEmptySheet = false;
//					for (int i = 0; i < numberOfSheets; i++) {
//						Sheet sheet = workbook.getSheetAt(i);
//						Row topRowData = sheet.getRow(0);
//						int noOfTopData = 0;
//						for (Cell topCell : topRowData) {
//							headerList.add(topCell.toString().trim());
//							noOfTopData += 1;
//						}
//
//						if (headerList.equals(stringList)) {
//							isValidFile = true;
//						}
//						if (isValidFile) {
//							if (!(sheet.getPhysicalNumberOfRows() > 1)) {
//								isEmptySheet = true;
//							} else {
//								for (Row row : sheet) {
//
//									String str = "";
//									if (row != null && !isRowEmpty(row)) {
//										for (int j = 0; j < noOfTopData; j++) {
//											Cell cel = row.getCell(j);
//
//											String cellName = "";
//											if (cel == null) {
//												if (str == "") {
//													str = str + "" + "/n";
//												} else {
//													str = str + " " + "/n";
//												}
//											} else {
//												cellName = cel.toString();
//												if (cel.toString().contains(".") && cel.toString().contains("E")) {
//													String[] stringArray = cel.toString().split("E");
//													List<String> wordList = Arrays.asList(stringArray);
//													String firstString = wordList.get(0);
//													String lastString = wordList.get(1);
//													if (firstString != null && !firstString.isEmpty()
//															&& lastString != null && !lastString.isEmpty()) {
//														try {
//															cellName = new DecimalFormat("#.##")
//																	.format(Double.parseDouble(firstString) * Math
//																			.pow(10, Double.parseDouble(lastString)));
//														} catch (Exception e) {
//														}
//													}
//												}
//												if (cel.getCellType() == CellType.NUMERIC
//														&& DateUtil.isCellDateFormatted(cel)) {
//													Date javaDate = cel.getDateCellValue();
//													SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//													cellName = formatter.format(javaDate);
//												}
//												if (str == "") {
//													str = str + cellName + " " + "/n";
//												} else {
//													str = str + cellName + " " + "/n";
//												}
//
//											}
//
//										}
//										if (!str.isEmpty() && str != "") {
//											cellValueString.add(str);
//											str = "";
//										} else {
//											str = "";
//										}
//									}
//
//								}
//							}
//						}
//
//					}
//					if (isValidFile) {
//						if (!isEmptySheet) {
//							List<String> updatedList = new ArrayList<>();
//							for (int i = 1; i < cellValueString.size(); i++) {
//								updatedList.add(cellValueString.get(i));
//							}
//							for (String str : updatedList) {
//								String[] commaSeparatedArray = str.split("/n");
//								List<String> wordList = Arrays.asList(commaSeparatedArray);
//								JsonObject obj = new JsonObject();
//
//								for (int i = 0; i < headerList.size(); i++) {
//									obj.addProperty(headerList.get(i), wordList.get(i));
//								}
//								objectList.add(obj);
//
//							}
//							List<Recommendation> recommendationList = new ArrayList<>();
//							for (Object obj : objectList) {
//								Recommendation recommendation = new Recommendation();
//								JSONObject object = new JSONObject(obj.toString());
//								if (object.has("Descriptions")) {
//									if (object.get("Descriptions") == null || object.get("Descriptions").equals(" ")
//											|| object.get("Descriptions").equals("")) {
//										recommendation.setDescriptions(null);
//									} else {
//										recommendation.setDescriptions(object.getString("Descriptions").trim());
//									}
//								}
//								if (object.has("Type")) {
//									if (object.get("Type") == null || object.get("Type").equals(" ")
//											|| object.get("Type").equals("")) {
//										recommendation.setDescriptions(null);
//									} else {
//										if (recommendationTypeMap
//												.containsKey(object.get("Type").toString().trim().toUpperCase())) {
//											recommendation.setRecommendationType(recommendationTypeMap
//													.get(object.get("Type").toString().trim().toUpperCase()));
//										} else {
//											recommendation.setRecommendationType(null);
//										}
//									}
//								}
//								if (object.has("Priority")) {
//									if (object.get("Priority") == null || object.get("Priority").equals(" ")
//											|| object.get("Priority").equals("")) {
//										recommendation.setPriorityId(null);
//									} else {
//										if (object.get("Priority").toString().trim().toUpperCase().equals("HIGH")) {
//											recommendation.setPriorityId(PriorityEnum.High.getId().longValue());
//										} else if (object.get("Priority").toString().trim().toUpperCase()
//												.equals("MEDIUM")) {
//											recommendation.setPriorityId(PriorityEnum.Medium.getId().longValue());
//										} else if (object.get("Priority").toString().trim().toUpperCase()
//												.equals("LOW")) {
//											recommendation.setPriorityId(PriorityEnum.Low.getId().longValue());
//										} else {
//											recommendation.setPriorityId(null);
//										}
//									}
//								}
//								if (object.has("Recommend end date")) {
//									if (object.get("Recommend end date") == null
//											|| object.get("Recommend end date").equals(" ")
//											|| object.get("Recommend end date").equals("")) {
//										recommendation.setRecommendDate(null);
//									} else {
//										DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//										Date date = formatter.parse(object.get("Recommend end date").toString().trim());
//										recommendation.setRecommendDate(date);
//									}
//								}
//								if (object.has("Department")) {
//									if (object.get("Department") == null || object.get("Department").equals(" ")
//											|| object.get("Department").equals("")) {
//										recommendation.setDepartment(null);
//									} else {
//										if (departmentMap.containsKey(
//												object.get("Department").toString().trim().toUpperCase())) {
//											recommendation.setDepartment(departmentMap
//													.get(object.get("Department").toString().trim().toUpperCase()));
//										} else {
//											recommendation.setDepartment(null);
//										}
//									}
//								}
//								if (object.has("Component name")) {
//									if (object.get("Component name") == null || object.get("Component name").equals(" ")
//											|| object.get("Component name").equals("")) {
//										recommendation.setComponent(null);
//									} else {
//										if (componentMap.containsKey(
//												object.get("Component name").toString().trim().toUpperCase())) {
//											recommendation.setComponent(componentMap
//													.get(object.get("Component name").toString().trim().toUpperCase()));
//										} else {
//											recommendation.setComponent(null);
//										}
//									}
//								}
//								if (object.has("Expected Impact")) {
//									if (object.get("Expected Impact") == null
//											|| object.get("Expected Impact").equals(" ")
//											|| object.get("Expected Impact").equals("")) {
//										recommendation.setExpectedImpact(null);
//									} else {
//										recommendation
//												.setExpectedImpact(object.get("Expected Impact").toString().trim());
//									}
//								}
//								if (object.has("Document link")) {
//									if (object.get("Document link") == null || object.get("Document link").equals(" ")
//											|| object.get("Document link").equals("")) {
//										recommendation.setDocumentUrl(null);
//									} else {
//										recommendation.setDocumentUrl(object.get("Document link").toString().trim());
//									}
//								}
//								recommendation.setCreatedAt(new Date());
//								recommendation.setUpdatedAt(new Date());
//								List<Recommendation> recommendationListObj = recommendationRepository.findAll();
//								recommendation.setReferenceId(generateReferenceId(recommendationListObj.size()));
//								recommendation.setRecommendationStatus(
//										new RecommendationStatus(StatusEnum.OEM_recommendation.getId().longValue()));
//								RecommendationTrail trailData = new RecommendationTrail();
//								trailData.setCreatedAt(new Date());
//								trailData.setRecommendationStatus(
//										new RecommendationStatus(StatusEnum.OEM_recommendation.getId().longValue()));
//								trailData.setReferenceId(recommendation.getReferenceId());
//								recommendationTrailRepository.save(trailData);
//								recommendation.setCreatedBy(master.get().getUserId());
//								recommendationList.add(recommendation);
//							}
//							Response<List<String>> response = new Response<>();
//							if (recommendationList != null && recommendationList.size() > 0) {
//								for (Recommendation recommendation : recommendationList) {
//									if (recommendation.getDescriptions() == null
//											|| recommendation.getDescriptions().equals("")) {
//										response.setMessage("Descriptions  can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else if (recommendation.getRecommendationType() == null) {
//										response.setMessage("Type can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else if (recommendation.getPriorityId() == null) {
//										response.setMessage("Priority can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else if (recommendation.getRecommendDate() == null) {
//										response.setMessage("Recommended end date can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else if (recommendation.getDepartment() == null) {
//										response.setMessage("Department can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else if (recommendation.getComponent() == null) {
//										response.setMessage("Component name can't be blank.");
//										response.setResponseCode(HttpStatus.BAD_REQUEST.value());
//										response.setData(null);
//										break;
//									} else {
//										response.setMessage("OK");
//										response.setResponseCode(HttpStatus.OK.value());
//									}
//								}
//							}
//							if (response.getResponseCode() == HttpStatus.OK.value()) {
//								recommendationRepository.saveAll(recommendationList);
//								return new Response<>(HttpStatus.OK.value(), "Recommendation list added successfully.",
//										null);
//							} else {
//								return response;
//							}
//						} else {
//							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
//						}
//					} else {
//						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Wrong File.", null);
//					}
//				} else {
//					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
//				}
//			} else {
//				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
//		}
//	}

	@Override
	public Response<?> addRecommendationThroughExcel(MultipartFile file) {
		try {
			Workbook workbook = WorkbookFactory.create(file.getInputStream());
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())
						|| master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {

					int numberOfSheets = workbook.getNumberOfSheets();
					List<String> headerList = new ArrayList<>();
					List<String> cellValueString = new ArrayList<>();
					List<JsonObject> objectList = new ArrayList<>();
					Boolean isValidFile = false;
					String[] expectedColumnNames = { "OEM/Vendor*", "Description*", "Type*", "Priority*",
							"Patch / Recommendation release date*", "Recommended End Date*", "Department*",
							"Recommended By", "Component Name*", "Expected Impact", "Impacted Departments", "Status*",
							"Document Link" };
					List<RecommendationType> recommendationTypeList = recommendationTypeRepository.findAll();
					List<Component> componentList = componentRepository.findAll();
					Map<String, RecommendationType> recommendationTypeMap = new HashMap<>();
					for (RecommendationType type : recommendationTypeList) {
						recommendationTypeMap.put(type.getName().trim().toUpperCase(), type);
					}
					Map<String, Component> componentMap = new HashMap<>();
					for (Component component : componentList) {
						componentMap.put(component.getName().trim().toUpperCase(), component);
					}
					List<String> stringList = Arrays.asList(expectedColumnNames);
					Boolean isEmptySheet = false;
					for (int i = 0; i < numberOfSheets; i++) {
						Sheet sheet = workbook.getSheetAt(i);
						Row topRowData = sheet.getRow(0);
						int noOfTopData = 0;
						for (Cell topCell : topRowData) {
//							headerList.add(topCell.toString().trim().replace("*", ""));
							headerList.add(topCell.toString().trim());
							noOfTopData += 1;
						}

						if (headerList.equals(stringList)) {
							isValidFile = true;
						} else {
							isValidFile = false;
						}

						if (isValidFile) {
							if (!(sheet.getPhysicalNumberOfRows() > 1)) {
								isEmptySheet = true;
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
						if (!isEmptySheet) {
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
							Response<List<String>> response = null;
							List<RecommendationAddRequestDto> requestDtosList = new ArrayList<>();
							for (Object obj : objectList) {

								response = new Response<>();
								List<Department> recommendationDepartments = new ArrayList<>();
								RecommendationAddRequestDto recommendationDto = new RecommendationAddRequestDto();
								JSONObject object = new JSONObject(obj.toString());
								if (object.has("OEM/Vendor*")) {
									if (object.get("OEM/Vendor*") == null
											|| object.get("OEM/Vendor*").toString().trim().isEmpty()) {
//										recommendationDto.setDescription(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide OEM/Vendor type", null);
									} else {
										String oemVendor = object.get("OEM/Vendor*").toString().trim();
										// Remove whitespace from the value before comparison
										if (oemVendor.equalsIgnoreCase("OEM")) {
											oemVendor = oemVendor.concat("_SI");
										}
										if (oemVendor.equalsIgnoreCase(UserType.OEM_SI.name())) {
											recommendationDto.setUserType(UserType.OEM_SI);
										} else if (oemVendor.equalsIgnoreCase(UserType.VENDOR.name())) {
											recommendationDto.setUserType(UserType.VENDOR);
										} else {
											return new Response<>(HttpStatus.BAD_REQUEST.value(),
													"Please provide valid OEM/Vendor type", null);
										}
									}
								}
								if (object.has("Description*")) {
									if (object.get("Description*") == null
											|| object.get("Description*").toString().trim().isEmpty()) {
//										recommendationDto.setDescription(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide description", null);
									} else {
										recommendationDto.setDescription(object.getString("Description*").trim());
									}
								}

								if (object.has("Type*")) {
									if (object.get("Type*") == null
											|| object.get("Type*").toString().trim().isEmpty()) {
										return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide type",
												null);
									} else {
										// Use StringEscapeUtils to unescape Unicode escape sequences
										String type = StringEscapeUtils
												.unescapeJava(object.get("Type*").toString().trim().toUpperCase());

										// Standardize the type key
										String standardizedType = type.replace("–", "-").replace("*", "");

										if (standardizedType.contains(".")) {
											// Split the type by "."
											String[] typeParts = standardizedType.split("\\.");

											// Set the standardized type to the first element
											standardizedType = typeParts[0];
										}

										if (recommendationTypeMap.containsKey(standardizedType)) {
											recommendationDto
													.setTypeId(recommendationTypeMap.get(standardizedType).getId());
										} else {
											recommendationDto.setTypeId(null);
										}
									}
								}

								if (object.has("Priority*")) {
									if (object.get("Priority*") == null
											|| object.get("Priority*").toString().trim().isEmpty()) {
//										recommendationDto.setPriorityId(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide priority",
												null);
									} else {
										if (object.get("Priority*").toString().trim().toUpperCase().equals("HIGH")) {
											recommendationDto.setPriorityId(PriorityEnum.High.getId().longValue());
										} else if (object.get("Priority*").toString().trim().toUpperCase()
												.equals("MEDIUM")) {
											recommendationDto.setPriorityId(PriorityEnum.Medium.getId().longValue());
										} else if (object.get("Priority*").toString().trim().toUpperCase()
												.equals("LOW")) {
											recommendationDto.setPriorityId(PriorityEnum.Low.getId().longValue());
										} else {
											recommendationDto.setPriorityId(null);
										}
									}
								}

								if (object.has("Patch / Recommendation release date*")) {
									if (object.get("Patch / Recommendation release date*") == null || object
											.get("Patch / Recommendation release date*").toString().trim().isEmpty()) {
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide Patch / Recommendation release date", null);
									} else {
										try {
											String dateString = object.get("Patch / Recommendation release date*")
													.toString().trim();

											// Split the date string into day, month, and year
											String[] dateParts = dateString.split("-");
											if (dateParts.length != 3) {
												throw new ParseException(
														"Invalid date format in Patch / Recommendation release date",
														0);
											}

											int day = Integer.parseInt(dateParts[2]);
											int month = Integer.parseInt(dateParts[1]);
											int year = Integer.parseInt(dateParts[0]);

											// Validate day, month, and year
											if (day < 1 || day > 31 || month < 1 || month > 12 || year < 1900
													|| year > 2100) {
												// Invalid date, return error response
												return new Response<>(HttpStatus.BAD_REQUEST.value(),
														"Invalid date in Patch / Recommendation release date", null);
											}

											// Parse the date
											DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
											Date date = formatter.parse(dateString);

											recommendationDto.setRecommendationReleasedDate(date);
										} catch (NumberFormatException | ParseException e) {
											// Handle parsing exception
											e.printStackTrace();
											return new Response<>(HttpStatus.BAD_REQUEST.value(),
													"Please provide a valid date in the format dd/MM/yyyy for Patch / Recommendation release date",
													null);
										}
									}
								}

								if (object.has("Recommended End Date*")) {
									if (object.get("Recommended End Date*") == null
											|| object.get("Recommended End Date*").toString().trim().isEmpty()) {
										// recommendationDto.setRecommendDate(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide Recommended End Date", null);
									} else {
										try {
											String dateString = object.get("Recommended End Date*").toString().trim();

											// Split the date string into day, month, and year
											String[] dateParts = dateString.split("-");
											if (dateParts.length != 3) {
												throw new ParseException("Invalid date format in Recommended End Date",
														0);
											}

											int day = Integer.parseInt(dateParts[2]);
											int month = Integer.parseInt(dateParts[1]);
											int year = Integer.parseInt(dateParts[0]);

											// Validate day, month, and year
											if (day < 1 || day > 31 || month < 1 || month > 12 || year < 1900
													|| year > 2100) {
												// Invalid date, return error response
												return new Response<>(HttpStatus.BAD_REQUEST.value(),
														"Invalid date in Recommended End Date", null);
											}

											// Parse the date
											DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
											Date date = formatter.parse(dateString);
											recommendationDto.setRecommendDate(date);
										} catch (NumberFormatException | ParseException e) {
											// Handle parsing exception
											return new Response<>(HttpStatus.BAD_REQUEST.value(),
													"Please provide a valid date in the format dd/MM/yyyy for Recommended End Date",
													null);
										}
									}
								}

								if (object.has("Department*")) {
									if (object.get("Department*").toString().trim().isEmpty()) {
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide department", null);
									}
									List<String> departmentNames = Arrays
											.asList(object.get("Department*").toString().split(","));

									// Convert the list to a set to remove duplicates, ignoring case
									Set<String> uniqueDepartmentNames = new HashSet<>();
									for (String departmentName : departmentNames) {
										uniqueDepartmentNames.add(departmentName.trim().toUpperCase());
									}

									// Check if the size of the set is different from the size of the list
									if (uniqueDepartmentNames.size() != departmentNames.size()) {
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Duplicate department found", null);
									}

									for (String departmentName : uniqueDepartmentNames) {
										// Standardize the department name
										String standardizedDepartment = departmentName.replace("–", "-").replace("*",
												"");

										if (dataRetrievalService.getAllDepartmentsMap()
												.containsKey(standardizedDepartment)) {
											recommendationDepartments.add(dataRetrievalService.getAllDepartmentsMap()
													.get(standardizedDepartment));
										} else if (dataRetrievalService
												.findDepartmentByName(standardizedDepartment) != null) {
											recommendationDepartments.add(
													dataRetrievalService.findDepartmentByName(standardizedDepartment));
										} else {
											return new Response<>(HttpStatus.BAD_REQUEST.value(),
													"Invalid department provided in recommendation with description, "
															+ recommendationDto.getDescription(),
													null);
										}

									}

								}

								if (object.has("Recommended By")) {
									if (object.get("Recommended By") == null
											|| object.get("Recommended By").toString().trim().isEmpty()) {
										recommendationDto.setRecommendedBy(null);
									} else {
										recommendationDto
												.setRecommendedBy(object.get("Recommended By").toString().trim());
									}
								}

								if (object.has("Component Name*")) {
									if (object.get("Component Name*") == null
											|| object.get("Component Name*").toString().trim().isEmpty()) {
//										recommendationDto.setComponentId(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide Component Name", null);
									} else {
										String componentName = object.get("Component Name*").toString().trim()
												.toUpperCase();

										String standardizedComponent = componentName.replace("–", "-").replace("*", "");

										if (componentMap.containsKey(standardizedComponent)) {
											recommendationDto
													.setComponentId(componentMap.get(standardizedComponent).getId());
										} else {
											recommendationDto.setComponentId(null);
										}
									}
								}

								if (object.has("Expected Impact")) {
									if (object.get("Expected Impact") == null
											|| object.get("Expected Impact").toString().trim().isEmpty()) {
										recommendationDto.setExpectedImpact(null);
									} else {
										recommendationDto
												.setExpectedImpact(object.get("Expected Impact").toString().trim());
									}
								}

								if (object.has("Impacted Departments")) {
									if (object.get("Impacted Departments") != null
											&& !object.get("Impacted Departments").toString().trim().isEmpty()) {
										String impactedDepartmentsString = object.get("Impacted Departments").toString()
												.trim();
										List<String> impactedDepartmentsList = Arrays
												.asList(impactedDepartmentsString.split(","));

										List<String> invalidDepartments = new ArrayList<>();

										for (String departmentName : impactedDepartmentsList) {
											departmentName = departmentName.trim().toUpperCase();
											// Standardize the department name
											String standardizedDepartment = departmentName.replace("–", "-")
													.replace("*", "");

											if (!dataRetrievalService.getAllDepartmentsMap()
													.containsKey(standardizedDepartment)) {
												invalidDepartments.add(departmentName);
											}
										}

										if (!invalidDepartments.isEmpty()) {
											StringBuilder errorMessage = new StringBuilder(
													"Invalid impacted department: ");
											errorMessage.append(String.join(", ", invalidDepartments));
											return new Response<>(HttpStatus.BAD_REQUEST.value(),
													errorMessage.toString(), null);
										}

										// All departments are valid, set the impactedDepartments field
										recommendationDto.setImpactedDepartments(impactedDepartmentsString);
									}
								}

								if (object.has("Status*")) {
									if (object.get("Status*") == null
											|| object.get("Status*").toString().trim().isEmpty()) {
//										recommendationDto.setStatus(null);
										return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide Status",
												null);
									} else if (!object.get("Status*").toString().trim()
											.equalsIgnoreCase(StatusEnum.Released.toString())) {
										return new Response<>(HttpStatus.BAD_REQUEST.value(),
												"Please provide only released recommendations", null);
									} else {
										recommendationDto
												.setStatus(new RecommendationStatus(StatusEnum.Released.getId()));
									}
								}

								if (object.has("Document Link")) {
									if (object.get("Document Link") == null
											|| object.get("Document Link").toString().trim().isEmpty()) {
										recommendationDto.setUrlLink(null);
									} else {
										recommendationDto.setUrlLink(object.get("Document Link").toString().trim());
									}
								}

								Date currentDate = new Date();

								// Check if Patch / Recommendation release date is after today
								if (recommendationDto.getRecommendationReleasedDate().after(currentDate)) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"Patch / Recommendation release date should be today or earlier", null);
								}

								List<Long> departmentIds = new ArrayList<>();
								for (Department department : recommendationDepartments) {
									departmentIds.add(department.getId());
								}
								recommendationDto.setDepartmentIds(departmentIds);
								if (recommendationDto.getDescription() == null
										|| recommendationDto.getDescription().equals("")) {
									response.setMessage("Descriptions  can't be blank.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else if (recommendationDto.getTypeId() == null) {
									response.setMessage("Invalid Recommendation Type.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else if (recommendationDto.getPriorityId() == null) {
									response.setMessage("Invalid Priority.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else if (recommendationDto.getRecommendDate() == null) {
									response.setMessage("Recommended end date can't be blank.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else if (recommendationDto.getDepartmentIds() == null
										&& recommendationDto.getDepartmentIds().isEmpty()) {
									response.setMessage("Invalid Department.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else if (recommendationDto.getComponentId() == null) {
									response.setMessage("Invalid Component.");
									response.setResponseCode(HttpStatus.BAD_REQUEST.value());
									response.setData(null);
									break;
								} else {
									response.setMessage("OK");
									response.setResponseCode(HttpStatus.OK.value());
								}

								if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())
										&& master.get().getUserId().getDepartment() != null
										&& !recommendationDto.getDepartmentIds()
												.contains(master.get().getUserId().getDepartment().getId())) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"You can only add recommendations for your department.", null);
								}

								requestDtosList.add(recommendationDto);
							}

							if (response == null) {
								return new Response<>(HttpStatus.BAD_REQUEST.value(),
										"Something wrong with the excel file", null);
							}

							if (response.getResponseCode() != HttpStatus.OK.value()) {
								return response;
							}

							Response<?> validExcelData = validateExcelRecommendationData(requestDtosList, master.get());
							if (validExcelData.getResponseCode() != HttpStatus.OK.value()) {
								return validExcelData;
							}

							Response<?> addRecommendation = saveAllRecommendationsThroughExcel(requestDtosList,
									master.get().getUserId());
							if (addRecommendation.getResponseCode() != HttpStatus.OK.value()) {
								response.setMessage(addRecommendation.getMessage());
								response.setResponseCode(addRecommendation.getResponseCode());
								response.setData(null);
							}

							if (response.getResponseCode() == HttpStatus.OK.value()) {
								return new Response<>(HttpStatus.OK.value(), "Recommendation list added successfully.",
										null);
							} else {
								return response;
							}
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
						}
					} else {
//						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Wrong File.", null);
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide a valid file.", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access.", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
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

	private boolean isRowEmptyV2(Row row) {
		if (row == null) {
			return true;
		}
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				return false; // Found a non-empty cell
			}
		}
		return true; // All cells in the row are empty
	}

	@Override
	public Response<?> updateRecommendationStatus(RecommendationDetailsRequestDto recommendationRequestDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {

					List<CredentialMaster> seniorManagementList = credentialMasterRepository
							.findByUserTypeId(UserType.GM_IT_INFRA);
					List<User> seniorManagementUsers = new ArrayList<>();
					if (seniorManagementList != null && seniorManagementList.size() > 0) {
						seniorManagementUsers = seniorManagementList.stream().map(CredentialMaster::getUserId)
								.collect(Collectors.toList());
					}

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
								if (updatedRecommendation.getCreatedBy().getUserType().name()
										.equals(UserType.APPLICATION_OWNER.name())) {
									responseText += "Email will be sent to AGM(" + approver.get().getAgm().getEmail()
											+ ") and GM";
									for (User user : seniorManagementUsers) {
										responseText += "(" + user.getEmail() + ") ";
									}
								} else {
									if (approver != null && approver.isPresent()) {
										if (approver.get().getAgm() != null
												&& !approver.get().getAgm().getEmail().isEmpty()) {
											responseText += "Email will be sent to AGM("
													+ approver.get().getAgm().getEmail() + "), OEM("
													+ updatedRecommendation.getCreatedBy().getEmail() + ") and GM";
											for (User user : seniorManagementUsers) {
												responseText += "(" + user.getEmail() + ") ";
											}
										}
									}
								}
								notificationService.save(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_RELEASED, null, null);

								emailTemplateService.sendMailRecommendation(updatedRecommendation,
										RecommendationStatusEnum.RECOMMENDATION_RELEASED);
							} else {

								if (recommendationObj.get().getCreatedBy().getUserType().name()
										.equals(UserType.APPLICATION_OWNER.name())) {
									responseText += ". Email will be sent to AGM(" + approver.get().getAgm().getEmail()
											+ ") and GM";
									for (User user : seniorManagementUsers) {
										responseText += "(" + user.getEmail() + ") ";
									}
								} else {

									if (approver != null && approver.isPresent()) {
										if (approver.get().getAgm() != null
												&& !approver.get().getAgm().getEmail().isEmpty()) {
											responseText += ". Email will be sent to AGM("
													+ approver.get().getAgm().getEmail() + ")" + " " + ", OEM("
													+ recommendationObj.get().getCreatedBy().getEmail() + ") and GM";
											for (User user : seniorManagementUsers) {
												responseText += "(" + user.getEmail() + ") ";
											}
										}
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
							&& !recommendationAddRequestDto.getReferenceId().toString().isEmpty()
							&& !recommendationAddRequestDto.getReferenceId().toString().isEmpty()) {

						Optional<Recommendation> rcmd = recommendationRepository
								.findByReferenceId(recommendationAddRequestDto.getReferenceId());

						if (checkIfRecommendationAlreadyExist(recommendationAddRequestDto).equals(Boolean.valueOf(true))) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Recommendation already reported",
									null);
						}

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
									if (recommendationAddRequestDto.getFile() != null) {
										recommendation.setFileName(
												recommendationAddRequestDto.getFile().getOriginalFilename());
									} else {
										recommendation.setFileUrl(null);
										recommendation.setFileName(null);
									}
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
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

//	private MultipartFile compressFile(MultipartFile file) throws IOException {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		ZipOutputStream zipOut = new ZipOutputStream(bos);
//		zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
//
//		zipOut.putNextEntry(new ZipEntry(file.getOriginalFilename()));
//
//		byte[] buffer = new byte[1024];
//		int bytesRead;
//
//		InputStream inputStream = new ByteArrayInputStream(file.getBytes());
//		while ((bytesRead = inputStream.read(buffer)) != -1) {
//			zipOut.write(buffer, 0, bytesRead);
//		}
//
//		zipOut.closeEntry();
//		zipOut.close();
//		inputStream.close();
//		MultipartFile multipartFile = convertToMultipartFile(bos, file.getOriginalFilename());
//
//		return multipartFile;
//	}

//	public static MultipartFile convertToMultipartFile(ByteArrayOutputStream byteArrayOutputStream, String fileName)
//			throws IOException {
//		InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//
//		DiskFileItemFactory factory = new DiskFileItemFactory();
//		FileItem fileItem = factory.createItem("file", "text/plain", true, fileName);
//		try (InputStream fileInputStream = inputStream) {
//			fileItem.getOutputStream().write(inputStream.re());
//		}
//
//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
//
//		return multipartFile;
//	}

	public Boolean checkIfRecommendationAlreadyExist(RecommendationAddRequestDto addRequestDto) {
		List<Recommendation> rcmds = recommendationRepository.findAllRecommendationsByData(addRequestDto);
		List<Long> departmentIds = addRequestDto.getDepartmentIds();
		List<Long> rcmdDepartmentIds = rcmds.stream().filter(e -> e.getDepartment() != null)
				.map(e -> e.getDepartment().getId()).collect(Collectors.toList());
		if (rcmdDepartmentIds.containsAll(departmentIds)) {
			return true;
		} else {
			return false;
		}
	}

	public Response<?> saveAllRecommendationsThroughExcel(List<RecommendationAddRequestDto> addRequestDtos, User user) {
		try {
			for (RecommendationAddRequestDto addRequestDto : addRequestDtos) {

				Recommendation savedRecommendation = new Recommendation();
				if (addRequestDto.getDepartmentIds() != null && addRequestDto.getDepartmentIds().size() > 0) {
					for (Long departmentId : addRequestDto.getDepartmentIds()) {
						Recommendation recommendation = new Recommendation();
						recommendation.setFileUrl(null);
						recommendation.setDocumentUrl(addRequestDto.getUrlLink());
						recommendation.setDescriptions(addRequestDto.getDescription());
						recommendation.setCreatedAt(new Date());
						recommendation.setRecommendDate(addRequestDto.getRecommendDate());
						recommendation.setCreatedBy(user);
						recommendation.setDepartment(new Department(departmentId));
						recommendation.setComponent(new Component(addRequestDto.getComponentId()));
						recommendation.setPriorityId(addRequestDto.getPriorityId());
						recommendation.setRecommendationType(new RecommendationType(addRequestDto.getTypeId()));
						recommendation.setRecommendationStatus(new RecommendationStatus(StatusEnum.Released.getId()));
						recommendation.setExpectedImpact(addRequestDto.getExpectedImpact());
						recommendation.setImpactedDepartment(addRequestDto.getImpactedDepartments());
						recommendation.setUserType(addRequestDto.getUserType());
						recommendation.setRecommendedBy(addRequestDto.getRecommendedBy());
						List<Recommendation> recommendList = recommendationRepository.findAll();

						Integer size = 0;
						if (recommendList != null && recommendList.size() > 0) {
							Collections.sort(recommendList, Comparator.comparing(Recommendation::getId).reversed());
							size = size + recommendList.get(0).getId().intValue();
						}
						String refId = generateReferenceId(size);
						recommendation.setIsAppOwnerApproved(true);
						recommendation.setIsAppOwnerRejected(false);
						recommendation.setIsAgmApproved(true);
						recommendation.setReferenceId(refId);
						recommendation.setUpdatedAt(new Date());
						savedRecommendation = recommendationRepository.save(recommendation);
						setTrailDataList(savedRecommendation, addRequestDto);

					}
				}
			}
			return new Response<>(HttpStatus.OK.value(), "Recommendations added successfully", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

//	public void saveTrialData(Recommendation recommendation, RecommendationAddRequestDto addRequestDto) {
//		List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
//		if (recommendation.getRecommendationStatus().getId() == StatusEnum.Released.getId()) {
//			statusList.remove(3);
//			for (RecommendationStatus status : statusList) {
//				RecommendationTrail trailData = new RecommendationTrail();
//				trailData.setCreatedAt(addRequestDto.getRecommendationReleasedDate());
//				trailData.setRecommendationStatus(new RecommendationStatus(status.getId()));
//				trailData.setReferenceId(recommendation.getReferenceId());
//				recommendationTrailRepository.save(trailData);
//			}
//		}
//		System.out.println("Trail list size, " + setTrailDataList(recommendation, addRequestDto).size());
//	}

	public void setTrailDataList(Recommendation recommendation, RecommendationAddRequestDto addRequestDto) {
		List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
		List<RecommendationTrail> trailList = new ArrayList<>();
		if (recommendation.getRecommendationStatus().getId() == StatusEnum.Released.getId()) {
			statusList.remove(3);
			for (RecommendationStatus status : statusList) {
				RecommendationTrail trailData = new RecommendationTrail();
				trailData.setCreatedAt(addRequestDto.getRecommendationReleasedDate());
				trailData.setUpdatedAt(new Date());
				trailData.setRecommendationStatus(new RecommendationStatus(status.getId()));
				trailData.setReferenceId(recommendation.getReferenceId());
				trailList.add(trailData);
			}
		}
		if (!trailList.isEmpty()) {
			recommendationTrailRepository.saveAll(trailList);
		}
//		return trailList;
	}

	public Response<?> validateExcelRecommendationData(List<RecommendationAddRequestDto> addRequestDtos,
			CredentialMaster master) {
		try {

			Set<String> uniqueEntries = new HashSet<>();
			for (RecommendationAddRequestDto dto : addRequestDtos) {

				if (master.getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					List<Long> departmentIds = dto.getDepartmentIds();

					if (departmentIds.size() > 1
							|| !Objects.equals(master.getUserId().getDepartment().getId(), departmentIds.get(0))) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"You can only add recommendations for your department.", null);
					}
				}

//				String description = dto.getDescription() != null ? dto.getDescription().toLowerCase().trim() : null;
//				Long typeId = dto.getTypeId();
//				Long priorityId = dto.getPriorityId();
//				Date recommendDate = dto.getRecommendDate();
//				Long componentId = dto.getComponentId();
//				String departmentIds = dto.getDepartmentIds().toString();
//				String entryString = description + "_" + typeId + "_" + priorityId + "_" + recommendDate + "_"
//						+ componentId + "_" + departmentIds;
//
//				// Same Recommendation with in the excel
//				if (!uniqueEntries.add(entryString)) {
//					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Duplicate Recommendation found", null);
//				}

				String description = dto.getDescription() != null ? dto.getDescription().toLowerCase().trim() : null;
				Long typeId = dto.getTypeId();
				Long priorityId = dto.getPriorityId();
				Date recommendDate = dto.getRecommendDate();
				Long componentId = dto.getComponentId();
				List<Long> departmentIdsList = dto.getDepartmentIds(); // Assuming departmentIds is a List<Long>

				// Iterate over each department ID and construct entry string
				for (Long departmentId : departmentIdsList) {
					String entryString = description + "_" + typeId + "_" + priorityId + "_" + recommendDate + "_"
							+ componentId + "_" + departmentId;

					// Same Recommendation within the excel
					if (!uniqueEntries.add(entryString)) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Duplicate Recommendation found", null);
					}
				}

				// If recommendation already exists in db
				if (checkIfRecommendationAlreadyExist(dto)) {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Duplicate recommendations found", null);
				}

				Response<?> commonComponentsResponse = departmentService
						.getCommonComponentsV2(new DepartmentListDto(dto.getDepartmentIds()));
				if (commonComponentsResponse.getResponseCode() == HttpStatus.OK.value()) {
					List<Component> components = (List<Component>) commonComponentsResponse.getData();
					if (!components.isEmpty()) {
						List<Long> componentIds = components.stream().map(Component::getId)
								.collect(Collectors.toList());
						if (!componentIds.contains(dto.getComponentId())) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(),
									"Component not mapped with department you provided for recommendation with description, "
											+ dto.getDescription(),
									null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Provide common component for the departments you provided for recommendation with description, "
										+ dto.getDescription(),
								null);
					}

				}

			}

			return new Response<>(HttpStatus.OK.value(), "success", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}

}
