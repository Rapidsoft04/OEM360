package com.sbi.oem.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sbi.oem.dto.Response;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.DepartmentApprover;
import com.sbi.oem.repository.DepartmentApproverRepository;
import com.sbi.oem.repository.DepartmentRepository;
import com.sbi.oem.service.DepartmentService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private DepartmentApproverRepository departmentApproverRepository;

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

	@Override
	public Response<?> saveDepartmentApprover(DepartmentApprover departmentApprover) {
		try {
			Optional<DepartmentApprover> agm = departmentApproverRepository
					.findByAgmId(departmentApprover.getAgm().getId());
			Optional<DepartmentApprover> appOwner = departmentApproverRepository
					.findByAppOwnerId(departmentApprover.getApplicationOwner().getId());
			Optional<DepartmentApprover> department = departmentApproverRepository
					.findAllByDepartmentId(departmentApprover.getDepartment().getId());

			if (agm != null && agm.isPresent()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "AGM is already an approver for other department",
						null);
			}
			if (appOwner != null && appOwner.isPresent()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(),
						"App Owner is already an approver for other department", null);
			}
			if(department != null && department.isPresent()) {
				return new Response<>(HttpStatus.BAD_REQUEST.value(),
						"Data already exist", null);
			}
			departmentApprover.setIsActive(true);
			departmentApproverRepository.save(departmentApprover);
			return new Response<>(HttpStatus.CREATED.value(), "Success", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> getAllDepartmentApproverList() {
		try {
			List<DepartmentApprover> list = departmentApproverRepository.findAll();
			return new Response<>(HttpStatus.OK.value(), "success", list);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}

	@Override
	public Response<?> getDepartmentApproverByDepartmentId(Long departmentId) {
		try {
			if (departmentId != null) {
				Optional<DepartmentApprover> departmentApprover = departmentApproverRepository
						.findAllByDepartmentId(departmentId);
				if (departmentApprover != null && departmentApprover.isPresent()) {
					return new Response<>(HttpStatus.OK.value(), "Success", departmentApprover.get());
				}
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Data not exist", null);
			} else {
				return new Response<>(HttpStatus.BAD_REQUEST.value(), "Please provide department Id", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Response<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
		}
	}
}
