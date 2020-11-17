package com.homw.gateway.admin.service.impl;

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

import com.homw.gateway.admin.service.IRedisSignService;
import com.homw.gateway.common.command.RedisQueryCommand;
import com.homw.gateway.common.command.delegate.RedisQueryDelegate;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.dao.SignDao;
import com.homw.gateway.common.util.BeanUtil;
import com.homw.gateway.entity.Sign;
import com.homw.gateway.entity.example.SignExample;

@Service("redisSignService")
public class RedisSignServiceImpl implements IRedisSignService {

	private static Logger logger = LoggerFactory.getLogger(RedisSignServiceImpl.class);

	@Autowired
	private SignDao signDao;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;

	@Override
	public Sign redisHMGetSign(final String appKey) {
		return new RedisQueryCommand<Sign>(redisTemplate, new RedisQueryDelegate<Sign>() {
			@Override
			public Sign queryInCache() {
				HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
				String key = getCacheKey();
				logger.info("redis key={}", key);
				Map<String, String> result = hashOps.entries(key);
				logger.info("result={}", result);
				if (MapUtils.isEmpty(result)) {
					return null;
				}
				Sign sign = BeanUtil.mapToBean(result, Sign.class);
				return sign;
			}

			@Override
			public Sign queryInDb() {
				SignExample signExample = new SignExample();
				signExample.createCriteria().andAppKeyEqualTo(appKey).andStatusEqualTo((short) 1);
				List<Sign> signs = signDao.selectByExample(signExample);
				if (CollectionUtils.isEmpty(signs)) {
					return null;
				}
				return signs.get(0);
			}

			@Override
			public String getCacheKey() {
				return Constant.REDIS_SIGN_PROPERTY_KEY + appKey;
			}

			@Override
			public void putIntoCache(Sign sign) {
				try {
					HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
					hashOps.putAll(getCacheKey(), BeanUtil.beanToMap(sign));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).exec();
	}

	@Override
	public void redisDeleteSign(String appKey) {
		String key = Constant.REDIS_SIGN_PROPERTY_KEY + appKey;
		logger.info("redis key={}", key);
		redisTemplate.delete(key);
	}

}
