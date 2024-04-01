package com.sbi.oem.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "functionality")
public class Functionality {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title_id")
	private Long titleId;

	@Column(name = "path")
	private String path;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTitleId() {
		return titleId;
	}

	public void setTitleId(Long titleId) {
		this.titleId = titleId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Functionality(Long id, Long titleId, String path) {
		super();
		this.id = id;
		this.titleId = titleId;
		this.path = path;
	}

	public Functionality() {
		super();
		// TODO Auto-generated constructor stub
	}

}
