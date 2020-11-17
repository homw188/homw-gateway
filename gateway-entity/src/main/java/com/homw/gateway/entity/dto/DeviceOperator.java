package com.homw.gateway.entity.dto;

import java.io.Serializable;

public class DeviceOperator implements Serializable {

	private static final long serialVersionUID = 2752277631905061766L;
	
	private Long userId;
	private String userType;
	private String userMobile;
	private Long createTime;

	public DeviceOperator() {
	}

	public DeviceOperator(CommandMessage sqsMessage) {
		this.userId = sqsMessage.getUserId();
		this.userType = sqsMessage.getUserType();
		this.userMobile = sqsMessage.getUserMobile();
		this.createTime = sqsMessage.getCreateTime();
	}

	public DeviceOperator(Long userId, String userType, String userMobile, Long createTime) {
		super();
		this.userId = userId;
		this.userType = userType;
		this.userMobile = userMobile;
		this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

}
