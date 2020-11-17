package com.homw.gateway.dto;

public class KedeReadWaterData {

	private String lj; // 累计用电量
	private String sy; // 剩余电量
	private String cs; // 充值次数
	private String tz; // 透支
	private String zt1; // 状态1
	private String zt2; // 状态2
	private String sj; // 时间

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

	public String getTz() {
		return tz;
	}

	public void setTz(String tz) {
		this.tz = tz;
	}

	public String getZt1() {
		return zt1;
	}

	public void setZt1(String zt1) {
		this.zt1 = zt1;
	}

	public String getZt2() {
		return zt2;
	}

	public void setZt2(String zt2) {
		this.zt2 = zt2;
	}

	public String getSj() {
		return sj;
	}

	public void setSj(String sj) {
		this.sj = sj;
	}

}
