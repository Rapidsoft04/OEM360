package com.sbi.oem.serviceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.AddDepartmentApproverDto;
import com.sbi.oem.dto.DepartmentApproverResponseDto;
import com.sbi.oem.dto.DepartmentDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.CredentialMasterRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.DepartmentApproverService;
import com.sbi.oem.service.EmailTemplateService;
import com.sbi.oem.service.ValidationService;

@Service
public class DepartmentApproverServiceImpl implements DepartmentApproverService {

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private CredentialMasterRepository credentialMasterRepository;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@Override
	public Response<?> save(DepartmentApprover departmentApprover) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {

					Response<?> validateDepartmentApprover = validationService
							.checkForDepartmentApproverAddPayload(departmentApprover);
					if (validateDepartmentApprover.getResponseCode() == HttpStatus.OK.value()) {
						if (departmentApprover.getDepartment() != null
								&& departmentApprover.getDepartment().getId() != null) {

							if ((departmentApprover.getAgm() == null || departmentApprover.getAgm().getId() == null)
									&& (departmentApprover.getApplicationOwner() == null
											|| departmentApprover.getApplicationOwner().getId() == null)
									&& (departmentApprover.getDgm() == null
											|| departmentApprover.getDgm().getId() == null)) {
								return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide valid data",
										null);
							}

							if (departmentApprover.getAgm() != null) {
								Optional<DepartmentApprover> agm = departmentApproverRepository
										.findByAgmId(departmentApprover.getAgm().getId());
								if (agm != null && agm.isPresent()) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"AGM is already an approver for other department", null);
								}
//								Optional<User> user = userRepository.findById(departmentApprover.getAgm().getId());
//								Optional<CredentialMaster> agmCredential = credentialMasterRepository
//										.findByUserId(user.get().getId());
//								if (user != null && user.isPresent()) {
//									user.get().setUserType(UserType.AGM);
//									user.get().setDepartment(departmentApprover.getDepartment());
//									user.get().setUpdatedAt(new Date());
//									user.get().setUpdatedBy(master.get().getUserId().getId());
//									agmCredential.get().setUserTypeId(UserType.AGM);
//									userRepository.save(user.get());
//									credentialMasterRepository.save(agmCredential.get());
//								}
							}

							if (departmentApprover.getApplicationOwner() != null) {
								Optional<DepartmentApprover> appOwner = departmentApproverRepository
										.findByAppOwnerId(departmentApprover.getApplicationOwner().getId());
								if (appOwner != null && appOwner.isPresent()) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"App Owner is already an approver for other department", null);
								}
//								Optional<User> user = userRepository
//										.findById(departmentApprover.getApplicationOwner().getId());
//								Optional<CredentialMaster> appOwnerCredential = credentialMasterRepository
//										.findByUserId(user.get().getId());
//								if (user != null && user.isPresent()) {
//									user.get().setUserType(UserType.APPLICATION_OWNER);
//									user.get().setDepartment(departmentApprover.getDepartment());
//									user.get().setUpdatedAt(new Date());
//									user.get().setUpdatedBy(master.get().getUserId().getId());
//									appOwnerCredential.get().setUserTypeId(UserType.APPLICATION_OWNER);
//									userRepository.save(user.get());
//									credentialMasterRepository.save(appOwnerCredential.get());
//								}
							}

							if (departmentApprover.getDgm() != null) {
//								Optional<User> user = userRepository.findById(departmentApprover.getDgm().getId());
//								Optional<CredentialMaster> dgmCredential = credentialMasterRepository
//										.findByUserId(user.get().getId());
//								if (user != null && user.isPresent()) {
//									user.get().setUserType(UserType.DGM);
//									user.get().setDepartment(departmentApprover.getDepartment());
//									user.get().setUpdatedAt(new Date());
//									user.get().setUpdatedBy(master.get().getUserId().getId());
//									dgmCredential.get().setUserTypeId(UserType.DGM);
//									userRepository.save(user.get());
//									credentialMasterRepository.save(dgmCredential.get());
//								}
							}

							Optional<DepartmentApprover> department = departmentApproverRepository
									.findAllByDepartmentId(departmentApprover.getDepartment().getId());

							if (department != null && department.isPresent()) {
								return new Response<>(HttpStatus.BAD_REQUEST.value(), "Data already exist", null);
							}
							departmentApprover.setIsActive(true);
