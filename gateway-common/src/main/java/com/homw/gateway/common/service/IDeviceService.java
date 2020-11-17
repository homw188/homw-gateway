package com.homw.gateway.common.service;

import java.util.List;

import com.homw.gateway.common.dto.DeviceStatus;
import com.homw.gateway.entity.dto.DeviceDataPoint;

public interface IDeviceService {

	List<DeviceStatus> queryBatchStatus(String[] deviceNoArr);
	
	List<DeviceDataPoint> queryBatchDataPoint(String curDay, String[] queryArray);
	
	List<DeviceStatus> queryBatchWaterStatus(String[] outerNos);
}
