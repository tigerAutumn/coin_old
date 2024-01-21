<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/user/login.css?v=20" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full login-body-full">
		<div class="container ">
			<div class="col-xs-12 " style="padding-top:180px !important;">
				<div class="col-xs-4 login col-xs-offset-7 padding-left-clear padding-right-clear">
					<div class="login-header"></div>
					<div class="login-bg"></div>
					<div class="login-body">
						<div class="form-group ">
							<input id="login-account"  autocomplete="on" class="form-control login-ipt" type="text" placeholder='<spring:message code="market.input.username"/>' />
						</div>
						<div class="form-group ">
							<input id="login-password" class="form-control login-ipt" type="password" placeholder='<spring:message code="market.input.password"/>' />
						</div>
						<div class="form-group ">
							<span id="login-errortips" class="text-danger"></span>
						</div>
						<div class="form-group ">
							<button id="login-submit" class="btn btn-danger btn-block btn-login"><spring:message code="user.login.btn"/></button>
						</div>
						<div class="form-group">
							<a href="/validate/reset_passwd.html"><spring:message code="index.forget.password"/></a>
							<a href="/user/register.html" class="pull-right"><spring:message code="index.register"/></a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<input id="forwardUrl" type="hidden" value="${forwardUrl }">
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/user/login.js?v=20"></script>
</body>
</html>
