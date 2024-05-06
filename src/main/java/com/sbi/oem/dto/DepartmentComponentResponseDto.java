package com.sbi.oem.dto;

import java.util.List;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.DepartmentComponentMapping;

public class DepartmentComponentResponseDto {

	List<DepartmentComponentMapping> existingDepartmentComponentMappings;

	List<Component> unMappedComponents;

	public List<DepartmentComponentMapping> getExistingDepartmentComponentMappings() {
		return existingDepartmentComponentMappings;
	}

	public void setExistingDepartmentComponentMappings(
			List<DepartmentComponentMapping> existingDepartmentComponentMappings) {
		this.existingDepartmentComponentMappings = existingDepartmentComponentMappings;
	}

	public List<Component> getUnMappedComponents() {
		return unMappedComponents;
	}

	public void setUnMappedComponents(List<Component> unMappedComponents) {
		this.unMappedComponents = unMappedComponents;
	}

}
