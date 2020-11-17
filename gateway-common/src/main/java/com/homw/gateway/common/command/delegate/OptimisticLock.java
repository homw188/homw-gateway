package com.homw.gateway.common.command.delegate;

public interface OptimisticLock<T> {
	
	T lock();
	
	void process(T t);
	
	int unlock(T t);

	void removeCache(T t);
}
