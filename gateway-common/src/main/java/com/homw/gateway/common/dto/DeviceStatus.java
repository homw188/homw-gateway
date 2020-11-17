package com.homw.gateway.common.dto;

public class DeviceStatus {

	private String outerNo;
	private String elecAddr;
	private String elecStatus;
	private String elecUsePoint;
	private String elecLeftPoint;
	
	public String getOuterNo() {
		return outerNo;
	}
	public void setOuterNo(String outerNo) {
		this.outerNo = outerNo;
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
	public String getElecLeftPoint() {
		return elecLeftPoint;
	}
	public void setElecLeftPoint(String elecLeftPoint) {
		this.elecLeftPoint = elecLeftPoint;
	}
	
}
