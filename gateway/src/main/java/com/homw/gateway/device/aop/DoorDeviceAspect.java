package com.homw.gateway.device.aop;

import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;
import com.homw.gateway.event.DeviceLogEvent;

/**
 * @description 门禁设备操作通用业务逻辑<br>
 *              1、记录请求参数；2、执行操作指令；3、记录操作日志
 * @author Hom
 * @version 1.0
 * @since 2020-11-03
 */
@Aspect
@Component
public class DoorDeviceAspect implements ApplicationEventPublisherAware {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Pointcut("execution(* com.homw.gateway.device.impl.*DoorDeviceImpl.open(..))")
	public void open() {
	}

	@Around("open()")
	@SuppressWarnings("unchecked")
	public Pair<Boolean, String> open(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		DeviceInfo deviceInfo = (DeviceInfo) args[0];
		DeviceOperator operator = (DeviceOperator) args[1];

		logger.info("deviceNo: {}", deviceInfo.getOuterNo());
		logger.info("doorAddr: {}", deviceInfo.getDoorAddr());
		logger.info("readNo: {}", deviceInfo.getDoorReadno());
		try {
			Object result = joinPoint.proceed();
			Pair<Boolean, String> pair = (Pair<Boolean, String>) result;
			logger.info("backData: {}", pair.getRight());
			if (pair.getLeft()) {
				logger.info("open success");
			} else {
				logger.warn("open failed");
			}
			return pair;
		} catch (Exception e) {
			logger.error("设备指令执行异常", e);
		} finally {
			// 记录操作日志
			applicationEventPublisher.publishEvent(new DeviceLogEvent(this, operator, deviceInfo));
		}
		return Pair.of(Boolean.FALSE, null);
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
