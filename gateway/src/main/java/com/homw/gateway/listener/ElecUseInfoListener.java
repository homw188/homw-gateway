package com.homw.gateway.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.service.IElecUseInfoService;
import com.homw.gateway.entity.DeviceElectricUseInfo;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.event.ElecUseInfoEvent;

@Component
public class ElecUseInfoListener implements ApplicationListener<ElecUseInfoEvent> {

	@Value("${elec.fee.public}")
	private String calcPublicFee; // 是否计算公摊

	@Autowired
	private IElecUseInfoService elecUseInfoService;

	@Override
	public void onApplicationEvent(ElecUseInfoEvent event) {
		DeviceInfo deviceInfo = event.getDeviceInfo();
		if (deviceInfo == null) {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deviceInfo is null");
		}
		DeviceElectricUseInfo deviceElectricUseInfo = elecUseInfoService.saveElecUseInfo(deviceInfo);
		if (calcPublicFee.equalsIgnoreCase("Y") && deviceInfo.getIsReferNode()) {
			elecUseInfoService.calcuateElecUseInfo(deviceElectricUseInfo);
		}
	}
}
