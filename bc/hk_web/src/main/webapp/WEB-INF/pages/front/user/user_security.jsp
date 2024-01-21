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
<link rel="stylesheet" href="${staticurl }/front/css/user/user.css" type="text/css"></link>
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
						<div class="user-top-icon">
							<h4>${fuser.frealname } ${loginName }，
								<spring:message code="user.real.secu.tip1"></spring:message>
								<spring:message code="user.real.secu.level${securityLevel}"></spring:message></h4>
							<h5><spring:message code="user.real.secu.tip2"></spring:message></h5>
						</div>
						<div class="col-xs-12 padding-clear padding-top-30">
							<div class="panel">
								<div class="panel-header">
									<span class="panel-title">
										<spring:message code="user.protect1"></spring:message>
										<span class="text-danger font-size-18"> ${bindcount } </span>
										<spring:message code="user.protect2"></spring:message>
										<span class="text-danger font-size-18">${6-bindcount } </span>
										<spring:message code="user.protect3"></spring:message>
									</span>
								</div>
								<div class="panel-body padding-left-clear padding-right-clear">
									<table class="table table-user">

										<tr>
											<td class="col-xs-3 user-tr">
												<i class="iconlist email"></i>
												<spring:message code="user.bind.phone"></spring:message>
											</td>
											<c:choose>
												<c:when test="${fuser.fistelephonebind }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.your.bind.phone"></spring:message> ${phoneString }</td>
													<td class="col-xs-1 user-tr"></td>
												</c:when>
												<c:when test="${fuser.ftelephone!=null && fuser.ftelephone!='' }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.not.bind"></spring:message></td>
													<td class="col-xs-7 user-tr">
														<spring:message code="user.your.notbind.phone"></spring:message> ${phoneString }
														<input id="bindphone-send-msg" type="hidden" value="${fuser.ftelephone }" />
													</td>
													<td class="col-xs-1 user-tr">
														<%--<a class="text-primary text-link" href="javascript:void(0)" id="bindphone-send"><spring:message code="user.resend.phone"></spring:message>>></a>--%>
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindphone"><spring:message code="user.bind"></spring:message>>></a>
													</td>
												</c:when>
												<c:otherwise>
													<td class="col-xs-1 user-tr"><spring:message code="user.not.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindphone"><spring:message code="user.bind"></spring:message>>></a>
													</td>
												</c:otherwise>
											</c:choose>
										</tr>

										<tr>
											<td class="col-xs-3 user-tr">
												<i class="iconlist email"></i>
												<spring:message code="user.bind.email"></spring:message>
											</td>
											<c:choose>
												<c:when test="${fuser.fismailbind }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.your.bind.email"></spring:message> ${emaString }</td>
													<td class="col-xs-1 user-tr"></td>
												</c:when>
												<c:when test="${fuser.femail!=null && fuser.femail!='' }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.not.bind"></spring:message></td>
													<td class="col-xs-7 user-tr">
														<spring:message code="user.your.notbind.email"></spring:message> ${emaString }
														<input id="bindemail-send-email" type="hidden" value="${fuser.femail }" />
													</td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="javascript:void(0)" id="bindemail-send"><spring:message code="user.resend.email"></spring:message>>></a>
													</td>
												</c:when>
												<c:otherwise>
													<td class="col-xs-1 user-tr"><spring:message code="user.not.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindemail"><spring:message code="user.bind"></spring:message>>></a>
													</td>
												</c:otherwise>
											</c:choose>
										</tr>
										<tr>
											<td class="col-xs-3 user-tr">
												<i class="iconlist google"></i>
												<spring:message code="user.bind.google"></spring:message>
											</td>
											<c:choose>
												<c:when test="${fuser.fgooglebind }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.google.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#unbindgoogle"><spring:message code="comm.search"></spring:message>>></a>
													</td>
												</c:when>
												<c:otherwise>
													<td class="col-xs-1 user-tr"><spring:message code="user.not.bind"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.google.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<a id="googleunbind" class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindgoogle"><spring:message code="user.bind"></spring:message>>></a>
													</td>
												</c:otherwise>
											</c:choose>
										</tr>
										<tr>
											<td class="col-xs-3 user-tr">
												<i class="iconlist loginpass"></i>
												<spring:message code="user.login.pwd"></spring:message>
											</td>
											<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.set"></spring:message></td>
											<td class="col-xs-7 user-tr"><spring:message code="user.login.tips"></spring:message></td>
											<td class="col-xs-1 user-tr">
												<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#unbindloginpass"><spring:message code="user.modify.login.pwd"></spring:message>>></a>
											</td>
										</tr>
										<tr id="traingtr">
											<td class="col-xs-3 user-tr">
												<i class="iconlist tradepass"></i>
												<spring:message code="comm.trade.pwd"></spring:message>
											</td>
											<c:choose>
												<c:when test="${bindTrade }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.set"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.trade.pwd.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindtradepass"><spring:message code="user.reset"></spring:message>>></a>
													</td>
												</c:when>
												<c:otherwise>
													<td class="col-xs-1 user-tr"><spring:message code="user.not.set"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.trade.pwd.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindtradepass"><spring:message code="user.set"></spring:message>>></a>
													</td>
												</c:otherwise>
											</c:choose>
										</tr>
										<tr>
											<td class="col-xs-3 user-tr">
												<i class="iconlist usercertificate"></i>
												<spring:message code="user.real.auth"></spring:message>
											</td>
											<c:choose>
												<c:when test="${identity.fstatus eq 1 }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.finish.auth"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.real.auth.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">

													</td>
												</c:when>
												<c:when test="${identity.fstatus eq 0 }">
													<td class="col-xs-1 user-tr text-danger"><spring:message code="user.wait.real.auth"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.realname.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<spring:message code="user.wait.real.auth" />
													</td>
												</c:when>
												<c:otherwise>
													<td class="col-xs-1 user-tr"><spring:message code="user.not.auth"></spring:message></td>
													<td class="col-xs-7 user-tr"><spring:message code="user.realname.tips"></spring:message></td>
													<td class="col-xs-1 user-tr">
														<a class="text-primary text-link" href="#" data-toggle="modal" data-target="#bindrealinfo"><spring:message code="user.realauth"></spring:message>>></a>
													</td>
												</c:otherwise>
											</c:choose>
										</tr>
									</table>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<c:if test="${!fuser.fismailbind }">
		<!-- 邮箱绑定 -->
		<div class="modal modal-custom fade" id="bindemail" tabindex="-1" role="dialog">
			<div class="modal-dialog" role="document">
				<div class="modal-mark"></div>
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
						<span class="modal-title"><spring:message code="user.bind.email"></spring:message></span>
					</div>
					<div class="modal-body form-horizontal">
						<div class="form-group ">
							<label for="bindemail-email" class="col-xs-3 control-label"><spring:message code="comm.email"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindemail-email" class="form-control" type="text">
							</div>
						</div>
						<div class="form-group">
							<label for="bindemail-errortips" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<span id="bindemail-errortips" class="text-danger"></span>
							</div>
						</div>
						<div class="form-group">
							<label for="bindemail-Btn" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<button id="bindemail-Btn" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"></spring:message></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</c:if>

	<c:if test="${!fuser.fistelephonebind }">
		<!-- 手机绑定 -->
		<div class="modal modal-custom fade" id="bindphone" tabindex="-1" role="dialog">
			<div class="modal-dialog" role="document">
				<div class="modal-mark"></div>
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
						<span class="modal-title"><spring:message code="user.bind.phone"></spring:message></span>
					</div>
					<div class="modal-body form-horizontal">
						<div class="form-group ">
							<label for="bindphone-phone" class="col-xs-3 control-label"><spring:message code="comm.phone"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindphone-phone" class="form-control" type="text">
							</div>
						</div>
						<div class="form-group ">
							<label for="bindphone-phone-code" class="col-xs-3 control-label">验证码</label>
							<div class="col-xs-6">
								<input id="bindphone-phone-code" class="form-control" type="text">
								<button id="bindphone-send-code" style="position: absolute;top:50%;right:18px;transform:translateY(-50%)">发送验证码</button>
							</div>
						</div>
						<div class="form-group">
							<label for="bindphone-errortips" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<span id="bindphone-errortips" class="text-danger"></span>
							</div>
						</div>
						<div class="form-group">
							<label for="bindphone-Btn" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<button id="bindphone-Btn" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"></spring:message></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</c:if>


	<c:choose>
		<c:when test="${!fuser.fgooglebind }">
			<!-- 谷歌绑定 -->
			<div class="modal modal-custom fade" id="bindgoogle" tabindex="-1" role="dialog">
				<div class="modal-dialog" role="document">
					<div class="modal-mark"></div>
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<span class="modal-title"><spring:message code="user.bind.google.device"></spring:message></span>
						</div>
						<div class="modal-body form-horizontal">
							<div class="form-group ">
								<div class="col-xs-12 clearfix">
									<div id="bindgoogle-code" class="form-control border-fff  form-qrcodebox">
										<div class="col-xs-12 clearfix form-qrcode-quotes form-qrcode-quotess"></div>
										<div class="form-qrcodebox-cld">
											<div class="form-qrcode-coded">
												<div class="form-qrcode-title text-center">
													<span><spring:message code="user.download.google.device"></spring:message></span>
												</div>
												<div class="form-qrcode">
													<img class="form-qrcode-img" src='${requestScope.constant.googleVerifyDownUrl }'/>
												</div>
											</div>
											<div class="form-qrcode-tips">
												<span>
													<spring:message code="user.uninstall.google.device1"></spring:message>
													<span class="text-danger"><spring:message code="user.uninstall.google.device2"></spring:message></span>
												</span>
											</div>
										</div>
										<div class="form-qrcodebox-cld">
											<div class="form-qrcode-codec">
												<div class="form-qrcode-title text-center">
													<span><spring:message code="user.bind.google.device"></spring:message></span>
												</div>
												<div class="form-qrcode" id="qrcode">
												</div>
											</div>
											<div class="form-qrcode-tips">
												<span>
													<spring:message code="user.uninstall.google.device3"></spring:message>
													<span class="text-danger"><spring:message code="user.uninstall.google.device4"></spring:message></span>
													<spring:message code="user.uninstall.google.device5"></spring:message>
												</span>
											</div>
										</div>
										<div class="col-xs-12 clearfix form-qrcode-quotes form-qrcode-quotese"></div>
									</div>
								</div>
							</div>
							<div class="form-group ">
								<label for="bindgoogle-key" class="col-xs-3 control-label"><spring:message code="user.google.key"></spring:message></label>
								<div class="col-xs-7">
									<span id="bindgoogle-key" class="form-control border-fff"></span>
									<input id="bindgoogle-key-hide" type="hidden">
								</div>
							</div>
							<div class="form-group ">
								<label for="bindgoogle-device" class="col-xs-3 control-label"><spring:message code="user.google.device.name"></spring:message></label>
								<div class="col-xs-7">
									<span id="bindgoogle-device" class="form-control border-fff">${device_name }</span>
								</div>
							</div>
							<div class="form-group ">
								<label for="bindgoogle-topcode" class="col-xs-3 control-label"><spring:message code="comm.google"></spring:message></label>
								<div class="col-xs-7">
									<input id="bindgoogle-topcode" class="form-control" type="text">
								</div>
							</div>
							<div class="form-group">
								<label for="bindgoogle-errortips" class="col-xs-3 control-label"></label>
								<div class="col-xs-7">
									<span id="bindgoogle-errortips" class="text-danger"></span>
								</div>
							</div>
							<div class="form-group">
								<label for="bindgoogle-Btn" class="col-xs-3 control-label"></label>
								<div class="col-xs-7">
									<button id="bindgoogle-Btn" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"></spring:message></button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<!-- 谷歌查看 -->
			<div class="modal modal-custom fade" id="unbindgoogle" tabindex="-1" role="dialog">
				<div class="modal-dialog" role="document">
					<div class="modal-mark"></div>
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<span class="modal-title"><spring:message code="user.see.google.device"></spring:message></span>
						</div>
						<div class="modal-body form-horizontal">
							<div class="form-group unbindgoogle-hide-box" style="display:none">
								<div class="col-xs-12 clearfix">
									<div id="unbindgoogle-code" class="form-control border-fff ">
										<div class="form-qrcode" style="margin-left: 175px;" id="unqrcode"></div>
									</div>
								</div>
							</div>
							<div class="form-group unbindgoogle-hide-box" style="display:none">
								<label for="unbindgoogle-key" class="col-xs-3 control-label" style = "width: 38%;"><spring:message code="user.google.key"></spring:message></label>
								<div class="col-xs-6">
									<span id="unbindgoogle-key" class="form-control border-fff"></span>
								</div>
							</div>
							<div class="form-group unbindgoogle-hide-box" style="display:none">
								<label for="unbindgoogle-device" class="col-xs-3 control-label"  style = "width: 38%;"><spring:message code="user.google.device.name"></spring:message></label>
								<div class="col-xs-6">
									<span id="unbindgoogle-device" class="form-control border-fff">${device_name }</span>
								</div>
							</div>
							<div class="form-group unbindgoogle-show-box">
								<label for="unbindgoogle-topcode" class="col-xs-3 control-label"><spring:message code="comm.google"></spring:message></label>
								<div class="col-xs-6">
									<input id="unbindgoogle-topcode" class="form-control" type="text">
								</div>
							</div>
							<div class="form-group unbindgoogle-show-box">
								<label for="unbindgoogle-errortips" class="col-xs-3 control-label"></label>
								<div class="col-xs-6">
									<span id="unbindgoogle-errortips" class="text-danger"></span>
								</div>
							</div>
							<div class="form-group unbindgoogle-show-box">
								<label for="unbindgoogle-Btn" class="col-xs-3 control-label"></label>
								<div class="col-xs-6">
									<button id="unbindgoogle-Btn" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"></spring:message></button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</c:otherwise>
	</c:choose>
	<!-- 登录密码修改 -->
	<div class="modal modal-custom fade" id="unbindloginpass" tabindex="-1" role="dialog">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title"><spring:message code="user.modify.loginpwd"></spring:message></span>
				</div>
				<div class="modal-body form-horizontal">
					<div class="form-group ">
						<label for="unbindloginpass-oldpass" class="col-xs-3 control-label"><spring:message code="user.old.loginpwd"></spring:message></label>
						<div class="col-xs-6">
							<input id="unbindloginpass-oldpass" class="form-control" type="password">
						</div>
					</div>
					<div class="form-group ">
						<label for="unbindloginpass-newpass" class="col-xs-3 control-label"><spring:message code="user.new.loginpwd"></spring:message></label>
						<div class="col-xs-6">
							<input id="unbindloginpass-newpass" class="form-control" type="password">
						</div>
					</div>
					<div class="form-group ">
						<label for="unbindloginpass-confirmpass" class="col-xs-3 control-label"><spring:message code="user.new.retradepwd"></spring:message></label>
						<div class="col-xs-6">
							<input id="unbindloginpass-confirmpass" class="form-control" type="password">
						</div>
					</div>
					<c:if test="${fuser.fgooglebind==true }">
						<div class="form-group">
							<label for="unbindloginpass-googlecode" class="col-xs-3 control-label"><spring:message code="comm.google"></spring:message></label>
							<div class="col-xs-6">
								<input id="unbindloginpass-googlecode" class="form-control" type="text">
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label for="unbindloginpass-errortips" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<span id="unbindloginpass-errortips" class="text-danger "></span>
						</div>
					</div>
					<div class="form-group">
						<label for="unbindloginpass-Btn" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<button id="unbindloginpass-Btn" class="btn btn-danger btn-block"><spring:message code="user.confirm.submit"></spring:message></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- 交易密码设置 -->
	<div class="modal modal-custom fade" id="bindtradepass" tabindex="-1" role="dialog">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title"><spring:message code="${bindTrade?'user.reset.tradepwd':'user.set.tradepwd' }"></spring:message></span>
				</div>
				<div class="modal-body form-horizontal">
					<c:if test="${bindTrade}">
						<div class="form-group text-center">
							<span class="modal-info-tips text-danger"><spring:message code="user.reset.tradepwd.tips"></spring:message>
							</span>
						</div>
						<div class="form-group ">
							<label for="bindtradepass-identityno" class="col-xs-3 control-label"><spring:message code="user.idcard.no"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindtradepass-identityno" class="form-control" type="text">
							</div>
						</div>
					</c:if>
					<div class="form-group ">
						<label for="bindtradepass-newpass" class="col-xs-3 control-label"><spring:message code="user.new.tradepwd"></spring:message></label>
						<div class="col-xs-6">
							<input id="bindtradepass-newpass" class="form-control" type="password">
						</div>
					</div>
					<div class="form-group ">
						<label for="bindtradepass-confirmpass" class="col-xs-3 control-label"><spring:message code="user.new.retradepwd"></spring:message></label>
						<div class="col-xs-6">
							<input id="bindtradepass-confirmpass" class="form-control" type="password">
						</div>
					</div>
					<c:if test="${fuser.fgooglebind==true }">
						<div class="form-group">
							<label for="bindtradepass-googlecode" class="col-xs-3 control-label"><spring:message code="comm.google"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindtradepass-googlecode" class="form-control" type="text">
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label for="bindtradepass-errortips" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<span id="bindtradepass-errortips" class="text-danger"></span>
						</div>
					</div>
					<div class="form-group margin-bottom-clear">
						<label for="bindtradepass-Btn" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<button id="bindtradepass-Btn" class="btn btn-danger btn-block"><spring:message code="user.confirm.submit"></spring:message></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<c:if test="${!fuser.fhasrealvalidate }">
		<!-- 实名认证 -->
		<div class="modal modal-custom fade" id="bindrealinfo" tabindex="-1" role="dialog">
			<div class="modal-dialog" role="document">
				<div class="modal-mark"></div>
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
						<span class="modal-title"><spring:message code="user.real.auth"></spring:message></span>
					</div>
					<div class="modal-body form-horizontal">
						<div class="form-group text-center">
							<span class="modal-info-tips ">
								<span class=" "></span>
								<span class="text-danger  Certificationtsimg"><spring:message code="user.age.tips"></spring:message></span>
							</span>
						</div>
						<div class="form-group ">
							<label for="bindrealinfo-realname" class="col-xs-3 control-label"><spring:message code="user.real.name"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindrealinfo-realname" class="form-control" placeholder="<spring:message code="user.input.real.name"></spring:message>" type="text">
								<span id="bindrealinfo-realname-errortips " class="text-danger certificationinfocolor"><spring:message code="user.realname.tips"></spring:message></span>
							</div>
						</div>
						<div class="form-group ">
							<label for="bindrealinfo-address" class="col-xs-3 control-label"><spring:message code="user.realname.location"></spring:message></label>
							<div class="col-xs-6">
								<select class="form-control" id="bindrealinfo-address">
									<option value="940">阿布哈兹(Abkhazia)</option>
									<option value="93">阿富汗(Afghanistan)</option>
									<option value="355">阿尔巴尼亚(Albania)</option>
									<option value="213">阿尔及利亚(Algeria)</option>
									<option value="1">美国(America)</option>
									<option value="376">安道尔(Andorra)</option>
									<option value="244">安哥拉(Angola)</option>
									<option value="1264">安圭拉岛(Anguilla)</option>
									<option value="1268">安提瓜(Antigua)</option>
									<option value="54">阿根廷(Argentina)</option>
									<option value="374">亚美尼亚(Armenia)</option>
									<option value="297">阿鲁巴(Aruba)</option>
									<option value="61">澳大利亚(Australia)</option>
									<option value="43">奥地利(Austria)</option>
									<option value="994">阿塞拜疆(Azerbaijan)</option>
									<option value="973">巴林(Bahrain)</option>
									<option value="880">孟加拉国(Bangladesh)</option>
									<option value="1246">巴巴多斯(Barbados)</option>
									<option value="375">白俄罗斯(Belarus)</option>
									<option value="32">比利时(Belgium)</option>
									<option value="501">伯利兹(Belize)</option>
									<option value="229">贝宁(Benin)</option>
									<option value="1441">百慕大(Bermuda)</option>
									<option value="975">不丹(Bhutan)</option>
									<option value="591">玻利维亚(Bolivia)</option>
									<option value="267">博茨瓦纳(Botswana)</option>
									<option value="55">巴西(Brazil)</option>
									<option value="673">文莱(Brunei Darussalam)</option>
									<option value="359">保加利亚(Bulgaria)</option>
									<option value="226">布基纳法索(Burkina Faso)</option>
									<option value="95">缅甸(Burma)</option>
									<option value="257">布隆迪(Burundi)</option>
									<option value="855">柬埔寨(Cambodia)</option>
									<option value="237">喀麦隆(Cameroon)</option>
									<option value="1">加拿大(Canada)</option>
									<option value="238">佛得角(Cape Verde)</option>
									<option value="1345">开曼群岛(Cayman Islands)</option>
									<option value="235">乍得(Chad)</option>
									<option value="56">智利(Chile)</option>
									<option value="86" selected="selected">中国大陆(China)</option>
									<option value="57">哥伦比亚(Colombia)</option>
									<option value="269">科摩罗和马约特(Comoros)</option>
									<option value="682">库克群岛(Cook Islands)</option>
									<option value="506">哥斯达黎加(Costa Rica)</option>
									<option value="385">克罗地亚(Croatia)</option>
									<option value="53">古巴(Cuba)</option>
									<option value="357">塞浦路斯(Cyprus)</option>
									<option value="420">捷克共和国(Czech Republic)</option>
									<option value="45">丹麦(Denmark)</option>
									<option value="253">吉布提(Djibouti)</option>
									<option value="1767">多米尼克(Dominica)</option>
									<option value="593">厄瓜多尔(Ecuador)</option>
									<option value="20">埃及(Egypt)</option>
									<option value="503">萨尔瓦多(El Salvador)</option>
									<option value="291">厄立特里亚(Eritrea)</option>
									<option value="372">爱沙尼亚(Estonia)</option>
									<option value="251">埃塞俄比亚(Ethiopia)</option>
									<option value="298">法罗群岛(Faroe Islands)</option>
									<option value="679">斐济(Fiji)</option>
									<option value="358">芬兰(Finland)</option>
									<option value="33">法国(France)</option>
									<option value="241">加蓬(Gabon)</option>
									<option value="995">格鲁吉亚(Georgia)</option>
									<option value="49">德国(Germany)</option>
									<option value="233">加纳(Ghana)</option>
									<option value="350">直布罗陀(Gibraltar)</option>
									<option value="30">希腊(Greece)</option>
									<option value="299">格陵兰(Greenland)</option>
									<option value="1473">格林纳达(Grenada)</option>
									<option value="502">危地马拉(Guatemala)</option>
									<option value="224">几内亚(Guinea)</option>
									<option value="245">几内亚比绍(Guinea-Bissau)</option>
									<option value="592">圭亚那(Guyana)</option>
									<option value="509">海地(Haiti)</option>
									<option value="504">洪都拉斯(Honduras)</option>
									<option value="852">香港(Hong Kong)</option>
									<option value="36">匈牙利(Hungary)</option>
									<option value="354">冰岛(Iceland)</option>
									<option value="91">印度(India)</option>
									<option value="62">印度尼西亚(Indonesia)</option>
									<option value="98">伊朗(Iran)</option>
									<option value="964">伊拉克(Iraq)</option>
									<option value="353">爱尔兰(Ireland)</option>
									<option value="972">以色列(Israel)</option>
									<option value="39">意大利(Italy)</option>
									<option value="1876">牙买加(Jamaica)</option>
									<option value="81">日本(Japan)</option>
									<option value="962">约旦(Jordan)</option>
									<option value="254">肯尼亚(Kenya)</option>
									<option value="850">北韓(Korea, North)</option>
									<option value="82">南韓(Korea, South)</option>
									<option value="965">科威特(Kuwait)</option>
									<option value="996">吉尔吉斯斯坦(Kyrgyzstan)</option>
									<option value="856">老挝(Laos)</option>
									<option value="371">拉脱维亚(Latvia)</option>
									<option value="961">黎巴嫩(Lebanon)</option>
									<option value="266">莱索托(Lesotho)</option>
									<option value="231">利比里亚(Liberia)</option>
									<option value="218">利比亚(Libya)</option>
									<option value="423">列支敦士登(Liechtenstein)</option>
									<option value="370">立陶宛(Lithuania)</option>
									<option value="352">卢森堡(Luxembourg)</option>
									<option value="853">澳门(Macao)</option>
									<option value="261">马达加斯加(Madagascar)</option>
									<option value="265">马拉维(Malawi)</option>
									<option value="60">马来西亚(Malaysia)</option>
									<option value="960">马尔代夫(Maldives)</option>
									<option value="223">马里(Mali)</option>
									<option value="356">马耳他(Malta)</option>
									<option value="596">马提尼克岛(Martinique)</option>
									<option value="222">毛里塔尼亚(Mauritania)</option>
									<option value="230">毛里求斯(Mauritius)</option>
									<option value="52">墨西哥(Mexico)</option>
									<option value="373">摩尔多瓦(Moldova)</option>
									<option value="377">摩纳哥(Monaco)</option>
									<option value="976">蒙古(Mongolia)</option>
									<option value="1664">蒙特塞拉特(Montserrat)</option>
									<option value="212">摩洛哥(Morocco)</option>
									<option value="258">莫桑比克(Mozambique)</option>
									<option value="264">纳米比亚(Namibia)</option>
									<option value="674">瑙鲁(Nauru)</option>
									<option value="977">尼泊尔(Nepal)</option>
									<option value="31">荷兰(Netherlands)</option>
									<option value="687">新喀里多尼亚(New Caledonia)</option>
									<option value="64">新西兰(New Zealand)</option>
									<option value="505">尼加拉瓜(Nicaragua)</option>
									<option value="227">尼日尔(Niger)</option>
									<option value="234">尼日利亚(Nigeria)</option>
									<option value="47">挪威(Norway)</option>
									<option value="968">阿曼(Oman)</option>
									<option value="92">巴基斯坦(Pakistan)</option>
									<option value="680">帕劳(Palau)</option>
									<option value="507">巴拿马(Panama)</option>
									<option value="595">巴拉圭(Paraguay)</option>
									<option value="51">秘鲁(Peru)</option>
									<option value="63">菲律宾(Philippines)</option>
									<option value="48">波兰(Poland)</option>
									<option value="351">葡萄牙(Portugal)</option>
									<option value="974">卡塔尔(Qatar)</option>
									<option value="262">留尼汪(Reunion)</option>
									<option value="40">罗马尼亚(Romania)</option>
									<option value="7">俄罗斯(Russia)、哈萨克斯坦</option>
									<option value="250">卢旺达(Rwanda)</option>
									<option value="1758">圣卢西亚(Saint Lucia)</option>
									<option value="685">萨摩亚(Samoa)</option>
									<option value="378">圣马力诺(San Marino)</option>
									<option value="966">沙特阿拉伯(Saudi Arabia)</option>
									<option value="221">塞内加尔(Senegal)</option>
									<option value="248">塞舌尔(Seychelles)</option>
									<option value="232">塞拉利昂(Sierra Leone)</option>
									<option value="65">新加坡(Singapore)</option>
									<option value="421">斯洛伐克(Slovakia)</option>
									<option value="386">斯洛文尼亚(Slovenia)</option>
									<option value="677">所罗门群岛(Solomon Islands)</option>
									<option value="252">索马里(Somalia)</option>
									<option value="27">南非(South Africa)</option>
									<option value="34">西班牙(Spain)</option>
									<option value="94">斯里兰卡(Sri Lanka)</option>
									<option value="249">苏丹(Sudan)</option>
									<option value="597">苏里南(Suriname)</option>
									<option value="268">斯威士兰(Swaziland)</option>
									<option value="46">瑞典(Sweden)</option>
									<option value="41">瑞士(Switzerland)</option>
									<option value="963">叙利亚(Syria)</option>
									<option value="886">台灣(Taiwan)</option>
									<option value="992">塔吉克斯坦(Tajikistan)</option>
									<option value="255">坦桑尼亚(Tanzania)</option>
									<option value="66">泰国(Thailand)</option>
									<option value="1242">巴哈马(The Bahamas)</option>
									<option value="220">冈比亚(The Gambia)</option>
									<option value="228">多哥(Togo)</option>
									<option value="676">汤加(Tonga)</option>
									<option value="216">突尼斯(Tunisia)</option>
									<option value="90">土耳其(Turkey)</option>
									<option value="993">土库曼斯坦(Turkmenistan)</option>
									<option value="256">乌干达(Uganda)</option>
									<option value="380">乌克兰(Ukraine)</option>
									<option value="44">英国(United Kingdom)</option>
									<option value="598">乌拉圭(Uruguay)</option>
									<option value="998">乌兹别克斯坦(Uzbekistan)</option>
									<option value="678">瓦努阿图(Vanuatu)</option>
									<option value="58">委内瑞拉(Venezuela)</option>
									<option value="84">越南(Vietnam)</option>
									<option value="967">也门(Yemen)</option>
									<option value="260">赞比亚(Zambia)</option>
									<option value="263">津巴布韦(Zimbabwe)</option>
								</select>
							</div>
						</div>
						<div class="form-group ">
							<label for="bindrealinfo-identitytype" class="col-xs-3 control-label"><spring:message code="user.lictence.type"></spring:message></label>
							<div class="col-xs-6">
								<select class="form-control" id="bindrealinfo-identitytype">
									<option value="0"><spring:message code="user.lictence.idcard"/></option>
									<option value="1"><spring:message code="user.lictence.passport"/></option>
									<option value="2"><spring:message code="user.lictence.pass"/></option>
								</select>
							</div>
						</div>
						<div class="form-group ">
							<label for="bindrealinfo-identityno" class="col-xs-3 control-label"><spring:message code="user.lictence.no"></spring:message></label>
							<div class="col-xs-6">
								<input id="bindrealinfo-identityno" class="form-control" type="text">
							</div>
						</div>
						<div class="form-group ">
							<label for="bindrealinfo-ckinfo" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<div class="checkbox disabled">
									<label id="bindrealinfo-ckinfolb">
										<input type="checkbox" value="" id="bindrealinfo-ckinfo">
										<spring:message code="user.lictence.statement"></spring:message>
									</label>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label for="bindrealinfo-errortips" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<span id="bindrealinfo-errortips" class="text-danger"></span>
							</div>
						</div>
						<div class="form-group">
							<label for="bindemail-Btn" class="col-xs-3 control-label"></label>
							<div class="col-xs-6">
								<button id="bindrealinfo-Btn" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"></spring:message></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</c:if>
	<input type="hidden" id="changePhoneCode" value="${fuser.ftelephone}" />
	<input type="hidden" id="isEmptyPhone" value="0" />
	<input type="hidden" id="isEmptygoogle" value="${fuser.fgooglebind==true?0:1 }" />
	<input id="messageType" type="hidden" value="0" />
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.qrcode.min.js?=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/user.security.js?=20"></script>
</body>
</html>
