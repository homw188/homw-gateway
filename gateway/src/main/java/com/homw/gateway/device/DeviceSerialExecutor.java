package com.homw.gateway.device;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.constant.DeviceConstant;
import com.homw.gateway.constant.DeviceConstant.SerialExecutorMode;

/**
 * @description 设备串行操作任务执行器
 * @author Hom
 * @version 1.0
 * @since 2020-11-11
 */
@Component
public class DeviceSerialExecutor {

	private static Logger logger = LoggerFactory.getLogger(DeviceSerialExecutor.class);

	@Value("${serial.executor.mode}")
	private String serialExecutorMode; // 串行执行器模式

	private Map<Constant.DeviceType, ThreadPoolExecutor> executorMap = new HashMap<>();

	@PostConstruct
	public void init() {
		SerialExecutorMode mode = getExecutorMode();
		if (mode == DeviceConstant.SerialExecutorMode.OVERALL) {
			ThreadPoolExecutor executor = getExecutor(null);
			for (Constant.DeviceType type : Constant.DeviceType.values()) {
				executorMap.put(type, executor);
			}
		} else if (mode == DeviceConstant.SerialExecutorMode.PART) {
			for (Constant.DeviceType type : Constant.DeviceType.values()) {
				if (type != Constant.DeviceType.DOOR) {
					executorMap.put(type, getExecutor(type));
				}
			}
		}
	}

	public DeviceConstant.SerialExecutorMode getExecutorMode() {
		if (StringUtils.isEmpty(serialExecutorMode)) {
			return DeviceConstant.SerialExecutorMode.OVERALL;
		}

		for (DeviceConstant.SerialExecutorMode mode : DeviceConstant.SerialExecutorMode.values()) {
			if (mode.name().equalsIgnoreCase(serialExecutorMode.trim())) {
				return mode;
			}
		}
		throw new ServiceException("not supported serial executor mode -> " + serialExecutorMode);
	}

	public <T> Future<T> submit(Callable<T> task, Constant.DeviceType type) {
		ThreadPoolExecutor executor = executorMap.get(type);
		if (executor != null) {
			return executor.submit(task);
		}
		logger.warn("not found serial executor for type: {}", type);
		return null;
	}

	private ThreadPoolExecutor getExecutor(Constant.DeviceType type) {
		return new PriorityThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, DeviceConstant.QUEUED_TASK_COUNT,
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, type == null ? "DeviceSerialExecutorThread"
								: type.name() + "-DeviceSerialExecutorThread");
					}
				}, new AbortPolicy());
	}
}
