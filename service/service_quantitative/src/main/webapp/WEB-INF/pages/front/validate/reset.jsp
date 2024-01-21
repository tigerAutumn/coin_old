<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/user/reset.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full ">
		<div class="container reset">
			<div class="col-xs-12 ">
				<p class="choose-title">请选择密码找回方式</p>
			</div>
			<div class="col-xs-12 reset padding-top-30">
				<div class="col-xs-5 col-xs-offset-1 text-center">
					<span class="choose-body">
						<span class="choose-icon choose-icon-email"></span>
						<h3>通过邮箱找回</h3>
						<h4>需要登录邮箱找回密码</h4>
						<a href="validate/reset_email.html" class="btn btn-danger btn-find">点击找回</a>
					</span>
				</div>
				<div class="col-xs-5  text-center">
					<span class="choose-body">
						<span class="choose-icon choose-icon-phone"></span>
						<h3 class="">通过手机找回</h3>
						<h4>需要手机验证找回密码</h4>
						<a href="validate/reset_phone.html" class="btn btn-danger btn-find">点击找回</a>
					</span>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
</body>
</html>
