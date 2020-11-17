package com.homw.gateway.admin.job;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.homw.gateway.admin.service.IPublicDeviceService;
import com.homw.gateway.admin.util.ApplicationContextHelper;
import com.homw.gateway.api.device.IWaterDevice;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.entity.DeviceInfo;

public class ReadWaterBatchJob extends AbstractScheduleJob {

	@Override
	protected void execute() {
		List<DeviceInfo> deviceInfoList = ApplicationContextHelper.getBean(IPublicDeviceService.class)
				.findPowerAllListbypam(Constant.DeviceType.WATER.name());
		if (CollectionUtils.isNotEmpty(deviceInfoList)) {
			for (DeviceInfo deviceInfo : deviceInfoList) {
				if (deviceInfo != null) {
					try {
						ApplicationContextHelper.getBean(IWaterDevice.class).search(deviceInfo);
					} catch (Exception e) {
						logger.warn("read water failed，deviceNo={}", deviceInfo.getOuterNo(), e);
					}
				} else {
					logger.warn("read water failed, deviceInfo is null");
				}
			}
		}
	}
}
