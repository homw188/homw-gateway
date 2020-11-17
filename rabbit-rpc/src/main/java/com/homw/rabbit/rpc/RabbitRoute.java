package com.homw.rabbit.rpc;

public class RabbitRoute {

    public final String exchange;
    public final String routingKey;

    public RabbitRoute(String exchange, String routingKey) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public String toString() {
        return exchange + "/" + routingKey;
    }

	public String getExchange() {
		return exchange;
	}

	public String getRoutingKey() {
		return routingKey;
	}
    
}
