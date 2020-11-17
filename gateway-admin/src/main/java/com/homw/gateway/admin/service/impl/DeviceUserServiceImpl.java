package com.homw.gateway.admin.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.service.IDeviceUserService;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dao.DeviceUserDao;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.DeviceUser;
import com.homw.gateway.entity.example.DeviceUserExample;

@Service("deviceUserService")
public class DeviceUserServiceImpl implements IDeviceUserService {

	private static Logger logger = LoggerFactory.getLogger(DeviceUserServiceImpl.class);

	@Autowired
	private DeviceUserDao deviceUserDao;
	
	public DeviceUser selectUserByPam(String name, String password) {
		try {
			DeviceUserExample syexample = new DeviceUserExample();
			syexample.createCriteria().andUserNameEqualTo(name).andPasswordEqualTo(password);
			List<DeviceUser> sys = deviceUserDao.selectByExample(syexample);
			if (null != sys && sys.size() > 0) {
				return sys.get(0);
			} else {
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean changeecsPassword(Long employeeId, String oldPassword, String newPassword, Operator operators) {
		boolean res = false;
		DeviceUser emps = new DeviceUser();
		emps.setUserId(employeeId);
		DeviceUser users = selectUserById(employeeId);
		if (users == null) {
			throw new ServiceException("000001", "当前用户信息为空");
		}

		oldPassword = StringUtil.getMD5(oldPassword);
		if (!oldPassword.equals(users.getPassword())) {
			throw new ServiceException("000001", "当前输入的旧密码不正确!");
		}

		newPassword = StringUtil.getMD5(newPassword);
		emps.setPassword(newPassword);

		updateaccountpwd(emps, operators);
		res = true;

		logger.info("修改密码成功:employeeId =" + employeeId);
		return res;
	}

	public void updateaccountpwd(final DeviceUser emps, Operator operators) {
		try {
			DeviceUser sp = new DeviceUser();
			sp.setUserId(emps.getUserId());
			sp.setPassword(emps.getPassword());

			DeviceUserExample pex = new DeviceUserExample();
			pex.createCriteria().andUserIdEqualTo(emps.getUserId());

			if (deviceUserDao.updateByExampleSelective(sp, pex) == 0) {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR, "updateaccountpwd version fail");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "updateaccountpwd printStackTrace");
		}
	}

	public DeviceUser selectUserById(Long Id) {
		try {
			return deviceUserDao.selectByPrimaryKey(Id);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
