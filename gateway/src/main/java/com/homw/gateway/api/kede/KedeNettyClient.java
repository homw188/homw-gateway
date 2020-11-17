package com.homw.gateway.api.kede;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.gateway.constant.ProtocolConstant;
import com.homw.gateway.listener.ResultFuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @description 科德通信客户端
 * @author Hom
 * @version 1.0
 * @since 2020-07-21
 */
public class KedeNettyClient {
	
	private static final Logger logger = LoggerFactory.getLogger(KedeNettyClient.class);
	
	private Channel channel;
	private EventLoopGroup worker;
	public static final AttributeKey<ResultFuture<String>> DATA_KEY = AttributeKey.valueOf("data_key");

	public void connect(String ip, int port) {
		Bootstrap bootstap = new Bootstrap();
		worker = new NioEventLoopGroup(1);
		try {
			bootstap.group(worker)
					// 连接超时
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ProtocolConstant.Kede.CONNECT_TIMEOUT)
					.option(ChannelOption.TCP_NODELAY, true)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<Channel>() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							ch.pipeline().addLast("decoder", new ByteArrayDecoder());
							ch.pipeline().addLast("encoder", new ByteArrayEncoder());
							ch.pipeline().addLast("clientHandler", new KedeClientHandler());
						}
					});

			// 等待建立连接
			ChannelFuture future = bootstap.connect(ip, port).sync();
			if (future.cause() != null) {
				throw future.cause();
			}
			channel = future.channel();
		} catch (Throwable e) {
			logger.error("连接异常：ip={}, port={}", ip, port, e);
		} 
	}
	
	public String send(String data, int timeout) {
		if (channel == null) return null;
		
		channel.writeAndFlush(KedeProtocolUtil.hexStrToBytes(data));
		
		Attribute<ResultFuture<String>> attr = channel.attr(DATA_KEY);
		ResultFuture<String> future = new ResultFuture<String>();
		attr.set(future);
		try {
			return future.get(timeout, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close() {
		if (worker != null) worker.shutdownGracefully();
	}
    
}