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
@Table(name = "recommendation_deployment_details")
public class RecommendationDeplyomentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recommend_ref_id")
	private String recommendRefId;

	@Column(name = "development_start_date")
	private Date developmentStartDate;

	@Column(name = "development_end_date")
	private Date developementEndDate;

	@Column(name = "test_completion_date")
	private Date testCompletionDate;

	@Column(name = "deployment_date")
	private Date deploymentDate;

	@Column(name = "impacted_department")
	private String impactedDepartment;

	@Column(name = "support_ref_number")
	private String globalSupportNumber;

	@Column(name = "created_at")
	private Date createdAt;

	@OneToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public RecommendationDeplyomentDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RecommendationDeplyomentDetails(Long id, String recommendRefId, Date developmentStartDate,
			Date developementEndDate, Date testCompletionDate, Date deploymentDate, String impactedDepartment,
			String globalSupportNumber, Date createdAt, User createdBy) {
		super();
		this.id = id;
		this.recommendRefId = recommendRefId;
		this.developmentStartDate = developmentStartDate;
		this.developementEndDate = developementEndDate;
		this.testCompletionDate = testCompletionDate;
		this.deploymentDate = deploymentDate;
		this.impactedDepartment = impactedDepartment;
		this.globalSupportNumber = globalSupportNumber;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
	}

	public RecommendationDeplyomentDetails(String recommendRefId, Date developmentStartDate, Date developementEndDate,
			Date testCompletionDate, Date deploymentDate, String impactedDepartment, String globalSupportNumber,
			User createdBy) {
		super();
		this.recommendRefId = recommendRefId;
		this.developmentStartDate = developmentStartDate;
		this.developementEndDate = developementEndDate;
		this.testCompletionDate = testCompletionDate;
		this.deploymentDate = deploymentDate;
		this.impactedDepartment = impactedDepartment;
		this.globalSupportNumber = globalSupportNumber;
		this.createdBy = createdBy;
	}

}
