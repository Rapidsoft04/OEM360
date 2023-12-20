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
@Table(name = "recommendation_messages")
public class RecommendationMessages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ref_id")
	private String referenceId;

	@OneToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@Column(name = "additional_message")
	private String additionalMessage;

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

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getAdditionalMessage() {
		return additionalMessage;
	}

	public void setAdditionalMessage(String additionalMessage) {
		this.additionalMessage = additionalMessage;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public RecommendationMessages() {
		super();
	}

	public RecommendationMessages(Long id, String referenceId, User createdBy, String rejectionReason,
			String additionalMessage, Date createdAt) {
		super();
		this.id = id;
		this.referenceId = referenceId;
		this.createdBy = createdBy;
		this.rejectionReason = rejectionReason;
		this.additionalMessage = additionalMessage;
		this.createdAt = createdAt;
	}

	public RecommendationMessages(String referenceId, User createdBy, String rejectionReason,
			String additionalMessage) {
		super();
		this.referenceId = referenceId;
		this.createdBy = createdBy;
		this.rejectionReason = rejectionReason;
		this.additionalMessage = additionalMessage;
	}

}
