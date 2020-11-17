package com.homw.rabbit.rpc.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.constant.RpcConstant.ResponseStatus;
import com.homw.rabbit.rpc.exception.NackException;
import com.homw.rabbit.rpc.message.ExceptionMessage;
import com.homw.rabbit.rpc.util.MessagingUtil;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RpcServerConsumer<I> extends DefaultConsumer {

	private static final boolean DEBUG = MessagingUtil.DEBUG;

	private static final String LOCAL_HOST_NAME;
	static {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			// do nothing
		}
		LOCAL_HOST_NAME = host;
	}

	private static Logger logger = LoggerFactory.getLogger(RpcServerConsumer.class);

	private final RequestHandler<I, ?> handler;
	private final TypeReference<I> requestType;
	private final Channel channel;
	private final String queueName;
	private final PostProcessors pps;

	public static <I> void start(RequestHandler<I, ?> handler, List<Channel> channels, String queueName,
			PostProcessors postProcessors) throws IOException {
		for (Channel channel : channels) {
			Consumer consumer = new RpcServerConsumer<I>(handler, channel, queueName, postProcessors);
			channel.basicConsume(queueName, false, consumer);
		}
	}

	private RpcServerConsumer(RequestHandler<I, ?> handler, Channel channel, String queueName, PostProcessors pps) {
		super(channel);
		this.handler = handler;
		this.requestType = handler.getRequestType();
		this.channel = channel;
		this.queueName = queueName;
		this.pps = pps;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties requestProps, byte[] body)
			throws IOException {
		I request = null;
		ResponseStatus status = null;
		Object response = null;
		Throwable thrown = null;
		long timeTaken = -1;
		try {
			long startTime = System.currentTimeMillis();
			try {
				request = MessagingUtil.getDeliveryBody(body, requestType);
			} catch (Exception e) {
				response = new ExceptionMessage(e);
				status = ResponseStatus.MALFORMED_REQUEST;
				thrown = e;
			}

			if (DEBUG) {
				logger.debug("*** RequestHandler " + handler.getClass().getCanonicalName() + " RECEIVED REQUEST ***");
				logger.debug("*** consumerTag: " + consumerTag);
				logger.debug("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
				logger.debug("*** requestProps:\n" + MessagingUtil.prettyPrint(requestProps));
				logger.debug("*** request: " + MessagingUtil.prettyPrintMessage(request));
			}

			if (request != null) {
				try {
					response = handler.handleRequest(request);
					status = ResponseStatus.OK;
				} catch (NackException e) {
					thrown = e;
					channel.basicNack(envelope.getDeliveryTag(), false, true);
					return;
				} catch (IllegalArgumentException e) {
					response = new ExceptionMessage(e);
					status = ResponseStatus.INVALID_REQUEST;
					thrown = e;
				} catch (Throwable e) {
					response = new ExceptionMessage(e);
					status = ResponseStatus.HANDLER_ERROR;
					thrown = e;
				}
			}
			timeTaken = System.currentTimeMillis() - startTime;

			channel.basicAck(envelope.getDeliveryTag(), false);

			String replyTo = requestProps.getReplyTo();
			String correlationId = requestProps.getCorrelationId();
			if (correlationId != null && replyTo != null) {
				Map<String, Object> headers = new LinkedHashMap<String, Object>();
				headers.put("status", String.valueOf(status));
				headers.put("handler_time_millis", String.valueOf(timeTaken));
				headers.put("hostname", LOCAL_HOST_NAME);

				BasicProperties.Builder replyProps = new BasicProperties.Builder();
				replyProps = replyProps.headers(headers);
				replyProps = replyProps.correlationId(correlationId);
				MessagingUtil.sendResponse(channel, replyTo, response, replyProps);
			}
		} catch (Throwable e) {
			status = ResponseStatus.NUCLEAR;
			thrown = e;
		} finally {
			if (pps != null) {
				pps.process(handler, queueName, status, request, response, thrown, timeTaken);
			}
		}
	}
}
