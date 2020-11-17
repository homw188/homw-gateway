package com.homw.rabbit.rpc.util;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.RabbitConnector;
import com.homw.rabbit.rpc.RabbitRoute;
import com.homw.rabbit.rpc.RpcClient;
import com.homw.rabbit.rpc.RpcServer;
import com.homw.rabbit.rpc.RpcServer.QueueManager;
import com.homw.rabbit.rpc.RpcWorker;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.exception.NackException;
import com.homw.rabbit.rpc.factory.RpcWorkerFactory;
import com.homw.rabbit.rpc.handler.RequestHandler;
import com.homw.rabbit.rpc.handler.RpcCallback;
import com.homw.rabbit.rpc.message.RequestMessage;
import com.homw.rabbit.rpc.message.ResponseMessage;
import com.homw.rabbit.rpc.message.RpcResponse;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class EndpointUtil {

	private static Logger logger = LoggerFactory.getLogger(EndpointUtil.class);

	public static void startRpcServer(RabbitRoute route, RpcCallback callback) {

		RequestHandler<RequestMessage, ResponseMessage> handler = new RequestHandler<RequestMessage, ResponseMessage>() {
			@Override
			public TypeReference<RequestMessage> getRequestType() {
				return new TypeReference<RequestMessage>() {
				};
			}

			@Override
			public ResponseMessage handleRequest(RequestMessage request) throws NackException, IllegalArgumentException {
				String result = callback.call(request.getName());
				ResponseMessage response = new ResponseMessage(result);
				if (logger.isDebugEnabled()) {
					logger.debug(response.getGreeting());
				}
				return response;
			}
		};

		RpcWorkerFactory factory = new RpcWorkerFactory(RpcConstant.CONNECTOR, RpcConstant.SERVER_WORKER_THREAD_NUM);
		factory.addHandler(route.getRoutingKey(), handler);

		QueueManager queueManager = new QueueManager() {
			@Override
			public void declareQueue(Channel channel, RpcWorker worker) throws IOException {
				QueueUtil.declarePermQueue(channel, worker.getQueueName(), null);
			}

			@Override
			public void bindQueue(Channel channel, RpcWorker worker) throws IOException {
				channel.exchangeDeclare(route.getExchange(), "topic");
				channel.queueBind(worker.getQueueName(), route.getExchange(), route.getRoutingKey());
			}
		};
		new RpcServer(factory, RpcConstant.QUEUE_PREFIX, queueManager, null, null).start();
	}

	public static RpcResponse<ResponseMessage> sendRequest(RabbitRoute route, String message, int timeout)
			throws Exception {
		Connection conn = null;
		try {
			conn = RpcConstant.CONNECTOR.newDaemonConnection(RpcConstant.CLIENT_WORKER_THREAD_NUM);
			Channel channel = conn.createChannel();
			RpcClient<ResponseMessage> client = RpcClient.create(channel, route, null,
					new TypeReference<ResponseMessage>() {
					});
			Future<RpcResponse<ResponseMessage>> future = client.sendRequest(new RequestMessage(message));
			return future.get(timeout, TimeUnit.SECONDS);
		} finally {
			RabbitConnector.closeConnection(conn);
		}
	}
}
