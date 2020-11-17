package com.homw.gateway.common.service;

import com.homw.gateway.entity.DeviceInfo;

public interface IRedisDeviceInfoService {

	DeviceInfo redisHMGetDeviceInfo(final String deviceNo, final String type);
	
	void redisDeleteDeviceInfo(final String deviceNo, String type);
}
