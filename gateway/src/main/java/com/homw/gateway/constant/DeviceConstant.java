package com.homw.gateway.constant;

public interface DeviceConstant {

	int QUEUED_TASK_COUNT = 1000;

	enum CommandType {
		DOOR_OPEN("开门"), ELEC_OPEN("开电源"), ELEC_CLOSE("关电源"), ELEC_SEARCH("电表示数查询"), WATER_SEARCH("水表示数查询"),
		DEVICE_BATCH_SEARCH("设备查询"), DEVICE_BATCH_SEARCHV2("设备查询V2"), DEVICE_BATCH_SEARCH_MQRPC("设备查询"),
		DEVICE_BATCH_SEARCHV2_MQRPC("设备查询V2"), WATER_BATCH_SEARCH("水表抄表"), FACE_UPLOAD("上传人脸"), FACE_DELETE("删除人脸");

		private String commandName;

		CommandType(String commandName) {
			this.commandName = commandName;
		}

		public String getCommandName() {
			return commandName;
		}
	}

	enum ElecProtocol {
		TCPIP, COMPORT
	}

	enum SerialExecutorMode {
		PART, OVERALL
	}

}
