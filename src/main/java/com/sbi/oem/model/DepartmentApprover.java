package com.sbi.oem.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "department_approver")
public class DepartmentApprover {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "agm_id")
	private User agm;

	@OneToOne
	@JoinColumn(name = "dgm_id")
	private User dgm;

	@OneToOne
	@JoinColumn(name = "app_owner_id")
	private User applicationOwner;

	@OneToOne
	@JoinColumn(name = "department_id")
	private Department department;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getAgm() {
		return agm;
	}

	public void setAgm(User agm) {
		this.agm = agm;
	}

	public User getApplicationOwner() {
		return applicationOwner;
	}

	public void setApplicationOwner(User applicationOwner) {
		this.applicationOwner = applicationOwner;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public User getDgm() {
		return dgm;
	}

	public void setDgm(User dgm) {
		this.dgm = dgm;
	}

	public DepartmentApprover() {
		super();
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public DepartmentApprover(Long id, User agm, User applicationOwner, Boolean isActive) {
		super();
		this.id = id;
		this.agm = agm;
		this.applicationOwner = applicationOwner;
		this.isActive = isActive;
	}

}
