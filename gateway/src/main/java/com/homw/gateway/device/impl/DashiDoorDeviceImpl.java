package com.homw.gateway.device.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.dashi.DashiDoorApi;
import com.homw.gateway.api.device.IDoorDevice;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 达石门禁设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-03
 */
@Service("dashiDoorDevice")
public class DashiDoorDeviceImpl implements IDoorDevice {
	private static Logger logger = LoggerFactory.getLogger(DashiDoorDeviceImpl.class);

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		int readNo = (int) Math.pow(2, deviceInfo.getDoorReadno());
		String macAddr = deviceInfo.getDoorAddr().substring(6);
		StringBuilder buf = new StringBuilder();
		buf.append("55").append("04").append(macAddr).append("2D").append("0001").append("0").append(readNo).append("00");
		byte[] sumBytes = DashiDoorApi.checkSum(DashiDoorApi.hex2Bytes(buf.toString()), 2);
		buf.append(DashiDoorApi.bytes2Hex(sumBytes).toUpperCase());
		byte[] data = DashiDoorApi.hex2Bytes(buf.toString());
		logger.info("send packet: {}", DashiDoorApi.bytes2Hex(data));
		// 发送开门指令
		DashiDoorApi.send(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(), data);
		return Pair.of(Boolean.TRUE, null);
	}

	@Override
	public DeviceBrand.DoorDeviceBrand getBrand() {
		return DeviceBrand.DoorDeviceBrand.DASHI;
	}
}
