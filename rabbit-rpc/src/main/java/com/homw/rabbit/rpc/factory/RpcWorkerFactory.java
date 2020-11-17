package com.homw.rabbit.rpc.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.homw.rabbit.rpc.RabbitConnector;
import com.homw.rabbit.rpc.RpcServer.WorkerFactory;
import com.homw.rabbit.rpc.RpcWorker;
import com.homw.rabbit.rpc.handler.RequestHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RpcWorkerFactory implements WorkerFactory {

	private final int numThreads;
	private final RabbitConnector connector;
	private final Map<String, RequestHandler<?, ?>> queueHandlerBindings = Maps.newLinkedHashMap();

	public RpcWorkerFactory(RabbitConnector connector, int numThreads) {
		this.connector = connector;
		this.numThreads = numThreads;
	}

	public void addHandler(String queueName, RequestHandler<?, ?> handler) {
		queueHandlerBindings.put(queueName, handler);
	}

	public Map<String, RequestHandler<?, ?>> getHandlerMap() {
		return Collections.unmodifiableMap(queueHandlerBindings);
	}

	@Override
	public Connection createConnection() throws IOException, TimeoutException {
		return connector.newConnection(numThreads);
	}

	@Override
	public List<RpcWorker> createWorkers(Connection conn, String queuePrefix) throws IOException {

		List<Channel> channels = new ArrayList<Channel>();
		for (int i = 0; i < numThreads; i++) {
			Channel channel = conn.createChannel();
			channel.basicQos(1);
			channels.add(channel);
		}

		List<RpcWorker> workers = Lists.newArrayListWithCapacity(queueHandlerBindings.size());
		for (String queueName : queueHandlerBindings.keySet()) {
			RequestHandler<?, ?> handler = queueHandlerBindings.get(queueName);
			RpcWorker worker = new RpcWorker(handler, channels, queuePrefix, queueName);
			workers.add(worker);
		}
		return workers;
	}
}
