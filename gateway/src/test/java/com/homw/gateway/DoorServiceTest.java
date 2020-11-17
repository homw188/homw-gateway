package com.homw.gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.device.proxy.DoorDeviceProxy;
import com.homw.gateway.device.proxy.ElecDeviceProxy;
import com.homw.gateway.device.proxy.WaterDeviceProxy;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.dto.DeviceOperator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public class DoorServiceTest {

	@Autowired
	DoorDeviceProxy doorDeviceProxy;
	@Autowired
	ElecDeviceProxy elecDeviceProxy;
	@Autowired
	WaterDeviceProxy waterDeviceProxy;

	@Autowired
	IRedisDeviceInfoService redisDeviceInfoService;

	@Test
	public void testOpen() throws Exception {
		String deviceNo = "RENTROOM:992:RENTENTRY:1337";
		DeviceOperator operator = new DeviceOperator(1L, "TEST", "13616201049", new Date().getTime());
		DeviceInfo deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(deviceNo, Constant.DeviceType.DOOR.name());
		doorDeviceProxy.open(deviceInfo, operator);
	}

	@Test
	public void testElecParallel() {
		String deviceNo = "RENTROOM:1587:ELECTRIC:1658";
		DeviceInfo deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(deviceNo,
				Constant.DeviceType.ELECTRIC.name());
		int parallel = 3;
		AtomicReference<Double> first = new AtomicReference<>(null);
		CountDownLatch latch = new CountDownLatch(parallel);
		for (int i = 0; i < parallel; i++) {
			new Thread() {
				public void run() {
					try {
						Pair<Boolean, Double> pair = elecDeviceProxy.search(deviceInfo);
						first.compareAndSet(null, pair.getRight());
						assertTrue(pair.getLeft());
						assertNotNull(pair.getRight());
						assertEquals(first.get(), pair.getRight());
						latch.countDown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}

	@Test
	public void testWater() throws Exception {
		String deviceNo = "RENTROOM:1587:ELECTRIC:1658";
		DeviceInfo deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(deviceNo, Constant.DeviceType.WATER.name());
		waterDeviceProxy.search(deviceInfo);
	}
}
