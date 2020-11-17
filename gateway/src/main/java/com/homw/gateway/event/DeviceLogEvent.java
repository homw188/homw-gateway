package com.homw.gateway.event;

import org.springframework.context.ApplicationEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLogEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2368099486847374678L;

	private Long userId;
	private String userType;
	private String userName;
	private String userMobile;
	private String deviceType;
	private String outerNo;
	private String doorName;
	private Long createTime;

	public DeviceLogEvent(Object source, DeviceOperator operator, DeviceInfo deviceInfo) {
		super(source);
		this.userId = operator.getUserId();
		this.userType = operator.getUserType();
		// this.userName = operator.getUserName();
		this.userMobile = operator.getUserMobile();
		this.outerNo = deviceInfo.getOuterNo();
		this.doorName = deviceInfo.getDoorName();
		this.deviceType = deviceInfo.getDeviceType();
		this.createTime = operator.getCreateTime();
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOuterNo() {
		return outerNo;
	}

	public void setOuterNo(String outerNo) {
		this.outerNo = outerNo;
	}

	public String getDoorName() {
		return doorName;
	}

	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

}
