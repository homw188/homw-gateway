package com.homw.gateway.admin.vo;

public class DeviceInfoVo {
	private Long deviceId;
	private String deviceType;
	private String outerNo;
	private String doorIp;
	private Integer doorPort;
	private String doorAddr;
	private Integer doorReadno;
	private String elecAddr;
	private String elecStatus;
	private String elecUsePoint;
	private Integer elecLeftPoint;
	private Short status;
	private Integer version;
	private Long createUserId;
	private String createTime;
	private String createUserType;

	private String doorName;
	private String updateTime;

	private Integer rate;

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getDoorName() {
		return doorName;
	}

	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
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

	public String getDoorIp() {
		return doorIp;
	}

	public void setDoorIp(String doorIp) {
		this.doorIp = doorIp;
	}

	public Integer getDoorPort() {
		return doorPort;
	}

	public void setDoorPort(Integer doorPort) {
		this.doorPort = doorPort;
	}

	public String getDoorAddr() {
		return doorAddr;
	}

	public void setDoorAddr(String doorAddr) {
		this.doorAddr = doorAddr;
	}

	public Integer getDoorReadno() {
		return doorReadno;
	}

	public void setDoorReadno(Integer doorReadno) {
		this.doorReadno = doorReadno;
	}

	public String getElecAddr() {
		return elecAddr;
	}

	public void setElecAddr(String elecAddr) {
		this.elecAddr = elecAddr;
	}

	public String getElecStatus() {
		return elecStatus;
	}

	public void setElecStatus(String elecStatus) {
		this.elecStatus = elecStatus;
	}

	public String getElecUsePoint() {
		return elecUsePoint;
	}

	public void setElecUsePoint(String elecUsePoint) {
		this.elecUsePoint = elecUsePoint;
	}

	public Integer getElecLeftPoint() {
		return elecLeftPoint;
	}

	public void setElecLeftPoint(Integer elecLeftPoint) {
		this.elecLeftPoint = elecLeftPoint;
	}

	public Short getStatus() {
		return status;
	}

	public void setStatus(Short status) {
		this.status = status;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateUserType() {
		return createUserType;
	}

	public void setCreateUserType(String createUserType) {
		this.createUserType = createUserType;
	}

}