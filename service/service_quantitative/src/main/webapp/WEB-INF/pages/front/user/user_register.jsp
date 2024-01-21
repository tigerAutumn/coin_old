<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/user/login.css?v=2" type="text/css"></link>
<link rel="stylesheet" href="${staticurl }/front/css/user/custom.css?v=1" type="text/css"></link>
	<link rel="stylesheet" href="${staticurl }/front/css/user/reset.css?v=1" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full login-body-full">
		<div class="container ">
			<div class="col-xs-12 " style="padding-top:150px !important;">
				<div class="col-xs-5 login register col-xs-offset-7 padding-left-clear padding-right-clear">
					<div class="login-header">
						<div class="login-header-nav">
							<a href="/user/phonereg.html" class="btn header-nav-btn">手机注册</a>
							<span>|</span>
							<a href="/user/register.html" class="btn header-nav-btn active">邮箱注册</a>
						</div>
					</div>
					<div class="login-bg"></div>
					<div class="login-body">
						<div class="form-group">
							<input id="register-email" class="form-control login-ipt" type="text"
								   placeholder="<spring:message code="comm.email"/>">
						</div>

						<div class="form-group">
							<input id="register-imgcode" class="form-control login-ipt" type="text"
								   placeholder="<spring:message code="comm.imgcode"/>">
							<img class="btn-imgcode register-imgmsg"
								 src="/servlet/ValidateImageServlet.html?r=<%=new java.util.Date().getTime()%>"/>
						</div>
						<div class="form-group">
							<input id="register-email-code" class="form-control login-ipt" type="text"
								   placeholder="<spring:message code="comm.email.code"/>">
							<button id="register-sendemail" data-msgtype="203" data-tipsid="register-email-code"
									class="btn btn-sendemailcode register-msg">
								<spring:message code="comm.email.code.send"/>
							</button>
						</div>
						<div class="form-group ">
							<input id="register-password" class="form-control login-ipt" type="password"
								   placeholder="<spring:message code="register.loginpwd"/>">
						</div>
						<div class="form-group ">
							<input id="register-confirmpassword" class="form-control login-ipt" type="password"
								   placeholder="<spring:message code="register.reloginpwd"/>">
						</div>

						<div class="form-group">
							<input type="text" id="register-intro" class="form-control login-ipt" placeholder="邀请码" />
						</div>

						<div class="checkbox" id="agreeBox">
							<label>
								<input id="agree" type="checkbox">
								<spring:message code="register.agree"/>
								<a target="_blank" href="/about/about.html?id=4" class="text-danger"><spring:message code="register.rule"/></a>
							</label>
						</div>
						<div class="form-group ">
							<span id="register-errortips" class="text-danger"></span>
						</div>
						<div class="form-group ">
							<button id="register-submit" class="btn btn-danger btn-block btn-login"><spring:message code="register.btn"/></button>
						</div>
						<div class="form-group">
							<a href="/user/login.html" class="text-danger"><spring:message code="register.login.btn"/>>></a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<input id="regType" type="hidden" value="1">
	<input id="intro_user" type="hidden" value="">
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/register.js?v=20"></script>
</body>
</html>
