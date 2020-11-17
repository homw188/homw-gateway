package com.homw.gateway.admin.bean;

import java.util.LinkedHashMap;

public class MapResponse extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = -7691333455529348649L;

	private final static String CODE = "000000";
	private final static String MESSAGE = "成功";
	private final static String CODE_KEY = "code";
	private final static String MESSAGE_KEY = "message";
	private final static String RESULT_KEY = "result";

	public MapResponse() {
		super(3);
		put(CODE_KEY, CODE);
		put(MESSAGE_KEY, MESSAGE);
	}
	
	public MapResponse(Object response) {
		super(3);
		put(CODE_KEY, CODE);
		put(MESSAGE_KEY, MESSAGE);
		put(RESULT_KEY, response);
	}
	
	public MapResponse(String code, String message) {
		super(3);
		put(CODE_KEY, code);
		put(MESSAGE_KEY, message);
	}
	
	public MapResponse(String code, String message, Object response) {
		super(3);
		put(CODE_KEY, code);
		put(MESSAGE_KEY, message);
		put(RESULT_KEY, response);
	}

	public String getCode() {
		return (String) this.get(CODE_KEY);
	}

	public void setCode(String code) {
		this.put(CODE_KEY, code);
	}

	public String getMessage() {
		return (String) this.get(MESSAGE_KEY);
	}

	public void setMessage(String message) {
		this.put(MESSAGE_KEY, message);
	}

	public Object getResult() {
		return this.get(RESULT_KEY);
	}

	public void setResult(Object result) {
		this.put(RESULT_KEY, result);
	}

}
