package com.sbi.oem.serviceImpl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.ForgetPasswordRequestDto;
import com.sbi.oem.dto.LoginRequest;
import com.sbi.oem.dto.LoginResponse;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SignUpRequest;
import com.sbi.oem.enums.FunctionalityEnum;
import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.CompanyWisePastDateConfiguration;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.Functionality;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.CompanyWisePastDateConfigurationRepository;
import com.sbi.oem.repository.CredentialMasterRepository;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.FunctionalityRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.security.JwtTokenUtil;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.EmailTemplateService;
import com.sbi.oem.service.UserService;
import com.sbi.oem.service.ValidationService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private CredentialMasterRepository credentialMasterRepository;

	@Autowired
	private UserRepository userDataRepository;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private CompanyWisePastDateConfigurationRepository companyWisePastDateConfigurationRepository;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

	@Autowired
	private FunctionalityRepository functionalityRepository;

	@Override
	public Response<?> login(LoginRequest loginRequest) throws Exception {
		try {
			LoginResponse loginResponse = new LoginResponse();

			UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

			if (userDetails != null) {

				Optional<CredentialMaster> credentialMasterOptional = credentialMasterRepository
						.findByEmail(loginRequest.getUsername());

				if (credentialMasterOptional.isPresent()) {

					CredentialMaster credentialMaster = credentialMasterOptional.get();

					if (credentialMaster.passwordMatches(loginRequest.getPassword())) {
						for (UserType userType : UserType.values()) {
							if (credentialMaster.getUserTypeId().name().equalsIgnoreCase(userType.name())) {
								loginResponse.setId(credentialMaster.getId());
								loginResponse.setEmail(credentialMaster.getEmail());
								loginResponse.setUserName(credentialMaster.getName());
								loginResponse.setUserType(credentialMaster.getUserTypeId().name());
								loginResponse.setToken(jwtTokenUtil.generateToken(userDetails));
								loginResponse.setImageUrl(credentialMaster.getUserId().getUserLogoUrl());
								loginResponse.setCompany(credentialMaster.getUserId().getCompany());
							}
						}
						Optional<CompanyWisePastDateConfiguration> companyWiseUserPastDateConfiguratioObj = companyWisePastDateConfigurationRepository
								.findByCompany(loginResponse.getCompany().getId());
						if (companyWiseUserPastDateConfiguratioObj != null
								&& companyWiseUserPastDateConfiguratioObj.isPresent()) {
							loginResponse.setHasAccessToUpdateForPastDate(true);
						} else {
							loginResponse.setHasAccessToUpdateForPastDate(false);
						}
						return new Response<>(HttpStatus.OK.value(), "Login success.", loginResponse);
					} else {
						return new Response<>(HttpStatus.BAD_REQUEST.value(), "INVALID CREDENTIALS", null);
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "INVALID CREDENTIALS", null);
				}

			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "INVALID CREDENTIALS", null);
			}
		} catch (Exception e) {
			return new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null);
		}

	}

	@Override
	public Response<?> registerUser(SignUpRequest signUpRequest) {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				if (master.get().getUserTypeId().name().equals(UserType.SUPER_ADMIN.name())) {
					Response<?> validationSignUpRequest = validationService.checkForUserAddPayload(signUpRequest);
					if (validationSignUpRequest.getResponseCode() == HttpStatus.OK.value()) {
						List<CredentialMaster> credentialMasterDBList = credentialMasterRepository
								.findAllByPhoneNoEmail(signUpRequest.getPhoneNo().trim(), signUpRequest.getEmail().trim());
						for (CredentialMaster credentialMaster : credentialMasterDBList) {
							if (credentialMaster.getEmail() != null && credentialMaster.getPhoneNo() != null
									&& (credentialMaster.getEmail().toLowerCase()
											.equals(signUpRequest.getEmail().toLowerCase())
											|| credentialMaster.getPhoneNo().equals(signUpRequest.getPhoneNo())))
								return new Response<>(HttpStatus.BAD_REQUEST.value(),
										"Email and phone number cannot be duplicate !!!", null);
						}
//						signUpRequest.setPassword(generateRandomPassword());
						signUpRequest.setPassword("Rst@2023");
						List<UserType> userTypeList = Arrays.asList(UserType.values());
						UserType userType = null;
						for (UserType user : userTypeList) {
							if (signUpRequest.getUserType() != null && !signUpRequest.getUserType().isEmpty()
									&& signUpRequest.getUserType().toLowerCase().trim()
											.equals(user.toString().toLowerCase().trim())) {
								userType = user;
							}
						}

						if (userType == null) {
							userType = UserType.USER;
						}

						DepartmentApprover approver = new DepartmentApprover();
						Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
								.findAllByDepartmentId(signUpRequest.getDepartmentId());
						if (userType != null) {
							if (departmentApprover != null && departmentApprover.isPresent()) {
								if (userType.name().equals(UserType.APPLICATION_OWNER.name())
										&& departmentApprover.get().getApplicationOwner() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(),
											"Application owner already exists", null);
								} else if (userType.name().equals(UserType.AGM.name())
										&& departmentApprover.get().getAgm() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(), "Agm already exists", null);
								} else if (userType.name().equals(UserType.DGM.name())
										&& departmentApprover.get().getDgm() != null) {
									return new Response<>(HttpStatus.BAD_REQUEST.value(), "DGM already exists", null);
								}
							}
						}

						Department department = signUpRequest.getDepartmentId() != null
								? new Department(signUpRequest.getDepartmentId())
								: null;

						CredentialMaster credentialMasterSave = new CredentialMaster(null, signUpRequest.getUserName(),
								userType, signUpRequest.getEmail(), signUpRequest.getPhoneNo(), null, null);
						credentialMasterSave
								.setPassword(credentialMasterSave.passwordEncoder(signUpRequest.getPassword()));
						User userDataSave = new User(null, signUpRequest.getUserName(), signUpRequest.getEmail(),
								signUpRequest.getPhoneNo(), userType, signUpRequest.getDesignation(), department,
								master.get().getUserId().getCompany(), true);
						userDataSave.setCreatedAt(new Date());
						userDataSave.setUpdatedAt(new Date());
						userDataSave.setCreatedBy(master.get().getUserId().getId());
						userDataSave.setUpdatedBy(master.get().getUserId().getId());
						userDataSave = userDataRepository.save(userDataSave);
						credentialMasterSave.setUserId(userDataSave);
						credentialMasterSave = credentialMasterRepository.save(credentialMasterSave);
//						emailTemplateService.sendMailForRegisterUser(signUpRequest);

						if (departmentApprover != null && !userType.name().equals(UserType.USER.name())) {
							if (userType.name().equals(UserType.AGM.name())) {
								departmentApprover.get().setAgm(userDataSave);
							} else if (userType.name().equals(UserType.APPLICATION_OWNER.name())) {
								departmentApprover.get().setApplicationOwner(userDataSave);
							} else if (userType.name().equals(UserType.DGM.name())) {
								departmentApprover.get().setDgm(userDataSave);
							}
							departmentApprover.get().setCreatedAt(departmentApprover.get().getCreatedAt());
							departmentApprover.get().setUpdatedAt(new Date());
							departmentApprover.get().setIsActive(departmentApprover.get().getIsActive());
						} else {
							if (userType != null && (userType.name().equals(UserType.AGM.name())
									|| userType.name().equals(UserType.APPLICATION_OWNER.name())
									|| userType.name().equals(UserType.DGM.name()))) {
								approver.setIsActive(true);
								if (userType.name().equals(UserType.AGM.name())) {
									approver.setAgm(userDataSave);
								} else if (userType.name().equals(UserType.APPLICATION_OWNER.name())) {
									approver.setApplicationOwner(userDataSave);
								} else if (userType.name().equals(UserType.DGM.name())) {
									approver.setDgm(userDataSave);
								}
								departmentApproverRepository.save(approver);
							}
						}

						if (credentialMasterSave != null)
							return new Response<>(HttpStatus.OK.value(),
									"User Added Succefully. Credentials will be sent through email!!!",
									credentialMasterSave);
						else
							return new Response<>(HttpStatus.BAD_REQUEST.value(), "Failed in User Registration!!!",
									null);
					} else {
						return validationSignUpRequest;
					}
				} else {
					return new Response<>(HttpStatus.BAD_REQUEST.value(), "You have no access", null);
				}

			} else {
				return new Response<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Register user service goes wrong.", null);
		}
	}

	@Override
	public Response<?> forgetPassword(ForgetPasswordRequestDto forgetPassword) {
		Optional<CredentialMaster> userData = credentialMasterRepository.findByEmail(forgetPassword.getEmail());
		if (userData != null && userData.isPresent()) {
			if (forgetPassword.getPassword().equals(forgetPassword.getRetypePassword())) {
				userData.get().setPassword(userData.get().passwordEncoder(forgetPassword.getPassword()));
			}

		}
		Response<?> response = new Response<>();
		credentialMasterRepository.save(userData.get());
		response.setMessage("Password Reset Succesful..");
		response.setResponseCode(HttpStatus.OK.value());
		return response;
	}

	@Override
	public Response<?> getAllUserTypes() {
		try {
			Optional<CredentialMaster> master = userDetailsService.getUserDetails();
			if (master != null && master.isPresent()) {
				List<UserType> userTypeList = new ArrayList<>(Arrays.asList(UserType.values()));
				if (master.get().getUserTypeId().equals(UserType.SUPER_ADMIN)) {
					Iterator<UserType> iterator = userTypeList.iterator();
					while (iterator.hasNext()) {
						UserType userType = iterator.next();
						if (userType.equals(UserType.SUPER_ADMIN) || userType.equals(UserType.GM_IT_INFRA)
								|| userType.equals(UserType.OEM_SI) || userType.equals(UserType.VENDOR)) {
							iterator.remove();
						}
					}
					return new Response<>(HttpStatus.OK.value(), "User type list", userTypeList);
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

	public static String generateRandomPassword() {

		final String ALPHA_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		final String NUMERIC_CHARACTERS = "0123456789";
		final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[{]}|;:,<.>/?";

		StringBuilder passwordBuilder = new StringBuilder();
		SecureRandom random = new SecureRandom();

		// Generate 3 random characters
		for (int i = 0; i < 3; i++) {
			int index = random.nextInt(ALPHA_CHARACTERS.length());
			passwordBuilder.append(ALPHA_CHARACTERS.charAt(index));
		}

		// Generate 2 random numbers
		for (int i = 0; i < 2; i++) {
			int index = random.nextInt(NUMERIC_CHARACTERS.length());
			passwordBuilder.append(NUMERIC_CHARACTERS.charAt(index));
		}

		// Generate 1 random special character
		int specialCharIndex = random.nextInt(SPECIAL_CHARACTERS.length());
		passwordBuilder.append(SPECIAL_CHARACTERS.charAt(specialCharIndex));

		// Shuffle the password to randomize the order of characters
		String password = passwordBuilder.toString();
		char[] passwordArray = password.toCharArray();
		for (int i = passwordArray.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			char temp = passwordArray[index];
			passwordArray[index] = passwordArray[i];
			passwordArray[i] = temp;
		}

		return new String(passwordArray);
	}
}
