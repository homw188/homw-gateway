package com.homw.gateway.common.util;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StringUtil {
	
	private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String formatTimestamp(Long timestamp, String format) {
		if (timestamp == null) {
			return null;
		}
		if (isEmpty(format)) {
			format = "yyyy-MM-dd";
		}
		return new SimpleDateFormat(format).format(new Date(timestamp));
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static boolean isNullOrEmpty(Object obj) {
		return obj == null || String.valueOf(obj).trim().length() == 0;
	}

	public static long addYear(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.YEAR, num);
		return calendar.getTimeInMillis();
	}

	public static long addMonth(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.MONTH, num);
		return calendar.getTimeInMillis();
	}

	public static long addWeek(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.WEEK_OF_YEAR, num);
		return calendar.getTimeInMillis();
	}

	public static long addDay(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.DAY_OF_MONTH, num);
		return calendar.getTimeInMillis();
	}

	public static long addHour(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.HOUR_OF_DAY, num);
		return calendar.getTimeInMillis();
	}

	public static long addMinute(Long timestamp, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.MINUTE, num);
		return calendar.getTimeInMillis();
	}

	public static String getYear() {
		Calendar calendar = Calendar.getInstance();
		return formatTimestamp(calendar.getTimeInMillis(), "yyyy");
	}

	public static String getMonth() {
		Calendar calendar = Calendar.getInstance();
		return formatTimestamp(calendar.getTimeInMillis(), "MM");
	}

	public static String getMD5(String word) {
		char hex[] = new char[16 * 2];
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(word.getBytes("UTF-8"));
			byte[] digests = md.digest(); // 结果为128 位长整数，16 个字节表示
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte b = digests[i];
				hex[k++] = hexDigits[b >>> 4 & 0xf];
				hex[k++] = hexDigits[b & 0xf];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String(hex);
	}
}
