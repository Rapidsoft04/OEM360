package com.sbi.oem.dto;

public class DashboardResponseDto {

	private Long totalRecommendation;

	private Long pendingForApproval;

	private Long rejectedRecommendation;

	private Long approvedRecommendationsToBeImplement;

	private Long implementationDoneRecommendations;

	private Long releasedRecommendations;

	private Long testingDoneRecommendations;

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

	public Long getImplementationDoneRecommendations() {
		return implementationDoneRecommendations;
	}

	public void setImplementationDoneRecommendations(Long implementationDoneRecommendations) {
		this.implementationDoneRecommendations = implementationDoneRecommendations;
	}

	public Long getReleasedRecommendations() {
		return releasedRecommendations;
	}

	public void setReleasedRecommendations(Long releasedRecommendations) {
		this.releasedRecommendations = releasedRecommendations;
	}

	public Long getTestingDoneRecommendations() {
		return testingDoneRecommendations;
	}

	public void setTestingDoneRecommendations(Long testingDoneRecommendations) {
		this.testingDoneRecommendations = testingDoneRecommendations;
	}

	public Long getApprovedRecommendationNotYetReleased() {
		return approvedRecommendationNotYetReleased;
	}

	public void setApprovedRecommendationNotYetReleased(Long approvedRecommendationNotYetReleased) {
		this.approvedRecommendationNotYetReleased = approvedRecommendationNotYetReleased;
	}

}
