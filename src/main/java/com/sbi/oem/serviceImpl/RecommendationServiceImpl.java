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
import com.sbi.oem.dto.RecommendationPageDto;
import com.sbi.oem.dto.RecommendationResponseDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationStatusRepository;
import com.sbi.oem.repository.RecommendationTrailRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.repository.UserRepository;
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
	private UserRepository userRepository;

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
			Recommendation recommendation = new Recommendation();
			if (recommendationAddRequestDto.getFile() != null) {
				String fileUrl = fileSystemStorageService.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
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
			List<Recommendation> recommendList = recommendationRepository.findAll();
			String refId = generateReferenceId(recommendList.size());
			recommendation.setReferenceId(refId);
			recommendationRepository.save(recommendation);
			RecommendationTrail trailData = new RecommendationTrail();
			trailData.setCreatedAt(new Date());
			trailData.setRecommendationStatus(new RecommendationStatus(1L));
			trailData.setReferenceId(refId);
			recommendationTrailRepository.save(trailData);
			return new Response<>(HttpStatus.CREATED.value(), "Recommendation created successfully.", refId);
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
	}

	@Override
	public Response<?> getAllRecommendedStatus() {
		List<RecommendationStatus> statusList = recommendationStatusRepository.findAll();
		return new Response<>(HttpStatus.OK.value(), "Recommend status list.", statusList);
	}

	@Override
	public Response<?> getAllRecommendations() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> user = userRepository.findByEmail(auth.getName());
		List<DepartmentApprover> departmentList = departmentApproverRepository.findAllByUserId(user.get().getId());
		List<Long> departmentIds = departmentList.stream().filter(e -> e.getDepartment().getId() != null)
				.map(e -> e.getDepartment().getId()).collect(Collectors.toList());
		List<RecommendationResponseDto> responseDtos = new ArrayList<>();
		if (departmentIds != null && departmentIds.size() > 0) {
			List<Recommendation> recommendationList = recommendationRepository.findAllByDepartmentIdIn(departmentIds);

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
				List<RecommendationTrail> trailList = recommendationTrailRepository
						.findAllByReferenceId(responseDto.getReferenceId());
				responseDto.setTrailData(trailList);
				responseDtos.add(responseDto);
			}
			return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
		} else {
			return new Response<>(HttpStatus.OK.value(), "Recommendation List.", responseDtos);
		}
	}

}
