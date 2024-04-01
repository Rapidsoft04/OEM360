package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Company;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.User;

public class DepartmentDto {

	private Department department;

	private List<User> approvers;

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<User> getApprovers() {
		return approvers;
	}

	public void setApprovers(List<User> approvers) {
		this.approvers = approvers;
	}

	public DepartmentDto(Department department, List<User> approvers) {
		super();
		this.department = department;
		this.approvers = approvers;
	}

	public DepartmentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
