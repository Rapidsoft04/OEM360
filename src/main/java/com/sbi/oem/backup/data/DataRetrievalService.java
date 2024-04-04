package com.sbi.oem.backup.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sbi.oem.model.Department;
import com.sbi.oem.repository.DepartmentRepository;

@Service
public class DataRetrievalService {

	@Autowired
	private DepartmentRepository departmentRepository;

	private Map<String, Department> departmentMap = new HashMap<>();

	public Map<String, Department> getAllDepartmentsMap() {
		if (departmentMap.isEmpty()) {
			List<Department> departmentList = departmentRepository.findAll();
			if (!departmentList.isEmpty()) {
				for (Department department : departmentList) {
					departmentMap.put(department.getName().toUpperCase(), department);
				}
			}
		}
		return departmentMap;
	}

}