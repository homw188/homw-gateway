<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="/common/head.jsp"%>
</head>
<body>
  <div class="layui-fluid">
    <div class="layui-row layui-col-space15">
      <div class="layui-col-md12">
        <div class="layui-card">
          <div class="layui-card-header">门禁访问记录</div>
          <div class="layui-card-body">
            <div class="test-table-reload-btn" style="margin-bottom: 10px;">
                                           设备名称
              <div class="layui-inline">
                <input class="layui-input" name="doorName" id="doorName" autocomplete="off">
              </div>
                                           手机号
              <div class="layui-inline">
                <input class="layui-input" name="userMobile" id="userMobile" autocomplete="off">
              </div>
                                         时间范围
              <div class="layui-inline">
                <input class="layui-input" style="width: 300px;" name="timeRange" id="timeRange" lay-verify="date" autocomplete="off">
              </div>
              <button class="layui-btn" onclick="search()">查询</button>
              <button class="layui-btn" onclick="reset()">重置</button>
            </div>
            
            <table class="layui-hide" id="data-table" lay-filter="data-table"></table>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <script type="text/javascript" src="<c:url value="/statics/plugins/layui/layui.all.js"/>"></script>
  <script>
  layui.laydate.render({
	  elem: '#timeRange',
	  range: '~',
	  type: 'datetime',
	  format: 'yyyy-MM-dd HH:mm:ss'
  });
  
  layui.table.render({
      elem: '#data-table',
      url: '<c:url value="/door/access/view"/>',
      toolbar: '#data-table-toolbar',
      title: '访问记录数据表',
      cellMinWidth: 80, //全局定义常规单元格的最小宽度
      cols: [[
        //{type:'checkbox'},
        {field:'doorName', title:'设备名称', sort:true},
        {field:'userMobile', title:'手机号', sort:true},
        {field:'createTime', title:'访问时间', sort:true},
        {field:'userType', title:'用户类型'},
        {field:'outerNo', title:'设备编号', width:300}
      ]],
      page: true,
      parseData: function(res){ //将原始数据解析成 table 组件所规定的数据
        return {
          "code": res.code == "000000" ? 0 : res.code, //解析接口状态
          "msg": res.message, //解析提示文本
          "count": res.result.total, //解析数据长度
          "data": res.result.content //解析数据列表
        };
      }
  });
    
  function search() {
      var doorName = layui.$("#doorName").val();
      var userMobile = layui.$("#userMobile").val();
      var timeRange = layui.$("#timeRange").val();
      
      var startTime = ''
      var endTime = '';
      if (typeof timeRange != 'undefined' && timeRange != '' 
    		  && timeRange.indexOf('~') != -1) {
	      var timeArr = timeRange.split('~');
	      startTime = timeArr[0].trim();
	      endTime = timeArr[1].trim();
      }
      
      layui.table.reload('data-table', {
        page: {
          curr: 1 //重新从第 1 页开始
        },
        where: {
	      doorName: layui.$("#doorName").val(),
	      userMobile: layui.$("#userMobile").val(),
	      startTime: startTime,
	      endTime: endTime
        }
      });
  }
  
  function reset() {
	  layui.$("#doorName").val('');
	  layui.$("#userMobile").val('');
	  layui.$("#timeRange").val('');
	  search();
  }
  </script>
</body>
</html>