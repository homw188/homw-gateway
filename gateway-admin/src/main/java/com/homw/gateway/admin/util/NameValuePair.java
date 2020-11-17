package com.homw.gateway.admin.util;

public class NameValuePair implements Comparable<NameValuePair> {

	private String name;
	private String value;

	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int compareTo(NameValuePair o) {
		int s = this.name.compareTo(o.name);
		return s != 0 ? s : this.value.compareTo(o.value);
	}
}
