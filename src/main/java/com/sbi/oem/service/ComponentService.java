package com.sbi.oem.service;

import com.sbi.oem.dto.AddComponentDto;
import com.sbi.oem.dto.Response;

public interface ComponentService {
	
	Response<?> saveComponent(AddComponentDto componentDto);
	
	Response<?> getAllByDepartmentId(Long departmentId);
	
	Response<?> getAllComponents();
	
	Response<?> getAllComponentWithDepartment();
}
