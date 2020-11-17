package com.homw.gateway.device.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IWaterDevice;
import com.homw.gateway.api.keda.KDZTService;
import com.homw.gateway.constant.ProtocolConstant;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 科大水表设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-04
 */
@Service("kedaWaterDevice")
public class KedaWaterDeviceImpl implements IWaterDevice {
	private static Logger logger = LoggerFactory.getLogger(KedaWaterDeviceImpl.class);

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backInfo = KDZTService.getSingleInstance().waterOnOff(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				Long.valueOf(deviceInfo.getElecAddr()), ProtocolConstant.Keda.OPEN_WATER);
		if ((backInfo.contains(",") && backInfo.split(",")[0].equals(ProtocolConstant.Keda.CODE_OK))
				|| backInfo.equals(ProtocolConstant.Keda.CODE_OK)) {
			return Pair.of(Boolean.TRUE, backInfo);
		}
		return Pair.of(Boolean.FALSE, backInfo);
	}

	@Override
	public Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backInfo = KDZTService.getSingleInstance().waterOnOff(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				Long.valueOf(deviceInfo.getElecAddr()), ProtocolConstant.Keda.CLOSE_WATER);
		if ((backInfo.contains(",") && backInfo.split(",")[0].equals(ProtocolConstant.Keda.CODE_OK))
				|| backInfo.equals(ProtocolConstant.Keda.CODE_OK)) {
			return Pair.of(Boolean.TRUE, backInfo);
		}
		return Pair.of(Boolean.FALSE, backInfo);
	}

	@Override
	public Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception {
		Double readNum = 0.0;
		String waterNum = KDZTService.getSingleInstance().readWaterValue(deviceInfo.getDoorIp(),
				deviceInfo.getDoorPort(), Long.valueOf(deviceInfo.getElecAddr()));
		try {
			// 多户表
			if (waterNum.contains(",") && waterNum.split(",")[0].equals(deviceInfo.getElecAddr())) {
				readNum = Double.parseDouble(waterNum.split(",")[1]);
				return Pair.of(Boolean.TRUE, readNum);
			}
		} catch (Exception e) {
			logger.info("parse failed: {}", waterNum);
		}
		return Pair.of(Boolean.FALSE, null);
	}

	@Override
	public String searchStatus(DeviceInfo deviceInfo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeviceBrand.WaterDeviceBrand getBrand() {
		return DeviceBrand.WaterDeviceBrand.KEDA;
	}
}
