package com.homw.gateway.device;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			int workQueueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(workQueueCapacity),
				threadFactory, handler);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(), threadFactory,
				handler);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		if (callable instanceof PriorityCallable) {
			PriorityCallable<T> task = (PriorityCallable<T>) callable;
			return new PriorityFutureTask<T>(task, task.getPriority());
		}
		return super.newTaskFor(callable);
	}

	class PriorityFutureTask<V> extends FutureTask<V> implements Comparable<PriorityFutureTask<V>> {

		private final int priority;

		public PriorityFutureTask(Callable<V> callable, int priority) {
			super(callable);
			this.priority = priority;
		}

		@Override
		public int compareTo(PriorityFutureTask<V> o) {
			if (o == null) {
				return 1;
			}
			return this.priority - o.priority;
		}
	}

}
