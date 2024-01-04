package com.sbi.oem.dto;

import com.sbi.oem.model.RecommendationMessages;
import com.sbi.oem.model.User;

public class RecommendationRejectionRequestDto {

	private String recommendRefId;
	private User createdBy;
	private String rejectionMessage;
	private String addtionalInformation;

	public String getRecommendRefId() {
		return recommendRefId;
	}

	public void setRecommendRefId(String recommendRefId) {
		this.recommendRefId = recommendRefId;
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
		return new RecommendationMessages(this.recommendRefId != null ? this.recommendRefId : null,
				this.createdBy != null ? this.createdBy : null,
				this.rejectionMessage != null ? this.rejectionMessage : null,
				this.addtionalInformation != null ? this.addtionalInformation : null);
	}

}
