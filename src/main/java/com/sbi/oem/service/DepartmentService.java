package com.sbi.oem.service;

import com.sbi.oem.dto.Response;

public interface DepartmentService {

	Response<?> getAllDepartmentByCompanyId(Long companyId);

}
