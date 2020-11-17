package com.homw.rabbit.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.homw.rabbit.rpc.util.MessagingUtil;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitConnector {

	private static Logger logger = LoggerFactory.getLogger(RabbitConnector.class);

	private static final boolean DEBUG = MessagingUtil.DEBUG;

	private final List<Address> addresses;
	private final String username;
	private final String password;
	private final String vhost;
	private final int port;

	public RabbitConnector(Address address, String username, String password, String vhost, Integer port) {
		this(Arrays.asList(address), username, password, vhost, port);
	}

	public RabbitConnector(List<Address> addresses, String username, String password, String vhost, Integer port) {
		this.addresses = ImmutableList.copyOf(addresses);
		this.username = username;
		this.password = password;
		this.vhost = vhost;
		this.port = port;
	}

	private class ConnectDaemonThread extends Thread {
		private final int numThreads;

		public ConnectDaemonThread(int numThreads) {
			this.numThreads = numThreads;
		}

		public volatile Connection conn = null;
		public volatile IOException ioe = null;

		@Override
		public void run() {
			try {
				conn = innerNewConnection(numThreads);
			} catch (IOException e) {
				ioe = e;
			} catch (TimeoutException e) {
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public synchronized void start() {
			setDaemon(true);
			super.start();
		}
	}

	public Connection newDaemonConnection(final int numThreads) throws IOException {
		ConnectDaemonThread tempThread = new ConnectDaemonThread(numThreads);
		tempThread.start();
		try {
			tempThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (tempThread.ioe != null) {
			throw tempThread.ioe;
		}
		if (DEBUG) {
			logger.debug("*** Connected to RabbitMQ: " + tempThread.conn);
		}
		return tempThread.conn;
	}

	public Connection newConnection(int numThreads) throws IOException, TimeoutException {
		Connection conn = innerNewConnection(numThreads);
		if (DEBUG) {
			logger.debug("*** Connected to RabbitMQ: " + conn);
		}
		return conn;
	}

	private Connection innerNewConnection(int numThreads) throws IOException, TimeoutException {
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setUsername(this.username);
		connFactory.setPassword(this.password);
		connFactory.setVirtualHost(this.vhost);
		connFactory.setPort(this.port);

		final ExecutorService executor = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
			AtomicInteger cnt = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "RabbitMQ-ConsumerThread-" + cnt.getAndIncrement());
				t.setDaemon(true);
				return t;
			}
		});
		Connection conn = connFactory.newConnection(executor, addresses.toArray(new Address[0]));
		conn.addShutdownListener(new ShutdownListener() {
			@Override
			public void shutdownCompleted(ShutdownSignalException sse) {
				executor.shutdown();
			}
		});
		return conn;
	}

	public static void closeConnection(Connection conn) {
		closeConnectionAndRemoveReconnector(conn, null);
	}

	public static void closeConnectionAndRemoveReconnector(Connection conn, RabbitReconnector reconnector) {
		if (conn == null) {
			return;
		}
		if (reconnector != null) {
			conn.removeShutdownListener(reconnector);
		}
		try {
			conn.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
