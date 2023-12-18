package com.sbi.oem.serviceImpl;

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
import com.sbi.oem.enums.UserType;
import com.sbi.oem.exception.UnauthorizedException;
import com.sbi.oem.model.CredentialMaster;
import com.sbi.oem.model.User;
import com.sbi.oem.repository.CredentialMasterRepository;
import com.sbi.oem.repository.UserRepository;
import com.sbi.oem.security.JwtTokenUtil;
import com.sbi.oem.security.JwtUserDetailsService;
import com.sbi.oem.service.UserService;

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

	@Override
	public LoginResponse login(LoginRequest loginRequest) throws Exception {
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
							loginResponse.setCompany(credentialMaster.getUserId().getCompany());
						}
					}

				} else {
					throw new UnauthorizedException("INVALID_CREDENTIALS");
				}
			}

			return loginResponse;
		} else {
			throw new Exception("Invalid Credential");
		}
	}

	@Override
	public Response<?> registerUser(SignUpRequest signUpRequest) {
		try {
			List<CredentialMaster> credentialMasterDBList = credentialMasterRepository
					.findAllByPhoneNoEmail(signUpRequest.getPhoneNo(), signUpRequest.getPhoneNo());
			for (CredentialMaster credentialMaster : credentialMasterDBList) {
				if (credentialMaster.getEmail() != null && credentialMaster.getPhoneNo() != null
						&& (credentialMaster.getEmail().equals(signUpRequest.getEmail())
								|| credentialMaster.getPhoneNo().equals(signUpRequest.getPhoneNo())))
					return new Response<>(HttpStatus.BAD_REQUEST.value(),
							"Email and phone number cannot be duplicate !!!", null);
			}
			CredentialMaster credentialMasterSave = new CredentialMaster(null, signUpRequest.getUserName(),
					UserType.OEM_SI, signUpRequest.getEmail(), signUpRequest.getPhoneNo(), null, null);
			credentialMasterSave.setPassword(credentialMasterSave.passwordEncoder(signUpRequest.getPassword()));
			User userDataSave = new User(null, signUpRequest.getUserName(), signUpRequest.getEmail(),
					signUpRequest.getPhoneNo(), true);
			userDataSave = userDataRepository.save(userDataSave);
			credentialMasterSave.setUserId(userDataSave);
			credentialMasterSave = credentialMasterRepository.save(credentialMasterSave);
			if (credentialMasterSave != null)
				return new Response<>(HttpStatus.OK.value(), "User Registered Succefully  !!!", credentialMasterSave);
			else
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Failed in User Registeration!!!", null);

		} catch (Exception e) {
			return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Register user service goes wrong.", null);
		}
	}

	@Override
	public Response<?> forgetPassword(ForgetPasswordRequestDto forgetPassword) {
		Optional<CredentialMaster> userData = credentialMasterRepository.findByEmail(forgetPassword.getEmail());
		if (userData != null && !userData.isEmpty()) {
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

}
