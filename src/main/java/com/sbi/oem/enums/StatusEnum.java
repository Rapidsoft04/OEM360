package com.sbi.oem.enums;

public enum StatusEnum {

	OEM_recommendation(1L, "OEM recommendation"), Review_process(2L, "Review process"), Approved(3L, "Approved"),
	Rejected(4L, "Rejected"), Department_implementation(5L, "Department implementation"),
	UAT_testing(6L, "UAT testing"), Released(7L, "Released"), No_Action(8L, "No action"), Delayed(9L, "Delay"), Released_With_Delay(10L, "Released with delay"),
	Planned(11L, "Planned"), On_time(12L, "On time");

	private Long id;
	private String name;

	private StatusEnum(Long id, String name) {
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
