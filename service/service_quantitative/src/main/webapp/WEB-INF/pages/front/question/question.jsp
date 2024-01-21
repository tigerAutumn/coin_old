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
		<div class="container">
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
			<div class="col-xs-9 question-right rightarea">
				<ul class="nav nav-tabs rightarea-tabs question-nav">
					<li class="active">
						<a href="/online_help/index.html">
							<spring:message code="question.navbar.question" />
						</a>
					</li>
					<li class="">
						<a href="/online_help/help_list.html">
							<spring:message code="question.navbar.list" />
						</a>
					</li>
				</ul>
				<div class="col-xs-12 padding-clear padding-top-30 form-horizontal">
					<div class="form-group ">
						<label for="question-type" class="col-xs-2 control-label">
							<spring:message code="question.question.type" />
						</label>
						<div class="col-xs-4">
							<select id="question-type" class="form-control">
								<c:forEach items="${fquestiontypes }" var="v">
									<option value="${v.key}">${v.value}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<div class="form-group ">
						<label for="question-desc" class="col-xs-2 control-label">
							<spring:message code="question.question.questiondesc" />
						</label>
						<div class="col-xs-9">
							<textarea id="question-desc" class="form-control" rows="10">请输入问题描述（不得少于10字）!</textarea>
						</div>
					</div>
					<div class="form-group ">
						<label for="" class="col-xs-2 control-label"> </label>
						<div class="col-xs-10">
							<span id="errortips" class="text-danger"></span>
						</div>
					</div>
					<div class="form-group ">
						<label for="diyMoney" class="col-xs-2 control-label"></label>
						<div class="col-xs-4">
							<button id="submitQuestion" class="btn btn-danger btn-block">
								<spring:message code="question.question.submit" />
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl}/front/js/question/question.js"></script>
</body>
</html>