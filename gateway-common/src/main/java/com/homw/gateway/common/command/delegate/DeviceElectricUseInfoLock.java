package com.homw.gateway.common.command.delegate;

import com.homw.gateway.common.dao.DeviceElectricUseInfoDao;
import com.homw.gateway.entity.DeviceElectricUseInfo;
import com.homw.gateway.entity.example.DeviceElectricUseInfoExample;

public abstract class DeviceElectricUseInfoLock implements OptimisticLock<DeviceElectricUseInfo> {

	private DeviceElectricUseInfo deviceElectricUseInfo;
	private DeviceElectricUseInfoDao deviceElectricUseInfoDao;

	public DeviceElectricUseInfoLock(DeviceElectricUseInfo deviceElectricUseInfo,
			DeviceElectricUseInfoDao deviceElectricUseInfoMapper) {
		this.deviceElectricUseInfo = deviceElectricUseInfo;
		this.deviceElectricUseInfoDao = deviceElectricUseInfoMapper;
	}

	@Override
	public DeviceElectricUseInfo lock() {
		return deviceElectricUseInfo;
	}

	@Override
	public int unlock(DeviceElectricUseInfo deviceElectricUseInfo) {
		DeviceElectricUseInfoExample deviceElectricUseInfoExample = new DeviceElectricUseInfoExample();
		deviceElectricUseInfoExample.createCriteria()
				.andDeviceUseInfoIdEqualTo(deviceElectricUseInfo.getDeviceUseInfoId())
				.andVersionEqualTo(deviceElectricUseInfo.getVersion());
		deviceElectricUseInfo.setUpdateTime(System.currentTimeMillis());
		deviceElectricUseInfo.setVersion(deviceElectricUseInfo.getVersion() + 1);
		int num = deviceElectricUseInfoDao.updateByExampleSelective(deviceElectricUseInfo,
				deviceElectricUseInfoExample);
		return num;
	}

	@Override
	public void removeCache(DeviceElectricUseInfo deviceElectricUseInfo) {
	}
}
