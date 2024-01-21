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
					<span id="resetprocess1" class="col-xs-3 col-xs-offset-3 ${emailvalidate!=null?'':'active' }">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">1</span>
						<span class="reset-process-text"><spring:message code="validate.reset.process1" /></span>
					</span>
					<span id="resetprocess2" class="col-xs-3 ${emailvalidate!=null?'active':'' }">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">2</span>
						<span class="reset-process-text"><spring:message code="validate.reset.process2" /></span>
					</span>
					<span id="resetprocess3" class="col-xs-3">
						<span class="reset-process-line"></span>
						<span class="reset-process-icon">3</span>
						<span class="reset-process-text"><spring:message code="validate.reset.process3" /></span>
					</span>
				</span>
			</div>
			<div class="col-xs-12 reset padding-top-30">
				<div class="col-xs-8 col-xs-offset-2 reset-body">
					<div class="col-xs-12">
						<span class="reset-title">
							<spring:message code="validate.reset.emailtitle" />
						</span>
					</div>
					<c:choose>
						<c:when test="${emailvalidate!=null }">
							<input type="hidden" id="fid" value="${fuser.fid}" />
							<div id="secondstep" class="col-xs-7 form-horizontal padding-top-30">
								<div class="form-group ">
									<label for="reset-loginname" class="col-xs-4 control-label"><spring:message code="validate.reset.account" /></label>
									<div class="col-xs-8">
										<span id="reset-loginname" class="form-control border-fff">${fuser.floginname }</span>
									</div>
								</div>
								<c:if test="${fuser.fgooglebind==true }">
									<div class="form-group">
										<label for="reset-googlecode" class="col-xs-4 control-label"><spring:message code="comm.google" /></label>
										<div class="col-xs-8">
											<input id="reset-googlecode" class="form-control" type="text">
										</div>
									</div>
								</c:if>
								<div class="form-group ">
									<label for="reset-newpass" class="col-xs-4 control-label"><spring:message code="validate.reset.newpas" /></label>
									<div class="col-xs-8">
										<input id="reset-newpass" class="form-control" type="password">
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-confirmpass" class="col-xs-4 control-label"><spring:message code="validate.reset.cifpas" /></label>
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
										<button id="btn-email-success" class="btn btn-danger btn-block"><spring:message code="validate.reset.next" /></button>
									</div>
								</div>
							</div>
							<div id="successstep" class="col-xs-12 padding-top-30 successstep text-center" style="display:none">
								<div>
									<i class="successstep-icon"></i>
									<spring:message code="validate.reset.success" />
								</div>
								<a href="/user/login.html" class="btn btn-danger btn-find"><spring:message code="validate.reset.login" /></a>
							</div>
						</c:when>
						<c:otherwise>
							<div class="col-xs-7 form-horizontal padding-top-30">
								<div class="form-group ">
									<label for="reset-email" class="col-xs-4 control-label"><spring:message code="comm.email" /></label>
									<div class="col-xs-8">
										<input id="reset-email" class="form-control" type="text">
									</div>
								</div>
								<div class="form-group ">
									<label for="reset-idcard" class="col-xs-4 control-label"><spring:message code="validate.reset.idcord" /></label>
									<div class="col-xs-8">
										<select class="form-control" id="reset-idcard">
											<option value="1"><spring:message code="validate.reset.cord1" /></option>
										</select>
									</div>
								</div>
								<div class="form-group ">
								<label for="reset-idcardno" class="col-xs-4 control-label"><spring:message code="validate.reset.idcordno" /></label>
								<div class="col-xs-8">
									<input id="reset-idcardno" class="form-control" type="text" placeholder="<spring:message code="validate.reset.idcordyips" />">
								</div>
							</div>
								<div class="form-group ">
									<label for="reset-imgcode" class="col-xs-4 control-label"><spring:message code="comm.imgcode" /></label>
									<div class="col-xs-8">
										<input id="reset-imgcode" class="form-control" type="text">
										<img class="btn btn-imgcode" src="/servlet/ValidateImageServlet.html?r=<%=new java.util.Date().getTime()%>"></img>
									</div>
								</div>
								<div class="form-group">
									<label for="reset-errortips" class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<span id="reset-errortips" class="text-danger"></span>
									</div>
								</div>
								<div class="form-group ">
									<label class="col-xs-4 control-label"></label>
									<div class="col-xs-8">
										<button id="btn-email-next" class="btn btn-danger btn-block"><spring:message code="comm.imgcode" /></button>
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
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/reset.js"></script>
</body>
</html>
