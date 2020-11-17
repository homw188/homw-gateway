package com.homw.gateway.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.homw.gateway.common.dao.DeviceCommandLogDao;
import com.homw.gateway.entity.DeviceCommandLog;
import com.homw.gateway.event.DeviceLogEvent;

@Component
public class DeviceOperateLogListener implements ApplicationListener<DeviceLogEvent> {

	private static Logger logger = LoggerFactory.getLogger(DeviceOperateLogListener.class);

	@Autowired
	private DeviceCommandLogDao deviceCommandLogDao;

	@Override
	public void onApplicationEvent(DeviceLogEvent event) {
		DeviceCommandLog commandLog = new DeviceCommandLog();
		commandLog.setCreateTime(new Date());
		commandLog.setDeviceType(event.getDeviceType());
		commandLog.setDoorName(event.getDoorName());
		commandLog.setOuterNo(event.getOuterNo());
		commandLog.setUserId(event.getUserId());
		commandLog.setUserType(event.getUserType());
		commandLog.setUserName(event.getUserName());
		commandLog.setUserMobile(event.getUserMobile());
		commandLog.setCastTime(System.currentTimeMillis() - event.getCreateTime());
		int cnt = deviceCommandLogDao.insertSelective(commandLog);
		if (cnt == 0) {
			logger.error("save device command log failed, userMobile: {}, deviceNo: {}", event.getUserMobile(), event.getOuterNo());
		}
	}
}
