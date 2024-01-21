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
	<div class="container-full padding-top-30">
		<div class="container reset">
			<div class="col-xs-12 ">
				<span class="reset-process">
					<span id="resetprocess1" class="col-xs-3 col-xs-offset-3 ${isSecondStep?'':'active' }">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">1</span>
						<span class="reset-process-text">填写账户名</span>
					</span>
					<span id="resetprocess2" class="col-xs-3 ${isSecondStep?'active':'' }">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">2</span>
						<span class="reset-process-text">设置登录密码</span>
					</span>
					<span id="resetprocess3" class="col-xs-3">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">3</span>
						<span class="reset-process-text">成功</span>
					</span>
				</span>
			</div>
			<div class="col-xs-12 reset padding-top-30">
				<div class="col-xs-8 col-xs-offset-2 reset-body">
					<div class="col-xs-12">
						<span class="reset-title">
							您正通过
							<span>手机</span>
							找回登录密码
						</span>
					</div>
					<c:choose>
						<c:when test="${isSecondStep }">
							<div id="secondstep" class="col-xs-7 form-horizontal padding-top-30">
								<div class="form-group ">
									<label for="reset-loginname" class="col-xs-4 control-label">登录帐号</label>
									<div class="col-xs-8">
										<span id="reset-loginname" class="form-control border-fff">${fuser.floginname }</span>
									</div>
								</div>
								<c:if test="${fuser.fgooglebind==true }">
									<div class="form-group">
										<label for="reset-googlecode" class="col-xs-4 control-label">谷歌验证码</label>
										<div class="col-xs-8">
											<input id="reset-googlecode" class="form-control" type="text">
										</div>
									</div>
								</c:if>
								<div class="form-group ">
									<label for="reset-newpass" class="col-xs-4 control-label">新密码</label>
									<div class="col-xs-8">
										<input id="reset-newpass" class="form-control" type="password">
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-confirmpass" class="col-xs-4 control-label">确认密码</label>
									<div class="col-xs-8">
										<input id="reset-confirmpass" class="form-control" type="password">
									</div>
								</div>
								<div class="form-group">
									<label for="reset-success-errortips" class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<span id="reset-success-errortips" class="text-danger"></span>
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-imgcode" class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<button id="btn-success" class="btn btn-danger btn-block">下一步</button>
									</div>
								</div>
							</div>
							<div id="successstep" class="col-xs-12 padding-top-30 successstep text-center" style="display:none">
								<div>
									<i class="successstep-icon"></i>
									恭喜，重置密码成功
								</div>
								<a href="/user/login.html" class="btn btn-danger btn-find">立即登录</a>
							</div>
						</c:when>
						<c:otherwise>
							<div class="col-xs-7 form-horizontal padding-top-30">
								<div class="form-group ">
									<label for="reset-areaCode" class="col-xs-4 control-label">所在地</label>
									<div class="col-xs-8">
										<select class="form-control" id="reset-areaCode">
											<option value="86">中国大陆(China)</option>
										</select>
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-phone" class="col-xs-4 control-label">手机号码</label>
									<div class="col-xs-8">
										<span id="reset-phone-areacode" class="btn btn-areacode">+86</span>
										<input id="reset-phone" class="form-control padding-left-92" type="text">
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-imgcode" class="col-xs-4 control-label">验证码</label>
									<div class="col-xs-8">
										<input id="reset-imgcode" class="form-control" type="text">
										<img class="btn btn-imgcode" src="/servlet/ValidateImageServlet.html?r=<%=new java.util.Date().getTime()%>"></img>
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-msgcode" class="col-xs-4 control-label">短信验证码</label>
									<div class="col-xs-8">
										<input id="reset-msgcode" class="form-control" type="text">
										<button id="reset-sendmessage" data-msgtype="109" data-tipsid="reset-errortips" class="btn btn-sendmsg">发送验证码</button>
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-idcard" class="col-xs-4 control-label">证件类型</label>
									<div class="col-xs-8">
										<select class="form-control" id="reset-idcard">
											<option value="1">身份证</option>
										</select>
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-idcardno" class="col-xs-4 control-label">证件号码</label>
									<div class="col-xs-8">
										<input id="reset-idcardno" class="form-control" type="text" placeholder="如果账户未实名认证此项可不填写">
									</div>
								</div>
								<div class="form-group">
									<label for="reset-errortips" class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<span id="reset-errortips" class="text-danger"></span>
									</div>
								</div>
								<div class="form-group ">
									<label for="btn-next" class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<button id="btn-next" class="btn btn-danger btn-block">下一步</button>
									</div>
								</div>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=25"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/reset.js?v=25"></script>
</body>
</html>
