package com.homw.gateway.api.device;

import org.apache.commons.lang3.tuple.Pair;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 门禁设备操作指令处理接口
 * @author Hom
 * @version 1.0
 * @since 2020-10-27
 */
public interface IDoorDevice {
	/**
	 * 开门
	 * 
	 * @param deviceInfo
	 * @param operator
	 * @return <code>pair-left: success status<br>
	 *         pair-right: back data</code>
	 * @throws Exception
	 */
	Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception;

	/**
	 * 查询设备品牌
	 * 
	 * @return
	 */
	DeviceBrand.DoorDeviceBrand getBrand();
}
