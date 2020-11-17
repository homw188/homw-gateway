package com.homw.gateway.device.proxy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IElecDevice;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.device.DeviceSerialExecutor;
import com.homw.gateway.device.PriorityCallable;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 电表设备
 * @author Hom
 * @version 1.0
 * @since 2020-10-28
 */
@Component("elecDeviceProxy")
public class ElecDeviceProxy implements IElecDevice {

	@Autowired
	private List<IElecDevice> deviceList;

	@Value("${elec.brand}")
	private String elecBrand; // 电表厂家

	@Autowired
	private DeviceSerialExecutor serialExecutor;

	public IElecDevice getDevice() {
		for (IElecDevice device : deviceList) {
			if (device != this && device.getBrand() == getBrand()) {
				return device;
			}
		}
		return null;
	}

	@Override
	public DeviceBrand.ElecDeviceBrand getBrand() {
		if (StringUtils.isEmpty(elecBrand)) {
			return DeviceBrand.ElecDeviceBrand.KEDE;
		}

		for (DeviceBrand.ElecDeviceBrand brand : DeviceBrand.ElecDeviceBrand.values()) {
			if (brand.name().equalsIgnoreCase(elecBrand.trim())) {
				return brand;
			}
		}
		throw new ServiceException("not supported device brand -> " + elecBrand);
	}

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		return serialExecutor.submit(new PriorityCallable<Pair<Boolean, String>>(PriorityCallable.CTRL_PRIORITY) {
			@Override
			public Pair<Boolean, String> call() throws Exception {
				return getDevice().open(deviceInfo, operator);
			}
		}, Constant.DeviceType.ELECTRIC).get();
	}

	@Override
	public Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		return serialExecutor.submit(new PriorityCallable<Pair<Boolean, String>>(PriorityCallable.CTRL_PRIORITY) {
			@Override
			public Pair<Boolean, String> call() throws Exception {
				return getDevice().close(deviceInfo, operator);
			}
		}, Constant.DeviceType.ELECTRIC).get();
	}

	@Override
	public Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception {
		return serialExecutor.submit(new PriorityCallable<Pair<Boolean, Double>>(PriorityCallable.NORMAL_PRIORITY) {
			@Override
			public Pair<Boolean, Double> call() throws Exception {
				return getDevice().search(deviceInfo);
			}
		}, Constant.DeviceType.ELECTRIC).get();
	}

	@Override
	public String searchStatus(DeviceInfo deviceInfo) throws Exception {
		return serialExecutor.submit(new PriorityCallable<String>(PriorityCallable.NORMAL_PRIORITY) {
			@Override
			public String call() throws Exception {
				return getDevice().searchStatus(deviceInfo);
			}
		}, Constant.DeviceType.ELECTRIC).get();
	}
}
