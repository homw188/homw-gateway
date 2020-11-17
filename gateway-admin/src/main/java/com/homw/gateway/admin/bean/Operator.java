package com.homw.gateway.admin.bean;

public class Operator {
	private Long id;
	private String type;
	private String name;
	private String company;

	public Operator(Long id, String type) {
		this.id = id;
		this.type = type;
	}

	public Operator(Long id, String type, String name, String company) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.company = company;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

}
