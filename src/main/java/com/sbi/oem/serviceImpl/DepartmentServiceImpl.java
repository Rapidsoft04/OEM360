package com.sbi.oem.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Department;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.service.DepartmentService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Override
	public Response<?> getAllDepartmentByCompanyId(Long companyId) {
		try {
			List<Department> departmentList = departmentRepository.findAllByCompanyId(companyId);
			return new Response<>(HttpStatus.OK.value(), "Department List.", departmentList);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}

	}

}
