package com.sbi.oem.service;

import com.sbi.oem.dto.AddDepartmentApproverDto;
import com.sbi.oem.dto.Response;
import com.sbi.oem.model.DepartmentApprover;

public interface DepartmentApproverService {

	Response<?> save(DepartmentApprover departmentApprover);
	
	Response<?> save2(AddDepartmentApproverDto departmentApprover);

	Response<?> getAllDataByDepartmentId(Long departmentId);

	Response<?> getAllDepartmentApproverList();

	Response<?> getDepartmentApproverByDepartmentId(Long departmentId);

}
