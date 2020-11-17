package com.homw.gateway.admin.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.*;

public class SignUtil {

	private static Logger logger = LoggerFactory.getLogger(SignUtil.class);

	public static String sign(Map<String, String[]> paramMap, String signValue) {
		Set<Map.Entry<String, String[]>> set = paramMap.entrySet();

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String[]> entry : set) {
			if ("sign".equals(entry.getKey())) {
				continue;
			}
			if (entry.getValue() != null) {
				for (String value : entry.getValue()) {
					list.add(new NameValuePair(entry.getKey(), value));
				}
			}
		}
		return sign(list, signValue);
	}

	public static String sign2(Map<String, String> paramMap, String signValue) {
		Set<Map.Entry<String, String>> set = paramMap.entrySet();

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : set) {
			if ("sign".equals(entry.getKey())) {
				continue;
			}
			if (entry.getValue() != null) {
				list.add(new NameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		return sign(list, signValue);
	}

	private static String sign(List<NameValuePair> list, String signValue) {
		StringWriter sw = new StringWriter();
		Collections.sort(list);

		for (NameValuePair nameValuePair : list) {
			sw.append(nameValuePair.getName()).append("=");
			if (!StringUtils.isEmpty(nameValuePair.getValue())) {
				sw.append(nameValuePair.getValue());
			}
		}
		sw.append(signValue);
		logger.info("sign str: {}", sw.toString());
		return DigestUtils.md5Hex(sw.toString());
	}
}
