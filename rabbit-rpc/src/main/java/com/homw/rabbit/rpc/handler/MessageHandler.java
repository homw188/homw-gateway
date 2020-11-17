package com.homw.rabbit.rpc.handler;

import org.codehaus.jackson.type.TypeReference;

public interface MessageHandler<T> {

    TypeReference<T> getMessageType();

    void handleMessage(T message);
}
