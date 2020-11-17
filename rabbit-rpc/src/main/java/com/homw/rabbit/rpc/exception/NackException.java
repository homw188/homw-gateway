package com.homw.rabbit.rpc.exception;

public class NackException extends Exception {

	private static final long serialVersionUID = 2637136761247347440L;

	public NackException(String message) {
		super(message);
	}

	public NackException(String message, Throwable cause) {
		super(message, cause);
	}
}
