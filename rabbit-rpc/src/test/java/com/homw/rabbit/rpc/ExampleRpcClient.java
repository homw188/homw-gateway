package com.homw.rabbit.rpc;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.type.TypeReference;

import com.homw.rabbit.rpc.RabbitConnector;
import com.homw.rabbit.rpc.RpcClient;
import com.homw.rabbit.rpc.ExampleRpcServer.Request;
import com.homw.rabbit.rpc.ExampleRpcServer.Response;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.message.RpcResponse;
import com.homw.rabbit.rpc.util.MessagingUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ExampleRpcClient {

	public static RpcResponse<Response> sendRequest(String name) throws Exception {
		Connection conn = null;
		try {
			conn = RpcConstant.CONNECTOR.newDaemonConnection(1);
			Channel channel = conn.createChannel();
			Map<String, Object> queueArgs = null;
			TypeReference<Response> responseType = new TypeReference<Response>() {
			};
			RpcClient<Response> client = RpcClient.create(channel, ExampleRpcServer.ROUTE, queueArgs, responseType);

			Request request = new Request();
			request.name = name;

			Future<RpcResponse<Response>> future = client.sendRequest(request);
			return future.get(12, TimeUnit.SECONDS);
		} finally {
			RabbitConnector.closeConnection(conn);
		}
	}

	public static void main(String[] args) throws Exception {
		RpcResponse<Response> response = sendRequest("Robert");
		System.out.println("HEADERS:\n" + response.getHeaders());
		System.out.println();
		System.out.println("BODY:\n" + MessagingUtil.prettyPrintMessage(response.getBody()));
	}
}
