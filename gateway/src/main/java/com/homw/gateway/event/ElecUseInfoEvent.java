package com.homw.gateway.event;

import org.springframework.context.ApplicationEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.homw.gateway.entity.DeviceInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElecUseInfoEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5403415947576462375L;

	private DeviceInfo deviceInfo;

	public ElecUseInfoEvent(Object source, DeviceInfo deviceInfo) {
		super(source);
		this.deviceInfo = deviceInfo;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
}
