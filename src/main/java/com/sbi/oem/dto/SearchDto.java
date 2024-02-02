package com.sbi.oem.dto;

import java.util.Date;
import java.util.List;

import com.sbi.oem.enums.StatusEnum;
import com.sbi.oem.model.Department;

public class SearchDto {

	private Long recommendationType;

	private Long priorityId;

	private String referenceId;

	private Long departmentId;

	private Long statusId;

	private String fromDate;

	private String toDate;

	private Long createdBy;

	private Date updatedAt;

	private Department department;

	private StatusEnum status;

	private List<Long> departmentIds;

	private String searchKey;

	private String chartSearchKey;

	private String dateFilterKey;

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public Long getRecommendationType() {
		return recommendationType;
	}

	public void setRecommendationType(Long recommendationType) {
		this.recommendationType = recommendationType;
	}

	public Long getPriorityId() {
		return priorityId;
	}

	public void setPriorityId(Long priorityId) {
		this.priorityId = priorityId;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<Long> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Long> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public String getChartSearchKey() {
		return chartSearchKey;
	}

	public void setChartSearchKey(String chartSearchKey) {
		this.chartSearchKey = chartSearchKey;
	}

	public String getDateFilterKey() {
		return dateFilterKey;
	}

	public void setDateFilterKey(String dateFilterKey) {
		this.dateFilterKey = dateFilterKey;
	}

}
