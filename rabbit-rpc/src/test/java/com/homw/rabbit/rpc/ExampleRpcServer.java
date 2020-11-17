package com.homw.rabbit.rpc;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import com.homw.rabbit.rpc.RabbitReconnector.ReconnectLogger;
import com.homw.rabbit.rpc.RpcServer.QueueManager;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.homw.rabbit.rpc.exception.NackException;
import com.homw.rabbit.rpc.factory.RpcWorkerFactory;
import com.homw.rabbit.rpc.handler.PostProcessors;
import com.homw.rabbit.rpc.handler.RequestHandler;
import com.homw.rabbit.rpc.util.QueueUtil;
import com.rabbitmq.client.Channel;

public class ExampleRpcServer {

	public static class Request {
		public String name;
	}

	public static class Response {
		public String greeting;
	}

	public static final RabbitRoute ROUTE = new RabbitRoute(RpcConstant.EXCHANGE, "example-rpc-routing-key");

	public static final RpcServer createRpcServer() {

		RequestHandler<Request, Response> handler = new RequestHandler<Request, Response>() {
			@Override
			public TypeReference<Request> getRequestType() {
				return new TypeReference<Request>() {
				};
			}

			@Override
			public Response handleRequest(Request request) throws NackException, IllegalArgumentException {
				Response response = new Response();
				// Thread.sleep(10000);
				response.greeting = "Hello " + request.name + "!";
				System.out.println(response.greeting);
				return response;
			}
		};

		RpcWorkerFactory factory = new RpcWorkerFactory(RpcConstant.CONNECTOR, 2);
		factory.addHandler("HelloWorld", handler);

		QueueManager queueDeclarator = new QueueManager() {
			@Override
			public void declareQueue(Channel channel, RpcWorker worker) throws IOException {
				Map<String, Object> queueArgs = null;
				QueueUtil.declarePermQueue(channel, worker.getQueueName(), queueArgs);
			}

			@Override
			public void bindQueue(Channel channel, RpcWorker worker) throws IOException {
				channel.exchangeDeclare(ROUTE.exchange, "topic");
				channel.queueBind(worker.getQueueName(), ROUTE.exchange, ROUTE.routingKey);
			}
		};

		String queuePrefix = "RpcExample_";
		PostProcessors postProcessors = null;
		ReconnectLogger reconnectLogger = null;

		return new RpcServer(factory, queuePrefix, queueDeclarator, postProcessors, reconnectLogger);
	}

	public static void main(String[] args) throws Exception {
		RpcServer server = createRpcServer();
		server.start();
	}
}
