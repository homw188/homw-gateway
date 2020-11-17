package com.homw.gateway.api.kede;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 科德电表数据协议转换
 * 
 * @author hadoop
 *
 */
public class KedeDataProtocolUtil {
	/**
	 * 瀵硅薄杞暟缁�
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	/**
	 * 鏁扮粍杞璞�
	 * 
	 * @param bytes
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}

	/**
	 * 瀛楃涓茶浆鎹㈡垚鍗佸叚杩涘埗瀛楃涓�
	 */
	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	/**
	 * 鎶�16杩涘埗瀛楃涓茶浆鎹㈡垚瀛楄妭鏁扮粍
	 * 
	 * @param hexString
	 * @return byte[]
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 鏁扮粍杞崲鎴愬崄鍏繘鍒跺瓧绗︿覆
	 * 
	 * @param byte[]
	 * @return HexString
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 鏁扮粍杞垚鍗佸叚杩涘埗瀛楃涓�
	 * 
	 * @param byte[]
	 * @return HexString
	 */
	public static String toHexString1(byte[] b) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < b.length; ++i) {
			buffer.append(toHexString1(b[i]));
		}
		return buffer.toString();
	}

	public static String toHexString1(byte b) {
		String s = Integer.toHexString(b & 0xFF);
		if (s.length() == 1) {
			return "0" + s;
		} else {
			return s;
		}
	}

	/**
	 * 鍗佸叚杩涘埗瀛楃涓茶浆鎹㈡垚瀛楃涓�
	 * 
	 * @param hexString
	 * @return String
	 */
	public static String hexStr2Str(String hexStr) {

		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	/**
	 * 鍗佸叚杩涘埗瀛楃涓茶浆鎹㈠瓧绗︿覆
	 * 
	 * @param HexString
	 * @return String
	 */
	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static String DataAdd33H(String str) {
		String ss = "";
		String s2 = "";
		for (int i = 0; i < str.length() - 1; i = i + 2) {
			int s1 = Integer.parseInt(str.substring(i, i + 2), 16);
			s1 = s1 + 51;
			s2 = "0000" + Integer.toHexString(s1 & 0xFF);
			ss = s2.substring(s2.length() - 2) + ss;
		}
		ss = ss.toUpperCase();
		return ss;
	}

	public static String DataDataAdd33H(String str) {
		String ss = "";
		String s2 = "";
		for (int i = 0; i < str.length() - 1; i = i + 2) {
			int s1 = Integer.parseInt(str.substring(i, i + 2), 16);
			s1 = s1 - 51;
			s2 = "0000" + Integer.toHexString(s1 & 0xFF);
			ss = s2.substring(s2.length() - 2) + ss;
		}
		ss = ss.toUpperCase();
		return ss;
	}

	public static String Datarrd(String str) {
		String str1 = "";
		for (int i = str.length(); i > 0; i = i - 2) {
			str1 = str1 + str.substring(i - 2, i);
		}
		str1 = str1.toUpperCase();
		return str1;
	}

	public static String Checknum(String str) {
		int result = 0;
		for (int i = 0; i < str.length() - 1; i = i + 2) {
			String s1 = str.substring(i, i + 2);
			result = result + Integer.parseInt(s1, 16);
		}
		String s2 = "0000" + Integer.toHexString(result & 0xFF);
		String s3 = s2.substring(s2.length() - 2);
		s3 = s3.toUpperCase();
		return s3;
	}

	public static String GetAllCode(String sAdd, String sCon, String sData) {
		int l = sData.length() / 2;
		String SendStr = "68"; // 起始
		SendStr = SendStr + Datarrd(sAdd); // 加入地址域
		SendStr = SendStr + "68"; // 加入地址结束标志
		SendStr = SendStr + sCon; // 加入控制域
		SendStr = SendStr + String.format("%02x", l);// 加入数据域长度
		SendStr = SendStr + sData; // 加入数据域
		SendStr = SendStr + Checknum(SendStr);// 加入校验和
		SendStr = SendStr + "16"; // 加入结束标志
		return SendStr;
	}

	public static String ZtcxData(String Rstr) {
		String RetStr;
		String strr = Rstr.toUpperCase();
		if (strr.substring(0, 2).equals("68") && strr.substring(strr.length() - 2).equals("16")) {
			if (strr.substring(14, 16).equals("68") && strr.substring(16, 18).equals("83")) {
				String bs = strr.substring(18, 20);
				int l = Integer.parseInt(bs, 16) - 4; // 数据长度
				String dota = strr.substring(28, l * 2 + 28); // 数据域
				bs = DataDataAdd33H(strr.substring(20, 28));// 数据标识
				if (bs.equals("078102FF")) {
					String lj, sy, cs;
					if (dota.substring(0, 2).equals("33")) // 判断是否为负数
					{
						sy = DataDataAdd33H(dota.substring(4, 10)) + "." + DataDataAdd33H(dota.substring(2, 4));
					} else {
						sy = "-" + DataDataAdd33H(dota.substring(4, 10)) + "." + DataDataAdd33H(dota.substring(2, 4));
					}
					lj = DataDataAdd33H(dota.substring(12, 18)) + "." + DataDataAdd33H(dota.substring(10, 12));
					cs = DataDataAdd33H(dota.substring(18, 22));
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("flag", "0");
					Map<String, Object> map2 = new HashMap<String, Object>();
					map2.put("lj", lj);
					map2.put("sy", sy);
					map2.put("cs", cs);
					map.put("data", map2);
					// JSONObject json = JSONObject.fromBean(map);
					// RetStr= json.toString();
					RetStr = JSON.toJSON(map).toString();
				} else {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("flag", "-3");
					Map<String, Object> map2 = new HashMap<String, Object>();
					map2.put("err", "NO078102FF");
					map.put("data", map2);
					// JSONObject json = JSONObject.fromBean(map);
					// RetStr= json.toString();
					RetStr = JSON.toJSON(map).toString();
				}
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("flag", "-2");
				Map<String, Object> map2 = new HashMap<String, Object>();
				map2.put("err", "NO83");
				map.put("data", map2);
				// JSONObject json = JSONObject.fromBean(map);
				// RetStr= json.toString();
				RetStr = JSON.toJSON(map).toString();
			}
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("flag", "-1");
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("err", "NO6816");
			map.put("data", map2);
			// JSONObject json = JSONObject.fromBean(map);
			// RetStr= json.toString();
			RetStr = JSON.toJSON(map).toString();
		}
		return RetStr;
	}

	public static String Set83(String Rstr) {
		String RetStr;
		String strr = Rstr.toUpperCase();
		if (strr.substring(0, 2).equals("68") && strr.substring(strr.length() - 2).equals("16")) {
			if (strr.substring(14, 16).equals("68") && strr.substring(16, 18).equals("83")) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("flag", "0");
				Map<String, Object> map2 = new HashMap<String, Object>();
				map2.put("err", "Yes");
				map.put("data", map2);
				// JSONObject json = JSONObject.fromBean(map);
				// RetStr= json.toString();
				RetStr = JSON.toJSON(map).toString();
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("flag", "-2");
				Map<String, Object> map2 = new HashMap<String, Object>();
				map2.put("err", "NO83");
				map.put("data", map2);
				// JSONObject json = JSONObject.fromBean(map);
				// RetStr= json.toString();
				RetStr = JSON.toJSON(map).toString();
			}
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("flag", "-1");
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("err", "NO6816");
			map.put("data", map2);
			// JSONObject json = JSONObject.fromBean(map);
			// RetStr= json.toString();
			RetStr = JSON.toJSON(map).toString();
		}
		return RetStr;
	}

	public static String Set94(String Rstr) {
		String RetStr;
		String strr = Rstr.toUpperCase();
		if (strr.substring(0, 2).equals("68") && strr.substring(strr.length() - 2).equals("16")) {
			if (strr.substring(16, 18).equals("9c") || strr.substring(16, 18).equals("9C")) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("flag", "0");
				Map<String, Object> map2 = new HashMap<String, Object>();
				map2.put("err", "Yes");
				map.put("data", map2);
				// JSONObject json = JSONObject.fromBean(map);
				// RetStr= json.toString();
				RetStr = JSON.toJSON(map).toString();
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("flag", "-2");
				Map<String, Object> map2 = new HashMap<String, Object>();
				map2.put("err", "NO9C");
				map.put("data", map2);
				// JSONObject json = JSONObject.fromBean(map);
				// RetStr= json.toString();
				RetStr = JSON.toJSON(map).toString();
			}
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("flag", "-1");
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("err", "NO6816");
			map.put("data", map2);
			// JSONObject json = JSONObject.fromBean(map);
			// RetStr= json.toString();
			RetStr = JSON.toJSON(map).toString();
		}
		return RetStr;
	}
	
	/**
	 * 解析水表读数
	 * 
	 * @param data
	 * @return lj(累计)，sy(剩余)，tz(透支)，cs(次数)，zt1(状态1)，zt2(状态2)，sj(时间) <br>
	 *         eg.{"data":{"tz":"0000000.0","lj":"0000019.7","zt2":"00","zt1":"00","sy":"0000000.0","sj":"200720150800","cs":"0000"},"flag":"0"}
	 */
	public static String parseWaterData(String data) {
		String ret;
		String str = data.toUpperCase();
		if (str.substring(0, 2).equals("68") && str.substring(str.length() - 2).equals("16")) {
			// 控制码: 0x91
			if (str.substring(14, 16).equals("68") && str.substring(16, 18).equals("91")) {
				String bs = str.substring(18, 20);
				int len = Integer.parseInt(bs, 16) - 4; // 数据长度
				String dataField = str.substring(28, len * 2 + 28); // 数据域
				bs = DataDataAdd33H(str.substring(20, 28)); // 数据标识
				if (bs.equals("20000100")) {
					// 累计，剩余，透支，次数，状态1，状态2，时间
					String lj, sy, tz, cs, zt1, zt2, sj;

					// 仅1位小数处理
					String decimal = DataDataAdd33H(dataField.substring(0, 2));
					lj = DataDataAdd33H(dataField.substring(2, 8)) + decimal.substring(0, 1) + "." + decimal.substring(1);
					decimal = DataDataAdd33H(dataField.substring(8, 10));
					sy = DataDataAdd33H(dataField.substring(10, 16)) + decimal.substring(0, 1) + "." + decimal.substring(1);
					decimal = DataDataAdd33H(dataField.substring(16, 18));
					tz = DataDataAdd33H(dataField.substring(18, 24)) + decimal.substring(0, 1) + "." + decimal.substring(1);

					cs = DataDataAdd33H(dataField.substring(24, 28));
					zt1 = DataDataAdd33H(dataField.substring(28, 30));
					zt2 = DataDataAdd33H(dataField.substring(30, 32));
					sj = DataDataAdd33H(dataField.substring(32, 44));
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("lj", lj);
					dataMap.put("sy", sy);
					dataMap.put("tz", tz);
					dataMap.put("cs", cs);
					dataMap.put("zt1", zt1);
					dataMap.put("zt2", zt2);
					dataMap.put("sj", sj);
					ret = getResult("0", dataMap);
				} else {
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("err", "NO20000100");
					ret = getResult("-3", dataMap);
				}
			} else {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("err", "NO91");
				ret = getResult("-2", dataMap);
			}
		} else {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("err", "NO6816");
			ret = getResult("-1", dataMap);
		}
		return ret;
	}
	
	/**
	 * 解析水表开关动作数据
	 * 
	 * @param str
	 * @return json字符串，eg.{"data":{"err":"Yes"},"flag":"0"}
	 */
	public static String parseWaterSwitch(String str) {
		String ret;
		String strr = str.toUpperCase();
		if (strr.substring(0, 2).equals("68") && strr.substring(strr.length() - 2).equals("16")) {
			if (strr.substring(16, 18).equals("94")) {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("err", "Yes");
				ret = getResult("0", dataMap);
			} else {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("err", "NO94");
				ret = getResult("-2", dataMap);
			}
		} else {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("err", "NO6816");
			ret = getResult("-1", dataMap);
		}
		return ret;
	}
	
	/**
	 * 获取结果
	 * 
	 * @param flag 标志位
	 * @param data 数据映射
	 * @return json字符串
	 */
	public static String getResult(String flag, Map<String, Object> data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flag", flag);
		map.put("data", data);
		return JSON.toJSON(map).toString();
	}
}
