package com.homw.rabbit.rpc.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.RabbitConnector;
import com.homw.rabbit.rpc.constant.RpcConstant;
import com.rabbitmq.client.Address;

public class ConfigLoader {
	
	private static Logger logger = LoggerFactory.getLogger(RpcConstant.class);
	
	private static String exchange;
	private static RabbitConnector connector;
	
	static {
		Properties prop = new Properties();
		String configFile = "config/rabbitmq.properties";
		String configPath = System.getProperty("configpath", null);
		InputStream inStream = null;
		try {
			if (configPath == null || configPath.trim().isEmpty()) {
				configPath = configFile;
				inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
			} else {
				configPath = configPath.replaceFirst("file:", "").trim();
				configPath += configFile;
				inStream = new FileInputStream(configPath);
			}
			prop.load(inStream);

			String host = prop.getProperty("rabbit.host", "127.0.0.1");
			String username = prop.getProperty("rabbit.username", "guest");
			String password = prop.getProperty("rabbit.password", "guest");
			String vHost = prop.getProperty("rabbit.virtualHost", "/");
			int port = 5672;
			try {
				port = Integer.valueOf(prop.getProperty("rabbit.port"));
			} catch (NumberFormatException e) {
				// e.printStackTrace();
			}
			exchange = prop.getProperty("rabbit.exchange.topic", exchange);
			connector = new RabbitConnector(new Address(host), username, password, vHost, port);
		} catch (IOException e) {
			logger.error("load rabbitmq configuration at [" + configPath + "] error", e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public static String getExchange() {
		return exchange;
	}

	public static RabbitConnector getConnector() {
		return connector;
	}
}
