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
<link rel="stylesheet" href="${staticurl }/front/css/activity/activity.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-30 activity">
		<div class="container">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear ">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea trade">
					<div class="col-xs-12 rightarea-con content ">
						<div class="col-xs-12 rightarea-con exchange">
							<div class="col-xs-12 exchange-infos">
								<div class="col-xs-3"></div>
								<div class="col-xs-6">
									<div class="form-group  info title">
										<span class="text-center"><spring:message code="activity.index.title" />:</span>
										<input type="text" placeholder="<spring:message code="activity.index.codetips" />" id="exchangeCode" />
									</div>
									<div class="form-group  info text-center">
										<Button class="btn btn-danger " type="button" id="exchange"><spring:message code="comm.confirm" /></Button>
									</div>
								</div>
								<div class="col-xs-3"></div>
							</div>
						</div>
					</div>
					<div class="col-xs-12">
						<div class="panel czcodeinfos">
							<div class="panel-heading">
								<span class="text-danger"><spring:message code="activity.index.record1" /></span>
								<span class="pull-right rewordtitle" data-type="0" data-value="0"><spring:message code="comm.shrink" /></span>
							</div>
							<div class="panel-body" id="recordbody0">
								<table class="table ">
									<tr>
										<td>
											<spring:message code="activity.index.record3" />
										</td>
										<td>
											<spring:message code="activity.index.record4" />
										</td>
										<td>
											<spring:message code="activity.index.record5" />
										</td>
										<td>
											<spring:message code="activity.index.record6" />
										</td>
										<td>
											<spring:message code="comm.status" />
										</td>
										<td>
											<spring:message code="activity.index.record9" />
										</td>
									</tr>
									<c:if test="${empty frewardcodes}">
										<tr>
											<td colspan="7" class="no-data-tips">
												<span>
													<spring:message code="activity.record.nodata" />
												</span>
											</td>
										</tr>
									</c:if>
									<c:forEach items="${frewardcodes }" var="v" varStatus="vs">
										<tr>
											<td>${vs.index+1 }</td>
											<td>${v.ftype_s}</td>
											<td>
												<fmt:formatNumber value="${v.famount}" pattern="#.##" maxFractionDigits="4" maxIntegerDigits="10" />
											</td>
											<td>${v.fcode }</td>
											<td>${v.fstate?'<spring:message code="activity.index.record10" />':'<spring:message code="activity.index.record11" />'}</td>
											<td>
												<fmt:formatDate value="${v.fupdatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
											</td>
										</tr>
									</c:forEach>
								</table>
								<div class="text-right">
									<ul class="pagination">${pagin }
									</ul>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/activity/activity.js"></script>
</body>
</html>
