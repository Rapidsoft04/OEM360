package com.sbi.oem.service;

import com.sbi.oem.dto.AddDepartmentDto;
import com.sbi.oem.dto.DepartmentListDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.model.DepartmentApprover;

public interface DepartmentService {
	
	Response<?> saveDepartment(AddDepartmentDto addDepartmentDto);

	Response<?> getAllDepartmentByCompanyId(Long companyId);

	Response<?> getAllDepartmentWithComponent(Long companyId);
	
	Response<?> saveDepartmentApprover(DepartmentApprover departmentApprover);
	
	Response<?> getAllDepartmentApproverList();
	
	Response<?> getDepartmentApproverByDepartmentId(Long departmentId);
	
	Response<?> getCommonComponents(String departmentList);

	Response<?> getCommonComponentsV2(DepartmentListDto departmentListDto);

}
