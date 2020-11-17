package com.homw.gateway.admin.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.homw.gateway.admin.constant.WebConstant;
import com.homw.gateway.common.constant.Constant;

@SuppressWarnings("serial")
public class JSTLSecurityUtil extends BodyTagSupport {

	// 页面元素的名称
	private String elementType;

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public int doAfterBody() throws JspException {
		try {
			// 如果认证通过就显示标签正文，否则跳过标签正文
			if (isAuthentificated(elementType)) {
				if (bodyContent != null) {
					JspWriter out = bodyContent.getEnclosingWriter();
					bodyContent.writeOut(out);
				} 
			}
		} catch (Exception e) {
			throw new JspException();
		}
		return SKIP_BODY;
	}

	// 检查该类型是否有该页面元素的权限
	private boolean isAuthentificated(String elementType) {
		elementType = (String) this.pageContext.getSession().getAttribute(WebConstant.USER_TYPE);
		if (null != elementType) {
			if (elementType.equals(Constant.UserType.C.name())) {
				return true;
			}
		}
		return false;
	}

}
