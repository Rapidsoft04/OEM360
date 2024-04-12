package com.sbi.oem.service;

import com.sbi.oem.dto.AddDepartmentComponentMapDto;
import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.DepartmentListDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.dto.UpdateDepartmentComponentMapRequestDto;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.model.DepartmentComponentMapping;

public interface DepartmentService {
	
	Response<?> saveDepartment(AddDepartmentDto addDepartmentDto);

	Response<?> getAllDepartmentByCompanyId(Long companyId);

	Response<?> getAllDepartmentWithComponent(Long companyId);
	
	Response<?> saveDepartmentApprover(DepartmentApprover departmentApprover);
	
	Response<?> getAllDepartmentApproverList();
	
	Response<?> getDepartmentApproverByDepartmentId(Long departmentId);
	
	Response<?> getCommonComponents(String departmentList);

	Response<?> getCommonComponentsV2(DepartmentListDto departmentListDto);
	
	Response<?> getDepartmentComponentDataToMapNewComponents(Long departmentId);
	
	Response<?> updateDepartmentComponent(UpdateDepartmentComponentMapRequestDto requestDto);
	
	Response<?> departmentComponentMapSave(AddDepartmentComponentMapDto requestDto);

}
