package com.homw.gateway.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.constant.Constant.MessageType;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.constant.DeviceConstant.CommandType;
import com.homw.gateway.device.proxy.DoorDeviceProxy;
import com.homw.gateway.device.proxy.ElecDeviceProxy;
import com.homw.gateway.device.proxy.WaterDeviceProxy;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.CommandMessage;
import com.homw.gateway.entity.dto.DeviceOperator;
import com.rabbitmq.client.Channel;

/**
 * @description 消息队列监听器
 * @author Hom
 * @version 1.0
 * @date 2020-01-14
 */
public class RabbitmqMessageListener implements ChannelAwareMessageListener {
	private static final Logger logger = LoggerFactory.getLogger(RabbitmqMessageListener.class);

	@Autowired
	private IRedisDeviceInfoService redisDeviceInfoService;
	@Autowired
	private DoorDeviceProxy doorDeviceProxy;
	@Autowired
	private ElecDeviceProxy elecDeviceProxy;
	@Autowired
	private WaterDeviceProxy waterDeviceProxy;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		logger.debug("recv msg time:{}", StringUtil.formatTimestamp(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
		try {
			CommandMessage msg = parseMessage(message, channel);
			DeviceInfo deviceInfo = null;
			if (msg != null) {
				try {
					CommandType commandType = null;
					MessageType messageType = Constant.MessageType.valueOf(msg.getMessageType());
					switch (messageType) {
						case DOOR:
							deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(msg.getOuterNo(),
									Constant.MessageType.DOOR.name());
							doorDeviceProxy.open(deviceInfo, new DeviceOperator(msg));
							break;
						case ELECTRIC:
							commandType = CommandType.valueOf(msg.getCommandType());
							deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(msg.getOuterNo(),
									Constant.MessageType.ELECTRIC.name());
							switch (commandType) {
								case ELEC_OPEN:
									elecDeviceProxy.open(deviceInfo, new DeviceOperator(msg));
									break;
								case ELEC_CLOSE:
									elecDeviceProxy.close(deviceInfo, new DeviceOperator(msg));
									break;
								case ELEC_SEARCH:
									elecDeviceProxy.search(deviceInfo);
									break;
								default:
									break;
							}
							break;
						case WATER:
							commandType = CommandType.valueOf(msg.getCommandType());
							deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(msg.getOuterNo(),
									Constant.MessageType.WATER.name());
							switch (commandType) {
								case WATER_SEARCH:
									waterDeviceProxy.search(deviceInfo);
									break;
								default:
									break;
							}
							break;
						default:
							break;
					}
					manualAck(message, channel);
				} catch (Exception e) {
					logger.error("device react error", e);
					manualAck(message, channel);
				}
			}
		} catch (Exception e) {
			logger.error("deal with msg error", e);
		}
	}
	
	private CommandMessage parseMessage(Message message, Channel channel) throws IOException {
		CommandMessage msg = null;
		try {
			String body = new String(message.getBody(), "UTF-8");
			logger.info("msg properties: {}", message.getMessageProperties());
			logger.info("msg body: {}", body);
			// avoid JSONException: syntax error for like "{\"commandType\":\"DOOR_OPEN\"}"
			// msg = JSON.parseObject(body, CommandMessage.class);
			msg = JSON.parseObject(JSON.parse(body).toString(), CommandMessage.class);
			msg.setCreateTime(message.getMessageProperties().getTimestamp().getTime());
		} catch (Exception e) {
			logger.error("format error", e);
			manualAck(message, channel);
			msg = null;
		}
		return msg;
	}

	private void manualAck(Message message, Channel channel) throws IOException {
		channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	}
}
