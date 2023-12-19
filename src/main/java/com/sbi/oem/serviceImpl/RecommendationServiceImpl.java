package com.sbi.oem.serviceImpl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.PriorityResponseDto;
import com.sbi.oem.dto.RecommendationAddRequestDto;
import com.sbi.oem.dto.RecommendationPageDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.Recommendation;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.repository.RecommendationRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
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
			Recommendation recommendation=new Recommendation();
			if(recommendationAddRequestDto.getFile()!=null) {
				String fileUrl = fileSystemStorageService.getUserExpenseFileUrl(recommendationAddRequestDto.getFile());
				if(fileUrl!=null && !fileUrl.isEmpty()) {
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
			List<Recommendation> recommendList=recommendationRepository.findAll();
			String refId=generateReferenceId(recommendList.size());
			recommendation.setReferenceId(refId);
			recommendationRepository.save(recommendation);
			return new Response<>(HttpStatus.CREATED.value(),"Recommendation created successfully.",refId);
		} catch (Exception e) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong.", null);
		}
	}
	
	public static String generateReferenceId(int size) {
		int year = Year.now().getValue();
		String refId="REF"+year+(size+1);
		return refId;
	}

	@Override
	public Response<?> viewRecommendation(String refId) {
		// TODO Auto-generated method stub
		return null;
	}

}
