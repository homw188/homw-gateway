package com.homw.rabbit.rpc;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.homw.rabbit.rpc.handler.PostProcessors;
import com.homw.rabbit.rpc.handler.RequestHandler;
import com.homw.rabbit.rpc.handler.RpcServerConsumer;
import com.rabbitmq.client.Channel;

public class RpcWorker {

	private final RequestHandler<?, ?> handler;
	private final List<Channel> channels;
	private final String queueName;
	private final String procedureName;
	private PostProcessors pps;

	public RpcWorker(RequestHandler<?, ?> handler, List<Channel> channels, String queuePrefix, String procedureName) {
		this.handler = handler;
		this.channels = ImmutableList.copyOf(channels);
		this.queueName = queuePrefix + procedureName;
		this.procedureName = procedureName;
	}

	public String getQueueName() {
		return queueName;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setPostProcessors(PostProcessors postProcessors) {
		pps = postProcessors;
	}

	public void start() throws IOException {
		RpcServerConsumer.start(handler, channels, queueName, pps);
	}
}
