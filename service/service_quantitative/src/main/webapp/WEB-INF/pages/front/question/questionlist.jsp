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
<link rel="stylesheet" href="../static/front/css/question/question.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-30 ">
		<div class="container question-maxbg">
			<!-- 左侧 -->
			<div class="col-xs-2 padding-left-clear">
				<div class="text-center question-left">
					<div class="question-online">
						<p id="question-pic">
							<span>
								<spring:message code="question.question.feedback" />
							</span>
						</p>
					</div>
					<div class="question-menu"></div>
					<div class="question-img"></div>
					<div class="question-menu"></div>
				</div>
			</div>
			<!-- 右侧 -->
			<div class="col-xs-10 question-right rightarea ">
				<ul class="nav nav-tabs rightarea-tabs">
					<li class="">
						<a href="/online_help/index.html">
							<spring:message code="question.navbar.question" />
						</a>
					</li>
					<li class="active">
						<a href="/online_help/help_list.html">
							<spring:message code="question.navbar.list" />
						</a>
					</li>
				</ul>
				<div class="col-xs-12 padding-clear">
					<table class="table table-striped">
						<tr>
							<td class="col-xs-1">
								<spring:message code="question.questioncolumn.no" />
							</td>
							<td class="col-xs-2">
								<spring:message code="question.questioncolumn.time" />
							</td>
							<td class="col-xs-2">
								<spring:message code="question.questioncolumn.type" />
							</td>
							<td class="col-xs-2">
								<spring:message code="question.questioncolumn.desc" />
							</td>							
							<td class="col-xs-1">
								<spring:message code="question.questioncolumn.status" />
							</td>
							<td class="col-xs-1">
								<spring:message code="comm.operation" />
							</td>
						</tr>
						<c:if test="${fn:length(list.data)!=0 }">
							<c:forEach items="${list.data}" var="v">
								<c:if test="${v.fstatus!=3}">
								<tr>
									<td>${v.fid}</td>
									<td>
										<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
									</td>
									<td>${v.ftype_s}</td>
									<td>${v.fdesc}</td>															
									<td>${v.fstatus_s }</td>
									<td>
										<a class="delete" href="javascript:void(0)" data-question='{"questionid":${v.fid}}'>
											<spring:message code="question.message.del" />
										</a>
										<c:if test="${v.fstatus==2}">
											||
											<a class="look" href="#"  data-target="#questiondetail${v.fid}"  data-toggle="modal" >
												<spring:message text="查看" />
											</a>
										</c:if>
									</td>
								</tr>
								</c:if>
								<div class="modal modal-custom fade" id="questiondetail${v.fid}" tabindex="-1" role="dialog">
									<div class="modal-dialog" role="document">
										<div class="modal-mark"></div>
										<div class="modal-content">
											<div class="modal-header">
												<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
													<span aria-hidden="true">&times;</span>
												</button>
												<span class="modal-title">问题详情</span>
											</div>
											<div class="modal-body form-horizontal">
												<div class="panel panel-default">
											  		<div class="panel-heading">
											   			 <h3 class="panel-title">提问内容</h3>
											  		</div>
											 		 <div class="panel-body">
											  		 	${v.fdesc}
													  </div>
												</div>
												<div class="panel panel-default">
											  		<div class="panel-heading">
											   			 <h3 class="panel-title">回复内容</h3>
											  		</div>
											 		 <div class="panel-body">
											  			 ${v.fanswer}
													  </div>
												</div>												
											</div>
										</div>
									</div>
								</div>
							</c:forEach>
							<tr>
								<td class="text-right" colspan="12">
									<div class="text-right">
										<ul class="pagination">${list.pagin }
										</ul>
									</div>
								</td>
							</tr>
						</c:if>
						<c:if test="${fn:length(list.data)==0 }">
							<tr class="no-data-tips text-center">
								<td colspan="6">
									<span>
										<spring:message code="question.questioncolumn.nodata" />
									</span>
								</td>
							</tr>
						</c:if>
					</table>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl}/front/js/question/question.js"></script>
</body>
</html>