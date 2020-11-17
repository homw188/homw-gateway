package com.homw.gateway.entity.dto;

public class CommandMessage {
	private String messageType;// 设备类型
	private String commandType;// 指令类型：设备开关，读取数据
	private String outerNo;// 设备编号
	private Long userId;
	private String userType;
	private String userMobile;
	private Long createTime;
	private String curDay;// 当前日期

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getCommandType() {
		return commandType;
	}

	public void setCommandType(String commandType) {
		this.commandType = commandType;
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

	public String getOuterNo() {
		return outerNo;
	}

	public void setOuterNo(String outerNo) {
		this.outerNo = outerNo;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getCurDay() {
		return curDay;
	}

	public void setCurDay(String curDay) {
		this.curDay = curDay;
	}

}
