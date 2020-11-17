package com.homw.gateway.device.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homw.gateway.api.constant.DeviceBrand;
import com.homw.gateway.api.device.IDoorDevice;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

/**
 * @description 中控门禁设备操作指令处理
 * @author Hom
 * @version 1.0
 * @since 2020-11-03
 */
@Service("zkDoorDevice")
public class ZkDoorDeviceImpl implements IDoorDevice {
	private static Logger logger = LoggerFactory.getLogger(ZkDoorDeviceImpl.class);

	@Value("${zkDoor.apiToken}")
	private String apiToken;
	private int holdInterval = 5;// 开门时长，单位：秒

	@Override
	public Pair<Boolean, String> open(DeviceInfo deviceInfo, DeviceOperator operator) throws Exception {
		String url = "http://" + deviceInfo.getDoorIp() + ":" + deviceInfo.getDoorPort()
				+ "/api/door/remoteOpenById?doorId=" + deviceInfo.getDoorAddr() + "&interval=" + holdInterval
				+ "&access_token=" + apiToken;
		logger.info("url: {}", url);
		HttpPost request = new HttpPost(url);
		CloseableHttpResponse response = HttpClients.createDefault().execute(request);
		if (response != null && response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity());
			JSONObject obj = JSON.parseObject(result);
			int code = obj.getIntValue("code");
			if (code > 0) {
				return Pair.of(Boolean.TRUE, result);
			} else {
				return Pair.of(Boolean.FALSE, result);
			}
		}
		return Pair.of(Boolean.FALSE, null);
	}

	@Override
	public DeviceBrand.DoorDeviceBrand getBrand() {
		return DeviceBrand.DoorDeviceBrand.ZHONGKONG;
	}
}
