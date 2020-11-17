package com.homw.gateway.common.command;

public interface Command<T> {

	T exec();

	T exec(int repeat);
}
