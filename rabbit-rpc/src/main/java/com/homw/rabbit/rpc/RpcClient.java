package com.homw.rabbit.rpc;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.codehaus.jackson.type.TypeReference;

import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.handler.RpcClientConsumer;
import com.homw.rabbit.rpc.message.ExceptionMessage;
import com.homw.rabbit.rpc.message.RpcResponse;
import com.homw.rabbit.rpc.util.MessagingUtil;
import com.homw.rabbit.rpc.util.QueueUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class RpcClient<O> {

	private final Channel channel;
	private final RabbitRoute requestRoute;
	private final TypeReference<O> responseType;
	private final boolean exceptionsAsJson;

	private final String responseQueueName;

	private final Map<String, RpcFuture<O>> responseFutureMap = new ConcurrentHashMap<String, RpcFuture<O>>();

	public static RpcClient<ExceptionMessage> create(Channel channel, RabbitRoute requestRoute,
			Map<String, Object> responseQueueArgs, boolean exceptionAsJson) throws IOException {
		return new RpcClient<ExceptionMessage>(channel, requestRoute, responseQueueArgs, ExceptionMessage.TYPE_REF,
				exceptionAsJson);
	}

	public static <O> RpcClient<O> create(Channel channel, RabbitRoute requestRoute, Map<String, Object> responseQueueArgs,
			TypeReference<O> responseType) throws IOException {

		return new RpcClient<O>(channel, requestRoute, responseQueueArgs, responseType, false);
	}

	private RpcClient(Channel channel, RabbitRoute requestRoute, Map<String, Object> responseQueueArgs,
			TypeReference<O> responseType, boolean exceptionsAsJson) throws IOException {

		this.channel = channel;
		this.requestRoute = requestRoute;
		this.responseType = responseType;
		this.exceptionsAsJson = exceptionsAsJson;

		if (exceptionsAsJson && !responseType.getType().equals(ExceptionMessage.TYPE_REF.getType())) {
			throw new IllegalArgumentException(
					"Can't have exceptionsAsJson unless your responseType is ExceptionMessage!");
		}

		responseQueueName = QueueUtil.declareRandomQueue(channel, RpcConstant.REPLY_QUEUE_PREFIX, responseQueueArgs)
				.getQueue();
		Consumer responseConsumer = new RpcClientConsumer<O>(channel, this);
		channel.basicConsume(responseQueueName, true, responseConsumer);
	}

	public Future<RpcResponse<O>> sendRequest(Object request) throws IOException {
		String correlationId = UUID.randomUUID().toString();
		RpcFuture<O> future = new RpcFuture<O>();
		responseFutureMap.put(correlationId, future);
		MessagingUtil.sendRequest(channel, requestRoute, request, responseQueueName, correlationId);
		return future;
	}

	public TypeReference<O> getResponseType() {
		return responseType;
	}

	public boolean isExceptionsAsJson() {
		return exceptionsAsJson;
	}

	public Map<String, RpcFuture<O>> getResponseFutureMap() {
		return responseFutureMap;
	}

}
