package com.sbi.oem.enums;

public enum FunctionalityEnum {

	Component(1L, "Component List"), Department(2L, "Department List"), User(3L, "User List"),
	RecommendationType(4L, "Type Master List");

	private Long id;
	private String name;

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

	private FunctionalityEnum(Long id, String name) {
		this.id = id;
		this.name = name;
	}

}
