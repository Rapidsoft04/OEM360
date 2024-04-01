package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;

public class ComponentDto {

	private Component component;

	private List<Department> departmentList;

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public List<Department> getDepartmentList() {
		return departmentList;
	}

	public void setDepartmentList(List<Department> departmentList) {
		this.departmentList = departmentList;
	}

	public ComponentDto(Component component, List<Department> departmentList) {
		super();
		this.component = component;
		this.departmentList = departmentList;
	}

	public ComponentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
