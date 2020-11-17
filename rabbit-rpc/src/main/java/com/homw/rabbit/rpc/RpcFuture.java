package com.homw.rabbit.rpc;

import com.google.common.util.concurrent.AbstractFuture;
import com.homw.rabbit.rpc.constant.RpcConstant.ResponseStatus;
import com.homw.rabbit.rpc.message.ExceptionMessage;
import com.homw.rabbit.rpc.message.RpcResponse;

public class RpcFuture<T> extends AbstractFuture<RpcResponse<T>> {
	public void complete(RpcResponse<T> response) {
		set(response);
	}

	public void completeException(ResponseStatus status, ExceptionMessage message) {
		setException(new Exception(status + ": " + message.getExceptionName() + "\n" + message.getExceptionMsg()));
	}
}
