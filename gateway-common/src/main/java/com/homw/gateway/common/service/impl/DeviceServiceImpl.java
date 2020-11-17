package com.homw.gateway.common.service.impl;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.dao.DeviceElectricUseInfoDao;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.dto.DeviceStatus;
import com.homw.gateway.common.service.IDeviceService;
import com.homw.gateway.entity.DeviceElectricUseInfo;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceDataPoint;
import com.homw.gateway.entity.example.DeviceElectricUseInfoExample;
import com.homw.gateway.entity.example.DeviceInfoExample;

@Service("deviceService")
public class DeviceServiceImpl implements IDeviceService {
	@Autowired
	private DeviceInfoDao deviceInfoDao;
	@Autowired
	private DeviceElectricUseInfoDao deviceElectricUseInfoDao;

	@Override
	public List<DeviceStatus> queryBatchStatus(String[] deviceNoArr) {
		List<DeviceStatus> dataList = Lists.newArrayList();
		if (deviceNoArr.length > 0) {
			List<String> types = Lists.newArrayList();
			types.add(Constant.DeviceType.DOOR.name());
			types.add(Constant.DeviceType.ELECTRIC.name());

			DeviceInfoExample deviceInfoExample = new DeviceInfoExample();
			deviceInfoExample.createCriteria().andDeviceTypeIn(types).andOuterNoIn(Arrays.asList(deviceNoArr));
			List<DeviceInfo> deviceInfoList = deviceInfoDao.selectByExample(deviceInfoExample);

			DeviceStatus data = null;
			DecimalFormat df = new DecimalFormat("0.00");
			for (DeviceInfo deviceInfo : deviceInfoList) {
				data = new DeviceStatus();
				if (Constant.DeviceType.ELECTRIC.name().equals(deviceInfo.getDeviceType())) {
					data.setElecAddr(deviceInfo.getElecAddr());
					if ("01".equals(deviceInfo.getElecStatus())) {
						data.setElecStatus(Constant.DeviceStatus.CLOSE.name());
					} else {
						data.setElecStatus(Constant.DeviceStatus.OPEN.name());
					}
					data.setElecLeftPoint(df.format(deviceInfo.getElecLeftPoint().floatValue() / 100));
					data.setElecUsePoint(df.format(deviceInfo.getElecUsePoint().floatValue() / 100));
				}
				data.setOuterNo(deviceInfo.getOuterNo());
				dataList.add(data);
			}
		}
		return dataList;
	}

	@Override
	public List<DeviceDataPoint> queryBatchDataPoint(String curDay, String[] deviceNoArr) {
		List<DeviceDataPoint> dataList = Lists.newArrayList();
		if (deviceNoArr.length > 0) {
			DeviceElectricUseInfoExample deviceElecUseExample = new DeviceElectricUseInfoExample();
			deviceElecUseExample.createCriteria().andOuterNoIn(Arrays.asList(deviceNoArr)).andNowdayEqualTo(curDay);
			List<DeviceElectricUseInfo> deviceElecUseList = deviceElectricUseInfoDao
					.selectByExample(deviceElecUseExample);
			DeviceDataPoint data = null;
			DecimalFormat df = new DecimalFormat("0.00");
			for (DeviceElectricUseInfo deviceElecUse : deviceElecUseList) {
				data = new DeviceDataPoint();
				data.setCurUsePoint(df.format(deviceElecUse.getCurUsePoint().floatValue() / 100));
				data.setYesdayPoint(df.format(deviceElecUse.getYesdayPoint().floatValue() / 100));
				data.setNowdayPoint(df.format(deviceElecUse.getNowdayPoint().floatValue() / 100));
				data.setOtherUsePoint(df.format(deviceElecUse.getOtherUsePoint().floatValue() / 100));
				data.setOuterNo(deviceElecUse.getOuterNo());
				dataList.add(data);
			}
		}
		return dataList;
	}

	@Override
	public List<DeviceStatus> queryBatchWaterStatus(String[] deviceNoArr) {
		List<DeviceStatus> dataList = Lists.newArrayList();
		if (deviceNoArr.length > 0) {
			List<String> types = Lists.newArrayList();
			types.add(Constant.DeviceType.WATER.name());

			DeviceInfoExample deviceInfoExample = new DeviceInfoExample();
			deviceInfoExample.createCriteria().andDeviceTypeIn(types).andOuterNoIn(Arrays.asList(deviceNoArr));
			List<DeviceInfo> deviceInfoList = deviceInfoDao.selectByExample(deviceInfoExample);

			DeviceStatus data = null;
			for (DeviceInfo deviceInfo : deviceInfoList) {
				data = new DeviceStatus();
				data.setElecAddr(deviceInfo.getElecAddr());
				if ("01".equals(deviceInfo.getElecStatus())) {
					data.setElecStatus(Constant.DeviceStatus.CLOSE.name());
				} else {
					data.setElecStatus(Constant.DeviceStatus.OPEN.name());
				}
				DecimalFormat df = new DecimalFormat("0.00");
				data.setElecLeftPoint(df.format(deviceInfo.getElecLeftPoint().floatValue() / 100));
				data.setElecUsePoint(df.format(deviceInfo.getElecUsePoint().floatValue() / 100));
				data.setOuterNo(deviceInfo.getOuterNo());
				dataList.add(data);
			}
		}
		return dataList;
	}

}
