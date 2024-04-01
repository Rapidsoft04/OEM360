package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.DepartmentComponentDto;
import com.sbi.oem.dto.DepartmentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.FunctionalityEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.DepartmentComponentMapping;
import com.sbi.oem.model.Functionality;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.DepartmentComponentMappingRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.repository.FunctionalityRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.DepartmentService;
import com.sbi.oem.service.ValidationService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private DepartmentComponentMappingRepository componentMappingRepository;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private FunctionalityRepository functionalityRepository;

	@Override
	public Response<?> saveDepartment(AddDepartmentDto addDepartmentDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					Response<?> response = validationService.checkForDepartmentAddPayload(addDepartmentDto);
					if (response.getResponseCode() != HttpStatus.OK.value()) {
						return response;
					}

					Optional<Department> departmentExist = departmentRepository
							.findDepartmentByName(addDepartmentDto.getName(), addDepartmentDto.getCode());
					Optional<Functionality> functionality = functionalityRepository
							.findByTitleId(FunctionalityEnum.Department.getId());
					if (!departmentExist.isPresent()) {
						Department department = new Department();
						department.setName(addDepartmentDto.getName());
						department.setCode(addDepartmentDto.getCode());
						department.setIsActive(true);
						department.setCompany(master.get().getUserId().getCompany());
						department.setCreatedAt(new Date());
						department.setUpdatedAt(new Date());
						Department savedDepartment = departmentRepository.save(department);

						if (savedDepartment != null) {
							if (functionality.isPresent()) {
								Long currentCount = functionality.get().getCount() != null
										? functionality.get().getCount()
										: 0L;
								functionality.get().setCount(currentCount + 1L);
								functionalityRepository.save(functionality.get());
							}
						}

						List<Long> componentIds = addDepartmentDto.getComponentIds();
						for (Long componentId : componentIds) {
							DepartmentComponentMapping departmentComponentMapping = new DepartmentComponentMapping();
							departmentComponentMapping.setComponent(new Component(componentId));
							departmentComponentMapping.setDepartment(new Department(savedDepartment.getId()));
							departmentComponentMapping.setUpdatedAt(new Date());
							departmentComponentMapping.setUpdatedAt(new Date());
							componentMappingRepository.save(departmentComponentMapping);
						}
						return new Response<>(HttpStatus.OK.value(), "success", null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Department already exists", null);
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
	public Response<?> getAllDepartmentByCompanyId(Long companyId) {
		try {

			List<Department> departmentList = departmentRepository.findAllByCompanyId(companyId);
			return new Response<>(HttpStatus.OK.value(), "Department List", departmentList);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}

	}

	@Override
	public Response<?> saveDepartmentApprover(DepartmentApprover departmentApprover) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {
					Optional<DepartmentApprover> agm = departmentApproverRepository
							.findByAgmId(departmentApprover.getAgm().getId());
					Optional<DepartmentApprover> appOwner = departmentApproverRepository
							.findByAppOwnerId(departmentApprover.getApplicationOwner().getId());
					Optional<DepartmentApprover> department = departmentApproverRepository
							.findAllByDepartmentId(departmentApprover.getDepartment().getId());

					if (agm != null && agm.isPresent()) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"AGM is already an approver for other department", null);
					}
					if (appOwner != null && appOwner.isPresent()) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"App Owner is already an approver for other department", null);
					}
					if (department != null && department.isPresent()) {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Data already exist", null);
					}
					departmentApprover.setIsActive(true);
					departmentApproverRepository.save(departmentApprover);
					return new Response<>(HttpStatus.CREATED.value(), "Success", null);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			}
			return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

