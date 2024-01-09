package com.sbi.oem.dto;

public class DashboardResponseDto {

	private Long totalRecommendation;

	private Long pendingForApproval;

	private Long rejectedRecommendation;

	private Long approvedRecommendationsToBeImplement;

	private Long onTimeDoneRecommendationCount;

	private Long releasedRecommendations;

	private Long delayRecommendationsCount;

	private Long approvedRecommendationNotYetReleased;

	public Long getTotalRecommendation() {
		return totalRecommendation;
	}

	public void setTotalRecommendation(Long totalRecommendation) {
		this.totalRecommendation = totalRecommendation;
	}

	public Long getPendingForApproval() {
		return pendingForApproval;
	}

	public void setPendingForApproval(Long pendingForApproval) {
		this.pendingForApproval = pendingForApproval;
	}

	public Long getRejectedRecommendation() {
		return rejectedRecommendation;
	}

	public void setRejectedRecommendation(Long rejectedRecommendation) {
		this.rejectedRecommendation = rejectedRecommendation;
	}

	public Long getApprovedRecommendationsToBeImplement() {
		return approvedRecommendationsToBeImplement;
	}

	public void setApprovedRecommendationsToBeImplement(Long approvedRecommendationsToBeImplement) {
		this.approvedRecommendationsToBeImplement = approvedRecommendationsToBeImplement;
	}

	public Long getOnTimeDoneRecommendationCount() {
		return onTimeDoneRecommendationCount;
	}

	public void setOnTimeDoneRecommendationCount(Long onTimeDoneRecommendationCount) {
		this.onTimeDoneRecommendationCount = onTimeDoneRecommendationCount;
	}

	public Long getDelayRecommendationsCount() {
		return delayRecommendationsCount;
	}

	public void setDelayRecommendationsCount(Long delayRecommendationsCount) {
		this.delayRecommendationsCount = delayRecommendationsCount;
	}

	public Long getReleasedRecommendations() {
		return releasedRecommendations;
	}

	public void setReleasedRecommendations(Long releasedRecommendations) {
		this.releasedRecommendations = releasedRecommendations;
	}

	public Long getApprovedRecommendationNotYetReleased() {
		return approvedRecommendationNotYetReleased;
	}

	public void setApprovedRecommendationNotYetReleased(Long approvedRecommendationNotYetReleased) {
		this.approvedRecommendationNotYetReleased = approvedRecommendationNotYetReleased;
	}

}
