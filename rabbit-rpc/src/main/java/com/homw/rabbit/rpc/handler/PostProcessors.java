package com.homw.rabbit.rpc.handler;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.homw.rabbit.rpc.constant.RpcConstant;

public class PostProcessors {

	public static interface PostProcessor {
		void process(RequestHandler<?, ?> handler, String queueName, RpcConstant.ResponseStatus responseStatus,
				Object request, Object response, Throwable thrown, long timeTaken) throws Exception;
	}

	private final List<PostProcessor> pps = Lists.newArrayList();

	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
			new LinkedBlockingDeque<>());

	public void add(PostProcessor pp) {
		pps.add(pp);
	}

	public void process(final RequestHandler<?, ?> handler, final String queueName,
			final RpcConstant.ResponseStatus responseStatus, final Object request, final Object response,
			final Throwable thrown, final long timeTaken) {
		for (PostProcessor pp : pps) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						pp.process(handler, queueName, responseStatus, request, response, thrown, timeTaken);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
