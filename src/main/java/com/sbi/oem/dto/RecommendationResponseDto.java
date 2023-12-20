package com.sbi.oem.dto;

import java.util.Date;
import java.util.List;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.RecommendationStatus;
import com.sbi.oem.model.RecommendationTrail;
import com.sbi.oem.model.RecommendationType;
import com.sbi.oem.model.User;

public class RecommendationResponseDto {

	private Long id;

	private String referenceId;

	private String descriptions;

	private RecommendationType recommendationType;

	private String priority;

	private Date recommendDate;

	private Department department;

	private Component component;

	private String expectedImpact;

	private String documentUrl;

	private String fileUrl;

	private Date createdAt;

	private User createdBy;

	private List<RecommendationTrail> trailData;

	private User approver;

	private User appOwner;

	private RecommendationStatus status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(String descriptions) {
		this.descriptions = descriptions;
	}

	public RecommendationType getRecommendationType() {
		return recommendationType;
	}

	public void setRecommendationType(RecommendationType recommendationType) {
		this.recommendationType = recommendationType;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Date getRecommendDate() {
		return recommendDate;
	}

	public void setRecommendDate(Date recommendDate) {
		this.recommendDate = recommendDate;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public String getExpectedImpact() {
		return expectedImpact;
	}

	public void setExpectedImpact(String expectedImpact) {
		this.expectedImpact = expectedImpact;
	}

	public String getDocumentUrl() {
		return documentUrl;
	}

	public void setDocumentUrl(String documentUrl) {
		this.documentUrl = documentUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
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

	public List<RecommendationTrail> getTrailData() {
		return trailData;
	}

	public void setTrailData(List<RecommendationTrail> trailData) {
		this.trailData = trailData;
	}

	public User getApprover() {
		return approver;
	}

	public void setApprover(User approver) {
		this.approver = approver;
	}

	public User getAppOwner() {
		return appOwner;
	}

	public void setAppOwner(User appOwner) {
		this.appOwner = appOwner;
	}

	public RecommendationStatus getStatus() {
		return status;
	}

	public void setStatus(RecommendationStatus status) {
		this.status = status;
	}

	public RecommendationResponseDto(Long id, String referenceId, String descriptions,
			RecommendationType recommendationType, Date recommendDate, Department department, Component component,
			String expectedImpact, String documentUrl, String fileUrl, Date createdAt, User createdBy,RecommendationStatus status) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.descriptions = descriptions;
		this.recommendationType = recommendationType;
		this.recommendDate = recommendDate;
		this.department = department;
		this.component = component;
		this.expectedImpact = expectedImpact;
		this.documentUrl = documentUrl;
		this.fileUrl = fileUrl;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.status=status;
	}

}
