package com.sbi.oem.dto;

import java.util.List;

public class AddDepartmentDto {

	private String name;

	private String code;

	private List<Long> componentIds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Long> getComponentIds() {
		return componentIds;
	}

	public void setComponentIds(List<Long> componentIds) {
		this.componentIds = componentIds;
	}

	public AddDepartmentDto(String name, String code, List<Long> componentIds) {
		super();
		this.name = name;
		this.code = code;
		this.componentIds = componentIds;
	}

	public AddDepartmentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
