package com.sbi.oem.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.sbi.oem.model.Department;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.User;

public class RecommendationDetailsRequestDto {

	private Long id;

	private String recommendRefId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date developmentStartDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date developementEndDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date testCompletionDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date deploymentDate;

	private String impactedDepartment;

	private String globalSupportNumber;

	private User createdBy;

	private String description;

	private RecommendationStatus recommendationStatus;

	private List<Department> departmentList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRecommendRefId() {
		return recommendRefId;
	}

	public void setRecommendRefId(String recommendRefId) {
		this.recommendRefId = recommendRefId;
	}

	public Date getDevelopmentStartDate() {
		return developmentStartDate;
	}

	public void setDevelopmentStartDate(Date developmentStartDate) {
		this.developmentStartDate = developmentStartDate;
	}

	public Date getDevelopementEndDate() {
		return developementEndDate;
	}

	public void setDevelopementEndDate(Date developementEndDate) {
		this.developementEndDate = developementEndDate;
	}

	public Date getTestCompletionDate() {
		return testCompletionDate;
	}

	public void setTestCompletionDate(Date testCompletionDate) {
		this.testCompletionDate = testCompletionDate;
	}

	public Date getDeploymentDate() {
		return deploymentDate;
	}

	public void setDeploymentDate(Date deploymentDate) {
		this.deploymentDate = deploymentDate;
	}

	public String getImpactedDepartment() {
		return impactedDepartment;
	}

	public void setImpactedDepartment(String impactedDepartment) {
		this.impactedDepartment = impactedDepartment;
	}

	public String getGlobalSupportNumber() {
		return globalSupportNumber;
	}

	public void setGlobalSupportNumber(String globalSupportNumber) {
		this.globalSupportNumber = globalSupportNumber;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RecommendationStatus getRecommendationStatus() {
		return recommendationStatus;
	}

	public void setRecommendationStatus(RecommendationStatus recommendationStatus) {
		this.recommendationStatus = recommendationStatus;
	}

	public List<Department> getDepartmentList() {
		return departmentList;
	}

	public void setDepartmentList(List<Department> departmentList) {
		this.departmentList = departmentList;
	}

	public RecommendationDeplyomentDetails convertToEntity() {
		return new RecommendationDeplyomentDetails(this.recommendRefId, this.developmentStartDate,
				this.developementEndDate, this.testCompletionDate, this.deploymentDate, this.impactedDepartment,
				this.globalSupportNumber, this.createdBy);
	}
}
