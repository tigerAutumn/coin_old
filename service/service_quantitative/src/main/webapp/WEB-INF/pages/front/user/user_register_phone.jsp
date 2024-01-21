<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/user/login.css?=v2" type="text/css"></link>
	<link rel="stylesheet" href="${staticurl }/front/css/user/custom.css?v=2" type="text/css"></link>
	<link rel="stylesheet" href="${staticurl }/front/css/user/reset.css?v=2" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>

	<div class="container-fluid login-body-full">
		<div class="container">
			<div class="row">
				<div class="col-xs-12" style="padding-top: 150px !important;">
					<div class="col-xs-5 login register col-xs-offset-7 padding-left-clear padding-right-clear">
						<div class="login-header">
							<div class="login-header-nav">
								<a href="/user/phonereg.html" class="btn header-nav-btn active">手机注册</a>
								<span>|</span>
								<a href="/user/register.html" class="btn header-nav-btn">邮箱注册</a>
							</div>
						</div>
						<div class="login-bg"></div>
						<div class="login-body">
							<div class="form-group">
								<div id="form-site" class="form-site clearfix" select-data="${defaultAreaCode}">
									<span>${defaultAreaName}${defaultAreaCode}</span>
									<i class="iconfont icon-xia"></i>
								</div>
								<div class="form-site-list">
									<ul>
										<c:forEach items="${areaCodes}" var="areaCode" varStatus="vs">
											<li class="form-site-item" code="${areaCode.code}">+${areaCode.code} ${areaCode.ZN}|${areaCode.EN}</li>
										</c:forEach>
									</ul>
								</div>
							</div>
							<div class="form-group">
								<input type="text" id="register-phone" class="form-control login-ipt" placeholder="手机号" />
							</div>

							<div class="form-group">
								<input type="text" id="register-imgcode" class="form-control login-ipt" placeholder="驗證碼" />
								<img src="/servlet/ValidateImageServlet.html?r=<%=new java.util.Date().getTime()%>" class="btn-imgcode register-imgmsg" />
							</div>
							<div class="form-group">
								<input type="text" id="register-phone-areacode" class="form-control login-ipt" placeholder="手机驗證碼" />
								<button id="register-sendemail" data-msgtype="203" data-tipsid="register-phone-areacode" class="btn btn-sendphonecode register-msg">
									<font style="vertical-align: inherit;">
										<font style="vertical-align: inherit;">发送验证码</font>
									</font>
								</button>
							</div>
							<div class="form-group">
								<input type="password" id="register-password" class="form-control login-ipt" placeholder="登錄密碼" />
							</div>
							<div class="form-group">
								<input type="password" id="register-confirmpassword" class="form-control login-ipt" placeholder="確認登錄密碼" />
							</div>
							<div class="form-group">
								<input type="text" id="register-intro" class="form-control login-ipt" placeholder="邀请码" />
							</div>
							<div class="checkbox" id="agreeBox">
								<label>
									<input type="checkbox" id="agree"  />
									<font style="vertical-align: inherit;">
										<font style="vertical-align: inherit;">
											我已阅读并同意
										</font>
									</font>
									<a href="/about/about.html?id=4" target="_blank" class="text-danger">
										<font style="vertical-align: inherit;">
											<font style="vertical-align: inherit;">
												用户协议
											</font>
										</font>
									</a>
								</label>
							</div>
							<div class="form-group">
								<span id="register-errortips" class="text-danger"></span>
							</div>
							<div class="form-group">
								<button id="register-submit" class="btn btn-danger btn-block btn-login">
									<font style="vertical-align: inherit;">
										<font style="vertical-align: inherit;">
											注册
										</font>
									</font>
								</button>
							</div>
							<div class="form-group">
								<a href="" class="text-danger" href="/user/login.html">
									<font style="vertical-align: inherit;">
										<font style="vertical-align: inherit;">
											直接登录>>
										</font>
									</font>
								</a>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%@include file="../comm/footer.jsp"%>
	<input id="regType" type="hidden" value="0">
	<input id="intro_user" type="hidden" value="">
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/register.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/custom.js?v=20"></script>
</body>
</html>
