package com.homw.gateway.device.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import com.homw.gateway.api.device.ICommPortDevice;
import com.homw.gateway.api.kede.KedeProtocolUtil;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dao.DeviceInfoDao;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.constant.DeviceConstant.ElecProtocol;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.example.DeviceInfoExample;
import com.homw.gateway.event.ElecUseInfoEvent;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

@Service("kedeCommPortElecDevice")
public class KedeCommPortElecDeviceImpl implements ICommPortDevice, ApplicationEventPublisherAware {

	private static Logger logger = LoggerFactory.getLogger(KedeCommPortElecDeviceImpl.class);

	private SerialPort serialPort;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ReentrantLock lock = new ReentrantLock();

	private static final int BAUD = 2400;// 波特率

	@Autowired
	private DeviceInfoDao deviceInfoDao;
	@Autowired
	private IRedisDeviceInfoService redisDeviceInfoService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Value("${elec.protocol}")
	private String elecProtocol; // 电表通信方式

	@PostConstruct
	public void init() {
		if (!StringUtils.isEmpty(elecProtocol)) {
			if (ElecProtocol.valueOf(elecProtocol.trim()) == ElecProtocol.COMPORT) {
				scanCommPort();

				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						if (inputStream == null && outputStream == null) {
							scanCommPort();
						}
					}
				}, 5000, 5000);

				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							closeCommPort();
						} catch (IOException e) {
							logger.error("close serial port error", e);
						}
					}
				}, 3600000, 3600000);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void scanCommPort() {
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				try {
					serialPort = (SerialPort) portId.open("DEVICE", 3000);

					outputStream = serialPort.getOutputStream();
					inputStream = serialPort.getInputStream();

					serialPort.notifyOnDataAvailable(true);
					serialPort.addEventListener(new SerialEventListener()); // 注册一个SerialPortEventListener事件来监听串口事件
					serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_EVEN);
				} catch (Exception e) {
					logger.error("open comm port error", e);
				}
			}
		}
	}

	public boolean open(String elecAddr) throws Exception {
		if (lock.tryLock(3000, TimeUnit.MILLISECONDS)) {
			try {
				// 开电源固定的一个字符串指令
				String commd = "68" + KedeProtocolUtil.revertEndian(elecAddr)
						+ "681C10CB333333343333334E3387873C5C3449";
				String msg = commd + KedeProtocolUtil.checksum(commd) + 16;
				logger.info("send packet: {}", msg);
				outputStream.write(KedeProtocolUtil.hexStrToBytes(msg));
				outputStream.flush();
				return true;
			} catch (Exception e) {
				logger.error("send packet error", e);
			} finally {
				Thread.sleep(500);
				lock.unlock();
			}
		} else {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "sendOpenElecMsg is not lockFail");
		}
		return false;
	}

	@Override
	public boolean close(String elecAddr) throws Exception {
		if (lock.tryLock(3000, TimeUnit.MILLISECONDS)) {
			try {
				String commd = "68" + KedeProtocolUtil.revertEndian(elecAddr)
						+ "681C10CB333333343333334D3389873C5C3449";
				String msg = commd + KedeProtocolUtil.checksum(commd) + 16;
				logger.info("send packet: {}", msg);
				outputStream.write(KedeProtocolUtil.hexStrToBytes(msg));
				outputStream.flush();
				return true;
			} catch (Exception e) {
				logger.error("send packet error", e);
			} finally {
				Thread.sleep(500);
				lock.unlock();
			}
		} else {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "sendCloseElecMsg is not lockFail");
		}
		return false;
	}

	@Override
	public boolean search(String elecAddr) throws Exception {
		if (lock.tryLock(3000, TimeUnit.MILLISECONDS)) {
			try {
				// 最后的一串数字代码操作的动作(抄表、开、关)
				String commd = "68" + KedeProtocolUtil.revertEndian(elecAddr) + "6803083235B43A34333333";
				String msg = commd + KedeProtocolUtil.checksum(commd) + 16;
				logger.info("send packet: {}", msg);
				outputStream.write(KedeProtocolUtil.hexStrToBytes(msg));
				outputStream.flush();
				return true;
			} catch (Exception e) {
				logger.error("send packet error", e);
			} finally {
				Thread.sleep(500);
				lock.unlock();
			}
		} else {
			throw new ServiceException(ErrorCode.SYSTEM_ERROR, "sendSearchElecMsg is not lockFail");
		}
		return false;
	}

	class SerialEventListener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			logger.info("serialEventListener type:{}", event.getEventType());
			switch (event.getEventType()) {
				case SerialPortEvent.BI:
					break;/* Break interrupt,通讯中断 */
				case SerialPortEvent.OE:
					break;/* Overrun error，溢位错误 */
				case SerialPortEvent.FE:
					break;/* Framing error，传帧错误 */
				case SerialPortEvent.PE:
					break;/* Parity error，奇偶校验错误 */
				case SerialPortEvent.CD:
					break;/* Carrier detect，载波检测 */
				case SerialPortEvent.CTS:
					break;/* Clear to send，清除发送 */
				case SerialPortEvent.DSR:
					break;/* Data set ready，数据设备就绪 */
				case SerialPortEvent.RI:
					break; /* Ring indicator，响铃指示 振铃指示 */
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
					break;/* Output buffer is empty，输出缓冲区清空 */
				case SerialPortEvent.DATA_AVAILABLE:// 当有可用数据时读取数据,并且给串口返回数据
					try {
						if (lock != null && lock.isLocked()) {
							lock.unlock();
						}
						
						int len = -1;
						// 接收缓冲区
						byte[] readBuf = new byte[1024];
						byte[] totalBuf = new byte[] {};
						while ((len = inputStream.read(readBuf)) > 0) {
							totalBuf = KedeProtocolUtil.mergeBytes(totalBuf, readBuf, len);
							readBuf = new byte[1024];
						}
						
						String data = KedeProtocolUtil.bytesToHexStr(totalBuf);
						logger.info("recv data: {}", data);
						if (data.length() > 24) {
							// 数据长度大于24为抄表的数据
							String status = data.substring(20, 28);// 返回的是开和关的状态
							String statusparam = "3235b43a";// 用来判断此结果是否是抄表返回的数据
							if (status.equals(statusparam)) {
								String addr = data.substring(2, 14);// 返回数据里取的电表地址
								addr = KedeProtocolUtil.revertEndian(addr);// 所有返回的地址都是反序的，这里进行正序操作
								logger.info("addr:{}", addr);

								DeviceInfo deviceInfo = new DeviceInfo();
								deviceInfo.setUpdateTime(System.currentTimeMillis());

								String elecStatus = KedeProtocolUtil.sub33H(data.substring(62, 66));
								logger.info("elecStatus:{}", elecStatus);
								if (elecStatus.length() > 2) {
									deviceInfo.setElecStatus(elecStatus.substring(0, 2));
								} else {
									deviceInfo.setElecStatus(elecStatus);
								}

								DeviceInfoExample deviceInfoExample = new DeviceInfoExample();
								deviceInfoExample.createCriteria().andElecAddrEqualTo(addr);

								List<DeviceInfo> deviceInfoList = deviceInfoDao.selectByExample(deviceInfoExample);
								for (DeviceInfo device : deviceInfoList) {
									// 更新该电表的当前状态、剩余度数(一般是0)、当前度数
									deviceInfo.setElecLeftPoint(
											Integer.parseInt(KedeProtocolUtil.sub33H(data.substring(30, 38))));
									Integer elecUsePoint = Integer
											.parseInt(KedeProtocolUtil.sub33H(data.substring(38, 46)));
									logger.info("elecUsePoint:{}", elecUsePoint);
									logger.info("elecRate:{}", device.getRate());

									// 电表规定：度数要乘以倍率(电表上的度数如果为10度，那就10X倍率)
									Integer rateElecUsePoint = elecUsePoint * device.getRate();
									logger.info("rateElecUsePoint", rateElecUsePoint);
									deviceInfo.setElecUsePoint(rateElecUsePoint);
									deviceInfoDao.updateByExampleSelective(deviceInfo, deviceInfoExample);

									redisDeviceInfoService.redisDeleteDeviceInfo(device.getOuterNo(),
											Constant.DeviceType.ELECTRIC.name());
									// 计算使用度数，比如公摊
									applicationEventPublisher.publishEvent(new ElecUseInfoEvent(this, device));
								}

								logger.info("解析：当前剩余正负号: " + KedeProtocolUtil.sub33H(data.substring(28, 30))); // [29,30]
								logger.info("解析：剩余电量: " + KedeProtocolUtil.sub33H(data.substring(30, 38)));
								logger.info("解析：累计电量: " + KedeProtocolUtil.sub33H(data.substring(38, 46)));
								logger.info("解析：够电次数: " + KedeProtocolUtil.sub33H(data.substring(46, 50)));
								logger.info("解析：用户户号: " + KedeProtocolUtil.sub33H(data.substring(50, 54)));
								logger.info("解析：A相: " + KedeProtocolUtil.sub33H(data.substring(54, 56)));
								logger.info("解析：当前功率: " + KedeProtocolUtil.sub33H(data.substring(56, 62)));
								logger.info("x解析：继电器状态: " + KedeProtocolUtil.sub33H(data.substring(62, 66)) + " , "
										+ KedeProtocolUtil.tripState(KedeProtocolUtil.sub33H(data.substring(62, 66))));
								logger.info("解析：当前电压: " + KedeProtocolUtil.sub33H(data.substring(66, 70)));
								logger.info("解析：当前电流: " + KedeProtocolUtil.sub33H(data.substring(70, 74)));
								logger.info("解析：功率因素: " + KedeProtocolUtil.sub33H(data.substring(74, 78)));
							}
						} else {
							// 不大于24就是开关的返回数据
							String addr = data.substring(2, 14);
							addr = KedeProtocolUtil.revertEndian(addr);
							try {
								search(addr);
							} catch (Exception e) {
								logger.info("search error addr:{}", addr);
							}
							logger.info("success");
						}
					} catch (IOException e) {
						logger.error("send packet error", e);
						try {
							closeCommPort();
						} catch (IOException e1) {
							logger.error("close serial port error", e1);
						}
					}
					break;
			}
		}
	}

	public void closeCommPort() throws IOException {
		if (inputStream != null) {
			inputStream.close();
			inputStream = null;
		}
		if (outputStream != null) {
			outputStream.close();
			outputStream = null;
		}
		if (serialPort != null) {
			serialPort.notifyOnDataAvailable(false);
			serialPort.close();// 这里一定要用close()方法关闭串口，释放资源
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}