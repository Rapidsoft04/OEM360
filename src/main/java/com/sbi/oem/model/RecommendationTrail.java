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

import com.sbi.oem.dto.RecommendationTrailResponseDto;

@Entity
@Table(name = "recommendation_trail")
public class RecommendationTrail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ref_id")
	private String referenceId;

	@OneToOne
	@JoinColumn(name = "status_id")
	private RecommendationStatus recommendationStatus;

	@Column(name = "created_at")
	private Date createdAt;

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

	public RecommendationTrail() {
		super();
	}

	public RecommendationTrail(Long id, String referenceId, RecommendationStatus recommendationStatus, Date createdAt) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.recommendationStatus = recommendationStatus;
		this.createdAt = createdAt;
	}

	public RecommendationTrailResponseDto convertToDto() {
		return new RecommendationTrailResponseDto(this.id != null ? this.id : null,
				this.referenceId != null ? this.referenceId : null,
				this.recommendationStatus != null ? this.recommendationStatus : null,
				this.createdAt != null ? this.createdAt : null);
	}

}
