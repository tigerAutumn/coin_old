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
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-30">
		<div class="container">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea user">
					<div class="col-xs-12 rightarea-con">
						<ul class="nav nav-tabs rightarea-tabs">
							<li class="active">
								<a href="javascript:void(0)"><spring:message code="user.loginlog.history"></spring:message></a>
							</li>
							<li>
								<a href="/user/user_settinglog.html"><spring:message code="user.security.history"></spring:message></a>
							</li>
						</ul>
						<div class="col-xs-12 padding-clear padding-top-30">
							<table class="table table-striped">
								<tr class="bg-danger">
									<td class="col-xs-6"><spring:message code="user.login.time"></spring:message></td>
									<td class="col-xs-6 text-right"><spring:message code="user.login.ip"></spring:message></td>
								</tr>
								<c:forEach items="${flogs.data }" var="v" varStatus="vs">
									<tr>
										<td>
											<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
										</td>
										<td class="text-right">${v.fip }</td>
									</tr>
								</c:forEach>
								<c:if test="${fn:length(flogs.data)==0 }">
									<tr>
										<td colspan="2" class="no-data-tips">
											<span> <spring:message code="user.login.none"></spring:message> </span>
										</td>
									</tr>
								</c:if>
							</table>
							<c:if test="${!empty(pagin) }">
								<div class="text-right">
									<ul class="pagination">${pagin }
									</ul>
								</div>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
</body>
</html>