<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path;
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet"
	href="${staticurl }/front/css/service/service.css?v=20" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-30">

		<div class="container service-max">

			<div class="col-xs-12 article-leftbg ">
				<div class="cols-xs-12 text-left" >					
					<p><a  href="/"><spring:message code="page.index"></spring:message></a>/<a href="/notice/index.html"><spring:message code="page.article"></spring:message></a></p>
				</div>	
				<div class="cols-xs-12" >					
					<h2 class="text-center ">${farticle.ftitle}</h2>
				</div>	
				<div class="cols-xs-12 text-center article-info" >	
					<span ><strong><spring:message code="index.public.time"></spring:message>:</strong>	<fmt:formatDate value="${farticle.fcreatedate}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
				</div>
				<div class="cols-xs-12  article-content">					
					${farticle.fcontent}
				</div>				
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.qrcode.min.js?v=20"></script>
</body>
</html>
