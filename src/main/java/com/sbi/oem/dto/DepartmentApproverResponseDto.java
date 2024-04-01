package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.User;

public class DepartmentApproverResponseDto {

	private List<User> approverList;

	private List<UserType> userTypeList;

	private List<User> userList;

	public List<User> getApproverList() {
		return approverList;
	}

	public void setApproverList(List<User> approverList) {
		this.approverList = approverList;
	}

	public List<UserType> getUserTypeList() {
		return userTypeList;
	}

	public void setUserTypeList(List<UserType> userTypeList) {
		this.userTypeList = userTypeList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

}
