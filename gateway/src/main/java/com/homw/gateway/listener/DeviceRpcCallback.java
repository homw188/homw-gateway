package com.homw.gateway.listener;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.homw.gateway.api.koala.KoalaApi_3_1;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.constant.Constant.MessageType;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.dto.BaseResponse;
import com.homw.gateway.common.service.IDeviceService;
import com.homw.gateway.constant.DeviceConstant.CommandType;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.CommandMessage;
import com.homw.gateway.entity.dto.FaceCommandMessage;
import com.homw.gateway.entity.example.DeviceInfoExample;
import com.homw.rabbit.rpc.RabbitRoute;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.handler.RpcCallback;
import com.homw.rabbit.rpc.util.EndpointUtil;

/**
 * @description 设备操作指令同步处理回调
 * @author Hom
 * @version 1.0
 * @since 2020-10-20
 */
@Service
public class DeviceRpcCallback implements RpcCallback {
	private static final Logger logger = LoggerFactory.getLogger(IDeviceService.class);

	@Autowired
	private DeviceInfoDao deviceInfoDao;
	@Autowired
	private IDeviceService deviceService;

	@Value("${rabbit.queue.rpc}")
	private String rpcQueue;

	@Value("${koala.url}")
	private String koalaUrl;
	@Value("${koala.userName}")
	private String koalaUserName;
	@Value("${koala.password}")
	private String koalaPassword;
	@Value("${koala.enabled}")
	private String koalaEnabled;

	public boolean koalaEnabled() {
		return !StringUtils.isEmpty(koalaEnabled) && "true".equals(koalaEnabled.trim());
	}

	@PostConstruct
	void init() {
		EndpointUtil.startRpcServer(new RabbitRoute(RpcConstant.EXCHANGE, rpcQueue), this);

		if (koalaEnabled()) {
			try {
				// KoalaApi_3_0.init(koalaUrl, koalaUserName, koalaPassword);
				KoalaApi_3_1.init(koalaUrl, koalaUserName, koalaPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String call(String message) {
		logger.info("message: {}", message);
		CommandMessage cmdMsg = JSON.parseObject(message, CommandMessage.class);
		MessageType messageType = Constant.MessageType.valueOf(cmdMsg.getMessageType());
		CommandType commandType = CommandType.valueOf(cmdMsg.getCommandType());
		logger.info("deviceType: {}", messageType.name());
		logger.info("commandType: {}", cmdMsg.getCommandType());

		String result = null;
		try {
			switch (messageType) {
				case SEARCH:
					result = doSearchCommand(cmdMsg, commandType);
					break;
				case FACE:
					result = doFaceCommand(message, commandType);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			logger.error("device rpc callback failed", e);
		}
		return result == null ? "{没有查询数据!}" : result;
	}

	private String doSearchCommand(CommandMessage cmdMsg, CommandType commandType) throws Exception {
		String[] deviceNoArr = cmdMsg.getOuterNo().split(",");
		logger.info("deviceNos: {}, size: {}", cmdMsg.getOuterNo(), deviceNoArr.length);
		List<?> dataList = null;
		String response = null;
		switch (commandType) {
			case DEVICE_BATCH_SEARCH:
				dataList = deviceService.queryBatchStatus(deviceNoArr);
				logger.info("dataList size: {}", dataList.size());
				response = JSON.toJSONString(dataList);
				logger.debug("response: {}", response);
				return response;
			case DEVICE_BATCH_SEARCHV2:
				if (cmdMsg.getCurDay() == null) {
					dataList = deviceService.queryBatchStatus(deviceNoArr);
				} else {
					dataList = deviceService.queryBatchDataPoint(cmdMsg.getCurDay(), deviceNoArr);
				}
				logger.info("dataList size:{}", dataList.size());
				response = JSON.toJSONString(dataList);
				logger.debug("response: {}", response);
				return response;
			case WATER_BATCH_SEARCH:
				dataList = deviceService.queryBatchWaterStatus(deviceNoArr);
				logger.info("result size:{}", dataList.size());
				response = JSON.toJSONString(dataList);
				logger.debug("response: {}", response);
				return response;
			default:
				break;
		}
		return null;
	}

	private String doFaceCommand(String message, CommandType commandType) {
		FaceCommandMessage cmdMsg = JSON.parseObject(message, FaceCommandMessage.class);
		String[] deviceNos = cmdMsg.getOuterNo().split(",");
		logger.info("deviceNos: {}, size: {}", cmdMsg.getOuterNo(), deviceNos.length);
		BaseResponse response = null;
		switch (commandType) {
			case FACE_UPLOAD:
				response = new BaseResponse();
				try {
					// 1、查找人脸识别设备id列表
					List<Integer> faceDeviceIdList = queryFaceDeviceIdList(deviceNos);

					// 2、上传人脸识别用户信息
					try {
						// KoalaApi_3_0.uploadUserInfo(koalaUrl, sqsFaceMessage, faceDeviceIdList);
						KoalaApi_3_1.uploadUserInfo(koalaUrl, cmdMsg, faceDeviceIdList);
					} catch (Exception e) {
						response.setCode(ErrorCode.FACE_UPLOAD_ERROR);
						response.setMessage(e.getMessage());
						logger.error(e.getMessage(), e);
					}
				} catch (Exception e) {
					response.setCode(ErrorCode.SYSTEM_ERROR);
					response.setMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				}
				return JSON.toJSONString(response);
			case FACE_DELETE:
				response = new BaseResponse();
				try {
					// 1、查找人脸识别设备id列表
					List<Integer> faceDeviceIdList = queryFaceDeviceIdList(deviceNos);

					// 2、删除人脸识别用户信息
					try {
						// KoalaApi_3_0.deleteUserInfo(koalaUrl, sqsFaceMessage, faceDeviceIdList);
						KoalaApi_3_1.deleteUserInfo(koalaUrl, cmdMsg, faceDeviceIdList);
					} catch (Exception e) {
						response.setCode(ErrorCode.FACE_UPLOAD_ERROR);
						response.setMessage(e.getMessage());
						logger.error(e.getMessage(), e);
					}
				} catch (Exception e) {
					response.setCode(ErrorCode.SYSTEM_ERROR);
					response.setMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				}
				return JSON.toJSONString(response);
			default:
				break;
		}
		return null;
	}

	/**
	 * 查找人脸识别设备id
	 * 
	 * @param deviceNoArr
	 * @return
	 */
	private List<Integer> queryFaceDeviceIdList(String[] deviceNoArr) {
		List<Integer> faceDeviceIdList = Lists.newArrayList();
		if (deviceNoArr.length > 0) {
			List<String> types = Lists.newArrayList();
			types.add(Constant.DeviceType.DOOR.name());

			DeviceInfoExample deviceInfoExample = new DeviceInfoExample();
			deviceInfoExample.createCriteria().andDeviceTypeIn(types).andOuterNoIn(Arrays.asList(deviceNoArr));
			List<DeviceInfo> deviceInfos = deviceInfoDao.selectByExample(deviceInfoExample);

			if (CollectionUtils.isNotEmpty(deviceInfos)) {
				for (DeviceInfo dev : deviceInfos) {
					faceDeviceIdList.add(dev.getFaceDeviceId());
				}
			}
		}
		return faceDeviceIdList;
	}

}
