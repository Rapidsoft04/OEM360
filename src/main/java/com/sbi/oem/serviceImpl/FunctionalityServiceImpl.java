package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.FunctionalityDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.FunctionalityEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.Functionality;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.repository.FunctionalityRepository;
import com.sbi.oem.repository.RecommendationTypeRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.FunctionalityService;

@Service
public class FunctionalityServiceImpl implements FunctionalityService {

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private FunctionalityRepository functionalityRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ComponentRepository componentRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private RecommendationTypeRepository recommendationTypeRepository;

	@Override
	public Response<?> getAll() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					Map<List<String>, Integer> componentSummaryMap = new HashMap<>();
					List<String> componentSummaryList = new ArrayList<>();
					componentSummaryList.add("Component");
					componentSummaryList.add("Recommendation Type");
					componentSummaryList.add("Department");
					componentSummaryList.add("User");
					componentSummaryMap.put(componentSummaryList, componentSummaryList.size());
					return new Response<>(HttpStatus.OK.value(), "success", componentSummaryMap);
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

	@Override
	public Response<?> getAllComponentsList() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					List<Functionality> functionalityList = functionalityRepository.findAll();
					List<FunctionalityDto> functionalityDtos = new ArrayList<>();
					for (Functionality functionality : functionalityList) {
						FunctionalityDto functionalityDto = new FunctionalityDto();
						if (functionality.getId().equals(FunctionalityEnum.User.getId())) {
//							List<DepartmentApprover> departmentApprovers = departmentApproverRepository.findAll();
//							List<User> userList = new ArrayList<>();
//							if (!departmentApprovers.isEmpty()) {
//								for (DepartmentApprover approver : departmentApprovers) {
//									if (approver.getApplicationOwner() != null
//											&& !userList.contains(approver.getApplicationOwner())) {
//										userList.add(approver.getApplicationOwner());
//									}
//									if (approver.getAgm() != null && !userList.contains(approver.getAgm())) {
//										userList.add(approver.getAgm());
//									}
//									if (approver.getDgm() != null && !userList.contains(approver.getDgm())) {
//										userList.add(approver.getDgm());
//									}
//								}
//							}
							List<User> userList = userRepository.findAll();

							functionalityDto.setId(functionality.getId());
							functionalityDto.setName(FunctionalityEnum.User.getName());
							functionalityDto.setCount(userList.isEmpty() ? 0L : (long) userList.size());
							functionalityDto.setPath(functionality.getPath());
							functionalityDtos.add(functionalityDto);
						} else if (functionality.getTitleId().equals(FunctionalityEnum.Component.getId())) {
							List<Component> componentList = componentRepository.findAll();
							functionalityDto.setId(functionality.getId());
							functionalityDto.setName(FunctionalityEnum.Component.getName());
							functionalityDto.setCount(componentList.isEmpty() ? 0L : (long) componentList.size());
							functionalityDto.setPath(functionality.getPath());
							functionalityDtos.add(functionalityDto);
						} else if (functionality.getTitleId().equals(FunctionalityEnum.Department.getId())) {
							List<Department> departmentList = departmentRepository.findAll();
							functionalityDto.setId(functionality.getId());
							functionalityDto.setName(FunctionalityEnum.Department.getName());
							functionalityDto.setCount(departmentList.isEmpty() ? 0L : (long) departmentList.size());
							functionalityDto.setPath(functionality.getPath());
							functionalityDtos.add(functionalityDto);
						} else if (functionality.getTitleId().equals(FunctionalityEnum.RecommendationType.getId())) {
							List<RecommendationType> recommendationTypeList = recommendationTypeRepository.findAll();
							List<RecommendationType> filteredList = new ArrayList<>();
							if (!recommendationTypeList.isEmpty()) {
								filteredList = recommendationTypeList.stream()
										.filter(rt -> rt.getIsActive() != null && rt.getIsActive())
										.collect(Collectors.toList());
							}
							functionalityDto.setId(functionality.getId());
							functionalityDto.setName(FunctionalityEnum.RecommendationType.getName());
							functionalityDto.setCount(filteredList.isEmpty() ? 0L : (long) filteredList.size());
							functionalityDto.setPath(functionality.getPath());
							functionalityDtos.add(functionalityDto);
						}
					}
					return new Response<>(HttpStatus.OK.value(), "Functionality List", functionalityDtos);
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

}
