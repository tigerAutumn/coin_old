<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
%>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<%@include file="../comm/link.inc.jsp"%>
	<link rel="stylesheet" href="${staticurl }/front/css/service/service.css?v=v" type="text/css"></link>
</head>
<body>
<%@include file="../comm/header.jsp"%>
<div class="container-full padding-top-30">
	<div class="container service-max">
		<div class="col-xs-12 service-leftbg ">
			<div class="col-xs-12 service-img">
				<div class="service-newsimg"></div>
			</div>
			<div class="col-xs-12 service-newsnav">
				<ul class="nav nav-tabs rightarea-tabs">
					<li class="${id==2?'active':'' }">
						<a class="${id==2?'active':'' }" href="/notice/index.html?id=2"><spring:message code="index.official.announcement"/></a>
					</li>
					<li class="${id==1?'active':'' }">
						<a class="${id==1?'active':'' }" href="/notice/index.html?id=1"><spring:message code="index.hot.topic"/></a>
					</li>
					<li class="${id==3?'active':'' }">
						<a class="${id==3?'active':'' }" href="/notice/index.html?id=3"><spring:message code="index.media.coverage"/></a>
					</li>
				</ul>
			</div>
			<!-- 新闻列表 -->
			<div class="col-xs-12 service-newscontent">
				<c:forEach items="${farticles}" var="news">
					<div class="snc-max">
						<div class="col-xs-2 snc-left">
							<a href="/notice/detail.html?id=${news.fid}"> <c:if test="${news.findeximg==null }">
								<img src="${staticurl }/images/service/defaultImg.jpg" class="snc-newsimg" /></a>
							</c:if>
							<c:if test="${news.findeximg!=null }">
								<img src="${news.findeximg }" class="snc-newsimg" />
								</a>
							</c:if>
						</div>
						<div class="col-xs-10 snc-right">
							<a href="/notice/detail.html?id=${news.fid}"><h3 class="snc-newscontent">${news.ftitle}</h3></a>
							<p>${news.fcontent_short}</p>
							<div class="col-xs-12 snc-newsinfo">
								<div class="col-xs-2 sncnc-lookinfo ">
									<p>qukuai</p>
								</div>
								<div class="col-xs-4 ">
									<fmt:formatDate value="${news.fcreatedate}" pattern="yyyy-MM-dd HH:mm:ss" />
								</div>
							</div>
						</div>
					</div>
				</c:forEach>
				<div class="text-right">
					<ul class="pagination">${pagin }</ul>
				</div>
			</div>
		</div>
	</div>
</div>
<%@include file="../comm/footer.jsp"%>
<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.qrcode.min.js?v=20"></script>
</body>
</html>
