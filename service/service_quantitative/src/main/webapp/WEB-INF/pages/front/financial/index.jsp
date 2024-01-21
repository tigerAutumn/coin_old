<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path;
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/finance/index.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea user">
					<div class="col-xs-12 rightarea-con ">
						<div class="col-xs-12 padding-clear padding-top-30">
							<table class="table table-striped text-center">
								<tr class="bg-gary">
									<td class="col-xs-2 border-top-clear"><spring:message code="financial.coinname"/></td>
									<td class="col-xs-2 border-top-clear"><spring:message code="financial.free"/></td>
									<td class="col-xs-2 border-top-clear"><spring:message code="financial.frozen"/></td>
									<td class="col-xs-2 border-top-clear"><spring:message code="financial.total"/></td>
								</tr>
								<c:forEach items="${userWalletList }" var="v" varStatus="vs">
									<tr>
										<td class="col-xs-2 border-top-clear">${v.shortName }</td>
										<td class="col-xs-2 border-top-clear">
											<fmt:formatNumber value="${v.total }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
										</td >
										<td class="col-xs-2 border-top-clear">
											<fmt:formatNumber value="${v.frozen }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
										</td>
										<td class="col-xs-2 border-top-clear">
											<fmt:formatNumber value="${v.total + v.frozen }" pattern="0.0000" maxFractionDigits="4"
												maxIntegerDigits="10" />
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
</body>
</html>