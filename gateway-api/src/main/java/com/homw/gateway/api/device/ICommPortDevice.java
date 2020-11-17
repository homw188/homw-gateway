package com.homw.gateway.api.device;

/**
 * @description 串口设备操作指令处理接口
 * @author Hom
 * @version 1.0
 * @since 2020-10-29
 */
public interface ICommPortDevice {

	/**
	 * 开
	 * @param addr 设备地址
	 * @return
	 * @throws Exception
	 */
	boolean open(String addr) throws Exception;
	
	/**
	 * 关
	 * @param addr 设备地址
	 * @return
	 * @throws Exception
	 */
	boolean close(String addr) throws Exception;
	
	/**
	 * 查询
	 * @param addr 设备地址
	 * @return
	 * @throws Exception
	 */
	boolean search(String addr) throws Exception;
}
