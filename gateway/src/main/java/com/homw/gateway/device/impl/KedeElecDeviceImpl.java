package com.homw.gateway.device.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.ICommPortDevice;
import com.homw.gateway.api.device.IElecDevice;
import com.homw.gateway.api.kede.KedeMeterUtil;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.constant.DeviceConstant;
import com.homw.gateway.constant.ProtocolConstant;
import com.homw.gateway.dto.KedeOperationData;
import com.homw.gateway.dto.KedeReadElecData;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 科德电表设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-02
 */
@Service("kedeElecDevice")
public class KedeElecDeviceImpl implements IElecDevice {
	private static Logger logger = LoggerFactory.getLogger(KedeElecDeviceImpl.class);

	@Value("${elec.protocol}")
	private String elecProtocol; // 电表通信方式
	private DeviceConstant.ElecProtocol elecProto;

	@Autowired
	private ICommPortDevice kedeCommPortDevice;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(elecProtocol)) {
			elecProto = DeviceConstant.ElecProtocol.TCPIP;
			return;
		}

		for (DeviceConstant.ElecProtocol proto : DeviceConstant.ElecProtocol.values()) {
			if (proto.name().equalsIgnoreCase(elecProtocol.trim())) {
				elecProto = proto;
				return;
			}
		}
		throw new ServiceException("not supported electric protocol -> " + elecProtocol);
	}

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backData = null;
		if (elecProto == DeviceConstant.ElecProtocol.TCPIP) {
			backData = KedeMeterUtil.pullElecSwitch(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
					deviceInfo.getElecAddr(), ProtocolConstant.Kede.TD_0, ProtocolConstant.Kede.READ_TIMEOUT);
			KedeOperationData oper = null;
			try {
				oper = JSON.parseObject(backData, KedeOperationData.class);
			} catch (Exception e) {
				logger.info("parse failed: {}", backData);
			}
			if (oper != null && KedeOperationData.isSuccess(oper.getData())) {
				return Pair.of(Boolean.TRUE, backData);
			}
		} else {
			return Pair.of(kedeCommPortDevice.open(deviceInfo.getElecAddr()), null);
		}
		return Pair.of(Boolean.FALSE, backData);
	}

	@Override
	public Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String backData = null;
		if (elecProto == DeviceConstant.ElecProtocol.TCPIP) {
			backData = KedeMeterUtil.pullElecSwitch(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
					deviceInfo.getElecAddr(), ProtocolConstant.Kede.TD_1, ProtocolConstant.Kede.READ_TIMEOUT);
			KedeOperationData oper = null;
			try {
				oper = JSON.parseObject(backData, KedeOperationData.class);
			} catch (Exception e) {
				logger.info("parse failed: {}", backData);
			}
			if (oper != null && KedeOperationData.isSuccess(oper.getData())) {
				return Pair.of(Boolean.TRUE, backData);
			}
		} else {
			return Pair.of(kedeCommPortDevice.close(deviceInfo.getElecAddr()), null);
		}
		return Pair.of(Boolean.FALSE, backData);
	}

	@Override
	public Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception {
		if (elecProto == DeviceConstant.ElecProtocol.TCPIP) {
			String backData = KedeMeterUtil.readElecData(deviceInfo.getDoorIp(), deviceInfo.getDoorPort(),
					deviceInfo.getElecAddr(), ProtocolConstant.Kede.READ_TIMEOUT);
			KedeReadElecData data = null;
			try {
				JSONObject jsonMap = JSON.parseObject(backData, JSONObject.class);
				data = JSON.parseObject(jsonMap.getString("data"), KedeReadElecData.class);
			} catch (Exception e) {
				logger.info("parse failed: {}", backData);
			}
			if (data != null) {
				return Pair.of(Boolean.TRUE, Double.parseDouble(data.getLj()));
			}
		} else {
			return Pair.of(kedeCommPortDevice.search(deviceInfo.getElecAddr()), null);
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
		return DeviceBrand.ElecDeviceBrand.KEDE;
	}
}
