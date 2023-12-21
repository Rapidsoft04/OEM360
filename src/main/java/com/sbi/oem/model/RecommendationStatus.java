package com.sbi.oem.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "recommendation_status")
public class RecommendationStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "status_name")
	private String statusName;

	@Column(name = "is_active")
	private Boolean isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public RecommendationStatus(Long id, String statusName, Boolean isActive) {
		super();
		this.id = id;
		this.statusName = statusName;
		this.isActive = isActive;
	}

	public RecommendationStatus() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public RecommendationStatus(Long id) {
		super();
		this.id = id;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	public RecommendationStatus(String statusName) {
		super();
		this.statusName = statusName;
	}

	
}
