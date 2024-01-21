<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/error/error.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full errorpage">
		<div class="container text-center">
			<div class="error-bg"></div>
			<div>
				<a href="/index.html" class="btn btn-danger btn-backhome">返回首页</a>
			</div>

		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
</body>
</html>
