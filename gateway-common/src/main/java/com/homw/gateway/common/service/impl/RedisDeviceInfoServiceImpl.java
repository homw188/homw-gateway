package com.homw.gateway.common.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.homw.gateway.common.command.RedisQueryCommand;
import com.homw.gateway.common.command.delegate.RedisQueryDelegate;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.common.util.BeanUtil;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.example.DeviceInfoExample;

@Service("redisDeviceInfoService")
public class RedisDeviceInfoServiceImpl implements IRedisDeviceInfoService {

	public static Logger logger = LoggerFactory.getLogger(RedisDeviceInfoServiceImpl.class);

	@Autowired
	private DeviceInfoDao deviceInfoDao;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;

	@Override
	public DeviceInfo redisHMGetDeviceInfo(final String deviceNo, final String type) {
		return new RedisQueryCommand<DeviceInfo>(redisTemplate, new RedisQueryDelegate<DeviceInfo>() {
			@Override
			public DeviceInfo queryInCache() {
				HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
				String key = getCacheKey();
				logger.info("redis key={}", key);
				Map<String, String> result = hashOps.entries(key);
				logger.info("result={}", result);
				if (MapUtils.isEmpty(result)) {
					return null;
				}
				return BeanUtil.mapToBean(result, DeviceInfo.class);
			}

			@Override
			public DeviceInfo queryInDb() {
				DeviceInfoExample deviceInfoExample = new DeviceInfoExample();
				deviceInfoExample.createCriteria().andOuterNoEqualTo(deviceNo);
				List<DeviceInfo> deviceInfos = deviceInfoDao.selectByExample(deviceInfoExample);
				if (!CollectionUtils.isEmpty(deviceInfos)) {
					return deviceInfos.get(0);
				}
				return null;
			}

			@Override
			public String getCacheKey() {
				return Constant.REDIS_DEVICE_KEY + type + ":" + deviceNo;
			}

			@Override
			public void putIntoCache(DeviceInfo deviceInfo) {
				HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
				hashOps.putAll(getCacheKey(), BeanUtil.beanToMap(deviceInfo));
			}
		}).exec();
	}

	@Override
	public void redisDeleteDeviceInfo(String deviceNo, String type) {
		String key = Constant.REDIS_DEVICE_KEY + type + ":" + deviceNo;
		logger.info("redis key={}", key);
		redisTemplate.delete(key);
	}

}
