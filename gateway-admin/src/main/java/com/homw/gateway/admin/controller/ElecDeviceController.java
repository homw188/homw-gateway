package com.homw.gateway.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.homw.gateway.admin.bean.MapResponse;
import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.bean.Page;
import com.homw.gateway.admin.bean.PageRequest;
import com.homw.gateway.admin.bean.Validate;
import com.homw.gateway.admin.service.IPublicDeviceService;
import com.homw.gateway.admin.util.ExcelUtil;
import com.homw.gateway.admin.vo.DeviceInfoVo;
import com.homw.gateway.api.device.IElecDevice;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.constant.ErrorCode;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.service.IRedisDeviceInfoService;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.DeviceUser;

@RestController
@RequestMapping("/power")
public class ElecDeviceController extends BaseController {
	private static Logger logger = LoggerFactory.getLogger(ElecDeviceController.class);

	@Autowired
	private IPublicDeviceService publicDeviceService;
	@Autowired
	private Validator validator;
	@Autowired
	private IRedisDeviceInfoService redisDeviceInfoService;

	@Autowired
	private IElecDevice elecDevice;

	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) {
		PageRequest pageRequest = new PageRequest();
		int count = publicDeviceService.selectDevicePageCountByType(Constant.DeviceType.ELECTRIC, pageRequest);
		request.setAttribute("pageRequest", pageRequest);
		if (count != 0) {
			request.setAttribute("pageCount", count);
		} else {
			request.setAttribute("pageCount", 0);
		}
		return new ModelAndView("/device/power");
	}

	/**
	 * 页面加载请求查询列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/view")
	public MapResponse view(HttpServletRequest request, PageRequest pageRequest, String doorName, String outerNo) {
		Page<DeviceInfo> spacePage = publicDeviceService.selectDevicePageByType(Constant.DeviceType.ELECTRIC, pageRequest,
				doorName, outerNo);
		List<DeviceInfoVo> lists = new ArrayList<DeviceInfoVo>();
		if (!CollectionUtils.isEmpty(spacePage.getContent())) {
			DeviceInfoVo vo = null;
			for (DeviceInfo agreem : spacePage.getContent()) {
				vo = new DeviceInfoVo();
				vo.setDeviceId(agreem.getDeviceId());
				vo.setOuterNo(agreem.getOuterNo());
				vo.setDoorName(agreem.getDoorName());
				vo.setElecAddr(agreem.getElecAddr());
				if (null != agreem.getElecStatus() && !agreem.getElecStatus().equals("")) {
					if (agreem.getElecStatus().equals("01")) {
						vo.setElecStatus("<font color='red'>关闸</font>");
					} else {
						vo.setElecStatus("<font color='green'>合闸</font>");
					}
				}
				double dd = (Double.parseDouble(agreem.getElecUsePoint().toString())) / 100;
				vo.setElecUsePoint(String.valueOf(dd));
				vo.setElecLeftPoint(agreem.getElecLeftPoint());
				vo.setUpdateTime(StringUtil.formatTimestamp(agreem.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
				lists.add(vo);
			}
			return new MapResponse(new Page<DeviceInfoVo>(lists, spacePage.getTotal(), pageRequest));
		} else {
			return new MapResponse(new Page<DeviceInfoVo>(lists, spacePage.getTotal(), pageRequest));
		}
	}

	@RequestMapping("/updateView")
	public MapResponse updateView(HttpServletRequest request, Long deviceId) {
		DeviceInfo deviceInfo = publicDeviceService.selectDeviceById(deviceId);
		return new MapResponse(deviceInfo);
	}

	@RequestMapping("/update")
	public MapResponse update(HttpServletRequest request, DeviceInfoVo deviceInfoVo) {
		Set<ConstraintViolation<DeviceInfoVo>> errors = validator.validate(deviceInfoVo, Validate.Update.class);
		// 有报错 则errors不为空
		if (!CollectionUtils.isEmpty(errors)) {
			@SuppressWarnings("unchecked")
			ConstraintViolation<DeviceInfoVo> error = (ConstraintViolation<DeviceInfoVo>) errors.toArray()[0];
			String errorMsg = error.getMessage();
			return new MapResponse(errorMsg);
		}

		DeviceUser user = (DeviceUser) request.getSession()
				.getAttribute(com.homw.gateway.admin.constant.WebConstant.USER_SESSION);
		Operator operator = new Operator(user.getUserId(), "systemUser");
		DeviceInfo deivce = new DeviceInfo();
		try {
			BeanUtils.copyProperties(deivce, deviceInfoVo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (null != deivce && deivce.getDeviceId() != 0) {
				publicDeviceService.updateDeviceById(deivce, operator);
			} else {
				deivce.setDeviceType("ELECTRIC");
				deivce.setDoorReadno(null);
				publicDeviceService.insertDevice(deivce, operator);
			}
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

	@RequestMapping("/delete")
	public MapResponse delete(HttpServletRequest request, Long deviceId) {
		DeviceUser user = (DeviceUser) request.getSession()
				.getAttribute(com.homw.gateway.admin.constant.WebConstant.USER_SESSION);
		Operator operator = new Operator(user.getUserId(), "systemUser");
		publicDeviceService.softDeleteById(deviceId, operator);
		return new MapResponse();
	}

	@RequestMapping("/search")
	public MapResponse search(String outerNo) throws Exception {
		logger.info("search elec outerNo: {}", outerNo);

		DeviceInfo deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(outerNo,
				Constant.MessageType.ELECTRIC.name());
		try {
			if (deviceInfo != null) {
				logger.info("elecAddr: {}", deviceInfo.getElecAddr());
				elecDevice.search(deviceInfo);
			} else {
				throw new ServiceException(ErrorCode.SYSTEM_ERROR, "search elec device is not exists");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ServiceException) {
				ServiceException serviceException = (ServiceException) e;
				String message = messageSource.getMessage("error.code." + serviceException.getCode(), new Object[] {},
						serviceException.getMessage(), Locale.CHINA);
				return new MapResponse(serviceException.getCode(), message);
			} else {
				return new MapResponse("000001", "读表失败，请稍后再试。");
			}
		}
		logger.info("search elec success");
		return new MapResponse();
	}

	@RequestMapping("/download")
	public String download(HttpServletRequest request, HttpServletResponse response, String doorName, String outerNo) {
		List<DeviceInfo> deviceList = publicDeviceService.selectDeviceByType(Constant.DeviceType.ELECTRIC, doorName,
				outerNo);
		if (deviceList.isEmpty()) {
			return "redirect:/power/index";
		}
		List<DeviceInfoVo> lists = new ArrayList<DeviceInfoVo>();
		if (!CollectionUtils.isEmpty(deviceList)) {
			DeviceInfoVo vo = null;
			for (DeviceInfo device : deviceList) {
				vo = new DeviceInfoVo();
				vo.setElecAddr(device.getElecAddr());
				if (null != device.getElecStatus() && !device.getElecStatus().equals("")) {
					if (device.getElecStatus().equals("01")) {
						vo.setElecStatus("关闸");
					} else {
						vo.setElecStatus("合闸");
					}
				}
				vo.setOuterNo(device.getOuterNo());
				if (null != device.getDoorName() && !device.getDoorName().equals("")) {
					vo.setDoorName(device.getDoorName());
				}

				double dd = (Double.parseDouble(device.getElecUsePoint().toString())) / 100;
				vo.setElecUsePoint(String.valueOf(dd));
				vo.setElecLeftPoint(device.getElecLeftPoint());
				if (null != device.getUpdateTime()) {
					vo.setUpdateTime(StringUtil.formatTimestamp(device.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
				}
				lists.add(vo);
			}
		}
		List<String[]> dataList = convertListToArray(lists, 2);
		String[] titleArray = new String[] { "电表地址", "电表状态", "电表配置", "设备名称", "使用电量", "剩余电量", "上次更新时间" };
		String sheetName = "电表抄表明细";
		String fileName = "电表抄表明细";
		ExcelUtil.createExcel(dataList, titleArray, sheetName, fileName, response);
		return null;
	}

	public List<String[]> convertListToArray(List<DeviceInfoVo> list, int index) {
		List<String[]> result = new ArrayList<String[]>();
		if (null != list && list.size() > 0) {
			String[] arr = null;
			for (DeviceInfoVo inv : list) {
				arr = new String[7];
				arr[0] = inv.getElecAddr();
				if (null != inv.getElecStatus() && !inv.getElecStatus().equals("")) {
					arr[1] = inv.getElecStatus();
				}
				arr[2] = inv.getOuterNo().toString();
				if (null != inv.getDoorName() && !inv.getDoorName().equals("")) {
					arr[3] = inv.getDoorName().toString();
				}
				arr[4] = inv.getElecUsePoint().toString();
				arr[5] = inv.getElecLeftPoint().toString();
				if (null != inv.getUpdateTime()) {
					arr[6] = inv.getUpdateTime();
				}
				result.add(arr);
			}
		}
		return result;
	}

	@RequestMapping("/searchAll")
	public MapResponse searchAll() throws Exception {
		List<DeviceInfo> deviceList = publicDeviceService.findPowerAllListbypam(Constant.MessageType.ELECTRIC.name());
		if (!CollectionUtils.isEmpty(deviceList)) {
			for (DeviceInfo device : deviceList) {
				if (device != null) {
					DeviceInfo deviceInfo = redisDeviceInfoService.redisHMGetDeviceInfo(device.getOuterNo(),
							Constant.MessageType.ELECTRIC.name());
					try {
						Thread.sleep(1000);
						logger.info("elecAddr: {}", deviceInfo.getElecAddr());
						elecDevice.search(deviceInfo);
					} catch (Exception e) {
						e.printStackTrace();
						if (e instanceof ServiceException) {
							ServiceException serviceException = (ServiceException) e;
							String message = messageSource.getMessage("error.code." + serviceException.getCode(),
									new Object[] {}, serviceException.getMessage(), Locale.CHINA);
							return new MapResponse(serviceException.getCode(), message);
						} else {
							return new MapResponse("000001", "电表读取数据异常!");
						}
					}
					logger.info("search all elec success");
				} else {
					throw new ServiceException(ErrorCode.SYSTEM_ERROR, "search all elec device is not exists");
				}
			}
		}
		return new MapResponse();
	}

}
