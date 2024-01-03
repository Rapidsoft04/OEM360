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

import org.springframework.format.annotation.DateTimeFormat;

import com.sbi.oem.dto.RecommendationResponseDto;

@Entity
@Table(name = "recommendation")
public class Recommendation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ref_id")
	private String referenceId;

	@Column(name = "descriptions")
	private String descriptions;

	@OneToOne
	@JoinColumn(name = "type_id")
	private RecommendationType recommendationType;

	@Column(name = "priority_id")
	private Long priorityId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "recommend_date")
	private Date recommendDate;

	@OneToOne
	@JoinColumn(name = "department_id")
	private Department department;

	@OneToOne
	@JoinColumn(name = "component_id")
	private Component component;

	@Column(name = "expected_impact")
	private String expectedImpact;

	@Column(name = "impacted_department")
	private String impactedDepartment;

	@Column(name = "document_url")
	private String documentUrl;

	@Column(name = "file_url")
	private String fileUrl;

	@Column(name = "created_at")
	private Date createdAt;

	@OneToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@OneToOne
	@JoinColumn(name = "status_id")
	private RecommendationStatus recommendationStatus;

	@Column(name = "is_app_owner_approve")
	private Boolean isAppOwnerApproved;

	@Column(name = "is_agm_approve")
	private Boolean isAgmApproved;

	@Column(name = "is_app_owner_rejected")
	private Boolean isAppOwnerRejected;

	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "is_agm_rejected")
	private Boolean isAgmRejected;

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

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public RecommendationStatus getRecommendationStatus() {
		return recommendationStatus;
	}

	public void setRecommendationStatus(RecommendationStatus recommendationStatus) {
		this.recommendationStatus = recommendationStatus;
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

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getImpactedDepartment() {
		return impactedDepartment;
	}

	public void setImpactedDepartment(String impactedDepartment) {
		this.impactedDepartment = impactedDepartment;
	}

	public Boolean getIsAgmRejected() {
		return isAgmRejected;
	}

	public void setIsAgmRejected(Boolean isAgmRejected) {
		this.isAgmRejected = isAgmRejected;
	}

	public Recommendation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Recommendation(Long id, String referenceId, String descriptions, RecommendationType recommendationType,
			Long priorityId, Date recommendDate, Department department, Component component, String expectedImpact,
			String documentUrl, Date createdAt, User createdBy) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.descriptions = descriptions;
		this.recommendationType = recommendationType;
		this.priorityId = priorityId;
		this.recommendDate = recommendDate;
		this.department = department;
		this.component = component;
		this.expectedImpact = expectedImpact;
		this.documentUrl = documentUrl;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
	}

	public Recommendation(Long id, String referenceId, String descriptions, RecommendationType recommendationType,
			Date recommendDate, Department department, Component component, String expectedImpact, String documentUrl,
			String fileUrl, Date createdAt, User createdBy) {
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
	}

	public RecommendationResponseDto convertToDto() {

		return new RecommendationResponseDto(this.id != null ? this.id : null,
				this.referenceId != null ? this.referenceId : null,
				this.descriptions != null ? this.descriptions : null,
				this.recommendationType != null ? this.recommendationType : null,
				this.recommendDate != null ? this.recommendDate : null,
				this.department != null ? this.department : null, this.component != null ? this.component : null,
				this.expectedImpact != null ? this.expectedImpact : null,
				this.impactedDepartment != null ? this.impactedDepartment : null,
				this.documentUrl != null ? this.documentUrl : null, this.fileUrl != null ? this.fileUrl : null,
				this.createdAt != null ? this.createdAt : null, this.createdBy != null ? this.createdBy : null,
				this.recommendationStatus != null ? this.recommendationStatus : null,
				this.isAppOwnerApproved != null ? this.isAppOwnerApproved : null,
				this.isAgmApproved != null ? this.isAgmApproved : null,
				this.isAppOwnerRejected != null ? this.isAppOwnerRejected : null);
	}

}
