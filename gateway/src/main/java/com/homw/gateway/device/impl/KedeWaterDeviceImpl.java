package com.homw.gateway.device.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IWaterDevice;
import com.homw.gateway.api.kede.KedeMeterUtil;
import com.homw.gateway.constant.ProtocolConstant;
import com.homw.gateway.dto.KedeOperationData;
import com.homw.gateway.dto.KedeReadWaterData;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 科德水表设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-04
 */
@Service("kedeWaterDevice")
public class KedeWaterDeviceImpl implements IWaterDevice {
	private static Logger logger = LoggerFactory.getLogger(KedeElecDeviceImpl.class);

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backData = KedeMeterUtil.pullWaterSwitch(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				deviceInfo.getElecAddr(), ProtocolConstant.Kede.TD_0, ProtocolConstant.Kede.READ_TIMEOUT);
		KedeOperationData oper = null;
		try {
			oper = JSON.parseObject(backData, KedeOperationData.class);
		} catch (Exception e) {
			logger.info("parse failed: {}", backData);
		}
		if (oper != null && KedeOperationData.isSuccess(oper.getData())) {
			return Pair.of(Boolean.TRUE, String.valueOf(backData));
		}
		return Pair.of(Boolean.FALSE, String.valueOf(backData));
	}

	@Override
	public Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backData = KedeMeterUtil.pullWaterSwitch(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				deviceInfo.getElecAddr(), ProtocolConstant.Kede.TD_1, ProtocolConstant.Kede.READ_TIMEOUT);
		KedeOperationData oper = null;
		try {
			oper = JSON.parseObject(backData, KedeOperationData.class);
		} catch (Exception e) {
			logger.info("parse failed: {}", backData);
		}
		if (oper != null && KedeOperationData.isSuccess(oper.getData())) {
			return Pair.of(Boolean.TRUE, String.valueOf(backData));
		}
		return Pair.of(Boolean.FALSE, String.valueOf(backData));
	}

	@Override
	public Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception {
		String backData = KedeMeterUtil.readWaterData(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
				deviceInfo.getElecAddr(), ProtocolConstant.Kede.READ_TIMEOUT);
		KedeReadWaterData data = null;
		try {
			JSONObject jsonMap = JSON.parseObject(backData, JSONObject.class);
			data = JSON.parseObject(jsonMap.getString("data"), KedeReadWaterData.class);
		} catch (Exception e) {
			logger.info("parse failed: {}", backData);
		}
		if (data != null) {
			return Pair.of(Boolean.TRUE, Double.parseDouble(data.getLj()));
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
		return DeviceBrand.WaterDeviceBrand.KEDE;
	}
}
