<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="/common/head.jsp"%>
  <link rel="stylesheet" href="<c:url value="/statics/plugins/layui/css/layui.login.css"/>">
</head>
<body>
  <div class="layadmin-user-login layadmin-user-display-show" id="LAY-user-login" style="display: none;">
    <div class="layadmin-user-login-main">
      <div class="layadmin-user-login-box layadmin-user-login-header">
        <h2>智能设备网关系统</h2>
      </div>
      <div class="layadmin-user-login-box layadmin-user-login-body layui-form">
        <form action="<c:url value="/loginIntoSystem"/>" method="post">
	        <div class="layui-form-item">
	          <label class="layadmin-user-login-icon layui-icon layui-icon-username" for="LAY-user-login-username"></label>
	          <input type="text" name="name" id="LAY-user-login-username" lay-verify="required" placeholder="用户名" class="layui-input">
	        </div>
	        <div class="layui-form-item">
	          <label class="layadmin-user-login-icon layui-icon layui-icon-password" for="LAY-user-login-password"></label>
	          <input type="password" name="password" id="LAY-user-login-password" lay-verify="required" placeholder="密码" class="layui-input">
	        </div>
	        <div class="layui-form-item">
	          <button class="layui-btn layui-btn-fluid" lay-submit lay-filter="LAY-user-login-submit">登 入</button>
	        </div>
	        
	  		<br><span>${errorMsg}<%session.removeAttribute("errorMsg"); %></span>
        </form>
      </div>
    </div>
    
    <div class="layui-trans layadmin-user-login-footer">
      <p>© 2020 <a href="<c:url value="/index"/>" target="_blank">智能设备网关系统</a></p>
    </div>
  </div>
  
  <script type="text/javascript" src="<c:url value="/statics/plugins/layui/layui.all.js"/>"></script>
</body>
</html>