package com.homw.gateway.common.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.gateway.common.command.delegate.OptimisticLock;

public class OptimisticLockCommand<T> implements Command<T> {

	public static final Logger logger = LoggerFactory.getLogger(OptimisticLockCommand.class);
	
	private int repeat = 3;
	private OptimisticLock<T> lock;
	
	public OptimisticLockCommand(OptimisticLock<T> lock) {
		this.lock = lock;
	}

	public OptimisticLockCommand(OptimisticLock<T> lock, int repeat) {
		this(lock);
		if (repeat > 0) {
			this.repeat = repeat;
		}
	}

	@Override
	public T exec() {
		return exec(repeat);
	}

	@Override
	public T exec(int repeat) {
		for (int i = 0; i < repeat; i++) {
			try {
				T t = lock.lock();
				if (t == null) {
					return t;
				}
				lock.process(t);
				if (lock.unlock(t) > 0) {
					lock.removeCache(t);
					return t;
				}
			} catch (Exception e) {
				logger.error("optimistic lock exception:", e);
			} 
		}
		return null;
	}
}
