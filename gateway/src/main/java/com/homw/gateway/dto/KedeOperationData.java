package com.homw.gateway.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class KedeOperationData {

	public static final String SUCCESS = "Yes"; // 操作成功

	private String data = "";
	private int flag;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public static boolean isSuccess(String json) {
		try {
			JSONObject obj = JSON.parseObject(json, JSONObject.class);
			if (obj.get("err").equals(KedeOperationData.SUCCESS)) {
				return true;
			}
		} catch (Exception e) {
			
		}
		return false;
	}

}
