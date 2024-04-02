package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.AddComponentDto;
import com.sbi.oem.dto.ComponentDepartmentDto;
import com.sbi.oem.dto.ComponentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentComponentMapping;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentComponentMappingRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.ComponentService;
import com.sbi.oem.service.ValidationService;

@Service
public class ComponentServiceImpl implements ComponentService {

	@Autowired
	private ValidationService validationService;

	@Autowired
	private ComponentRepository componentRepository;

	@Autowired
	private DepartmentComponentMappingRepository componentMappingRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Override
	public Response<?> saveComponent(AddComponentDto componentDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					Response<?> response = validationService.checkForComponentAddPayload(componentDto);
					if (response.getResponseCode() != HttpStatus.OK.value()) {
						return response;
					}

					Optional<Component> componentExist = componentRepository
							.findComponentByName(componentDto.getName().trim());
					if (!componentExist.isPresent()) {
						Component component = new Component();
						component.setName(componentDto.getName());
						component.setCompany(master.get().getUserId().getCompany());
						component.setIsActive(true);
						component.setCreatedAt(new Date());
						component.setUpdatedAt(new Date());
						Component savedComponent = componentRepository.save(component);

						List<Long> departmentIds = componentDto.getDepartmentIds();
						if (!departmentIds.isEmpty()) {
							for (Long departmentId : departmentIds) {
								DepartmentComponentMapping departmentComponentMapping = new DepartmentComponentMapping();
								departmentComponentMapping.setComponent(savedComponent);
								departmentComponentMapping.setDepartment(new Department(departmentId));
								departmentComponentMapping.setCreatedAt(new Date());
								departmentComponentMapping.setUpdatedAt(new Date());
								componentMappingRepository.save(departmentComponentMapping);
							}
						}
						return new Response<>(HttpStatus.OK.value(), "success", null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Component already exist.", null);
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

	@Override
	public Response<?> getAllByDepartmentId(Long departmentId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					if (departmentId != null) {
						List<DepartmentComponentMapping> componentMappings = componentMappingRepository
								.findAllByDepartmentId(departmentId);
						List<Component> componentList = new ArrayList<>();
						if (!componentMappings.isEmpty()) {
							for (DepartmentComponentMapping componentMapping : componentMappings) {
								if (componentMapping.getComponent() != null) {
									componentList.add(componentMapping.getComponent());
								}
							}
						}
						return new Response<>(HttpStatus.OK.value(), "Component List", componentList);
					}
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department id", null);
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
	public Response<?> getAllComponents() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					List<Component> componentList = componentRepository.findAll();
					List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository.findAll();
					List<ComponentDto> componentDtos = new ArrayList<>();
					for (Component component : componentList) {
						List<Department> departments = new ArrayList<>();
						ComponentDto componentDto = new ComponentDto();
						componentDto.setComponent(component);
						for (DepartmentComponentMapping componentMapping : departmentComponentMappings) {
							if (component.getId().equals(componentMapping.getComponent().getId())) {
								departments.add(componentMapping.getDepartment());
								componentDto.setDepartmentList(departments);
							}
						}
						componentDtos.add(componentDto);
					}
					return new Response<>(HttpStatus.OK.value(), "Component List", componentDtos);
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
	public Response<?> getAllComponentWithDepartment() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					List<Component> componentList = componentRepository
							.findAllByCompanyId(master.get().getUserId().getCompany().getId());
					List<ComponentDepartmentDto> list = new ArrayList<>();
					if (!componentList.isEmpty()) {
						for (Component component : componentList) {
							ComponentDepartmentDto componentDepartmentDto = new ComponentDepartmentDto();
							List<DepartmentComponentMapping> componentMappings = componentMappingRepository
									.findAllByComponentId(component.getId());
							List<Department> departmentList = new ArrayList<>();
							if (componentMappings != null) {
								departmentList = componentMappings.stream()
										.map(DepartmentComponentMapping::getDepartment).collect(Collectors.toList());
							}
							componentDepartmentDto.setId(component.getId());
							componentDepartmentDto.setName(component.getName());
							componentDepartmentDto.setCompany(component.getCompany());
							componentDepartmentDto.setDepartmentList(departmentList);
							componentDepartmentDto.setIsActive(component.getIsActive());
							list.add(componentDepartmentDto);
						}
					}

					return new Response<>(HttpStatus.OK.value(), "Component List", list);
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
