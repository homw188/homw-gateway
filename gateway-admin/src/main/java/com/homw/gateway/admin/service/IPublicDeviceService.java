package com.homw.gateway.admin.service;

import java.util.List;

import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.bean.Page;
import com.homw.gateway.admin.bean.PageRequest;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.entity.DeviceCommandLog;
import com.homw.gateway.entity.DeviceInfo;

public interface IPublicDeviceService{
	
	int selectDevicePageCountByType(Constant.DeviceType type, PageRequest pageRequest);
	
	int selectCmdLogPageCountByType(Constant.DeviceType type, PageRequest pageRequest);
	
	Page<DeviceInfo> selectDevicePageByType(Constant.DeviceType type, PageRequest pageRequest, String doorName, String outerNo);
	
	List<DeviceInfo> selectDeviceByType(Constant.DeviceType type, String doorName, String outerNo);
	
	Page<DeviceCommandLog> selectCmdLogPageByType(Constant.DeviceType type, PageRequest pageRequest, String doorName, String userMobile, String startTime, String endTime);
	
	List<DeviceCommandLog> selectCmdLogByType(Constant.DeviceType type, String doorName, String userMobile, String startTime, String endTime);
	
	void insertDevice(DeviceInfo prd, Operator operators);
	
	void softDeleteById(Long deviceId,Operator operators);
	
	DeviceInfo selectDeviceById(Long deviceId);
	
	void updateDeviceById(DeviceInfo prd, Operator operators);
	
	List<DeviceInfo> findPowerAllListbypam(String devicetype);
	
	List<DeviceInfo> findElecReferNode();
}
