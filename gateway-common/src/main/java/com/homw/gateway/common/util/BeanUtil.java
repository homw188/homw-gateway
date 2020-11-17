package com.homw.gateway.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.Map;

public class BeanUtil {

	public static final ObjectMapper objectMapper = new ObjectMapper();

	public static <T> T mapToBean(Map<?, ?> map, Class<T> clazz) {
		return objectMapper.convertValue(map, clazz);
	}

	public static Map<String, String> beanToMap(Object bean) {
		Map<String, String> map = objectMapper.convertValue(bean, new TypeReference<Map<String, String>>() {});
		return Maps.filterValues(map, new Predicate<Object>() {
			@Override
			public boolean apply(Object input) {
				return input != null;
			}
		});
	}
}
