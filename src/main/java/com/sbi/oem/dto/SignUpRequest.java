package com.sbi.oem.dto;

public class SignUpRequest {

	private String email;
	private String password;
	private String userName;
	private String phoneNo;
	private String userType;
	private String designation;
	private Long departmentId;

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

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public SignUpRequest() {
		super();
	}

	public SignUpRequest(String email, String password, String userName, String phoneNo, String userType,
			String designation, Long departmentId) {
		super();
		this.email = email;
		this.password = password;
		this.userName = userName;
		this.phoneNo = phoneNo;
		this.userType = userType;
		this.designation = designation;
		this.departmentId = departmentId;
	}

}
