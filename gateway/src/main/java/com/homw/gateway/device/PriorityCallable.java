package com.homw.gateway.device;

import java.util.concurrent.Callable;

public abstract class PriorityCallable<V> implements Callable<V> {

	public static final int CTRL_PRIORITY = 10;
	public static final int NORMAL_PRIORITY = 5;

	private final int priority;

	public PriorityCallable(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}
