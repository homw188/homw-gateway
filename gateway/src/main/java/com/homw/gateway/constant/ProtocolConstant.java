package com.homw.gateway.constant;

public interface ProtocolConstant {
	public interface Kede {
		int TD_0 = 0; // 通/合闸

		int TD_1 = 1; // 断/拉闸
		
		int READ_TIMEOUT = 3; // 电表读超时，单位秒

		int CONNECT_TIMEOUT = 1500; // 连接超时，单位毫秒
	}

	public interface Keda {
		int OPEN = 1; // 送电

		int CLOSE = 0; // 断电

		String OPEN_WATER = "55"; // 开阀

		String CLOSE_WATER = "99"; // 关阀
		
		int READ_TIMEOUT = 3; // 电表读超时，单位秒
		
		String CODE_ERROR = "Err"; // 错误码

		String CODE_OK = "OK";
	}
}
