package com.homw.gateway.admin.job;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.homw.gateway.admin.service.IPublicDeviceService;
import com.homw.gateway.admin.util.ApplicationContextHelper;
import com.homw.gateway.api.device.IElecDevice;
import com.homw.gateway.entity.DeviceInfo;

public class ReadElecReferNodeBatchJob extends AbstractScheduleJob {

	@Override
	protected void execute() {
		List<DeviceInfo> deviceInfoList = ApplicationContextHelper.getBean(IPublicDeviceService.class)
				.findElecReferNode();
		if (CollectionUtils.isNotEmpty(deviceInfoList)) {
			for (DeviceInfo deviceInfo : deviceInfoList) {
				if (deviceInfo != null) {
					try {
						ApplicationContextHelper.getBean(IElecDevice.class).search(deviceInfo);
					} catch (Exception e) {
						logger.warn("read elec failed，deviceNo={}", deviceInfo.getOuterNo(), e);
					}
				} else {
					logger.warn("read elec failed, deviceInfo is null");
				}
			}
		}
	}
}
