package com.homw.gateway.dto;

public class KedeReadElecData {

	private String lj; // 累计用电量
	private String sy; // 剩余电量
	private String cs; // 充值次数

	public String getLj() {
		return lj;
	}

	public void setLj(String lj) {
		this.lj = lj;
	}

	public String getSy() {
		return sy;
	}

	public void setSy(String sy) {
		this.sy = sy;
	}

	public String getCs() {
		return cs;
	}

	public void setCs(String cs) {
		this.cs = cs;
	}

}
