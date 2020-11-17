package com.homw.gateway.admin.controller;

import java.text.SimpleDateFormat;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.homw.gateway.admin.bean.MapResponse;
import com.homw.gateway.admin.bean.Operator;
import com.homw.gateway.admin.bean.Page;
import com.homw.gateway.admin.bean.PageRequest;
import com.homw.gateway.admin.bean.Validate;
import com.homw.gateway.admin.constant.WebConstant;
import com.homw.gateway.admin.service.IPublicDeviceService;
import com.homw.gateway.admin.util.ExcelUtil;
import com.homw.gateway.admin.vo.DeviceCommandLogVo;
import com.homw.gateway.admin.vo.DeviceInfoVo;
import com.homw.gateway.common.constant.Constant;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.entity.DeviceCommandLog;
import com.homw.gateway.entity.DeviceInfo;
import com.homw.gateway.entity.DeviceUser;

@RestController
@RequestMapping("/door")
public class DoorDeviceController extends BaseController {

	@Autowired
	private IPublicDeviceService publicDeviceService;
	@Autowired
	private Validator validator;

	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) {
		PageRequest page = new PageRequest();
		int count = publicDeviceService.selectDevicePageCountByType(Constant.DeviceType.DOOR, page);
		request.setAttribute("pageRequest", page);
		if (count != 0) {
			request.setAttribute("pageCount", count);
		} else {
			request.setAttribute("pageCount", 0);
		}
		return new ModelAndView("/device/door");
	}

	@RequestMapping("/view")
	public MapResponse view(HttpServletRequest request, PageRequest pageRequest, String doorName, String outerNo) {
		Page<DeviceInfo> spacePage = publicDeviceService.selectDevicePageByType(Constant.DeviceType.DOOR, pageRequest, doorName,
				outerNo);
		List<DeviceInfoVo> lists = new ArrayList<DeviceInfoVo>();
		if (!CollectionUtils.isEmpty(spacePage.getContent())) {
			DeviceInfoVo vo = null;
			for (DeviceInfo device : spacePage.getContent()) {
				vo = new DeviceInfoVo();
				vo.setDeviceId(device.getDeviceId());
				vo.setDoorIp(device.getDoorIp());
				vo.setDoorPort(device.getDoorPort());
				vo.setDoorAddr(device.getDoorAddr());
				vo.setDoorReadno(device.getDoorReadno());
				vo.setOuterNo(device.getOuterNo());
				vo.setDoorName(device.getDoorName());
				lists.add(vo);
			}
			return new MapResponse(new Page<DeviceInfoVo>(lists, spacePage.getTotal(), pageRequest));
		} else {
			return new MapResponse(new Page<DeviceInfoVo>(lists, spacePage.getTotal(), pageRequest));
		}
	}

	@RequestMapping("/access/index")
	public ModelAndView accessIndex(HttpServletRequest request) {
		PageRequest page = new PageRequest();
		int count = publicDeviceService.selectCmdLogPageCountByType(Constant.DeviceType.DOOR, page);
		request.setAttribute("pageRequest", page);
		if (count != 0) {
			request.setAttribute("pageCount", count);
		} else {
			request.setAttribute("pageCount", 0);
		}
		return new ModelAndView("/device/door-access");
	}

	@RequestMapping("/access/view")
	public MapResponse accessView(HttpServletRequest request, PageRequest pageRequest, String doorName,
			String userMobile, String startTime, String endTime) {
		Page<DeviceCommandLog> spacePage = publicDeviceService.selectCmdLogPageByType(Constant.DeviceType.DOOR, pageRequest,
				doorName, userMobile, startTime, endTime);
		List<DeviceCommandLogVo> lists = new ArrayList<DeviceCommandLogVo>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (!CollectionUtils.isEmpty(spacePage.getContent())) {
			DeviceCommandLogVo vo = null;
			for (DeviceCommandLog log : spacePage.getContent()) {
				vo = new DeviceCommandLogVo();
				vo.setLogId(log.getLogId());
				vo.setUserMobile(log.getUserMobile());
				vo.setOuterNo(log.getOuterNo());
				vo.setDoorName(log.getDoorName());
				vo.setCreateTime(log.getCreateTime() == null ? "" : df.format(log.getCreateTime()));
				vo.setUserType("MANAGER".equals(log.getUserType()) ? "管理员" : "会员");
				lists.add(vo);
			}
			return new MapResponse(new Page<DeviceCommandLogVo>(lists, spacePage.getTotal(), pageRequest));
		} else {
			return new MapResponse(new Page<DeviceCommandLogVo>(lists, spacePage.getTotal(), pageRequest));
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
			return new MapResponse(error.getMessage());
		}

		DeviceUser user = (DeviceUser) request.getSession().getAttribute(WebConstant.USER_SESSION);
		Operator operator = new Operator(user.getUserId(), "systemUser");
		DeviceInfo deviceInfo = new DeviceInfo();
		try {
			BeanUtils.copyProperties(deviceInfo, deviceInfoVo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (null != deviceInfo && deviceInfo.getDeviceId() != 0) {
				publicDeviceService.updateDeviceById(deviceInfo, operator);
			} else {
				deviceInfo.setDeviceType("DOOR");
				publicDeviceService.insertDevice(deviceInfo, operator);
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
		DeviceUser user = (DeviceUser) request.getSession().getAttribute(WebConstant.USER_SESSION);
		Operator operator = new Operator(user.getUserId(), "systemUser");
		publicDeviceService.softDeleteById(deviceId, operator);
		return new MapResponse();
	}

	@RequestMapping("/download")
	public String download(HttpServletRequest request, HttpServletResponse response, String doorName, String outerNo) {
		List<DeviceInfo> deviceList = publicDeviceService.selectDeviceByType(Constant.DeviceType.DOOR, doorName,
				outerNo);
		if (deviceList.isEmpty()) {
			return "redirect:/door/index";
		}
		List<DeviceInfoVo> lists = new ArrayList<DeviceInfoVo>();
		DeviceInfoVo vo = null;
		for (DeviceInfo device : deviceList) {
			vo = new DeviceInfoVo();
			vo.setDeviceId(device.getDeviceId());
			vo.setDoorIp(device.getDoorIp());
			vo.setDoorPort(device.getDoorPort());
			vo.setDoorAddr(device.getDoorAddr());
			vo.setDoorReadno(device.getDoorReadno());
			vo.setOuterNo(device.getOuterNo());
			vo.setDoorName(device.getDoorName());
			lists.add(vo);
		}
		List<String[]> dataList = convertListToArray(lists, 2);
		String[] titleArray = new String[] { "IP", "端口", "门禁地址", "设备名称", "配置" };
		String sheetName = "电表抄表明细";
		String fileName = "电表抄表明细";
		ExcelUtil.createExcel(dataList, titleArray, sheetName, fileName, response);
		return null;
	}

	@RequestMapping("/access/download")
	public String accessDownload(HttpServletRequest request, HttpServletResponse response, String doorName,
			String userMobile, String startTime, String endTime) {

		List<DeviceCommandLog> logList = publicDeviceService.selectCmdLogByType(Constant.DeviceType.DOOR, doorName, userMobile,
				startTime, endTime);
		if (CollectionUtils.isEmpty(logList)) {
			return "redirect:/door/access/index";
		}

		List<DeviceCommandLogVo> lists = new ArrayList<DeviceCommandLogVo>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DeviceCommandLogVo vo = null;
		for (DeviceCommandLog log : logList) {
			vo = new DeviceCommandLogVo();
			vo.setLogId(log.getLogId());
			vo.setUserMobile(log.getUserMobile());
			vo.setOuterNo(log.getOuterNo());
			vo.setDoorName(log.getDoorName());
			vo.setCreateTime(log.getCreateTime() == null ? "" : df.format(log.getCreateTime()));
			vo.setUserType("MANAGER".equals(log.getUserType()) ? "管理员" : "会员");
			lists.add(vo);
		}
		
		List<String[]> dataList = convertAccessListToArray(lists, 2);
		String[] titleArray = new String[] { "设备名称", "手机号", "访问时间", "用户类型", "配置编号" };
		String sheetName = "门禁访问记录";
		String fileName = "门禁访问记录";
		ExcelUtil.createExcel(dataList, titleArray, sheetName, fileName, response);
		return null;
	}

	public List<String[]> convertListToArray(List<DeviceInfoVo> list, int index) {
		List<String[]> result = new ArrayList<String[]>();
		if (null != list && list.size() > 0) {
			String[] arr = null;
			for (DeviceInfoVo inv : list) {
				arr = new String[5];
				arr[0] = inv.getDoorIp();
				arr[1] = inv.getDoorPort().toString();
				arr[2] = inv.getDoorAddr();
				arr[3] = inv.getDoorName();
				arr[4] = inv.getOuterNo();
				result.add(arr);
			}
		}
		return result;
	}

	public List<String[]> convertAccessListToArray(List<DeviceCommandLogVo> list, int index) {
		List<String[]> result = new ArrayList<String[]>();
		if (null != list && list.size() > 0) {
			String[] arr = null;
			for (DeviceCommandLogVo inv : list) {
				arr = new String[5];
				arr[0] = inv.getDoorName();
				arr[1] = inv.getUserMobile();
				arr[2] = inv.getCreateTime();
				arr[3] = inv.getUserType();
				arr[4] = inv.getOuterNo();
				result.add(arr);
			}
		}
		return result;
	}

}
