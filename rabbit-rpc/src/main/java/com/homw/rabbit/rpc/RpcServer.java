package com.homw.rabbit.rpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.RabbitReconnector.ReconnectHandler;
import com.homw.rabbit.rpc.RabbitReconnector.ReconnectLogger;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.handler.PostProcessors;
import com.homw.rabbit.rpc.util.TimeUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RpcServer {

	public interface WorkerFactory {
		Connection createConnection() throws IOException, TimeoutException;

		List<RpcWorker> createWorkers(Connection conn, String queuePrefix) throws IOException;
	}

	public static interface QueueManager {
		void declareQueue(Channel channel, RpcWorker worker) throws IOException;

		void bindQueue(Channel channel, RpcWorker worker) throws IOException;
	}
	
	private static Logger logger = LoggerFactory.getLogger(RpcServer.class);

	private final WorkerFactory workerFactory;
	public final String queuePrefix;
	private final QueueManager queueManager;
	private final PostProcessors postProcessors;
	private final RabbitReconnector reconnector;

	public RpcServer(WorkerFactory workerFactory, String queuePrefix) {
		this(workerFactory, queuePrefix, null, null, null);
	}

	public RpcServer(WorkerFactory workerFactory, String queuePrefix, QueueManager queueDeclarator,
			PostProcessors postProcessors, ReconnectLogger reconnectLogger) {
		this.workerFactory = workerFactory;
		this.queuePrefix = queuePrefix;
		this.queueManager = queueDeclarator;
		this.postProcessors = postProcessors;

		ReconnectHandler reconnectHandler = new ReconnectHandler() {
			@Override
			public boolean reconnect() throws Exception {
				return _start();
			}
		};
		reconnector = new RabbitReconnector(reconnectHandler, reconnectLogger, RpcConstant.SECONDS_BEFORE_RECONNECT);
	}

	public void start() {
		if (_start() == false) {
			Executors.newSingleThreadExecutor().execute(reconnector);
		}
	}

	private volatile Connection conn = null;

	private boolean _start() {
		try {
			logger.info(TimeUtil.now() + " Starting RpcServer: ");
			conn = workerFactory.createConnection();
			List<RpcWorker> workers = workerFactory.createWorkers(conn, queuePrefix);
			logger.info(conn + " with " + workers.size() + " workers.");

			Channel channel = conn.createChannel();

			if (queueManager != null) {
				for (RpcWorker worker : workers) {
					queueManager.declareQueue(channel, worker);
					queueManager.bindQueue(channel, worker);
				}
			}

			for (RpcWorker worker : workers) {
				worker.setPostProcessors(postProcessors);
				String queueName = worker.getQueueName();
				logger.info("\t" + "Starting RpcWorker for queue: " + queueName);
				channel.queueDeclarePassive(queueName); // make sure the handler's queue exists
				worker.start();
			}

			channel.close();
			conn.addShutdownListener(reconnector);
			return true;
		} catch (Throwable t) {
			logger.error(TimeUtil.now() + " ERROR starting RpcServer:", t);
			stop();
			return false;
		}
	}

	public void stop() {
		logger.info(TimeUtil.now() + " Stopping RpcServer: " + conn);
		RabbitConnector.closeConnectionAndRemoveReconnector(conn, reconnector);
		conn = null;
	}
}
