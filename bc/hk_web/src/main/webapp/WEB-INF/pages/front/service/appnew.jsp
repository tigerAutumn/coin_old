<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path;
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<meta name="format-detection" content="telephone=no">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<%-- <link rel="stylesheet" href="${staticurl }/front/css/service/appnews.css" type="text/css"></link> --%>
<link rel="stylesheet" href="/static/front/css/service/appnews.css" type="text/css"></link>
<style type="text/css">
.container-fluid {
	width: inherit;
	max-width: 640px;
	padding: 10px;
}
</style>
</head>
<body>
	<div class="container-fluid">
		<div class="news-detail">
			<!-- 新闻详情 -->
			<p class="title">${farticle.ftitle}</p>
			<p class="date">
				<fmt:formatDate value="${farticle.fcreatedate }" pattern="yyyy-MM-dd" />
			</p>
			<p class="content">${farticle.fcontent}</p>
		</div>
	</div>
</body>
</html>