package com.homw.gateway.api.koala;

import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.util.ImageUtil;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.dto.FaceCommandMessage;

/**
 * @description koala主机api，主机3.1版本
 * @author James
 * @version 1.0
 * @date 2019-11-18
 */
public class KoalaApi_3_1 
{
	private static Logger logger = LoggerFactory.getLogger(KoalaApi_3_1.class);
	
	private static CookieStore authCookie = null;
	
	/**
	 * 初始化，登录koala主机并保存认证信息
	 * @param url
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public static void init(String url, String username, String password) throws Exception {
		authCookie = authLogin(url, username, password);
	}
	
	/**
	 * 登录 获取 Cookie
	 * @param url API地址
	 * @param username 账号, 注意不要使用admin@megvii.com
	 * @param password 密码
	 * @return cookie CookieStore
	 * @throws Exception
	 */
	public static CookieStore authLogin(String url, String username, String password) throws Exception
	{
		logger.info("Start /auth/login to ...");
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost request = new HttpPost(url + "/auth/login");

		// 设置user-agent为 "Koala Admin"
		// 设置Content-Type为 "application/json"
		request.setHeader("User-Agent", "Koala Admin");
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("password", password);

		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		// 发起网络请求，获取结果值
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpclient.execute(request, context);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		logger.info("Login Success, id:" + resp.getJSONObject("data").getIntValue("id"));
		return context.getCookieStore();
	}
	
	/**
	 * 上传用户信息
	 * @param url
	 * @param message
	 * @param screen_ids
	 * @return
	 * @throws Exception
	 */
	public static void uploadUserInfo(String url, FaceCommandMessage message, List<Integer> screen_ids) throws Exception 
	{
		if (authCookie == null) {
			throw new IllegalStateException("未初始化");
		}
		
		if (StringUtils.isEmpty(message.getFacePhone())) {
			throw new IllegalArgumentException("手机号不能为空");
		}
		
		// 1、检查用户是否已存在
		int subject_id = -1;
		JSONArray photo_ids = new JSONArray();
		// 获取所有用户列表
		JSONArray users = getUsers(url, getUserCategory(message.getFaceUserType()), message.getFaceUserName());
		for (Object obj : users) 
		{
			JSONObject user = (JSONObject) obj;
			String phone = user.getString("phone");
			// 按手机号查找是否已存在
			if (message.getFacePhone().equals(phone)) 
			{
				subject_id = user.getIntValue("id");
				JSONArray photos = user.getJSONArray("photos");
				for (Object photo : photos) {
					photo_ids.add(((JSONObject) photo).get("id"));
				}
				break;
			}
		}
		
		if (subject_id == -1) 
		{
			// 2、创建用户
			if (StringUtils.isEmpty(message.getFacePhoto())) {
				subject_id = createUser(url, message);
			} else {
				subject_id = createUserWithPhoto(url, message);
			}
		} else 
		{
			// 3、更新用户
			if (StringUtils.isEmpty(message.getFacePhoto())) {
				updateUser(url, subject_id, photo_ids.toArray(), message);
			} else 
			{
				// 检查入库底图质量
				checkPhoto(url, message.getFacePhoto(), message.getFacePhotoFileName());
				// 更新用户信息，并清空识别底图
				updateUser(url, subject_id, new Object[] {}, message);
				// 上传新的识别底图
				uploadPhoto(url, message.getFacePhoto(), message.getFacePhotoFileName(), subject_id);
			}
		}
		
		// 4、建人员分组
		int subject_group_id = getUserGroupId(url, message.getFaceUserGroupName(), subject_id, message.getFaceUserType());
		
		// 5、建门禁分组
		int screen_group_id = getScreenGroupId(url, message.getFaceDeviceGroupName(), screen_ids);
		
		// 时段有效期
		String start_time = StringUtil.formatTimestamp(Long.valueOf(message.getFaceStartTime()), "yyyy-MM-dd");
		String end_time = StringUtil.formatTimestamp(Long.valueOf(message.getFaceEndTime()), "yyyy-MM-dd");
		// 计算门禁访问时段
		List<List<long[]>> timeRange = calcTimeRange(message.getFaceTimeRangeStart(), message.getFaceTimeRangeEnd());
		
		// 6、建时间段
		int schedule_id = getScheduleGroupId(url, message.getFaceUserGroupName(), start_time, end_time, timeRange);
		
		// 7、建权限设置
		JSONArray settings = getSettings(url, String.valueOf(subject_id), String.valueOf(screen_group_id),
				String.valueOf(schedule_id));
		if (CollectionUtils.isEmpty(settings)) {
			createSetting(url, message.getFaceUserGroupName(), subject_group_id, Arrays.asList(screen_group_id),
					Arrays.asList(schedule_id), null);
		}
	}
	
