package com.sbi.oem.dto;

public class SignUpRequest {

	private String email;
	private String password;
	private String userName;
	private String phoneNo;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public SignUpRequest() {
		super();
	}

	public SignUpRequest(String email, String password, String userName, String phoneNo) {
		super();
		this.email = email;
		this.password = password;
		this.userName = userName;
		this.phoneNo = phoneNo;
	}

}
