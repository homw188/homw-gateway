package com.homw.gateway.api.koala;

import java.net.URI;
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
import com.homw.gateway.common.exception.ServiceException;
import com.homw.gateway.common.util.ImageUtil;
import com.homw.gateway.entity.dto.FaceCommandMessage;

/**
 * @description koala主机api，主机3.0版本
 * @author James
 * @version 1.0
 * @date 2019-11-02
 */
public class KoalaApi_3_0 
{
	private static Logger logger = LoggerFactory.getLogger(KoalaApi_3_0.class);
	
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
		JSONArray photo_ids = null;
		// 获取所有用户列表
		JSONArray users = getUsers(url);
		for (Object obj : users) 
		{
			JSONObject user = (JSONObject) obj;
			String phone = user.getString("phone");
			// 按手机号查找是否已存在
			if (message.getFacePhone().equals(phone)) 
			{
				subject_id = user.getIntValue("id");
				photo_ids = user.getJSONArray("photo_ids");
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
		} else {
			// 3、更新用户
			if (StringUtils.isEmpty(message.getFacePhoto())) {
				updateUser(url, subject_id, photo_ids.toArray(), message);
			} else {
				// 检查入库底图质量
				checkPhoto(url, message.getFacePhoto(), message.getFacePhotoFileName());
				// 更新用户信息，并清空识别底图
				updateUser(url, subject_id, new Object[] {}, message);
				// 上传新的识别底图
				uploadPhoto(url, message.getFacePhoto(), message.getFacePhotoFileName(), subject_id);
			}
		}
		
		// 4、绑定门禁
		/*if (CollectionUtils.isNotEmpty(screen_ids)) {
			for (Integer screen_id : screen_ids) {
				bindCamera(url, subject_id, screen_id);
			}
		}*/
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
		JSONArray users = getUsers(url);
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
			// 2、解绑门禁
			if (CollectionUtils.isNotEmpty(screen_ids)) {
				for (Integer screen_id : screen_ids) {
					unbindCamera(url, subject_id, screen_id);
				}
			}
			// 3、删除底图
			deletePhoto(url, subject_id);
			// 4、删除用户
			deleteSubject(url, subject_id);
		}
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
		// 避免中文字段乱码
		reqEntity.setCharset(Consts.UTF_8);
		// 0:员工, 1:访客, 2: VIP访客, 3: 黄名单
		reqEntity.addTextBody("subject_type", message.getFaceUserType()); // 如果subject_type不等于0，必须要指定start_time，end_time
		reqEntity.addTextBody("phone", message.getFacePhone());
		reqEntity.addTextBody("name", message.getFaceUserName());
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
	 * 获取所有用户列表
	 * @param url
	 * @return 用户列表
	 * @throws Exception
	 */
	public static JSONArray getUsers(String url) throws Exception 
	{
		logger.info("Start GET /mobile-admin/subjects 获取所有用户列表 ...");

		// 自定义HttpClients 设置CookieStore
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpGet request = new HttpGet(url + "/mobile-admin/subjects");

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
	 * 绑定用户
	 * @param url
	 * @param subject_id
	 * @param screen_id
	 * @throws Exception
	 */
	public static void bindCamera(String url, int subject_id, int screen_id) throws Exception
	{
		logger.info("Start PUT /subject/bind_camera 绑定用户...");
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpPut request = new HttpPut(url + "/subject/bind_camera");
		
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("subject_id", subject_id);
		json.put("screen_id", screen_id);

		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		// 发起网络请求，获取结果值
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpclient.execute(request, context);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	/**
	 * 解绑用户
	 * @param url
	 * @param subject_id
	 * @param screen_id
	 * @throws Exception
	 */
	public static void unbindCamera(String url, int subject_id, int screen_id) throws Exception
	{
		logger.info("Start DELETE /subject/bind_camera 解绑用户...");
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(authCookie).build();
		HttpDeleteWithBody request = new HttpDeleteWithBody(url + "/subject/bind_camera");
		
		// 设置Content-Type
		request.setHeader("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("subject_id", subject_id);
		json.put("screen_id", screen_id);

		request.setEntity(new StringEntity(json.toString(), "UTF-8"));

		// 发起网络请求，获取结果值
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpclient.execute(request, context);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
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
		
		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
		reqEntity.addTextBody("subject_id", String.valueOf(subject_id));
		
		HttpDeleteWithBody request = new HttpDeleteWithBody(url + "/subject/photo");
		request.setEntity(reqEntity.build());

		CloseableHttpResponse response = httpclient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		// 解析JSON数据
		JSONObject resp = JSON.parseObject(responseBody);
		checkException(resp);
	}
	
	static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
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
