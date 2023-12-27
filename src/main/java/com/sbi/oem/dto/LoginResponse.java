package com.sbi.oem.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.sbi.oem.model.Company;

public class LoginResponse {

	private Long id;
	@NotNull(message = "email can not be blank")
	@Email
	private String email;

	private String userType;

	private String userName;

	@NotNull(message = "token can not be blank")
	private String token;

	private Company company;

	private String imageUrl;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
