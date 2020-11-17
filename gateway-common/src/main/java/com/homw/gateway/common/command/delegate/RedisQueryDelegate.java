package com.homw.gateway.common.command.delegate;

public interface RedisQueryDelegate<T> {

	T queryInCache();
	
	T queryInDb();
	
	String getCacheKey();
	
	void putIntoCache(T t);
}
