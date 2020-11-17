package com.homw.gateway.admin.intercept;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.homw.gateway.admin.constant.WebConstant;
import com.homw.gateway.admin.service.IRedisSignService;
import com.homw.gateway.admin.util.JsonUtil;
import com.homw.gateway.admin.util.SignUtil;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dto.BaseResponse;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.Sign;

public class SignAuthInterceptor implements HandlerInterceptor {

	private static Logger logger = LoggerFactory.getLogger(SignAuthInterceptor.class);

	@Autowired
	protected ReloadableResourceBundleMessageSource messageSource;
	@Autowired
	private IRedisSignService redisSignService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String appKey = request.getParameter("sign_key");
		Sign storeSign = redisSignService.redisHMGetSign(appKey);

		if (storeSign == null || StringUtil.isNullOrEmpty(storeSign.getAppValue())) {
			logger.warn("storeSign is null");
			BaseResponse baseResponse = new BaseResponse();
			baseResponse.setCode(ErrorCode.SIGN_ERROR);
			baseResponse.setMessage(messageSource.getMessage("error.code." + ErrorCode.SIGN_ERROR, new String[] {},
					ErrorCode.SIGN_ERROR, LocaleContextHolder.getLocale()));
			response.getWriter().print(JsonUtil.write(baseResponse));
			response.addHeader("Content-Type", "application/json");
			return false;
		}

		request.getSession().setAttribute(WebConstant.SIGN_SESSION, storeSign);
		if (CollectionUtils.isEmpty(request.getParameterMap())) {
			return true;
		}

		String clientSign = request.getParameter("sign");
		String serverSign = SignUtil.sign(request.getParameterMap(), storeSign.getAppValue());
		if (!serverSign.equals(clientSign)) {
			logger.warn("serverSign=" + serverSign + " clientSign=" + clientSign + " appKey=" + appKey + " appValue="
					+ storeSign.getAppValue());
			BaseResponse baseResponse = new BaseResponse();
			baseResponse.setCode(ErrorCode.SIGN_ERROR);
			baseResponse.setMessage(messageSource.getMessage("error.code." + ErrorCode.SIGN_ERROR, new String[] {},
					ErrorCode.SIGN_ERROR, LocaleContextHolder.getLocale()));
			response.getWriter().print(JsonUtil.write(baseResponse));
			response.addHeader("Content-Type", "application/json");
			return false;
		}
		logger.info("serverSign=" + serverSign + " clientSign=" + clientSign);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e)
			throws Exception {
	}
}
