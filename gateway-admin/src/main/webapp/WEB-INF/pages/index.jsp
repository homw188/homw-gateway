<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="/common/head.jsp"%>
</head>
<body class="layui-layout-body">
  <div id="LAY_app">
    <div class="layui-layout layui-layout-admin">
      <div class="layui-header">
        <!-- 头部区域 -->
        <ul class="layui-nav layui-layout-left">
          <li class="layui-nav-item layadmin-flexible">
            <a href="javascript:;" onclick="sideFlexible()" title="侧边伸缩">
              <i class="layui-icon layui-icon-shrink-right" id="LAY_app_flexible"></i>
            </a>
          </li>
          <li class="layui-nav-item">
            <a href="javascript:;" onclick="refreshTab()" title="刷新">
              <i class="layui-icon layui-icon-refresh-3"> </i>
            </a>
          </li>
        </ul>
        <ul class="layui-nav layui-layout-right" lay-filter="layadmin-layout-right">
          <li class="layui-nav-item layui-hide-xs">
          	<a href="<c:url value="/loginOut"/>">退出</a>
          </li>
        </ul>
      </div>
      
      <!-- 侧边菜单 -->
      <div class="layui-side layui-side-menu">
        <div class="layui-side-scroll">
          <div class="layui-logo">
            <span>智能设备网关系统</span>
          </div>
          
          <ul class="layui-nav layui-nav-tree" lay-shrink="all" id="LAY-system-side-menu" lay-filter="layadmin-system-side-menu">
            <li data-name="set" class="layui-nav-item">
              <a href="javascript:;" lay-href="<c:url value="/door/index" />" lay-text="门禁管理" >
                <i class="layui-icon layui-icon-template"></i>
                <cite>门禁管理</cite>
              </a>
            </li>
            <li data-name="app" class="layui-nav-item">
              <a href="javascript:;" lay-href="<c:url value="/power/index" />" lay-text="电表管理">
                <i class="layui-icon layui-icon-console"></i>
                <cite>电表管理</cite>
              </a>
            </li>
            <li data-name="get" class="layui-nav-item">
              <a href="javascript:;" lay-href="<c:url value="/water/index" />" lay-text="水表管理">
                <i class="layui-icon layui-icon-console"></i>
                <cite>水表管理</cite>
              </a>
            </li>
            <li data-name="get" class="layui-nav-item">
              <a href="javascript:;" lay-href="<c:url value="/door/access/index" />" lay-text="历史记录">
                <i class="layui-icon layui-icon-auz"></i>
                <cite>历史记录</cite>
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 页面标签 -->
      <div class="layadmin-pagetabs" id="LAY_app_tabs">
      	<div class="layui-icon layadmin-tabs-control layui-icon-prev" onclick="leftPage()"></div>
        <div class="layui-icon layadmin-tabs-control layui-icon-next" onclick="rightPage()"></div>
        <div class="layui-icon layadmin-tabs-control layui-icon-down">
          <ul class="layui-nav layadmin-tabs-select" lay-filter="layadmin-pagetabs-nav">
            <li class="layui-nav-item">
              <a href="javascript:;"></a>
              <dl class="layui-nav-child layui-anim-fadein">
                <dd onclick="closeThisTabs()"><a href="javascript:;">关闭当前标签页</a></dd>
                <dd onclick="closeOtherTabs()"><a href="javascript:;">关闭其它标签页</a></dd>
                <dd onclick="closeAllTabs()"><a href="javascript:;">关闭全部标签页</a></dd>
              </dl>
            </li>
          </ul>
        </div>
      	
        <div class="layui-tab" lay-filter="layadmin-layout-tabs" lay-allowClose="true">
          <ul class="layui-tab-title" id="LAY_app_tabsheader">
            <li lay-id="<c:url value="/home"/>" lay-attr="<c:url value="/home"/>" class="layui-this">
            	<i class="layui-icon layui-icon-home"></i>
            </li>
          </ul>
        </div>
      </div>
      
      <!-- 主体内容 -->
      <div class="layui-body" id="LAY_app_body">
        <div class="layadmin-tabsbody-item layui-show">
          <iframe src="<c:url value="/home"/>" frameborder="0" class="layadmin-iframe"></iframe>
        </div>
      </div>
    </div>
  </div>

  <script type="text/javascript" src="<c:url value="/statics/plugins/layui/layui.all.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/statics/js/layui.admin.js"/>"></script>
</body>
</html>