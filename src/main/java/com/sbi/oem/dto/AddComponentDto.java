package com.sbi.oem.dto;

import java.util.List;

public class AddComponentDto {

	private String name;

	private List<Long> departmentIds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Long> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Long> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public AddComponentDto(String name, List<Long> departmentIds) {
		super();
		this.name = name;
		this.departmentIds = departmentIds;
	}

	public AddComponentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
