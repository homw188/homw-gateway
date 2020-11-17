package com.homw.rabbit.rpc.handler;

import org.codehaus.jackson.type.TypeReference;

import com.homw.rabbit.rpc.constant.RpcConstant.ResponseStatus;
import com.homw.rabbit.rpc.exception.NackException;

/**
 * @description rpc请求处理器
 * @author Hom
 * @version 1.0
 * @since 2020-11-16
 * 
 * @param <I> 请求类型，对应输入（input）
 * @param <O> 响应类型，对应输出（output）
 */
public interface RequestHandler<I, O> {

	/**
	 * 请求类型，用于jackson反序列化
	 * 
	 * @return
	 */
	TypeReference<I> getRequestType();

	/**
	 * 处理请求
	 * 
	 * @param request 请求
	 * @return 响应
	 * @throws NackException {@link ResponseStatus#NACK}
	 * @throws IllegalArgumentException {@link ResponseStatus#INVALID_REQUEST}
	 */
	O handleRequest(I request) throws NackException, IllegalArgumentException;
}
