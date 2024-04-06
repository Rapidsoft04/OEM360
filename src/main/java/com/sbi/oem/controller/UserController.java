package com.sbi.oem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.oem.dto.ChangePasswordDto;
import com.sbi.oem.dto.ForgetPasswordRequestDto;
import com.sbi.oem.dto.LoginRequest;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.SignUpRequest;
import com.sbi.oem.service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/v1/signIn", method = RequestMethod.POST)
	public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest) throws Exception {

		Response<?> loginResponse = userService.login(loginRequest);

		return new ResponseEntity<>(loginResponse, HttpStatus.valueOf(loginResponse.getResponseCode()));

	}

	@RequestMapping(value = "/v1/signUp", method = RequestMethod.POST)
	public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) throws Exception {

		Response<?> customResponse = userService.registerUser(signUpRequest);

		return new ResponseEntity<>(customResponse, HttpStatus.valueOf(customResponse.getResponseCode()));

	}

	@PostMapping("/v1/forgetPassword")
	private ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordRequestDto forgetPassword) {
		Response<?> response = userService.forgetPassword(forgetPassword);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/v1/userTypes", method = RequestMethod.GET)
	public ResponseEntity<?> getAllUserTypes() {
		Response<?> response = userService.getAllUserTypes();
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@RequestMapping(value = "/update/password", method = RequestMethod.POST)
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
		Response<?> response = userService.updatePassword(changePasswordDto);
		return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
	}

	@RequestMapping(value = "/types/by/department/id", method = RequestMethod.GET)
	public ResponseEntity<?> getUserTypeByDepartmentId(@RequestParam("departmentId") Long departmentId) {
		try {
			Response<?> response = userService.getUserTypeByDepartmentId(departmentId);
			return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST.value(), "Something went wrong", null),
					HttpStatus.BAD_REQUEST);
		}
	}
}