//	@Override
//	public Response<?> getAllDepartmentApproverList() {
//		try {
//			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
//			if (master.isPresent()) {
//				if (master.get().getUserTypeId() == UserType.SUPER_ADMIN) {
//					List<DepartmentApprover> departmentApprovers = departmentApproverRepository.findAll();
//					Map<String, List<User>> departmentApproverMap = new HashMap<>();
//					for (DepartmentApprover departmentApprover : departmentApprovers) {
//						addToUserTypeList(departmentApproverMap, "Application_owner",
//								departmentApprover.getApplicationOwner());
//						addToUserTypeList(departmentApproverMap, "Agm", departmentApprover.getAgm());
//						addToUserTypeList(departmentApproverMap, "Dgm", departmentApprover.getDgm());
//					}
//					return new Response<>(HttpStatus.OK.value(), "success", departmentApproverMap);
//				} else if (master.get().getUserTypeId() == UserType.DGM) {
//					List<DepartmentApprover> departmentApprovers = departmentApproverRepository.findAll();
//					Map<String, List<User>> departmentApproverMap = new HashMap<>();
//					for (DepartmentApprover departmentApprover : departmentApprovers) {
//						addToUserTypeList(departmentApproverMap, "Application_owner",
//								departmentApprover.getApplicationOwner());
//						addToUserTypeList(departmentApproverMap, "Agm", departmentApprover.getAgm());
//					}
//					return new Response<>(HttpStatus.OK.value(), "success", departmentApproverMap);
//				} else {
//					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
//				}
//			}
//			return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
//		}
//	}

//	private void addToUserTypeList(Map<String, List<User>> usersByUserType, String userType, User user) {
//		if (user != null) {
//			List<User> userList = usersByUserType.getOrDefault(userType, new ArrayList<>());
//			userList.add(user);
//			usersByUserType.put(userType, userList);
//		}
//	}

	@Override
	public Response<?> getAllDepartmentApproverList() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (master.get().getUserTypeId() == UserType.SUPER_ADMIN) {
					List<DepartmentApprover> departmentApprovers = departmentApproverRepository.findAll();
					return new Response<>(HttpStatus.OK.value(), "success", departmentApprovers);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			}
			return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> getDepartmentApproverByDepartmentId(Long departmentId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (master.get().getUserTypeId() == UserType.SUPER_ADMIN) {
					if (departmentId != null) {
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(departmentId);
						if (departmentApprover != null && departmentApprover.isPresent()) {
							DepartmentDto departmentDto = new DepartmentDto();
							departmentDto.setDepartment(departmentApprover.get().getDepartment());
							List<User> departmentApproverList = new ArrayList<>();
							departmentApproverList.add(departmentApprover.get().getApplicationOwner());
							departmentApproverList.add(departmentApprover.get().getAgm());
							departmentApproverList.add(departmentApprover.get().getDgm());
							departmentDto.setApprovers(departmentApproverList);
							return new Response<>(HttpStatus.OK.value(), "Department Approver List", departmentDto);
						}
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Data not exist", null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department Id", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> getAllDepartmentWithComponent(Long companyId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (master.get().getUserTypeId() == UserType.SUPER_ADMIN
						|| master.get().getUserTypeId() == UserType.APPLICATION_OWNER) {
					List<Department> departmentList = departmentRepository.findAllByCompanyId(companyId);
					List<DepartmentComponentDto> list = new ArrayList<>();
					if (!departmentList.isEmpty()) {
						for (Department department : departmentList) {
							DepartmentComponentDto departmentComponentDto = new DepartmentComponentDto();
							List<DepartmentComponentMapping> componentMappings = componentMappingRepository
									.findAllByDepartmentId(department.getId());
							List<Component> components = new ArrayList<>();
							if (!componentMappings.isEmpty()) {
								components = componentMappings.stream().map(DepartmentComponentMapping::getComponent)
										.collect(Collectors.toList());
							}
							departmentComponentDto.setId(department.getId());
							departmentComponentDto.setName(department.getName());
							departmentComponentDto.setCompany(department.getCompany());
							departmentComponentDto.setCode(department.getCode());
							departmentComponentDto.setComponentList(components);
							departmentComponentDto.setIsActive(department.getIsActive());
							list.add(departmentComponentDto);
						}
					}
					return new Response<>(HttpStatus.OK.value(), "Department List", list);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorize", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}
}
