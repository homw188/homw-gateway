package com.homw.gateway.device.proxy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IDoorDevice;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 门禁设备
 * @author Hom
 * @version 1.0
 * @since 2020-10-28
 */
@Component("doorDeviceProxy")
public class DoorDeviceProxy implements IDoorDevice {

	@Autowired
	private List<IDoorDevice> deviceList;
	
	@Value("${door.brand}")
	private String doorBrand; // 门禁厂家
	
	public IDoorDevice getDevice() {
		for (IDoorDevice device : deviceList) {
			if (device != this && device.getBrand() == getBrand()) {
				return device;
			}
		}
		return null;
	}
	
	@Override
	public DeviceBrand.DoorDeviceBrand getBrand() {
		if (StringUtils.isEmpty(doorBrand)) {
			return DeviceBrand.DoorDeviceBrand.DASHI;
		}
		
		for (DeviceBrand.DoorDeviceBrand brand : DeviceBrand.DoorDeviceBrand.values()) {
			if (brand.name().equalsIgnoreCase(doorBrand.trim())) {
				return brand;
			}
		}
		throw new ServiceException("not supported device brand -> " + doorBrand);
	}

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		return getDevice().open(deviceInfo, operator);
	}
}