	/**
	 * 计算门禁访问时间段，默认周一到周五早8点，晚6点，周末休息
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private static List<List<long[]>> calcTimeRange(String startTime, String endTime) 
	{
		List<List<long[]>> time_range = Lists.newArrayList();
		Calendar zero = Calendar.getInstance();
		zero.set(Calendar.HOUR, 0);
		zero.set(Calendar.MINUTE, 0);
		zero.set(Calendar.SECOND, 0);
		zero.set(Calendar.MILLISECOND, 0);
		
		Calendar start = Calendar.getInstance();
		try {
			start.set(Calendar.HOUR, Integer.valueOf(startTime));
		} catch (NumberFormatException e) {
			start.set(Calendar.HOUR, 8);
		}
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		
		Calendar end = Calendar.getInstance();
		try {
			end.set(Calendar.HOUR, Integer.valueOf(endTime));
		} catch (NumberFormatException e) {
			end.set(Calendar.HOUR, 18);
		}
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);
		
		// 每天
		List<long[]> weekday = null;
		// 工作日
		long[] workday = new long[] { (start.getTimeInMillis() - zero.getTimeInMillis()) / 1000, (end.getTimeInMillis() - zero.getTimeInMillis()) / 1000 };
		// 周末
		long[] weekend = new long[] { 0, 0 };
		for (int i = 1; i <= 7; i++) 
		{
			weekday = Lists.newArrayList();
			if (i < 6) {
				weekday.add(workday);
			} else {
				weekday.add(weekend);
			}
			time_range.add(weekday);
		}
		return time_range;
	}

	/**
	 * 查询时间段id
	 * @param url
	 * @param groupName
	 * @param startTime
	 * @param endTime
	 * @param time_range
	 * @return
	 * @throws Exception
	 */
	private static int getScheduleGroupId(String url, String groupName, String startTime, String endTime,
			List<List<long[]>> time_range) throws Exception 
	{
		int schedule_id = -1;
		JSONArray schedules = getSchedules(url, groupName);
		if (CollectionUtils.isNotEmpty(schedules)) 
		{
			JSONObject schedule = (JSONObject) schedules.get(0);
			schedule_id = schedule.getIntValue("id");
			updateSchedule(url, schedule_id, startTime, endTime, time_range);
		} else {
			schedule_id = createSchedule(url, groupName, startTime, endTime, time_range);
		}
		return schedule_id;
	}
	
	/**
	 * 查询门禁组id
	 * @param url
	 * @param groupName
	 * @param screen_ids
	 * @return
	 * @throws Exception
	 */
	private static int getScreenGroupId(String url, String groupName, List<Integer> screen_ids) throws Exception
	{
		int screen_group_id = -1;
		JSONArray screens = getScreenGroups(url, groupName);
		if (CollectionUtils.isNotEmpty(screens)) {
			JSONObject screen = (JSONObject) screens.get(0);
			screen_group_id = screen.getIntValue("id");
		} else {
			screen_group_id = createScreenGroup(url, groupName);
		}
		
		if (CollectionUtils.isNotEmpty(screen_ids)) 
		{
			JSONObject screenGroup = getScreenGroup(url, screen_group_id);
			screens = screenGroup.getJSONArray("screens");
			if (CollectionUtils.isNotEmpty(screens)) 
			{
				for (Object obj : screens) 
				{
					JSONObject screen = (JSONObject) obj;
					int screen_id = screen.getIntValue("id");
					if (screen_ids.contains(screen_id)) {
						screen_ids.remove(Integer.valueOf(screen_id));
					}
				}
			}
			if (CollectionUtils.isNotEmpty(screen_ids)) {
				addScreen(url, screen_group_id, screen_ids);
			}
		}
		return screen_group_id;
	}

