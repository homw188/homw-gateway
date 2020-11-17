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
import org.springframework.transaction.annotation.Transactional;

import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;
import com.homw.gateway.event.DeviceLogEvent;
import com.homw.gateway.event.ElecUseInfoEvent;

/**
 * @description 水表设备操作通用业务逻辑<br>
 *              1、记录请求参数；2、执行操作指令；3、保存状态；<br>
 *              4、清除缓存；5、计算公摊（可选）；6、记录操作日志
 * @author Hom
 * @version 1.0
 * @since 2020-11-04
 */
@Aspect
@Component
public class WaterDeviceAspect implements ApplicationEventPublisherAware {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IRedisDeviceInfoService redisDeviceInfoService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private DeviceInfoDao deviceInfoDao;

	@Pointcut("execution(* com.homw.gateway.device.impl.*WaterDeviceImpl.open(..))")
	public void open() {
	}

	@Pointcut("execution(* com.homw.gateway.device.impl.*WaterDeviceImpl.close(..))")
	public void close() {
	}

	@Pointcut("execution(* com.homw.gateway.device.impl.*WaterDeviceImpl.search(..))")
	public void search() {
	}

	@Around("open()")
	@SuppressWarnings("unchecked")
	public Pair<Boolean, String> open(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		DeviceInfo deviceInfo = (DeviceInfo) args[0];
		DeviceOperator operator = (DeviceOperator) args[1];

		logger.info("deviceNo: {}", deviceInfo.getOuterNo());
		logger.info("elecAddr: {}", deviceInfo.getElecAddr());
		try {
			Object result = joinPoint.proceed();
			Pair<Boolean, String> pair = (Pair<Boolean, String>) result;
			logger.info("backData: {}", pair.getRight());

			if (pair.getLeft()) {
				updateDeviceStatus(deviceInfo, "00");
				redisDeviceInfoService.redisDeleteDeviceInfo(deviceInfo.getOuterNo(), Constant.DeviceType.WATER.name());
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

	@Around("close()")
	@SuppressWarnings("unchecked")
	public Pair<Boolean, String> close(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		DeviceInfo deviceInfo = (DeviceInfo) args[0];
		DeviceOperator operator = (DeviceOperator) args[1];

		logger.info("deviceNo: {}", deviceInfo.getOuterNo());
		logger.info("elecAddr: {}", deviceInfo.getElecAddr());
		try {
			Object result = joinPoint.proceed();
			Pair<Boolean, String> pair = (Pair<Boolean, String>) result;
			logger.info("backData: {}", pair.getRight());

			if (pair.getLeft()) {
				updateDeviceStatus(deviceInfo, "01");
				redisDeviceInfoService.redisDeleteDeviceInfo(deviceInfo.getOuterNo(), Constant.DeviceType.WATER.name());
				logger.info("close success");
			} else {
				logger.warn("close failed");
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

	@Transactional
	@Around("search()")
	@SuppressWarnings("unchecked")
	public Pair<Boolean, Double> search(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		DeviceInfo deviceInfo = (DeviceInfo) args[0];

		logger.info("deviceNo: {}", deviceInfo.getOuterNo());
		logger.info("elecAddr: {}", deviceInfo.getElecAddr());

		Object result = joinPoint.proceed();
		Pair<Boolean, Double> pair = (Pair<Boolean, Double>) result;
		if (pair.getLeft()) {
			if (pair.getRight() != null) {
				Double usePoint = pair.getRight() * 100;
				Integer leftPoint = usePoint.intValue();

				deviceInfo.setElecLeftPoint(leftPoint);
				logger.info("leftPoint: {}", leftPoint);
				logger.info("rate: {}", deviceInfo.getRate());

				// 电表规定：度数要乘以倍率(电表上的度数如果为10度，那就10X倍率)
				Integer rateUsePoint = leftPoint * deviceInfo.getRate();
				logger.info("rateUsePoint: {}", rateUsePoint);

				deviceInfo.setElecUsePoint(rateUsePoint);
				deviceInfo.setUpdateTime(System.currentTimeMillis());
				deviceInfoDao.updateByPrimaryKeySelective(deviceInfo);

				redisDeviceInfoService.redisDeleteDeviceInfo(deviceInfo.getOuterNo(), Constant.DeviceType.WATER.name());
				// 计算使用度数，比如公摊
				applicationEventPublisher.publishEvent(new ElecUseInfoEvent(this, deviceInfo));
			}
			logger.info("search success");
		} else {
			logger.warn("search failed");
		}
		return pair;
	}

	private int updateDeviceStatus(DeviceInfo deviceInfo, String status) {
		DeviceInfo updeviceInfo = new DeviceInfo();
		updeviceInfo.setDeviceId(deviceInfo.getDeviceId());
		updeviceInfo.setUpdateTime(System.currentTimeMillis());
		updeviceInfo.setElecStatus(status);
		return deviceInfoDao.updateDeviceStatus(updeviceInfo);
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
