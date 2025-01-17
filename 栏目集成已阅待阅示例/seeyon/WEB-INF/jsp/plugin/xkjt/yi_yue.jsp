<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>已阅</title>
<style>
html,body{
	margin:0;
	padding:0;
}
a{
	text-decoration:none;
	color:#000;
}

td {
	font-family: Arial, "Ping Fang SC", "Microsoft YaHei", Helvetica, sans-serif, "SimSun";
	height:30px;
	line-height:30px;
	white-space: nowrap;
	padding-left: 5px;
	font-size:14px;
}
</style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
    <tbody>
	    <c:forEach var="detail" items="${xkjtLeaderYiYues}">
		    <tr>
	            <td width="50%" onclick="xkjtOpen(this,'${detail.id}','${detail.edocId}')"><a href="javascript:void(0);">${detail.title}</a></td>
	            <td width="10%" align="center"><span title="${detail.senderName}">${detail.senderName}</span></td>
	            <td width="30%" align="center"><span title="<fmt:formatDate value='${detail.sendDate}' pattern='yyyy-MM-dd' />"><fmt:formatDate value='${detail.sendDate}' pattern='yyyy-MM-dd' /></span></td>
	            <td width="10%" align="center"><span title="已阅">已阅</span></td>
	        </tr>
		</c:forEach>
    </tbody>
</table>
</body>
<script type="text/javascript">
function xkjtOpen(obj,id,edocId){
	var url = "edocController.do?method=edocDetailInDoc&summaryId="+edocId+"&openFrom=lenPotent&lenPotent=100&isShow=1";
	window.open(url,"_blank");
}
</script>
</html>