	/**
	 * 查询人员组id
	 * @param url
	 * @param groupName
	 * @param subject_id
	 * @param subject_type
	 * @return
	 * @throws Exception
	 */
	private static int getUserGroupId(String url, String groupName, int subject_id, String subject_type)
			throws Exception 
	{
		int user_group_id = -1;
		JSONArray groups = getUserGroups(url, groupName, subject_type);
		if (CollectionUtils.isNotEmpty(groups)) {
			JSONObject group = (JSONObject) groups.get(0);
			user_group_id = group.getIntValue("id");
		} else {
			user_group_id = createUserGroup(url, groupName, subject_type);
		}
		
		boolean exists = false;
		JSONObject userGroup = getUserGroup(url, user_group_id);
		JSONArray subjects = userGroup.getJSONArray("subjects");
		if (CollectionUtils.isNotEmpty(subjects)) 
		{
			for (Object obj : subjects) 
			{
				JSONObject subject = (JSONObject) obj;
				if (subject_id == subject.getIntValue("id")) {
					exists = true;
					break;
				}
			}
		}
		if (!exists) {
			addUser(url, user_group_id, Arrays.asList(subject_id));
		}
		return user_group_id;
	}
	
	/**
	 * 删除用户信息
	 * @param url
	 * @param message
	 * @param screen_ids
	 * @return
	 * @throws Exception
	 */
	public static void deleteUserInfo(String url, FaceCommandMessage message, List<Integer> screen_ids) throws Exception 
	{
		if (authCookie == null) {
			throw new IllegalStateException("未初始化");
		}
		
		if (StringUtils.isEmpty(message.getFacePhone())) {
			throw new IllegalArgumentException("手机号不能为空");
		}
		
		// 1、检查用户是否已存在
		int subject_id = -1;
		// 获取所有用户列表
		JSONArray users = getUsers(url, getUserCategory(message.getFaceUserType()), message.getFaceUserName());
		for (Object obj : users) 
		{
			JSONObject user = (JSONObject) obj;
			String phone = user.getString("phone");
			// 按手机号查找是否已存在
			if (message.getFacePhone().equals(phone)) 
			{
				subject_id = user.getIntValue("id");
				break;
			}
		}
		
		if (subject_id != -1) 
		{
			// 2、删除底图
			//deletePhoto(url, subject_id);
			// 3、删除用户
			//deleteSubject(url, subject_id);
			
			// 4、删除人员分组
			int user_group_id = -1;
			JSONArray groups = getUserGroups(url, message.getFaceUserGroupName(), message.getFaceUserType());
			if (CollectionUtils.isNotEmpty(groups))
			{
				JSONObject group = (JSONObject) groups.get(0);
				user_group_id = group.getIntValue("id");
				
				JSONObject userGroup = getUserGroup(url, user_group_id);
				JSONArray subjects = userGroup.getJSONArray("subjects");
				if (CollectionUtils.isEmpty(subjects))
				{
					// TODO: 未提供删除人员分组接口
					
					// 5、删除时段分组
					int schedule_id = -1;
					JSONArray schedules = getSchedules(url, message.getFaceUserGroupName());
					if (CollectionUtils.isNotEmpty(schedules)) 
					{
						JSONObject schedule = (JSONObject) schedules.get(0);
						schedule_id = schedule.getIntValue("id");
					} 
					// 6、删除门禁规则
				}
			} 
		}
	}

