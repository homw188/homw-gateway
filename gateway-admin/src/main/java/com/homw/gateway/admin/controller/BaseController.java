package com.homw.gateway.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.homw.gateway.admin.bean.MapResponse;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.dto.BaseResponse;
import com.homw.gateway.common.exception.ServiceException;

public class BaseController {

	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

	protected static final String ERR_CODE_PREFIX = "error.code.";
	@Autowired
	protected ReloadableResourceBundleMessageSource messageSource;

	@ResponseBody
	@ExceptionHandler(ServiceException.class)
	public BaseResponse handleServiceException(ServiceException e) {
		logger.error("ServiceException error:", e);
		BaseResponse baseResponse = new BaseResponse();
		baseResponse.setCode(e.getCode());
		Object value = e.getValue();
		// value不为空 并且是 Object[]类型，使用value拼装message
		Object[] args = value != null && value instanceof Object[] ? (Object[]) value : null;
		baseResponse.setMessage(messageSource.getMessage("error.code." + e.getCode(), args, e.getCode(),
				LocaleContextHolder.getLocale()));
		return baseResponse;
	}

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public BaseResponse handleException(Exception e) {
		logger.error("Exception error:", e);
		BaseResponse response = new BaseResponse();
		response.setCode(ErrorCode.SYSTEM_ERROR);
		response.setMessage(messageSource.getMessage("error.code." + ErrorCode.SYSTEM_ERROR, null,
				ErrorCode.SYSTEM_ERROR, LocaleContextHolder.getLocale()));
		return response;
	}

	public MapResponse getSuccess(Object obj) {
		MapResponse mapResponse = new MapResponse(obj);
		mapResponse.setMessage(
				messageSource.getMessage("error.code.000000", null, "000000", LocaleContextHolder.getLocale()));
		return mapResponse;
	}

	public BaseResponse getSuccess() {
		BaseResponse baseResponse = new BaseResponse();
		baseResponse.setMessage(
				messageSource.getMessage("error.code.000000", null, "000000", LocaleContextHolder.getLocale()));
		return baseResponse;
	}
}
