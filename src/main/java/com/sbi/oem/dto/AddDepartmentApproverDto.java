package com.sbi.oem.dto;

public class AddDepartmentApproverDto {

	private Long userId;

	private Long departmentId;

	private String userType;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public AddDepartmentApproverDto(Long userId, Long departmentId, String userType) {
		super();
		this.userId = userId;
		this.departmentId = departmentId;
		this.userType = userType;
	}

	public AddDepartmentApproverDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
