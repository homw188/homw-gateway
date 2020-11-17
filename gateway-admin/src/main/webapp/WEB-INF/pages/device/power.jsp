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
          <div class="layui-card-header">电表管理</div>
          <div class="layui-card-body">
            <div class="test-table-reload-btn" style="margin-bottom: 10px;">
                                           设备名称
              <div class="layui-inline">
                <input class="layui-input" name="doorName" id="doorName" autocomplete="off">
              </div>
                                           设备编号
              <div class="layui-inline">
                <input class="layui-input" name="outerNo" id="outerNo" autocomplete="off">
              </div>
              <button class="layui-btn" onclick="search()">查询</button>
              <button class="layui-btn" onclick="reset()">重置</button>
            </div>
            
            <table class="layui-hide" id="data-table" lay-filter="data-table"></table>
             
            <script type="text/html" id="data-table-bar">
              <a class="layui-btn layui-btn-xs" lay-event="read">抄表</a>
            </script>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <script type="text/javascript" src="<c:url value="/statics/plugins/layui/layui.all.js"/>"></script>
  <script>
  layui.table.render({
      elem: '#data-table',
      url: '<c:url value="/power/view"/>',
      toolbar: '#data-table-toolbar',
      title: '电表设备数据表',
      cellMinWidth: 80, //全局定义常规单元格的最小宽度
      cols: [[
        //{type:'checkbox'},
        {field:'doorName', title:'设备名称', sort:true},
        {field:'elecAddr', title:'MAC地址'},
        {field:'elecStatus', title:'电表状态'},
        {field:'elecUsePoint', title:'电表读数'},
        {field:'updateTime', title:'更新时间', width:160, sort:true},
        {field:'outerNo', title:'设备编号', width:260},
        {fixed:'right', title:'操作', toolbar:'#data-table-bar'}
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
      var outerNo = layui.$("#outerNo").val();
      
      layui.table.reload('data-table', {
        page: {
          curr: 1 //重新从第 1 页开始
        },
        where: {
	      doorName: layui.$("#doorName").val(),
	      outerNo: layui.$("#outerNo").val()
        }
      });
  }
  
  function reset() {
	  layui.$("#doorName").val('');
	  layui.$("#outerNo").val('');
	  search();
  }
  
  function readMeter(outerNo){
	  var index = layui.layer.load();
      layui.$.ajax({
            type: "POST",
            url: "<c:url value='/power/search'/>",
            data: "outerNo=" + outerNo,
            success: function(data) {
            	layui.layer.close(index);
	            if (data.code == "000000") {
                	layui.layer.alert("抄表成功");
                    search();
	            } else {
	            	layui.layer.alert(data.message);
	            }
            },
            error: function(e) {
            	layui.layer.close(index);
            	layui.layer.alert("抄表失败：" + e);
		    }
       });
  }
  
  //监听行工具事件
  layui.table.on('tool(data-table)', function(obj) {
      var data = obj.data;
      if(obj.event === 'read'){
    	readMeter(data.outerNo);
      }
  });
  </script>
</body>
</html>