package com.sbi.oem.service;

import com.sbi.oem.dto.ForgetPasswordRequestDto;
import com.sbi.oem.dto.LoginRequest;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SignUpRequest;

public interface UserService {

	Response<?> login(LoginRequest loginRequest) throws Exception;

	Response<?> registerUser(SignUpRequest signUpRequest);

	Response<?> forgetPassword(ForgetPasswordRequestDto forgetPassword);

}
