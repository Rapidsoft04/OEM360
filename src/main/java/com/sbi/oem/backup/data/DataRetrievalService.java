package com.sbi.oem.backup.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sbi.oem.enums.PriorityEnum;
import com.sbi.oem.model.Department;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.serviceImpl.RecommendationServiceImpl;

@Service
public class DataRetrievalService {

	@Autowired
	private DepartmentRepository departmentRepository;

	private Map<String, Department> departmentMap = new HashMap<>();

	public static Map<Long, String> priorityMap = new HashMap<>();

	public static void setPriorityMap(Map<Long, String> priorityMap) {
		Map<Long, String> newMap = new HashMap<>();
		newMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.getName());
		newMap.put(PriorityEnum.Medium.getId().longValue(), PriorityEnum.Medium.getName());
		newMap.put(PriorityEnum.Low.getId().longValue(), PriorityEnum.Low.getName());
		RecommendationServiceImpl.priorityMap = newMap;
	}

	public  String getPriority(Long priorityId) {

		if (priorityMap != null && priorityMap.containsKey(priorityId)) {
			return priorityMap.get(priorityId);
		} else {
			String priority = "";
			if (priorityId == 1) {
				priority = PriorityEnum.High.getName();
				priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
				return priority;
			} else if (priorityId == 2) {
				priority = PriorityEnum.Medium.getName();
				priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
				return priority;
			} else {
				priority = PriorityEnum.Low.getName();
				priorityMap.put(PriorityEnum.High.getId().longValue(), PriorityEnum.High.name());
				return priority;
			}
		}
	}

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