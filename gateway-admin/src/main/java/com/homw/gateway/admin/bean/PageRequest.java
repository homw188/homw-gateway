package com.homw.gateway.admin.bean;

public class PageRequest {

	/**
	 * 页数(页码从1开始计算)
	 */
	private int page;
	/**
	 * 每页条数
	 */
	private int limit;

	public PageRequest() {
		page = 1;
		limit = 10;
	}

	public PageRequest(int page, int limit) {
		this.page = page;
		this.limit = limit;
	}

	@Override
	public String toString() {
		return " LIMIT " + getStart() + "," + limit;
	}

	public int getStart() {
		return (page - 1) * limit;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
