package com.homw.rabbit.rpc.constant;

import com.homw.rabbit.rpc.RabbitConnector;
import com.homw.rabbit.rpc.util.ConfigLoader;

public interface RpcConstant {

	String EXCHANGE = ConfigLoader.getExchange();

	RabbitConnector CONNECTOR = ConfigLoader.getConnector();

	int SECONDS_BEFORE_RECONNECT = 1;

	int SERVER_WORKER_THREAD_NUM = 2;

	int CLIENT_WORKER_THREAD_NUM = 1;

	String QUEUE_PREFIX = "RpcExample_";

	String REPLY_QUEUE_PREFIX = "Roger-RpcClient";

	public enum ResponseStatus {
		OK, MALFORMED_REQUEST, INVALID_REQUEST, HANDLER_ERROR, NACK, NUCLEAR
	}
}
