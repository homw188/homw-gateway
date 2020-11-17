package com.homw.gateway.admin.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.bean.Page;
import com.homw.gateway.admin.bean.PageRequest;
import com.homw.gateway.admin.service.IPublicDeviceService;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dao.DeviceCommandLogDao;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.entity.DeviceCommandLog;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.example.DeviceCommandLogExample;
import com.homw.gateway.entity.example.DeviceInfoExample;
import com.homw.gateway.entity.example.DeviceInfoExample.Criteria;

@Service("publicDeviceService")
public class PublicDeviceServiceImpl implements IPublicDeviceService {

	@Autowired
	private DeviceInfoDao deviceInfoDao;
	@Autowired
	private DeviceCommandLogDao deviceCommandLogDao;
	@Autowired
	private IRedisDeviceInfoService redisDeviceInfoService;

	public int selectDevicePageCountByType(Constant.DeviceType type, PageRequest pageRequest) {
		try {
			List<Short> status = Lists.newArrayList();
			status.add((short) 1);
			status.add((short) 2);
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());

			DeviceInfoExample syexample = new DeviceInfoExample();
			syexample.createCriteria().andStatusIn(status).andDeviceTypeIn(deviceType);

			Integer pageStart = (pageRequest.getPage() - 1) * pageRequest.getLimit();
			syexample.setCount((long) pageRequest.getLimit());
			syexample.setStart(pageStart.longValue());
			syexample.setOrderByClause(" UPDATE_TIME, DEVICE_ID desc");

			return deviceInfoDao.countByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public int selectCmdLogPageCountByType(Constant.DeviceType type, PageRequest pageRequest) {
		try {
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());

			DeviceCommandLogExample syexample = new DeviceCommandLogExample();
			syexample.createCriteria().andDeviceTypeIn(deviceType);

			Integer pageStart = (pageRequest.getPage() - 1) * pageRequest.getLimit();
			syexample.setCount((long) pageRequest.getLimit());
			syexample.setStart(pageStart.longValue());
			syexample.setOrderByClause(" LOG_ID desc");

			return deviceCommandLogDao.countByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public Page<DeviceInfo> selectDevicePageByType(Constant.DeviceType type, PageRequest pageRequest, String doorName,
			String outerNo) {
		try {
			List<Short> status = Lists.newArrayList();
			status.add((short) 1);
			status.add((short) 2);
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());
			DeviceInfoExample syexample = new DeviceInfoExample();
			Criteria criteria = syexample.createCriteria();

			criteria.andStatusIn(status);
			criteria.andDeviceTypeIn(deviceType);

			if (null != doorName && !doorName.equals("")) {
				criteria.andDoorNameLike("%" + doorName + "%");
			}
			if (null != outerNo && !outerNo.equals("")) {
				criteria.andOuterNoLike("%" + outerNo + "%");
			}

			Integer pageStart = (pageRequest.getPage() - 1) * pageRequest.getLimit();
			syexample.setCount((long) pageRequest.getLimit());
			syexample.setStart(pageStart.longValue());
			syexample.setOrderByClause(" UPDATE_TIME, DEVICE_ID desc");

			List<DeviceInfo> sysProperty = deviceInfoDao.selectByExample(syexample);
			int cnt = deviceInfoDao.countByExample(syexample);
			return new Page<DeviceInfo>(sysProperty, cnt, pageRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public List<DeviceInfo> selectDeviceByType(Constant.DeviceType type, String doorName, String outerNo) {
		try {
			List<Short> status = Lists.newArrayList();
			status.add((short) 1);
			status.add((short) 2);
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());
			DeviceInfoExample syexample = new DeviceInfoExample();
			Criteria criteria = syexample.createCriteria();

			criteria.andStatusIn(status);
			criteria.andDeviceTypeIn(deviceType);

			if (null != doorName && !doorName.equals("")) {
				criteria.andDoorNameLike("%" + doorName + "%");
			}
			if (null != outerNo && !outerNo.equals("")) {
				criteria.andOuterNoLike("%" + outerNo + "%");
			}

			syexample.setOrderByClause(" UPDATE_TIME, DEVICE_ID desc");

			return deviceInfoDao.selectByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Page<DeviceCommandLog> selectCmdLogPageByType(Constant.DeviceType type, PageRequest pageRequest, String doorName,
			String userMobile, String startTime, String endTime) {
		try {
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());
			DeviceCommandLogExample syexample = new DeviceCommandLogExample();
			DeviceCommandLogExample.Criteria criteria = syexample.createCriteria();

			criteria.andDeviceTypeIn(deviceType);

			if (null != doorName && !doorName.equals("")) {
				criteria.andDoorNameLike("%" + doorName + "%");
			}
			if (null != userMobile && !userMobile.equals("")) {
				criteria.andUserMobileLike("%" + userMobile + "%");
			}

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (null != startTime && !startTime.equals("")) {
				criteria.andCreateTimeGreaterThanOrEqualTo(df.parse(startTime));
			}
			if (null != endTime && !endTime.equals("")) {
				criteria.andCreateTimeLessThan(df.parse(endTime));
			}

			Integer pageStart = (pageRequest.getPage() - 1) * pageRequest.getLimit();
			syexample.setCount((long) pageRequest.getLimit());
			syexample.setStart(pageStart.longValue());
			syexample.setOrderByClause(" LOG_ID desc");

			List<DeviceCommandLog> sysProperty = deviceCommandLogDao.selectByExample(syexample);
			int cnt = deviceCommandLogDao.countByExample(syexample);
			return new Page<DeviceCommandLog>(sysProperty, cnt, pageRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public List<DeviceCommandLog> selectCmdLogByType(Constant.DeviceType type, String doorName, String userMobile,
			String startTime, String endTime) {
		try {
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(type.name());
			DeviceCommandLogExample syexample = new DeviceCommandLogExample();
			DeviceCommandLogExample.Criteria criteria = syexample.createCriteria();

			criteria.andDeviceTypeIn(deviceType);

			if (null != doorName && !doorName.equals("")) {
				criteria.andDoorNameLike("%" + doorName + "%");
			}
			if (null != userMobile && !userMobile.equals("")) {
				criteria.andUserMobileLike("%" + userMobile + "%");
			}

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (null != startTime && !startTime.equals("")) {
				criteria.andCreateTimeGreaterThanOrEqualTo(df.parse(startTime));
			}
			if (null != endTime && !endTime.equals("")) {
				criteria.andCreateTimeLessThan(df.parse(endTime));
			}

			syexample.setOrderByClause(" LOG_ID desc");

			return deviceCommandLogDao.selectByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void insertDevice(DeviceInfo prd, Operator operators) {
		try {
			prd.setCreateUserId(operators.getId());
			prd.setCreateTime(System.currentTimeMillis());
			prd.setUpdateTime(System.currentTimeMillis());
			prd.setUpdateUserId(operators.getId());
			prd.setUpdateUserType(operators.getType());
			prd.setCreateUserType(operators.getType());
			prd.setStatus((short) 1);

			if (deviceInfoDao.insertSelective(prd) == 0) {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR, "insertDevice version fail");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "insertDevice printStackTrace");
		}
	}

	public void softDeleteById(Long deviceId, Operator operators) {
		try {
			DeviceInfo sp = new DeviceInfo();
			sp.setDeviceId(deviceId);
			sp.setStatus((short) 2);
			sp.setUpdateTime(System.currentTimeMillis());
			sp.setUpdateUserId(operators.getId());
			sp.setUpdateUserType(operators.getType());

			DeviceInfoExample pex = new DeviceInfoExample();
			pex.createCriteria().andDeviceIdEqualTo(deviceId);

			if (deviceInfoDao.updateByExampleSelective(sp, pex) == 0) {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deleteDevice version fail");
			}
			redisDeviceInfoService.redisDeleteDeviceInfo(sp.getOuterNo(), sp.getDeviceType());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deleteDevice printStackTrace");
		}
	}

	public DeviceInfo selectDeviceById(Long deviceId) {
		try {
			return deviceInfoDao.selectByPrimaryKey(deviceId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "selectDeviceById printStackTrace");
		}
	}

	public void updateDeviceById(DeviceInfo prd, Operator operators) {
		try {
			DeviceInfo sp = new DeviceInfo();
			sp.setOuterNo(prd.getOuterNo());
			sp.setDoorIp(prd.getDoorIp());
			sp.setDoorPort(prd.getDoorPort());
			sp.setDoorAddr(prd.getDoorAddr());
			sp.setDoorName(prd.getDoorName());
			sp.setElecAddr(prd.getElecAddr());
			sp.setElecStatus(prd.getElecStatus());
			sp.setElecUsePoint(prd.getElecUsePoint());
			sp.setElecLeftPoint(prd.getElecLeftPoint());
			sp.setVersion(prd.getVersion() + 1);

			DeviceInfoExample pex = new DeviceInfoExample();
			pex.createCriteria().andDeviceIdEqualTo(prd.getDeviceId());

			if (deviceInfoDao.updateByExampleSelective(sp, pex) == 0) {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deviceUpdate version fail");
			}

			redisDeviceInfoService.redisDeleteDeviceInfo(sp.getOuterNo(), sp.getDeviceType());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "deviceUpdate printStackTrace");
		}
	}

	/**
	 * 查询所有电表
	 */
	public List<DeviceInfo> findPowerAllListbypam(String devicetype) {
		try {
			List<Short> status = Lists.newArrayList();
			status.add((short) 1);
			// status.add((short)2);
			List<String> deviceType = new ArrayList<String>();
			deviceType.add(devicetype);

			DeviceInfoExample syexample = new DeviceInfoExample();
			syexample.setOrderByClause(" IS_REFER_NODE ASC");
			syexample.createCriteria().andStatusIn(status).andDeviceTypeIn(deviceType);

			return deviceInfoDao.selectByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Lists.newArrayList();
	}

	/**
	 * 查询空调外机的电表
	 */
	@Override
	public List<DeviceInfo> findElecReferNode() {
		try {
			List<Short> status = Lists.newArrayList();
			status.add((short) 1);

			DeviceInfoExample syexample = new DeviceInfoExample();
			syexample.setOrderByClause(" IS_REFER_NODE ASC");
			syexample.createCriteria().andStatusIn(status).andDeviceTypeEqualTo(Constant.DeviceType.ELECTRIC.name())
					.andIsReferNodeEqualTo(true);

			return deviceInfoDao.selectByExample(syexample);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return Lists.newArrayList();
	}

}
