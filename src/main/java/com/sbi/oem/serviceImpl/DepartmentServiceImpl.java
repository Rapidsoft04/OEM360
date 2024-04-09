package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.DepartmentComponentDto;
import com.sbi.oem.dto.DepartmentDto;
import com.sbi.oem.dto.DepartmentListDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.Component;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.DepartmentComponentMapping;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.ComponentRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.DepartmentComponentMappingRepository;
import com.sbi.oem.repository.DepartmentRepository;
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
	private ComponentRepository componentRepository;

	@Autowired
	private ValidationService validationService;

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
							.findDepartmentByName(addDepartmentDto.getName().trim(), addDepartmentDto.getCode());

					List<Component> components = componentRepository.findAll();
					List<Long> componentIds = addDepartmentDto.getComponentIds();

					Set<Long> componentIdSet = new HashSet<>();
					for (Component component : components) {
						componentIdSet.add(component.getId());
					}

					for (Long componentId : componentIds) {
						if (!componentIdSet.contains(componentId)) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Invalid Component Selected", null);
						}
					}

					if (!departmentExist.isPresent()) {
						Department department = new Department();
						department.setName(addDepartmentDto.getName());
						department.setCode(addDepartmentDto.getCode());
						department.setIsActive(true);
						department.setCompany(master.get().getUserId().getCompany());
						department.setCreatedAt(new Date());
						department.setUpdatedAt(new Date());
						Department savedDepartment = departmentRepository.save(department);

						for (Long componentId : componentIds) {
							DepartmentComponentMapping departmentComponentMapping = new DepartmentComponentMapping();
							departmentComponentMapping.setComponent(new Component(componentId));
							departmentComponentMapping.setDepartment(new Department(savedDepartment.getId()));
							departmentComponentMapping.setCreatedAt(new Date());
							departmentComponentMapping.setUpdatedAt(new Date());
							componentMappingRepository.save(departmentComponentMapping);
						}
						return new Response<>(HttpStatus.OK.value(), "Department Added Successfully", null);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(),
								"Department already exists with same name or same code", null);
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
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())
						|| master.get().getUserTypeId().name().equals(UserType.OEM_SI.name())) {
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
				} else if (master.get().getUserTypeId().name().equals(UserType.APPLICATION_OWNER.name())) {
					List<DepartmentComponentDto> list = new ArrayList<>();
					Department department = master.get().getUserId().getDepartment() != null
							? master.get().getUserId().getDepartment()
							: null;
					if (department != null) {
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

//	@Override
//	public Response<?> getCommonComponents(DepartmentListDto departmentListDto) {
//		try {
//			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
//			if (master.isPresent()) {
//				if (!departmentListDto.getDepartmentIds().isEmpty()) {
//					List<Component> componentList = new ArrayList<>();
//					if (departmentListDto.getDepartmentIds().size() == 1) {
//						List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository
//								.findAllByDepartmentId(departmentListDto.getDepartmentIds().get(0));
//						if (!departmentComponentMappings.isEmpty()) {
//							componentList = departmentComponentMappings.stream()
//									.map(DepartmentComponentMapping::getComponent).collect(Collectors.toList());
//						}
//					} else {
//
////						List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository
////								.findCommonComponents(departmentListDto.getDepartmentIds(),
////										departmentListDto.getDepartmentIds().size());
////						if (!departmentComponentMappings.isEmpty()) {
////							componentList = departmentComponentMappings.stream()
////									.map(DepartmentComponentMapping::getComponent).collect(Collectors.toList());
////						}
//						componentList = componentMappingRepository.findCommonComponents(
//								departmentListDto.getDepartmentIds(), (long)departmentListDto.getDepartmentIds().size());
//					}
//					return new Response<>(HttpStatus.OK.value(), "Component list", componentList);
//				} else {
//					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid department.", null);
//				}
//			} else {
//				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorize", null);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
//		}
//	}

	@Override
	public Response<?> getCommonComponents(String departmentList) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				// Split the input string into a list of department IDs
				if (departmentList == null || departmentList.isEmpty() || departmentList.trim().isEmpty()) {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid department", null);
				}
				String[] departmentIdsArray = departmentList.split("\\s*,\\s*");
				List<Long> departmentIds = new ArrayList<>();
				for (String departmentIdStr : departmentIdsArray) {
					if (!departmentIdStr.trim().isEmpty()) {
						try {
							long departmentId = Long.parseLong(departmentIdStr.trim());
							departmentIds.add(departmentId);
						} catch (NumberFormatException ex) {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Invalid department ID format", null);
						}
					}
				}

				if (!departmentIds.isEmpty()) {
					List<Component> componentList = new ArrayList<>();
					if (departmentIds.size() == 1) {
						List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository
								.findAllByDepartmentId(departmentIds.get(0));
						if (!departmentComponentMappings.isEmpty()) {
							componentList = departmentComponentMappings.stream()
									.map(DepartmentComponentMapping::getComponent).collect(Collectors.toList());
						}
					} else {
						componentList = componentMappingRepository.findCommonComponents(departmentIds,
								(long) departmentIds.size());
					}
					return new Response<>(HttpStatus.OK.value(), "Component list", componentList);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid department.", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}
	}

	@Override
	public Response<?> getCommonComponentsV2(DepartmentListDto departmentListDto) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (!departmentListDto.getDepartmentIds().isEmpty()) {
					List<Component> componentList = new ArrayList<>();
					if (departmentListDto.getDepartmentIds().size() == 1) {
						List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository
								.findAllByDepartmentId(departmentListDto.getDepartmentIds().get(0));
						if (!departmentComponentMappings.isEmpty()) {
							componentList = departmentComponentMappings.stream()
									.map(DepartmentComponentMapping::getComponent).collect(Collectors.toList());
						}
					} else {

//						List<DepartmentComponentMapping> departmentComponentMappings = componentMappingRepository
//								.findCommonComponents(departmentListDto.getDepartmentIds(),
//										departmentListDto.getDepartmentIds().size());
//						if (!departmentComponentMappings.isEmpty()) {
//							componentList = departmentComponentMappings.stream()
//									.map(DepartmentComponentMapping::getComponent).collect(Collectors.toList());
//						}
						componentList = componentMappingRepository.findCommonComponents(
								departmentListDto.getDepartmentIds(),
								(long) departmentListDto.getDepartmentIds().size());
					}
					return new Response<>(HttpStatus.OK.value(), "Component list", componentList);
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid department.", null);
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
