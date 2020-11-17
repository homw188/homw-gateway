package com.homw.gateway.api.keda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.gateway.constant.ProtocolConstant;

/**
 * @description thread-safe version of SocketTool
 * @author Hom
 * @version 1.0
 * @date 2020-01-20
 */
public class SocketToolThreadSafe {
	private static final Logger logger = LoggerFactory.getLogger(SocketToolThreadSafe.class);
	
	private ThreadPoolExecutor socketReadExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
			new LinkedBlockingDeque<>(1), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "KedaDeviceSocketReadThread");
				}
			}, new AbortPolicy());

	/**
	 * 建立连接
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @return
	 */
	private Socket connectServer(String serverIp, int serverPort) {
		Socket socket = null;
		try {
			socket = new Socket(serverIp, serverPort);
			socket.setSoTimeout(ProtocolConstant.Keda.READ_TIMEOUT * 1000);
			socket.getOutputStream().write("客户端".getBytes("UTF-8"));

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String response = null;
			while ((response = reader.readLine()) != null) {
				if (response.contains("cmd::Successful")) {
					break;
				}
			}
			logger.debug("连接成功.....");

			/*byte[] b =  new byte[50];
			int x = socket.getInputStream().read(b, 0, b.length);
			String s = new String(b, 0, x);
			String info = s.replace("\0", "");
			if (info != null && info.contains("cmd::Successful")) {
				logger.debug("连接成功.....");
			}*/
		} catch (IOException e) {
			logger.error("连接失败：ip={}, port={}", serverIp, serverPort, e);
		}
		return socket;
	}

	/**
	 * 向通讯程序发送指令
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @param content
	 * @param tag        （223 集中式电表 221 单表 255水表)
	 * @return
	 * @throws Exception
	 */
	public Object sendData(String serverIp, int serverPort, byte[] content, int tag) {
		// 建立连接
		final Socket socket = connectServer(serverIp, serverPort);
		if (socket == null)
			return false;

		String result = null;
		try {
			byte[] heads = new byte[4];
			heads[0] = (byte) 0xCC;
			heads[1] = (byte) 0xCC;
			heads[2] = (byte) 0;
			heads[3] = (byte) tag;

			// 发送请求
			byte[] packet = new byte[content.length + heads.length];// 前导字节+指令字节组合
			System.arraycopy(heads, 0, packet, 0, heads.length);
			System.arraycopy(content, 0, packet, heads.length, content.length);
			try {
				logger.info("send packet: " + CommonTool.bytes2Hex(packet));
				socket.getOutputStream().write(packet);
			} catch (IOException e) {
				logger.error("发送失败", e);
			}

			// 等待响应
			Future<String> future = socketReadExecutor.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					byte[] buf = new byte[256];
					int len = -1;
					String resp = null;
					try {
						while ((len = socket.getInputStream().read(buf)) != -1) {
							resp = CommonTool.bytes2Hex(buf);
							resp = resp.substring(0, len * 2);
							break;
						}
					} catch (IOException e) {
						logger.error("等待响应失败", e);
					}
					logger.info("socket resp: " + resp);
					return resp;
				}
			});

			try {
				// 防止read阻塞, 超时直接退出
				result = future.get(ProtocolConstant.Keda.READ_TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.error("等待响应失败", e);
			}
		} finally {
			// 关闭连接
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("关闭连接失败", e);
			}
		}
		return result;
	}

	public void setExit(boolean isExit) {
		// TODO: nothing
	}
}
