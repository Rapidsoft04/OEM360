package com.sbi.oem.enums;

public enum PriorityEnum {

	High(1L, "High"), Medium(2L, "Medium"), Low(3L, "Low");

	private Long id;
	private String name;

	private PriorityEnum(Long id, String name) {
		this.id = id;
		this.name = name;
	}

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

}
