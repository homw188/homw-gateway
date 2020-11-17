package com.homw.gateway.admin.service;

import com.homw.gateway.entity.Sign;

public interface IRedisSignService {

	Sign redisHMGetSign(String appKey);
	
	void redisDeleteSign(String appKey);
}
