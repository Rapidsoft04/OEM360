package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Company;
import com.sbi.oem.model.Component;

public class DepartmentComponentDto {

	private Long id;
	private String name;
	private String code;
	private List<Component> componentList;
	private Company company;
	private Boolean isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public List<Component> getComponentList() {
		return componentList;
	}

	public void setComponentList(List<Component> componentList) {
		this.componentList = componentList;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public DepartmentComponentDto(Long id, String name, String code, List<Component> componentList, Company company,
			Boolean isActive) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
		this.componentList = componentList;
		this.company = company;
		this.isActive = isActive;
	}

	public DepartmentComponentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
