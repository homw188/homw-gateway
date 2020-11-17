package com.homw.gateway.admin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static <T> T get(String json, TypeReference<T> typeReference) {
		try {
			return OBJECT_MAPPER.readValue(json, typeReference);
		} catch (Exception e) {
			return null;
		}
	}

	public static String write(Object object) {
		if (object == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] toByte(Object object) {
		if (object == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsBytes(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(byte[] b, Class<T> clazz) {
		try {
			return OBJECT_MAPPER.readValue(b, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(String s, Class<T> clazz) {
		try {
			return OBJECT_MAPPER.readValue(s, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
