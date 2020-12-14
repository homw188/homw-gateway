package com.homw.rabbit.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.util.TimeUtil;
import com.rabbitmq.client.*;

public class RabbitReconnector implements ShutdownListener, Runnable {

	private static Logger log = LoggerFactory.getLogger(RabbitReconnector.class);

	public static interface ReconnectHandler {
		boolean reconnect() throws Exception;
	}

	public static interface ReconnectLogger {
		void log(ShutdownSignalException cause);

		void log(int attempt);
	}

	private final ReconnectHandler handler;
	private final ReconnectLogger logger;
	private final int secondsBeforeRetry;

	public RabbitReconnector(ReconnectHandler handler, int secondsBeforeRetry) {
		this(handler, null, secondsBeforeRetry);
	}

	public RabbitReconnector(ReconnectHandler handler, ReconnectLogger logger, int secondsBeforeRetry) {
		this.handler = handler;
		this.logger = logger;
		this.secondsBeforeRetry = secondsBeforeRetry;
	}

	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
		log.info(TimeUtil.now() + " RabbitMQ connection SHUTDOWN!");
		log.error("CAUSE: ", cause);
		run();
	}

	private volatile long timeLastRun = System.currentTimeMillis();

	@Override
	public synchronized void run() {
		log.info(TimeUtil.now() + " Attempting to reconnect to RabbitMQ...");
		long millisBeforeRetry = secondsBeforeRetry * 1000;
		int attempt = 0;

		while (true) {
			attempt++;

			long millisSinceLastTry = System.currentTimeMillis() - timeLastRun;
			long millisToWait = millisBeforeRetry - millisSinceLastTry;
			if (millisToWait > 0) {
				log.warn(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " too soon!  Waiting for " + millisToWait
						+ " millis...");
				sleep(millisToWait);
			}

			try {
				timeLastRun = System.currentTimeMillis();
				if (handler.reconnect()) {
					log.info(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " SUCCEEDED!");
					return;
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (logger != null) {
				logger.log(attempt);
			}
			log.warn(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " FAILED!  Retrying in " + secondsBeforeRetry
					+ " seconds...");
			sleep(millisBeforeRetry);
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// throw new RuntimeException(e);
		}
	}
}
