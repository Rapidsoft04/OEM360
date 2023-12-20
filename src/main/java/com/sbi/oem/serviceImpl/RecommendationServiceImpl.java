package com.sbi.oem.serviceImpl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.PriorityResponseDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationDetailsRequestDto;
import com.sbi.oem.dto.RecommendationPageDto;
import com.sbi.oem.dto.RecommendationRejectionRequestDto;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.Response;
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

//	@Autowired
//	private UserRepository userRepository;

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
			RecommendationPageDto recommendationPageDto = new RecommendationPageDto();
			List<RecommendationType> recommendationList = recommendationTypeRepository.findAllByCompanyId(companyId);
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
				recommendation.setRecommendationType(new RecommendationType(recommendationAddRequestDto.getTypeId()));
				recommendation.setRecommendationStatus(new RecommendationStatus(1L));
				List<Recommendation> recommendList = recommendationRepository.findAll();
				String refId = generateReferenceId(recommendList.size());
				recommendation.setReferenceId(refId);
				Recommendation savedRecommendation = recommendationRepository.save(recommendation);

				RecommendationTrail trailData = new RecommendationTrail();
				trailData.setCreatedAt(new Date());
				trailData.setRecommendationStatus(new RecommendationStatus(1L));
				trailData.setReferenceId(refId);
				recommendationTrailRepository.save(trailData);
				notificationService.save(savedRecommendation, RecommendationStatusEnum.CREATED);
				emailTemplateService.sendMail(savedRecommendation);
				return new Response<>(HttpStatus.CREATED.value(), "Recommendation created successfully.", refId);
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
			List<RecommendationResponseDto> responseDtos = new ArrayList<>();
			if (master.get().getUserTypeId().name() != UserType.OEM_SI.name()) {
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
						responseDtos.add(responseDto);
					}
					return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
				} else {
					return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
				}
			} else {
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
					responseDto.setTrailData(trailList);
					responseDtos.add(responseDto);
				}
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
					recommendationRepository.save(recommendation.get());
					RecommendationTrail trail = new RecommendationTrail();
					trail.setCreatedAt(new Date());
					trail.setRecommendationStatus(new RecommendationStatus(2L));
					trail.setReferenceId(details.getRecommendRefId());
					recommendationTrailRepository.save(trail);

					notificationService.save(recommendation.get(), RecommendationStatusEnum.APPROVED_BY_APPOWNER);

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

				return new Response<>(HttpStatus.OK.value(), "Recommendation rejected successfully.", null);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access to reject.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}

	}

}
