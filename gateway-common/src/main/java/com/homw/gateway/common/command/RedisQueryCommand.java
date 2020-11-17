package com.homw.gateway.common.command;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.homw.gateway.common.command.delegate.RedisQueryDelegate;
import com.homw.gateway.common.constant.Constant;

public class RedisQueryCommand<T> implements Command<T> {

	private static final Logger logger = LoggerFactory.getLogger(RedisQueryCommand.class);

	private RedisQueryDelegate<T> delegate;
	private RedisTemplate<String, String> redisTemplate;

	public RedisQueryCommand(RedisTemplate<String, String> redisTemplate, RedisQueryDelegate<T> delegate) {
		this.delegate = delegate;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public T exec() {
		T result = null;
		try {
			result = this.delegate.queryInCache();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		if (result == null) {
			ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
			String emptyKey = Constant.REDIS_EMPTY_KEY + delegate.getCacheKey();
			Object emptyValue = null;
			try {
				emptyValue = valueOps.get(emptyKey);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
			if (emptyValue == null) {
				result = delegate.queryInDb();
				if (result == null) {
					try {
						logger.debug("emptyKey={}", emptyKey);
						if (valueOps.setIfAbsent(emptyKey, Constant.REDIS_EMPTY_VALUE)) {
							redisTemplate.expire(emptyKey, Constant.REDIS_EMPTY_EXPIRE_TIME, TimeUnit.SECONDS);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					try {
						delegate.putIntoCache(result);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		return result;
	}

	@Override
	public T exec(int repeat) {
		return exec();
	}

}
