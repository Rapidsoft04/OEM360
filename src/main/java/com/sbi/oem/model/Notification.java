package com.sbi.oem.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "message")
	private String message;

	@Column(name = "ref_id")
	private String referenceId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "is_seen")
	private Boolean isSeen;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getIsSeen() {
		return isSeen;
	}

	public void setIsSeen(Boolean isSeen) {
		this.isSeen = isSeen;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Notification(Long id, String message, String referenceId, User user, Boolean isSeen, Date createdAt,
			Date updatedAt) {
		super();
		this.id = id;
		this.message = message;
		this.referenceId = referenceId;
		this.user = user;
		this.isSeen = isSeen;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Notification() {
		super();
	}

}
