package com.sbi.oem.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.sbi.oem.enums.UserType;
import com.sbi.oem.model.RecommendationStatus;

public class RecommendationAddRequestDto {

	private String referenceId;
	private String description;
	private Long typeId;
	private Long priorityId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date recommendDate;
	private Long departmentId;
	private Long componentId;
	private MultipartFile file;
	private String urlLink;
	private Long createdBy;
	private String expectedImpact;
	private List<Long> departmentIds;
	private RecommendationStatus status;
	private Date recommendationReleasedDate;
	private UserType userType;
	private String recommendedBy;
	private String impactedDepartments;
	private String fileName;
	private String fileUrl;

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	public Long getPriorityId() {
		return priorityId;
	}

	public void setPriorityId(Long priorityId) {
		this.priorityId = priorityId;
	}

	public Date getRecommendDate() {
		return recommendDate;
	}

	public void setRecommendDate(Date recommendDate) {
		this.recommendDate = recommendDate;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Long getComponentId() {
		return componentId;
	}

	public void setComponentId(Long componentId) {
		this.componentId = componentId;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getUrlLink() {
		return urlLink;
	}

	public void setUrlLink(String urlLink) {
		this.urlLink = urlLink;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public String getExpectedImpact() {
		return expectedImpact;
	}

	public void setExpectedImpact(String expectedImpact) {
		this.expectedImpact = expectedImpact;
	}

	public List<Long> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Long> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public RecommendationStatus getStatus() {
		return status;
	}

	public void setStatus(RecommendationStatus status) {
		this.status = status;
	}

	public Date getRecommendationReleasedDate() {
		return recommendationReleasedDate;
	}

	public void setRecommendationReleasedDate(Date recommendationReleasedDate) {
		this.recommendationReleasedDate = recommendationReleasedDate;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getRecommendedBy() {
		return recommendedBy;
	}

	public void setRecommendedBy(String recommendedBy) {
		this.recommendedBy = recommendedBy;
	}

	public String getImpactedDepartments() {
		return impactedDepartments;
	}

	public void setImpactedDepartments(String impactedDepartments) {
		this.impactedDepartments = impactedDepartments;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

}
