package com.homw.gateway.common.exception;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -146484487380303539L;

	private String code;
	private Object value;

	public ServiceException(String code) {
		super(code);
		this.code = code;
	}

	public ServiceException(String code, String message) {
		super(message);
		this.code = code;
	}

	public ServiceException(String code, Object value) {
		this.code = code;
		this.value = value;
	}

	public ServiceException(String code, String message, Exception e) {
		super(message, e);
		this.code = code;
	}

	public Object getValue() {
		return value;
	}

	public String getCode() {
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (obj instanceof ServiceException) {
			ServiceException other = (ServiceException) obj;
			
			if (other.code == null || other.getMessage() == null)
				return false;
			return other.code.equals(this.code) && other.getMessage().equals(getMessage());
		}
		return false;
	}
}
