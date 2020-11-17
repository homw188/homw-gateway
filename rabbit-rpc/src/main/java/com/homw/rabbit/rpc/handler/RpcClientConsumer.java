package com.homw.rabbit.rpc.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.RpcClient;
import com.homw.rabbit.rpc.RpcFuture;
import com.homw.rabbit.rpc.constant.RpcConstant.ResponseStatus;
import com.homw.rabbit.rpc.message.ExceptionMessage;
import com.homw.rabbit.rpc.message.RpcResponse;
import com.homw.rabbit.rpc.util.MessagingUtil;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RpcClientConsumer<T> extends DefaultConsumer {

	private static final boolean DEBUG = MessagingUtil.DEBUG;
	private static Logger logger = LoggerFactory.getLogger(RpcClientConsumer.class);

	private RpcClient<T> client;

	public RpcClientConsumer(Channel channel, RpcClient<T> client) {
		super(channel);
		this.client = client;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] body)
			throws IOException {
		RpcFuture<T> future = client.getResponseFutureMap().remove(props.getCorrelationId());
		if (future == null) {
			logger.error("### Received a response not meant for me! ###");
			logger.error("### " + MessagingUtil.prettyPrint(props));
			logger.error("### " + MessagingUtil.prettyPrintMessage(body));
			return;
		}
		ResponseStatus status = RpcResponse.getStatus(props);

		final Object debugMessage;
		if (status == ResponseStatus.OK || client.isExceptionsAsJson()) {
			T message = MessagingUtil.getDeliveryBody(body, client.getResponseType());
			debugMessage = message;
			RpcResponse<T> response = new RpcResponse<T>(props, message);
			future.complete(response);
		} else {
			ExceptionMessage message = MessagingUtil.getDeliveryBody(body, ExceptionMessage.TYPE_REF);
			debugMessage = message;
			future.completeException(status, message);
		}

		if (DEBUG) {
			logger.debug("*** ClientConsumer RECEIVED RESPONSE ***");
			logger.debug("*** consumerTag: " + consumerTag);
			logger.debug("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
			logger.debug("*** properties:\n" + MessagingUtil.prettyPrint(props));
			logger.debug("*** response: " + MessagingUtil.prettyPrintMessage(debugMessage));
		}
	}
}