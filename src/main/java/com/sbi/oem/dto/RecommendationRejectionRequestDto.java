package com.sbi.oem.dto;

import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.User;

public class RecommendationRejectionRequestDto {

	private String referenceId;
	private User createdBy;
	private String rejectionMessage;
	private String addtionalInformation;

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

	public String getRejectionMessage() {
		return rejectionMessage;
	}

	public void setRejectionMessage(String rejectionMessage) {
		this.rejectionMessage = rejectionMessage;
	}

	public String getAddtionalInformation() {
		return addtionalInformation;
	}

	public void setAddtionalInformation(String addtionalInformation) {
		this.addtionalInformation = addtionalInformation;
	}

	public RecommendationMessages convertToEntity() {
		return new RecommendationMessages(this.referenceId != null ? this.referenceId : null,
				this.createdBy != null ? this.createdBy : null,
				this.rejectionMessage != null ? this.rejectionMessage : null,
				this.addtionalInformation != null ? this.addtionalInformation : null);
	}

}
