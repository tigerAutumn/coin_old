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
		<div class="container text-center validate">
			<div class="col-xs-12 clearfix padding-clear padding-top-30 text-center">
				<div class="validate-online">
					<c:choose>
						<c:when test="${validate==true }">
							<span class="validate-success">
								<span class="validate-text"><spring:message code="email.bind.succ"></spring:message></span>
							</span>
						</c:when>
						<c:otherwise>
							<span class="validate-success failure">
								<span class="validate-text failure"><spring:message code="email.bind.failed"></spring:message></span>
							</span>
						</c:otherwise>
					</c:choose>
					<div class="form-group">
						<a class="btn btn-danger btn-block" href="/index.html"><spring:message code="reback.index"></spring:message></a>
					</div>
					<div class="form-group">
						<a class="btn btn-danger btn-block" href="/trade/cny_coin.html"><spring:message code="reback.trade"></spring:message></a>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
</body>
</html>
