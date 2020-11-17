package com.homw.gateway.common.service;

import com.homw.gateway.entity.DeviceElectricUseInfo;
import com.homw.gateway.entity.DeviceInfo;

public interface IElecUseInfoService {

	/**
	 * <b>按天保存抄表数据</b><br>
	 * 1、获取设备的最新读数<br> 
	 * 2、判断该设备当天有无用电记录，如果没有则在数据库中保存，有的话就更新当前设备的使用电量
	 * 
	 * @param deviceInfo
	 * @return
	 */
	DeviceElectricUseInfo saveElecUseInfo(DeviceInfo deviceInfo);

	/**
	 * <b>计算空调外机对应电量</b><br>
	 * 判断当前设备为空调外机的时候，需要去计算当前外机关联所有内机的分摊电量
	 * 
	 * @param deviceElectricUseInfo
	 */
	void calcuateElecUseInfo(DeviceElectricUseInfo deviceElectricUseInfo);
}
