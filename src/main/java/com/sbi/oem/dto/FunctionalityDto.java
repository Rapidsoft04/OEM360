package com.sbi.oem.dto;

public class FunctionalityDto {

	private Long id;

	private String name;

	private Long count;

	private String path;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public FunctionalityDto(Long id, String name, Long count, String path) {
		super();
		this.id = id;
		this.name = name;
		this.count = count;
		this.path = path;
	}

	public FunctionalityDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
