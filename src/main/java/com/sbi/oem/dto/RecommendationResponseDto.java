package com.sbi.oem.dto;

import java.util.Date;
import java.util.List;

import com.sbi.oem.model.Component;
import com.sbi.oem.model.Department;
import com.sbi.oem.model.RecommendationDeplyomentDetails;
import com.sbi.oem.model.RecommendationMessages;
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

	private String impactedDepartment;

	private String documentUrl;

	private String fileUrl;

	private Date createdAt;

	private User createdBy;

	private List<RecommendationTrail> trailData;

	private User approver;

	private User appOwner;

	private RecommendationStatus status;

	private List<RecommendationMessages> messageList;

	private Boolean isAppOwnerApproved;

	private Boolean isAgmApproved;

	private Boolean isAppOwnerRejected;

	List<RecommendationResponseDto> pendingRecommendation;

	List<RecommendationResponseDto> approvedRecommendation;

	List<RecommendationResponseDto> recommendations;

	private RecommendationDeplyomentDetails recommendationDeploymentDetails;

	private List<RecommendationTrailResponseDto> trailResponse;

	private String pastExperienceComment;

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

	public List<RecommendationMessages> getMessageList() {
		return messageList;
	}

	public void setMessageList(List<RecommendationMessages> messageList) {
		this.messageList = messageList;
	}

	public Boolean getIsAppOwnerApproved() {
		return isAppOwnerApproved;
	}

	public void setIsAppOwnerApproved(Boolean isAppOwnerApproved) {
		this.isAppOwnerApproved = isAppOwnerApproved;
	}

	public Boolean getIsAgmApproved() {
		return isAgmApproved;
	}

	public void setIsAgmApproved(Boolean isAgmApproved) {
		this.isAgmApproved = isAgmApproved;
	}

	public Boolean getIsAppOwnerRejected() {
		return isAppOwnerRejected;
	}

	public void setIsAppOwnerRejected(Boolean isAppOwnerRejected) {
		this.isAppOwnerRejected = isAppOwnerRejected;
	}

	public String getImpactedDepartment() {
		return impactedDepartment;
	}

	public void setImpactedDepartment(String impactedDepartment) {
		this.impactedDepartment = impactedDepartment;
	}

	public List<RecommendationResponseDto> getPendingRecommendation() {
		return pendingRecommendation;
	}

	public void setPendingRecommendation(List<RecommendationResponseDto> pendingRecommendation) {
		this.pendingRecommendation = pendingRecommendation;
	}

	public List<RecommendationResponseDto> getApprovedRecommendation() {
		return approvedRecommendation;
	}

	public void setApprovedRecommendation(List<RecommendationResponseDto> approvedRecommendation) {
		this.approvedRecommendation = approvedRecommendation;
	}

	public List<RecommendationResponseDto> getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(List<RecommendationResponseDto> recommendations) {
		this.recommendations = recommendations;
	}

	public RecommendationDeplyomentDetails getRecommendationDeploymentDetails() {
		return recommendationDeploymentDetails;
	}

	public void setRecommendationDeploymentDetails(RecommendationDeplyomentDetails recommendationDeploymentDetails) {
		this.recommendationDeploymentDetails = recommendationDeploymentDetails;
	}

	public List<RecommendationTrailResponseDto> getTrailResponse() {
		return trailResponse;
	}

	public void setTrailResponse(List<RecommendationTrailResponseDto> trailResponse) {
		this.trailResponse = trailResponse;
	}

	public String getPastExperienceComment() {
		return pastExperienceComment;
	}

	public void setPastExperienceComment(String pastExperienceComment) {
		this.pastExperienceComment = pastExperienceComment;
	}

	public RecommendationResponseDto(Long id, String referenceId, String descriptions,
			RecommendationType recommendationType, Date recommendDate, Department department, Component component,
			String expectedImpact, String impactedDepartment, String documentUrl, String fileUrl, Date createdAt,
			User createdBy, RecommendationStatus status, Boolean isAppOwnerApproved, Boolean isAgmApproved,
			Boolean isAppOwnerRejected) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.descriptions = descriptions;
		this.recommendationType = recommendationType;
		this.recommendDate = recommendDate;
		this.department = department;
		this.component = component;
		this.expectedImpact = expectedImpact;
		this.impactedDepartment = impactedDepartment;
		this.documentUrl = documentUrl;
		this.fileUrl = fileUrl;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.status = status;
		this.isAppOwnerApproved = isAppOwnerApproved;
		this.isAgmApproved = isAgmApproved;
		this.isAppOwnerRejected = isAppOwnerRejected;
	}

	public RecommendationResponseDto() {
		super();
	}

}
