package com.homw.rabbit.rpc.message;

public class ResponseMessage extends AbstractMessage {
	private String greeting;
	
	public ResponseMessage() {
	}
	public ResponseMessage(String greeting) {
		this.greeting = greeting;
	}
	
	public String getGreeting() {
		return greeting;
	}
	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}
}
