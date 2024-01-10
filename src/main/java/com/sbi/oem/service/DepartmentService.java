package com.sbi.oem.service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.DepartmentApprover;

public interface DepartmentService {

	Response<?> getAllDepartmentByCompanyId(Long companyId);

	Response<?> saveDepartmentApprover(DepartmentApprover departmentApprover);
	
	Response<?> getAllDepartmentApproverList();
	
	Response<?> getDepartmentApproverByDepartmentId(Long departmentId);

}
