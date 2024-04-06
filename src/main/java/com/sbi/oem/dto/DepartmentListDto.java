package com.sbi.oem.dto;

import java.util.List;

public class DepartmentListDto {

	List<Long> departmentIds;

	public List<Long> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Long> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public DepartmentListDto(List<Long> departmentIds) {
		super();
		this.departmentIds = departmentIds;
	}

	public DepartmentListDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
