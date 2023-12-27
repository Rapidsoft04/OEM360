package com.sbi.oem.dto;

import java.util.Date;

import com.sbi.oem.model.RecommendationStatus;

public class RecommendationTrailResponseDto {

	private Long id;

	private String referenceId;

	private RecommendationStatus recommendationStatus;

	private Date createdAt;

	private Boolean isStatusDone;

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

	public RecommendationStatus getRecommendationStatus() {
		return recommendationStatus;
	}

	public void setRecommendationStatus(RecommendationStatus recommendationStatus) {
		this.recommendationStatus = recommendationStatus;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getIsStatusDone() {
		return isStatusDone;
	}

	public void setIsStatusDone(Boolean isStatusDone) {
		this.isStatusDone = isStatusDone;
	}

	public RecommendationTrailResponseDto(Long id, String referenceId, RecommendationStatus recommendationStatus,
			Date createdAt) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.recommendationStatus = recommendationStatus;
		this.createdAt = createdAt;
	}

}
