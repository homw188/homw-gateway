package com.homw.gateway.common.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homw.gateway.common.command.OptimisticLockCommand;
import com.homw.gateway.common.command.delegate.DeviceElectricUseInfoLock;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dao.DeviceElectricUseInfoDao;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.service.IElecUseInfoService;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.DeviceElectricUseInfo;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.example.DeviceElectricUseInfoExample;

@Service("elecUseInfoService")
public class ElecUseInfoServiceImpl implements IElecUseInfoService {

	private static Logger logger = LoggerFactory.getLogger(ElecUseInfoServiceImpl.class);

	@Autowired
	private DeviceElectricUseInfoDao deviceElectricUseInfoDao;
	@Autowired
	private DeviceInfoDao deviceInfoDao;

	@Override
	@Transactional
	public DeviceElectricUseInfo saveElecUseInfo(final DeviceInfo deviceInfo) {
		String nowDay = StringUtil.formatTimestamp(System.currentTimeMillis(), "yyyyMMdd");
		String yesDay = StringUtil.formatTimestamp(StringUtil.addDay(System.currentTimeMillis(), -1), "yyyyMMdd");
		DeviceElectricUseInfoExample deviceElectricUseInfoExample = new DeviceElectricUseInfoExample();
		deviceElectricUseInfoExample.createCriteria().andDeviceIdEqualTo(deviceInfo.getDeviceId())
				.andStatusEqualTo((short) 1).andNowdayEqualTo(nowDay);
		List<DeviceElectricUseInfo> deviceElecUseList = deviceElectricUseInfoDao
				.selectByExample(deviceElectricUseInfoExample);
		DeviceElectricUseInfo deviceElecUseInfo = null;
		if (deviceElecUseList.size() == 0) {
			deviceElectricUseInfoExample = new DeviceElectricUseInfoExample();
			deviceElectricUseInfoExample.createCriteria().andDeviceIdEqualTo(deviceInfo.getDeviceId())
					.andStatusEqualTo((short) 1).andNowdayEqualTo(yesDay);
			List<DeviceElectricUseInfo> yesdeviceElecUseList = deviceElectricUseInfoDao
					.selectByExample(deviceElectricUseInfoExample);
			if (yesdeviceElecUseList == null || yesdeviceElecUseList.size() == 0) {
				logger.warn("昨天电表度数为0，则取该设备最近一次的抄表的记录");
				// 查询最近有抄表记录的日期
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("nowDay", nowDay);
				map.put("outerNo", deviceInfo.getOuterNo());
				Integer nearDay = deviceElectricUseInfoDao.getNearMaxDay(map);
				if (nearDay == null || nearDay == 0) {
					nearDay = Integer.parseInt(yesDay);
				}
				// 再查上次抄表记录
				deviceElectricUseInfoExample = new DeviceElectricUseInfoExample();
				deviceElectricUseInfoExample.createCriteria().andDeviceIdEqualTo(deviceInfo.getDeviceId())
						.andStatusEqualTo((short) 1).andNowdayEqualTo(nearDay.toString());
				yesdeviceElecUseList = deviceElectricUseInfoDao.selectByExample(deviceElectricUseInfoExample);
				// 上次抄表有数据日期
				yesDay = nearDay.toString();
			}
			deviceElecUseInfo = new DeviceElectricUseInfo();
			deviceElecUseInfo.setCreateTime(System.currentTimeMillis());
			deviceElecUseInfo.setElecAddr(deviceInfo.getElecAddr());
			deviceElecUseInfo.setNowday(nowDay);
			deviceElecUseInfo.setNowdayPoint(deviceInfo.getElecUsePoint());
			deviceElecUseInfo.setOuterNo(deviceInfo.getOuterNo());
			deviceElecUseInfo.setYesday(yesDay);
			deviceElecUseInfo.setReferElecId(deviceInfo.getReferElecId());
			deviceElecUseInfo.setDeviceId(deviceInfo.getDeviceId());
			if (yesdeviceElecUseList.size() == 0) {
				deviceElecUseInfo.setYesdayPoint(0);
				deviceElecUseInfo.setCurUsePoint(0);
			} else {
				deviceElecUseInfo.setYesdayPoint(yesdeviceElecUseList.get(0).getNowdayPoint());
				deviceElecUseInfo
						.setCurUsePoint(deviceElecUseInfo.getNowdayPoint() - deviceElecUseInfo.getYesdayPoint());
			}
			deviceElecUseInfo.setOtherUsePoint(0); // 公摊度数
			if (deviceElectricUseInfoDao.insertSelective(deviceElecUseInfo) == 0) {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR,
						"save deviceElectricUseInfo failed: " + deviceInfo.getOuterNo());
			}
		} else {
			deviceElecUseInfo = deviceElecUseList.get(0);
			new OptimisticLockCommand<DeviceElectricUseInfo>(
					new DeviceElectricUseInfoLock(deviceElecUseInfo, deviceElectricUseInfoDao) {
						@Override
						public void process(DeviceElectricUseInfo device) {
							device.setNowdayPoint(deviceInfo.getElecUsePoint());
							if (device.getYesdayPoint() == null || device.getYesdayPoint() == 0) {
								device.setCurUsePoint(0);
							} else {
								device.setCurUsePoint(device.getNowdayPoint() - device.getYesdayPoint());
							}
						}
					}).exec();
		}
		return deviceElecUseInfo;
	}

	@Override
	@Transactional
	public void calcuateElecUseInfo(DeviceElectricUseInfo deviceElectricUseInfo) {
		if (deviceElectricUseInfo == null)
			return;

		DeviceInfo deviceInfo = deviceInfoDao.selectByPrimaryKey(deviceElectricUseInfo.getDeviceId());
		if (deviceInfo == null || !deviceInfo.getIsReferNode()) {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deviceInfo is null or not referNode, referId:"
					+ deviceElectricUseInfo.getReferElecId() + " is null");
		}

		// 历史上出现过读表为负数的情况，加此逻辑拦截
		if (deviceElectricUseInfo.getCurUsePoint() < 0 || deviceInfo.getElecUsePoint() < 0) {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "电表度数出现负数情况，deviceId=" + deviceInfo.getDeviceId());
		}

		// 查询所有外机对应的内机列表，计算分摊电费
		String nowDay = StringUtil.formatTimestamp(System.currentTimeMillis(), "yyyyMMdd");
		DeviceElectricUseInfoExample deviceElectricUseInfoExample = new DeviceElectricUseInfoExample();
		deviceElectricUseInfoExample.createCriteria().andReferElecIdEqualTo(deviceElectricUseInfo.getDeviceId()) // 外机关联ID
				.andNowdayEqualTo(nowDay).andStatusEqualTo((short) 1);

		List<DeviceElectricUseInfo> innerDeviceElecList = deviceElectricUseInfoDao
				.selectByExample(deviceElectricUseInfoExample);

		Integer innerTotalPoint = 0; // 计算外机所关联的内机设备的累计电量
		Integer outerPoint = deviceElectricUseInfo.getCurUsePoint(); // 外机的使用电量
		for (DeviceElectricUseInfo innerDevice : innerDeviceElecList) {
			innerTotalPoint += innerDevice.getCurUsePoint();
		}

		logger.info("innerTotalPoint: {}", innerTotalPoint);
		if (innerTotalPoint > 0) { // 计算公摊
			for (final DeviceElectricUseInfo innerDevice : innerDeviceElecList) {
				double ratio = innerDevice.getCurUsePoint().doubleValue() / innerTotalPoint.doubleValue(); // 计算占比
				final Integer otherUsePoint = (new BigDecimal(outerPoint).multiply(new BigDecimal(ratio)))
						.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(); // 计算公摊公式
				logger.info("deviceElectricUseInfo before update => outerNo: {}, nowday: {}, otherUsePoint: {}",
						innerDevice.getOuterNo(), innerDevice.getNowday(), innerDevice.getOtherUsePoint());
				if (otherUsePoint > 0) { // 更新公摊电表使用度数
					new OptimisticLockCommand<DeviceElectricUseInfo>(
							new DeviceElectricUseInfoLock(innerDevice, deviceElectricUseInfoDao) {
								@Override
								public void process(DeviceElectricUseInfo device) {
									device.setOtherUsePoint(otherUsePoint);
								}
							}).exec();
				}
			}
		}
	}
}
