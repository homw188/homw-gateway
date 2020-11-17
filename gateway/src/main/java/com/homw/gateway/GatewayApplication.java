package com.homw.gateway;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GatewayApplication {

	private static final Logger logger = LoggerFactory.getLogger(GatewayApplication.class);
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Condition STOP = LOCK.newCondition();

	public static void main(String[] args) {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					context.close();
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				try {
					LOCK.lock();
					STOP.signal();
				} finally {
					LOCK.unlock();
				}
			}
		});

		context.start();
		logger.info("gateway application started");

		try {
			LOCK.lock();
			STOP.await();
		} catch (InterruptedException e) {
			logger.warn("gateway application stoped, interrupted by other thread", e);
		} finally {
			LOCK.unlock();
		}
	}
}
