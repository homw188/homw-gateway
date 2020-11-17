package com.homw.gateway.common.constant;

public interface Constant {

	String REDIS_EMPTY_KEY = "empty:";

	String REDIS_EMPTY_VALUE = "1";

	Long REDIS_EMPTY_EXPIRE_TIME = 60L;

	String REDIS_DEVICE_KEY = "device:deviceNo:";

	String REDIS_SIGN_PROPERTY_KEY = "sign:property:";

	enum MessageType {
		DOOR, // 门禁操作
		ELECTRIC, // 电源操作
		WATER, // 水表操作
		SEARCH, // 查询操作
		FACE// 人脸识别操作
	}
	
	enum DeviceType {
		DOOR, // 门禁
		ELECTRIC, // 电源
		WATER // 水表
	}

	enum DeviceStatus {
		OPEN, CLOSE
	}

	enum UserType {
		C, E
	}

	enum MessageReceiverType {
		SYS, USER, MANAGER
	}
}
