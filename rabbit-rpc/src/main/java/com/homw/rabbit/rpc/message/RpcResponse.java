package com.homw.rabbit.rpc.message;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.homw.rabbit.rpc.constant.RpcConstant.ResponseStatus;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RpcResponse<T> {

	private final BasicProperties props;
	private final T body;
	private final ResponseStatus status;

	public RpcResponse(BasicProperties props, T body) {
		this.props = props;
		this.body = body;
		this.status = getStatus(props);
	}

	public static Map<String, String> getHeaders(BasicProperties props) {
		Map<String, Object> headers = props.getHeaders();
		if (headers == null) {
			return Collections.emptyMap();
		}

		Map<String, String> map = Maps.newHashMap();
		for (Map.Entry<String, Object> entry : headers.entrySet()) {
			map.put(entry.getKey(), String.valueOf(entry.getValue()));
		}
		return map;
	}

	public static ResponseStatus getStatus(BasicProperties props) {
		String statusStr = getHeaders(props).get("status");
		return ResponseStatus.valueOf(statusStr);
	}

	public Map<String, String> getHeaders() {
		return getHeaders(props);
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public T getBody() {
		return body;
	}
}
