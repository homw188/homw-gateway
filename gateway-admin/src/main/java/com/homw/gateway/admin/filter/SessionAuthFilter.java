package com.homw.gateway.admin.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;

import com.homw.gateway.admin.constant.WebConstant;
import com.homw.gateway.entity.DeviceUser;

public class SessionAuthFilter implements Filter {

	private static final String[] IGNORE_URI = { "/login/", "/loginOut/", "/loginIntoSystem/" };

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("just support HTTP requests");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = httpRequest.getSession(true);

		StringBuffer url = httpRequest.getRequestURL();
		if (IGNORE_URI != null && IGNORE_URI.length > 0) {
			for (String str : IGNORE_URI) {
				if (url.indexOf(str) >= 0) {
					filterChain.doFilter(request, response);
				}
			}
		}
		
		Object obj = session.getAttribute(WebConstant.USER_SESSION);
		DeviceUser user = obj == null ? null : (DeviceUser) obj;
		if (user == null) {
			boolean isAjaxRequest = isAjaxRequest(httpRequest);
			if (isAjaxRequest) {
				httpResponse.setCharacterEncoding("UTF-8");
				httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "您已经太长时间没有操作,请刷新页面");
			}
		}
		filterChain.doFilter(request, response);
	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith("/api");
//		String requestType = request.getHeader("X-Requested-With");
//		return requestType != null && requestType.equals("XMLHttpRequest");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
		
	}

}