//							departmentApproverRepository.save(departmentApprover);
							return new Response<>(HttpStatus.CREATED.value(), "Success", null);
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department id", null);
						}
					} else {
						return validateDepartmentApprover;
					}

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
	public Response<?> save2(AddDepartmentApproverDto departmentApproverRequest) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {

					Response<?> validateDepartmentApprover = validationService
							.checkForDepartmentApproverAddPayloadV2(departmentApproverRequest);
					if (validateDepartmentApprover.getResponseCode() != HttpStatus.OK.value()) {
						return validateDepartmentApprover;
					}

					Optional<DepartmentApprover> departmentApproverExist = departmentApproverRepository
							.findAllByDepartmentId(departmentApproverRequest.getDepartmentId());
					Optional<User> user = userRepository.findById(departmentApproverRequest.getUserId());
					Optional<CredentialMaster> credentialMaster = credentialMasterRepository
							.findByUserId(departmentApproverRequest.getUserId());
					if (user.isPresent() && credentialMaster.isPresent()) {

						if (user.get().getUserType() != null
								&& (user.get().getUserType().name().equals(UserType.USER.name())
										|| user.get().getUserType().name().equals(UserType.DGM.name()))) {

							if (departmentApproverExist.isPresent()) {

								// Check if the role already exist in the department
								if (departmentApproverRequest.getUserType().equalsIgnoreCase(UserType.AGM.name())
										&& departmentApproverExist.get().getAgm() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(), "Agm Already exist.", null);
								} else if (departmentApproverRequest.getUserType()
										.equalsIgnoreCase(UserType.APPLICATION_OWNER.name())
										&& departmentApproverExist.get().getApplicationOwner() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"Application owner already exist.", null);
								} else if (departmentApproverRequest.getUserType().equalsIgnoreCase(UserType.DGM.name())
										&& departmentApproverExist.get().getDgm() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(), "DGM already exist.", null);
								}

								// Set the user as approver and update user and credential master in that
								// department
								if (departmentApproverExist.get().getAgm() == null && departmentApproverRequest
										.getUserType().equalsIgnoreCase(UserType.AGM.name())) {
									departmentApproverExist.get()
											.setAgm(new User(departmentApproverRequest.getUserId()));
									user.get().setUserType(UserType.AGM);
									user.get()
											.setDepartment(new Department(departmentApproverRequest.getDepartmentId()));
									credentialMaster.get().setUserTypeId(UserType.AGM);
								} else if (departmentApproverExist.get().getApplicationOwner() == null
										&& departmentApproverRequest.getUserType()
												.equalsIgnoreCase(UserType.APPLICATION_OWNER.name())) {
									departmentApproverExist.get()
											.setApplicationOwner(new User(departmentApproverRequest.getUserId()));
									user.get().setUserType(UserType.APPLICATION_OWNER);
									user.get()
											.setDepartment(new Department(departmentApproverRequest.getDepartmentId()));
									credentialMaster.get().setUserTypeId(UserType.APPLICATION_OWNER);
								} else if (departmentApproverExist.get().getDgm() == null && departmentApproverRequest
										.getUserType().equalsIgnoreCase(UserType.DGM.name())) {
									departmentApproverExist.get()
											.setDgm(new User(departmentApproverRequest.getUserId()));
									if (user.get().getUserType() == null) {
										user.get().setUserType(UserType.DGM);
										user.get().setDepartment(
												new Department(departmentApproverRequest.getDepartmentId()));
									}
									credentialMaster.get().setUserTypeId(UserType.DGM);
								}
								User updatedUser = userRepository.save(user.get());
								if (updatedUser != null) {
									emailTemplateService.sendMailForAssignRole(updatedUser);
								}
								credentialMasterRepository.save(credentialMaster.get());
								departmentApproverExist.get()
										.setCreatedAt(departmentApproverExist.get().getCreatedAt());
								departmentApproverExist.get().setUpdatedAt(new Date());
								departmentApproverRepository.save(departmentApproverExist.get());
								return new Response<>(HttpStatus.OK.value(), "success", null);

							} else {
								DepartmentApprover newDepartmentApprover = new DepartmentApprover();
								newDepartmentApprover
										.setDepartment(new Department(departmentApproverRequest.getDepartmentId()));
								newDepartmentApprover.setIsActive(true);
								newDepartmentApprover.setCreatedAt(new Date());
								newDepartmentApprover.setUpdatedAt(new Date());
								if (departmentApproverRequest.getUserType().equalsIgnoreCase(UserType.AGM.name())) {
									newDepartmentApprover.setAgm(new User(departmentApproverRequest.getUserId()));
									user.get().setUserType(UserType.AGM);
									user.get()
											.setDepartment(new Department(departmentApproverRequest.getDepartmentId()));
									credentialMaster.get().setUserTypeId(UserType.AGM);
								} else if (departmentApproverRequest.getUserType()
										.equalsIgnoreCase(UserType.DGM.name())) {
									newDepartmentApprover.setDgm(new User(departmentApproverRequest.getUserId()));
									if (user.get().getUserType() == null) {
										user.get().setUserType(UserType.DGM);
										user.get().setDepartment(
												new Department(departmentApproverRequest.getDepartmentId()));
									}
									credentialMaster.get().setUserTypeId(UserType.DGM);
								} else if (departmentApproverRequest.getUserType()
										.equalsIgnoreCase(UserType.APPLICATION_OWNER.name())) {
									newDepartmentApprover
											.setApplicationOwner(new User(departmentApproverRequest.getUserId()));
									user.get().setUserType(UserType.APPLICATION_OWNER);
									user.get()
											.setDepartment(new Department(departmentApproverRequest.getDepartmentId()));
									credentialMaster.get().setUserTypeId(UserType.APPLICATION_OWNER);
								}
								User updatedUser = userRepository.save(user.get());
								if (updatedUser != null) {
									emailTemplateService.sendMailForAssignRole(updatedUser);
								}
								credentialMasterRepository.save(credentialMaster.get());
								departmentApproverRepository.save(newDepartmentApprover);
								return new Response<>(HttpStatus.OK.value(), "success", null);
							}
						} else {
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "User already has a role", null);
						}
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "User does not exist", null);
					}

				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> getAllDataByDepartmentId(Long departmentId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {

					List<User> userList = userRepository.findAllUnAssignedUsers(departmentId);
					List<User> dgms = userRepository.findAllDgm(4l);
					Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
							.findAllByDepartmentId(departmentId);

					DepartmentApproverResponseDto approverDataDto = new DepartmentApproverResponseDto();
					List<UserType> userTypeList = new ArrayList<>();
					List<User> approverList = new ArrayList<>();
					if (!dgms.isEmpty()) {
						for (User user : dgms) {
							userList.add(user);
						}
					}

					if (departmentApprover != null && departmentApprover.isPresent()) {

						if (departmentApprover.get().getApplicationOwner() != null) {
							approverList.add(departmentApprover.get().getApplicationOwner());
						}
						if (departmentApprover.get().getAgm() != null) {
							approverList.add(departmentApprover.get().getAgm());
						}
						if (departmentApprover.get().getDgm() != null) {
							approverList.add(departmentApprover.get().getDgm());
						}

//						userTypeList.add(UserType.GM_IT_INFRA);
						if (departmentApprover.get().getAgm() == null) {
							userTypeList.add(UserType.AGM);
						}
						if (departmentApprover.get().getApplicationOwner() == null) {
							userTypeList.add(UserType.APPLICATION_OWNER);
						}
						if (departmentApprover.get().getDgm() == null) {
							userTypeList.add(UserType.DGM);
						}
					} else {
						userTypeList.add(UserType.APPLICATION_OWNER);
						userTypeList.add(UserType.AGM);
						userTypeList.add(UserType.DGM);
//						userTypeList.add(UserType.GM_IT_INFRA);
					}

					approverDataDto.setApproverList(approverList);
					approverDataDto.setUserTypeList(userTypeList);
					approverDataDto.setUserList(userList);
					return new Response<>(HttpStatus.OK.value(), "Department Approver Data", approverDataDto);
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
	public Response<?> getAllDepartmentApproverList() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())
						|| master.get().getUserTypeId().name().equals(UserType.GM_IT_INFRA.name())
						|| master.get().getUserTypeId().name().equals(UserType.DGM.name())) {

					List<User> userList = userRepository.findAll();
					if (!userList.isEmpty()) {
//						List<User> sortedUserList = userList.stream()
//								.sorted(Comparator.comparing(User::getUpdatedAt).reversed())
//								.collect(Collectors.toList());
						List<User> sortedUserList = userList.stream()
								.sorted(Comparator.nullsLast(Comparator.comparing(User::getUpdatedAt).reversed()))
								.collect(Collectors.toList());
						return new Response<>(HttpStatus.OK.value(), "User list", sortedUserList);
					}
					return new Response<>(HttpStatus.OK.value(), "No users", new ArrayList<>());

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
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
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
							return new Response<>(HttpStatus.OK.value(), "Department Approver", departmentDto);
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
	public Response<?> getUserTypeByDepartmentId(Long departmentId) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					if (departmentId != null) {
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(departmentId);

						List<UserType> userTypeList = new ArrayList<>();
						if (departmentApprover.isPresent()) {
							if (departmentApprover.get().getApplicationOwner() == null) {
								userTypeList.add(UserType.APPLICATION_OWNER);
							}
							if (departmentApprover.get().getAgm() == null) {
								userTypeList.add(UserType.AGM);
							}
							if (departmentApprover.get().getDgm() == null) {
								userTypeList.add(UserType.DGM);
							}
						}
						userTypeList.add(UserType.USER);

						return new Response<>(HttpStatus.OK.value(), "User types list", userTypeList);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department id", null);
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
}
