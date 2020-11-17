package com.homw.gateway.device.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IElecDevice;
import com.homw.gateway.api.keda.KDZTService;
import com.homw.gateway.constant.ProtocolConstant;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 科大电表设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-02
 */
@Service("kedaElecDevice")
public class KedaElecDeviceImpl implements IElecDevice {
	private static Logger logger = LoggerFactory.getLogger(KedaElecDeviceImpl.class);

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backInfo = KDZTService.getSingleInstance().eleOnOff(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				Integer.parseInt(deviceInfo.getElecAddr()), deviceInfo.getDoorReadno(), ProtocolConstant.Keda.OPEN);
		if ((backInfo.contains(",") && backInfo.split(",")[0].equals(ProtocolConstant.Keda.CODE_OK))
				|| backInfo.equals(ProtocolConstant.Keda.CODE_OK)) {
			return Pair.of(Boolean.TRUE, backInfo);
		}
		return Pair.of(Boolean.FALSE, backInfo);
	}

	@Override
	public Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backInfo = KDZTService.getSingleInstance().eleOnOff(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				Integer.parseInt(deviceInfo.getElecAddr()), deviceInfo.getDoorReadno(), ProtocolConstant.Keda.CLOSE);
		if ((backInfo.contains(",") && backInfo.split(",")[0].equals(ProtocolConstant.Keda.CODE_OK))
				|| backInfo.equals(ProtocolConstant.Keda.CODE_OK)) {
			return Pair.of(Boolean.TRUE, backInfo);
		}
		return Pair.of(Boolean.FALSE, backInfo);
	}

	@Override
	public Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception {
		Double readNum = 0.0;
		Integer seqNo = deviceInfo.getDoorReadno();
		String elecNum = KDZTService.getSingleInstance().readPowerValue(deviceInfo.getDoorIp(),
				deviceInfo.getDoorPort(), Integer.parseInt(deviceInfo.getDoorAddr()), seqNo);
		try {
			// 多户表
			if (elecNum.contains(",") && elecNum.split(",")[0].equals(seqNo.toString())) {
				readNum = Double.parseDouble(elecNum.split(",")[1]);
				return Pair.of(Boolean.TRUE, readNum);
			}
		} catch (Exception e) {
			logger.info("parse failed: {}", elecNum);
		}
		return Pair.of(Boolean.FALSE, null);
	}

	@Override
	public String searchStatus(DeviceInfo deviceInfo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeviceBrand.ElecDeviceBrand getBrand() {
		return DeviceBrand.ElecDeviceBrand.KEDA;
	}
}
