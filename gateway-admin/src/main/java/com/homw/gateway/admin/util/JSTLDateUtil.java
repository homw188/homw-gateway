package com.homw.gateway.admin.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.homw.gateway.common.util.StringUtil;

public class JSTLDateUtil extends TagSupport {

	private static final long serialVersionUID = -7010926982028851057L;

	private String value;
	private String parttern;

	public void setValue(String value) {
		this.value = value;
	}

	public void setParttern(String parttern) {
		this.parttern = parttern;
	}

	public int doStartTag() throws JspException {
		String str = "";
		if (StringUtil.isNullOrEmpty(value)) {
			str = "";
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.valueOf(value));
			SimpleDateFormat dateformat = new SimpleDateFormat(parttern);
			str = dateformat.format(calendar.getTime());
		}
		try {
			pageContext.getOut().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doStartTag();
	}
}
