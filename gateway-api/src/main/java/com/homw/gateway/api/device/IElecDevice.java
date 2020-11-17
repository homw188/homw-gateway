package com.homw.gateway.api.device;

import org.apache.commons.lang3.tuple.Pair;

import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 电表设备操作指令处理接口
 * @author Hom
 * @version 1.0
 * @since 2020-10-27
 */
public interface IElecDevice {
	/**
	 * 合闸
	 * 
	 * @param deviceInfo
	 * @param operator
	 * @return <code>pair-left: success status<br>
	 *         pair-right: back data</code>
	 * @throws Exception
	 */
	Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception;

	/**
	 * 拉闸
	 * 
	 * @param deviceInfo
	 * @param operator
	 * @return <code>pair-left: success status<br>
	 *         pair-right: back data</code>
	 * @throws Exception
	 */
	Pair<Boolean, String> close(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception;

	/**
	 * 查询
	 * 
	 * @param deviceInfo
	 * @return <code>pair-left: success status<br>
	 *         pair-right: back data</code>
	 * @throws Exception
	 */
	Pair<Boolean, Double> search(DeviceInfo deviceInfo) throws Exception;

	/**
	 * 查询设备状态
	 * 
	 * @param deviceInfo
	 * @return
	 * @throws Exception
	 */
	String searchStatus(DeviceInfo deviceInfo) throws Exception;

	/**
	 * 查询设备品牌
	 * 
	 * @return
	 */
	DeviceBrand.ElecDeviceBrand getBrand();
}