	/**
	 * 查询用户类型，默认visitor
	 * @param subject_type 0员工，1访客，2vip访客，3黄名单
	 * @return
	 */
	public static String getUserCategory(String subject_type) 
	{
		String category = "visitor";
		if ("0".equals(subject_type)) {
			category = "employee";
		} else if ("3".equals(subject_type)){
			category = "yellowlist";
		}
		return category;
	}
	
	/**
	 * 创建用户
	 * @param url
	 * @param message
	 * @return 用户id
	 * @throws Exception
	 */
	public static int createUser(String url, FaceCommandMessage message) throws Exception
	{
		logger.info("Start POST /subject 创建用户 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPost request = new HttpPost(url + "/subject");

		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		// 类型，0员工，1访客，2vip，3黄名单
		json.put("subject_type", message.getFaceUserType());// 如果subject_type不等于0，必须要指定start_time，end_time
		json.put("phone", message.getFacePhone());
		json.put("name", message.getFaceUserName());
		json.put("start_time", String.valueOf(Long.valueOf(message.getFaceStartTime()) / 1000));
		json.put("end_time", String.valueOf(Long.valueOf(message.getFaceEndTime()) / 1000));
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 上传识别照片
	 * @param url
	 * @param photo 底库照片，base64编码
	 * @param photoFileName
	 * @param subject_id 用户id
	 * @return 图片id
	 * @throws Exception
	 */
	public static int uploadPhoto(String url, String photo, String photoFileName, int subject_id) throws Exception
	{
		logger.info("Start POST /subject/photo 上传识别照片 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		// 设置底库照片 并关联到用户
		// BROWSER_COMPATIBLE自定义charset，RFC6532=utf-8，STRICT=iso-8859-1
		// 此处一定要用RFC6532，网上普遍用的BROWSER_COMPATIBLE依然会出现中文名乱码
		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
		reqEntity.addBinaryBody("photo", ImageUtil.decodeBase64ToImage(photo), ContentType.DEFAULT_BINARY, photoFileName);
		reqEntity.addTextBody("subject_id", String.valueOf(subject_id));
		
		HttpPost request = new HttpPost(url + "/subject/photo");
		request.setEntity(reqEntity.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 入库图片质量判断
	 * @param url
	 * @param photo 底库照片，base64编码
	 * @param photoFileName
	 * @throws Exception
	 */
	public static void checkPhoto(String url, String photo, String photoFileName) throws Exception
	{
		logger.info("Start POST /subject/photo/check 入库图片质量判断 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
		reqEntity.addBinaryBody("photo", ImageUtil.decodeBase64ToImage(photo), ContentType.DEFAULT_BINARY, photoFileName);
		
		HttpPost request = new HttpPost(url + "/subject/photo/check");
		request.setEntity(reqEntity.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}

	/**
	 * 创建用户及识别底图
	 * @param url
	 * @param message
	 * @return 用户id
	 * @throws Exception
	 */
	public static int createUserWithPhoto(String url, FaceCommandMessage message) throws Exception 
	{
		logger.info("Start POST /subject/file 创建用户 ...");
		
		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();

		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
		// 0:员工, 1:访客, 2: VIP访客, 3: 黄名单
		reqEntity.addTextBody("subject_type", message.getFaceUserType()); // 如果subject_type不等于0，必须要指定start_time，end_time
		reqEntity.addTextBody("phone", message.getFacePhone());
		reqEntity.addTextBody("name", message.getFaceUserName(), ContentType.create("text/plain", Consts.UTF_8));
		// 时间戳（秒）
		reqEntity.addTextBody("start_time", String.valueOf(Long.valueOf(message.getFaceStartTime()) / 1000));
		reqEntity.addTextBody("end_time", String.valueOf(Long.valueOf(message.getFaceEndTime()) / 1000));
		reqEntity.addBinaryBody("photo", ImageUtil.decodeBase64ToImage(message.getFacePhoto()),
				ContentType.DEFAULT_BINARY, message.getFacePhotoFileName());
		
		HttpPost request = new HttpPost(url + "/subject/file");
		request.setEntity(reqEntity.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSONObject.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 获取人员列表
	 * @param url
	 * @param category employee员工, visitor访客, yellowlist黄名单
	 * @param name 按姓名查询
	 * @return 人员列表
	 * @throws Exception
	 */
	public static JSONArray getUsers(String url, String category, String name) throws Exception 
	{
		logger.info("Start GET /subject/list 获取人员列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		URIBuilder uriBuilder = new URIBuilder(url + "/subject/list");
		uriBuilder.addParameter("category", category);
		if (StringUtils.isNotEmpty(name)) {
			uriBuilder.addParameter("name", name);
		}
		HttpGet request = new HttpGet(uriBuilder.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONArray("data");
	}

	/**
	 * 检查异常
	 * @param resp
	 * @throws Exception
	 */
	private static void checkException(JSONObject resp) throws ServiceException
	{
		int code = resp.getIntValue("code");
		if (code != 0) {
			String desc = resp.getString("desc");
			logger.error("code:" + code + ", desc:" + desc);
			throw new ServiceException(String.valueOf(code), desc);
		}
	}
	
	/**
	 * 更新用户信息
	 * @param url /subject/[id]
	 * @param subject_id
	 * @param photo_ids 底库列表，空列表会删除用户所有底库
	 * @param message 名字
	 * @throws Exception
	 */
	public static void updateUser(String url, int subject_id, Object[] photo_ids, FaceCommandMessage message) throws Exception 
	{
		logger.info("Start PUT /subject/[id] 更新用户信息 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		JSONObject json = new JSONObject();
		// 0:员工, 1:访客, 2: VIP访客, 3: 黄名单
        json.put("subject_type", message.getFaceUserType()); // 如果subject_type不等于0，必须要指定start_time，end_time
        json.put("phone", message.getFacePhone());
        json.put("name", message.getFaceUserName());
		// 时间戳（秒）
        json.put("start_time", String.valueOf(Long.valueOf(message.getFaceStartTime()) / 1000));
        json.put("end_time", String.valueOf(Long.valueOf(message.getFaceEndTime()) / 1000));
        json.put("photo_ids", photo_ids);

		HttpPut request = new HttpPut(url + "/subject/" + subject_id);
		//设置Content-Type
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		JSONObject userInfo = resp.getJSONObject("data");
		logger.info("id:" + userInfo.getIntValue("id") + ",name:" + userInfo.getString("name"));
	}
	
	/**
	 * 删除用户
	 * @param url
	 * @param subject_id
	 * @throws Exception
	 */
	public static void deleteSubject(String url, int subject_id) throws Exception
	{
		System.out.println("Start DELETE /subject/[id] 删除用户 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpDelete request = new HttpDelete(url + "/subject/" + subject_id);
		// 设置Content-Type
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 删除用户底库
	 * @param url
	 * @param subject_id
	 * @throws Exception
	 */
	public static void deletePhoto(String url, int subject_id) throws Exception
	{
		logger.info("Start DELETE /subject/photo 删除用户底库 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		HttpDeleteWithBody request = new HttpDeleteWithBody(url + "/subject/photo");
		// 设置Content-Type
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		JSONObject json = new JSONObject();
		json.put("subject_id", subject_id);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 获取人员分组列表
	 * @param url
	 * @param name
	 * @param subject_type 0: 员工 1:访客 不传或者其他: 全部
	 * @return
	 * @throws Exception
	 */
	public static JSONArray getUserGroups(String url, String name, String subject_type) throws Exception 
	{
		logger.info("Start GET /subjects/group/list 获取人员分组列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		URIBuilder uriBuilder = new URIBuilder(url + "/subjects/group/list");
		if (StringUtils.isNotEmpty(name)) {
			uriBuilder.addParameter("name", name);
		}
		if (StringUtils.isNotEmpty(subject_type)) {
			uriBuilder.addParameter("subject_type", subject_type);
		}
		HttpGet request = new HttpGet(uriBuilder.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONArray("data");
	}
	
	/**
	 * 获取人员分组
	 * @param url
	 * @param gid
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getUserGroup(String url, int gid) throws Exception 
	{
		logger.info("Start GET  /subjects/group/{gid} 获取人员分组 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpGet request = new HttpGet(url + "/subjects/group/" + gid);

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data");
	}
	
	/**
	 * 加入人员
	 * @param url
	 * @param gid
	 * @param subject_ids
	 * @return
	 * @throws Exception
	 */
	public static void addUser(String url, int gid, List<Integer> subject_ids) throws Exception
	{
		logger.info("Start POST /subjects/group/{gid}/insert 加入人员 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPost request = new HttpPost(url + "/subjects/group/" + gid + "/insert");

		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("subject_ids", subject_ids);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 创建人员分组
	 * @param url
	 * @param name
	 * @param subject_type 0:员工 1:访客，非必填，默认0
	 * @return 分组id
	 * @throws Exception
	 */
	public static int createUserGroup(String url, String name, String subject_type) throws Exception
	{
		logger.info("Start PUT /subjects/group 创建人员分组 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPut request = new HttpPut(url + "/subjects/group");
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("subject_type", subject_type);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 获取门禁分组列表
	 * @param url
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static JSONArray getScreenGroups(String url, String name) throws Exception 
	{
		logger.info("Start GET /devices/screens/group/list 获取门禁分组列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		URIBuilder uriBuilder = new URIBuilder(url + "/devices/screens/group/list");
		if (StringUtils.isNotEmpty(name)) {
			uriBuilder.addParameter("name", name);
		}
		HttpGet request = new HttpGet(uriBuilder.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONArray("data");
	}
	
	/**
	 * 获取门禁分组
	 * @param url
	 * @param gid
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getScreenGroup(String url, int gid) throws Exception 
	{
		logger.info("Start GET /devices/screens/group/{gid} 获取门禁分组 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpGet request = new HttpGet(url + "/devices/screens/group/" + gid);

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data");
	}
	
	/**
	 * 加入门禁
	 * @param url
	 * @param gid
	 * @param screen_ids
	 * @return
	 * @throws Exception
	 */
	public static void addScreen(String url, int gid, List<Integer> screen_ids) throws Exception
	{
		logger.info("Start POST /devices/screens/group/{gid}/insert 加入门禁 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPost request = new HttpPost(url + "/devices/screens/group/" + gid + "/insert");

		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("screen_ids", screen_ids);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 创建门禁分组
	 * @param url
	 * @param name
	 * @return 分组id
	 * @throws Exception
	 */
	public static int createScreenGroup(String url, String name) throws Exception
	{
		logger.info("Start PUT /devices/screens/group 创建门禁分组 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPut request = new HttpPut(url + "/devices/screens/group");
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("name", name);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 删除门禁分组
	 * @param url
	 * @param screen_group_id
	 * @throws Exception
	 */
	public static void deleteScreenGroup(String url, int screen_group_id) throws Exception 
	{
		logger.info("Start DELETE /devices/screens/group/{gid} 删除门禁分组");
		
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpDelete request = new HttpDelete(url + "/devices/screens/group/" + screen_group_id);
		request.setHeader("Content-Type", "application/json");
		
		CloseableHttpResponse response = httpClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 删除人员分组
	 * @param url
	 * @param subject_group_id
	 * @throws Exception
	 */
	public static void deleteUserGroup(String url, int subject_group_id) throws Exception 
	{
		logger.info("Start DELETE /subjects/group/{gid} 删除人员分组");
		
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpDelete request = new HttpDelete(url + "/subjects/group/" + subject_group_id);
		request.setHeader("Content-Type", "application/json");
		
		CloseableHttpResponse response = httpClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 获取时段列表
	 * @param url
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static JSONArray getSchedules(String url, String name) throws Exception 
	{
		logger.info("Start GET /access/schedule/list 获取时段列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		URIBuilder uriBuilder = new URIBuilder(url + "/access/schedule/list");
		if (StringUtils.isNotEmpty(name)) {
			uriBuilder.addParameter("name", name);
		}
		HttpGet request = new HttpGet(uriBuilder.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONArray("data");
	}
	
	/**
	 * 创建时段规则
	 * @param url
	 * @param name
	 * @param start_time
	 * @param end_time
	 * @param time_range
	 * @return 时段id
	 * @throws Exception
	 */
	public static int createSchedule(String url, String name, String start_time, String end_time,
			List<List<long[]>> time_range) throws Exception
	{
		logger.info("Start PUT /access/schedule 创建时段规则 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPut request = new HttpPut(url + "/access/schedule");
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("start_time", start_time);
		json.put("end_time", end_time);
		json.put("time_range", time_range);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	/**
	 * 更新时段规则
	 * @param url
	 * @param schedule_id
	 * @param start_time
	 * @param end_time
	 * @param time_range
	 * @throws Exception
	 */
	public static void updateSchedule(String url, int schedule_id, String start_time, String end_time,
			List<List<long[]>> time_range) throws Exception
	{
		logger.info("Start POST /access/schedule/{id} 更新时段规则 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPost request = new HttpPost(url + "/access/schedule/" + schedule_id);
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("start_time", start_time);
		json.put("end_time", end_time);
		json.put("time_range", time_range);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		JSONObject schedule = resp.getJSONObject("data");
		logger.info("id:" + schedule.getIntValue("id") + ",name:" + schedule.getString("name"));
	}
	
	/**
	 * 获取门禁设置列表
	 * @param url
	 * @param subject_id
	 * @param screen_group_id
	 * @param schedule_id
	 * @return
	 * @throws Exception
	 */
	public static JSONArray getSettings(String url, String subject_id, String screen_group_id, String schedule_id)
			throws Exception 
	{
		logger.info("Start GET /access/setting/list 获取门禁设置列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		
		URIBuilder uriBuilder = new URIBuilder(url + "/access/setting/list");
		if (StringUtils.isNotEmpty(subject_id)) {
			uriBuilder.addParameter("subject_id", subject_id);
		}
		if (StringUtils.isNotEmpty(screen_group_id)) {
			uriBuilder.addParameter("screen_group_id", screen_group_id);
		}
		if (StringUtils.isNotEmpty(schedule_id)) {
			uriBuilder.addParameter("schedule_id", schedule_id);
		}
		HttpGet request = new HttpGet(uriBuilder.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONArray("data");
	}
	
	/**
	 * 创建门禁设置
	 * @param url
	 * @param name
	 * @param subject_group_id 特殊值 全员工:0 全访客: 1
	 * @param screen_group_ids
	 * @param schedule_ids
	 * @param calendar_ids
	 * @return 设置id
	 * @throws Exception
	 */
	public static int createSetting(String url, String name, int subject_group_id, List<Integer> screen_group_ids,
			List<Integer> schedule_ids, List<Integer> calendar_ids) throws Exception
	{
		logger.info("Start PUT /access/setting 创建门禁设置 ...");

		// 自定义HttpClient 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPut request = new HttpPut(url + "/access/setting");
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("subject_group_id", subject_group_id);
		json.put("screen_group_ids", screen_group_ids);
		json.put("schedule_ids", schedule_ids);
		json.put("calendar_ids", calendar_ids == null ? Arrays.asList() : calendar_ids);
		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
		return resp.getJSONObject("data").getIntValue("id");
	}
	
	static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase 
	{
	    public static final String METHOD_NAME = "DELETE";
	    
	    @Override
	    public String getMethod() {
	        return METHOD_NAME;
	    }

	    public HttpDeleteWithBody(final String uri) {
	        super();
	        setURI(URI.create(uri));
	    }

	    public HttpDeleteWithBody(final URI uri) {
	        super();
	        setURI(uri);
	    }

	    public HttpDeleteWithBody() {
	        super();
	    }
	}
}
