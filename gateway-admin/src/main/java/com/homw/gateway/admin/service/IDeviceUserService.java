package com.homw.gateway.admin.service;

import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.entity.DeviceUser;

public interface IDeviceUserService {

	DeviceUser selectUserByPam(String name,String password);
	
	boolean changeecsPassword(Long userId, String loginPwd,String newloginPwd,Operator operators);
}
