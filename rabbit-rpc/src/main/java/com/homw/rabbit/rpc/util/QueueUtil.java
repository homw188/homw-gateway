package com.homw.rabbit.rpc.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

public class QueueUtil {

	private QueueUtil() {
	}

	public static DeclareOk declareRandomQueue(Channel channel, String queuePrefix, Map<String, Object> args)
			throws IOException {
		final String queueName;
		if (queuePrefix == null || queuePrefix.isEmpty()) {
			queueName = "";
		} else {
			queueName = queuePrefix + "-" + UUID.randomUUID().toString();
		}

		final boolean durable = false;
		final boolean exclusive = true;
		final boolean autoDelete = true;
		return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
	}

	public static DeclareOk declareTempQueue(Channel channel, String queueName, Map<String, Object> args)
			throws IOException {
		final boolean durable = false;
		final boolean exclusive = false;
		final boolean autoDelete = true;
		return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
	}

	public static DeclareOk declarePermQueue(Channel channel, String queueName, Map<String, Object> args)
			throws IOException {
		final boolean durable = true;
		final boolean exclusive = false;
		final boolean autoDelete = false;
		return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
	}

	private static DeclareOk declareQueue(Channel channel, String queueName, boolean durable, boolean exclusive,
			boolean autoDelete, Map<String, Object> args) throws IOException {
		if (args == null) {
			args = Collections.emptyMap();
		}
		return channel.queueDeclare(queueName, durable, exclusive, autoDelete, args);
	}
}
