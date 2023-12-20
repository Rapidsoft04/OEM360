package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.RecommendationType;

public class RecommendationPageDto {

	private List<RecommendationType> recommendationTypeList;
	private List<PriorityResponseDto> priorityList;
	private List<Department> departmentList;
	private List<Component> componentList;

	public List<RecommendationType> getRecommendationTypeList() {
		return recommendationTypeList;
	}

	public void setRecommendationTypeList(List<RecommendationType> recommendationTypeList) {
		this.recommendationTypeList = recommendationTypeList;
	}

	public List<PriorityResponseDto> getPriorityList() {
		return priorityList;
	}

	public void setPriorityList(List<PriorityResponseDto> priorityList) {
		this.priorityList = priorityList;
	}

	public List<Department> getDepartmentList() {
		return departmentList;
	}

	public void setDepartmentList(List<Department> departmentList) {
		this.departmentList = departmentList;
	}

	public List<Component> getComponentList() {
		return componentList;
	}

	public void setComponentList(List<Component> componentList) {
		this.componentList = componentList;
	}

}
