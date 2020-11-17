package com.homw.rabbit.rpc.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.homw.rabbit.rpc.RabbitRoute;
import com.rabbitmq.client.*;

public class MessagingUtil {
	
	private static Logger logger = LoggerFactory.getLogger(MessagingUtil.class);

	public static final boolean DEBUG = Boolean.getBoolean("ROGER.DEBUG");

	private static final Charset UTF_8 = Charset.forName("utf-8");

	private MessagingUtil() {
	}

	public static void sendMessage(Channel channel, RabbitRoute route, Object message) throws IOException {
		BasicProperties.Builder props = new BasicProperties.Builder();
		sendMessage(channel, route.exchange, route.routingKey, message, props);
	}

	public static void sendRequest(Channel channel, RabbitRoute route, Object request, String callbackQueue,
			String correlationId) throws IOException {
		BasicProperties.Builder props = new BasicProperties.Builder().replyTo(callbackQueue)
				.correlationId(correlationId);
		sendMessage(channel, route.exchange, route.routingKey, request, props);
	}

	public static void sendResponse(Channel channel, String queueName, Object response, BasicProperties.Builder props)
			throws IOException {
		String exchange = "";
		sendMessage(channel, exchange, queueName, response, props);
	}

	private static void sendMessage(Channel channel, String exchange, String routingKey, Object message,
			BasicProperties.Builder props) throws IOException {
		byte[] bytes = objectMapper.writeValueAsBytes(message);
		props = props.contentEncoding(UTF_8.name());
		props = props.contentType("application/json");
		props = props.timestamp(new Date());
		if (DEBUG) {
			logger.debug("*** MessagingUtil SENDING MESSAGE ***");
			logger.debug("*** routingKey: " + routingKey);
			logger.debug("*** props:\n" + prettyPrint(props.build()));
			logger.debug("*** message: " + prettyPrintMessage(message));
		}
		channel.basicPublish(exchange, routingKey, props.build(), bytes);
	}

	public static String prettyPrint(BasicProperties props) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t" + "ContentType: " + props.getContentType() + "\n");
		sb.append("\t" + "ContentEncoding: " + props.getContentEncoding() + "\n");
		sb.append("\t" + "Headers: " + props.getHeaders() + "\n");
		sb.append("\t" + "DeliveryMode: " + props.getDeliveryMode() + "\n");
		sb.append("\t" + "Priority: " + props.getPriority() + "\n");
		sb.append("\t" + "CorrelationId: " + props.getCorrelationId() + "\n");
		sb.append("\t" + "ReplyTo: " + props.getReplyTo() + "\n");
		sb.append("\t" + "Expiration: " + props.getExpiration() + "\n");
		sb.append("\t" + "MessageId: " + props.getMessageId() + "\n");
		sb.append("\t" + "Timestamp: " + props.getTimestamp() + "\n");
		sb.append("\t" + "Type: " + props.getType() + "\n");
		sb.append("\t" + "UserId: " + props.getUserId() + "\n");
		sb.append("\t" + "AppId: " + props.getAppId());
		return sb.toString();
	}

	public static String prettyPrint(Envelope envelope) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t" + "Exchange: " + envelope.getExchange() + "\n");
		sb.append("\t" + "RoutingKey: " + envelope.getRoutingKey() + "\n");
		sb.append("\t" + "DeliveryTag: " + envelope.getDeliveryTag() + "\n");
		sb.append("\t" + "isRedeliver: " + envelope.isRedeliver());
		return sb.toString();
	}

	public static String prettyPrintMessage(Object message) {
		try {
			return prettyPrintWriter.writeValueAsString(message);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return "CAN'T PRETTY PRINT MESSAGE!";
		}
	}

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final ObjectWriter prettyPrintWriter = objectMapper.writerWithDefaultPrettyPrinter();

	public static <T> T getDeliveryBody(byte[] body, TypeReference<?> typeRef) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(body);
		InputStreamReader reader = new InputStreamReader(in, UTF_8);
		return objectMapper.readValue(reader, typeRef);
	}
}
