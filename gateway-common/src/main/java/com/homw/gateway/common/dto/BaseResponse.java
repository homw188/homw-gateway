package com.homw.gateway.common.dto;

public class BaseResponse {
	private String code = "000000";
	private String message = "成功";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
