package com.homw.gateway.admin.controller;

import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.homw.gateway.admin.bean.MapResponse;
import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.bean.Validate;
import com.homw.gateway.admin.constant.WebConstant;
import com.homw.gateway.admin.service.IDeviceUserService;
import com.homw.gateway.admin.vo.DeviceUserVo;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.DeviceUser;

@Controller
@RequestMapping("/")
public class LoginController extends BaseController {

	@Autowired
	private Validator validator;
	@Autowired
	private IDeviceUserService deviceUserService;
	
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		request.getSession().removeAttribute(WebConstant.USER_SESSION);
		return "/login";
	}

	@RequestMapping("/loginOut")
	public String loginOut(HttpServletRequest request) {
		request.getSession().removeAttribute(WebConstant.USER_SESSION);
		return "redirect:/login";
	}

	@RequestMapping("/loginIntoSystem")
	public String loginIntoSystem(HttpServletRequest request, String name, String password) {
		String userPassword = StringUtil.getMD5(password);
		DeviceUser user = deviceUserService.selectUserByPam(name, userPassword);
		if (null != user) {
			// 保存登录相关信息
			request.getSession().setAttribute(WebConstant.USER_SESSION, user);
			if (user.getType().equals(Constant.UserType.C.name())) {
				request.getSession().setAttribute(WebConstant.USER_TYPE, user.getType());
			} else if (user.getType().equals(Constant.UserType.E.name())) {
				request.getSession().setAttribute(WebConstant.USER_TYPE, user.getType());
			}
			return "redirect:/index";
		} else {
			request.setAttribute("errorMsg", "用户名或密码错误!");
			return "redirect:/login";
		}
	}

	@ResponseBody
	@RequestMapping("/userUpdatePwd")
	public MapResponse userUpdatePwd(HttpServletRequest request, DeviceUserVo employeeVo) {
		Set<ConstraintViolation<DeviceUserVo>> errors = validator.validate(employeeVo, Validate.Update.class);
		// 有报错 则errors不为空
		if (!CollectionUtils.isEmpty(errors)) {
			@SuppressWarnings("unchecked")
			ConstraintViolation<DeviceUserVo> error = (ConstraintViolation<DeviceUserVo>) errors.toArray()[0];
			String errorMsg = error.getMessage();
			return new MapResponse("000001", errorMsg);
		}

		DeviceUser deviceUser = new DeviceUser();
		try {
			BeanUtils.copyProperties(deviceUser, employeeVo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DeviceUser user = (DeviceUser) request.getSession().getAttribute(WebConstant.USER_SESSION);
		Operator operator = new Operator(Long.valueOf(user.getUserId()), Constant.UserType.E.name());
		try {
			String loginPwd = (String) request.getParameter("loginPwd"); // 旧密码
			String newloginPwd = (String) request.getParameter("newloginPwd");// 新密码
			String newloginPwdcheck = (String) request.getParameter("newloginPwdcheck");// 确认新密码

			if (StringUtils.isEmpty(loginPwd) || StringUtils.isEmpty(newloginPwd)
					|| StringUtils.isEmpty(newloginPwdcheck)) {
				return new MapResponse("000001", "请输入旧密码与新密码");
			}
			if (!newloginPwd.equals(newloginPwdcheck)) {
				return new MapResponse("000001", "两次输入的新密码必须相同");
			}
			if (loginPwd.equals(newloginPwd)) {
				return new MapResponse("000001", "新密码与旧密码不允许相同");
			}
			deviceUserService.changeecsPassword(Long.valueOf(user.getUserId()), loginPwd, newloginPwd, operator);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e;
				String message = messageSource.getMessage("error.code." + serviceException.getCode(), new Object[] {},
						serviceException.getMessage(), Locale.CHINA);
				return new MapResponse(serviceException.getCode(), message);
			} else {
				String message = messageSource.getMessage("error.code." + e.getMessage(), new Object[] {},
						e.getMessage(), Locale.CHINA);
				return new MapResponse("000001", message);
			}
		}
		return new MapResponse();
	}

}
