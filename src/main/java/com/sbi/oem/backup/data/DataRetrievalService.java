package com.sbi.oem.backup.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	public Department findDepartmentByName(String departmentName) {
		try {
			Optional<Department> department = departmentRepository.findDepartmentByName(departmentName);
			if (department != null && department.isPresent()) {
				return department.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

	}

}