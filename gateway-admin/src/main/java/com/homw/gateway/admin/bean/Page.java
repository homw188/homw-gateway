package com.homw.gateway.admin.bean;

import java.util.List;

public class Page<T> {
	private List<T> content;

	private int pageIndex;

	private int pageSize;

	private int total;

	/**
	 * @param content     当前页内容
	 * @param total       总条数
	 * @param pageRequest 分页请求参数(请求页码、每页条数)
	 */
	public Page(final List<T> content, int total, PageRequest pageRequest) {
		this(content, total, pageRequest.getPage(), pageRequest.getLimit());
	}

	/**
	 * @param content   当前页内容
	 * @param total     总条数
	 * @param pageIndex 请求页码
	 * @param pageSize  每页条数
	 */
	public Page(final List<T> content, int total, int pageIndex, int pageSize) {
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
		this.total = total;
		this.content = content;
	}

	/**
	 * 总条数
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * 总页数
	 */
	public int getTotalPageNum() {
		return pageSize == 0 ? 1 : (int) Math.ceil((double) total / (double) pageSize);
	}

	/**
	 * 页数
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * 每页条数
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 当前页实际条数
	 */
	public int getContentSize() {
		return content.size();
	}

	/**
	 * 当前页内容
	 */
	public List<T> getContent() {
		return content;
	}

	/**
	 * 是否最后一页
	 */
	public boolean isLast() {
		return pageIndex >= getTotalPageNum();
	}
}
