package com.homw.gateway.admin.intercept;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LogInterceptor implements HandlerInterceptor {

	private static Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		StringWriter sw = new StringWriter();
		sw.append("<request>\t" + request.getMethod() + "\turl:" + request.getRequestURL() + "\theads:");
		Enumeration<String> it = request.getHeaderNames();
		while (it.hasMoreElements()) {
			String key = it.nextElement();
			sw.append(key).append("=").append(request.getHeader(key)).append("&");
		}
		sw.append("\tparams:");

		Map<String, String[]> parameterMap = request.getParameterMap();
		if (CollectionUtils.isEmpty(parameterMap)) {
			sw.append("NONE");
		} else {
			for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
				for (String value : entry.getValue()) {
					sw.append(entry.getKey()).append("=").append(value).append("&");
				}
			}
		}
		logger.info(sw.toString());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e)
			throws Exception {
		logger.info("<response>\t" + request.getMethod() + "\turl:" + request.getRequestURL() + "\tstatusCode:"
				+ response.getStatus() + "\tcontent-type:" + response.getContentType() + "\texception:"
				+ (e == null ? "NONE" : e.getMessage()));
	}
}
