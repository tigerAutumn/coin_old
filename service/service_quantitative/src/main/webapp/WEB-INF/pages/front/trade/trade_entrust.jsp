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
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-12 padding-right-clear padding-left-clear rightarea user">
				<div class="col-xs-12 rightarea-con">
					<div class="col-xs-12 padding-clear">
						<table class="table table-striped text-left">
							<tr class="bg-gary">
								<th width="160" class="text-center">
									<spring:message code="trade.entrust.time" />
								</th>
								<th width="50">
									<spring:message code="trade.entrust.type" />
								</th>
								<th width="80">
									<spring:message code="trade.entrust.number" />
								</th>
								<th width="80">
									<spring:message code="comm.money" />
								</th>
								<th width="80">
									<spring:message code="trade.entrust.fee" />
								</th>
								<th width="70">
									<spring:message code="trade.entrust.price" />
								</th>
								<th width="105">
									<spring:message code="trade.entrust.succnumber" />
								</th>
								<th width="105">
									<spring:message code="trade.entrust.succprice" />
								</th>
								<th width="90">
									<spring:message code="trade.entrust.avgprice" />
								</th>
								<th width="130">
									<spring:message code="comm.status" />
									/
									<spring:message code="comm.operation" />
								</th>
							</tr>
							<c:if test="${fn:length(fentrusts)==0 }">
								<tr>
									<td colspan="10" class="no-data-tips">
										<span>
											<spring:message code="trade.noentrus" />
										</span>
									</td>
								</tr>
							</c:if>
							<c:forEach items="${fentrusts }" var="v" varStatus="vs">
								<tr>
									<td>
										<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
									</td>
									<td class="${v.ftype==0?'green':'red' }">
										<spring:message code="trade.enum.entrusttype${v.ftype}" />
									</td>
									<td>
										${tradeType.sellSymbol}
										<fmt:formatNumber value="${v.fcount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="${coinDigit}" />
									</td>
									<td>
										${tradeType.buySymbol}
										<fmt:formatNumber value="${v.famount}" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="${cnyDigit}" />
									</td>
									<c:choose>
										<c:when test="${v.ftype==0 }">
											<td>
												${tradeType.sellSymbol}
												<fmt:formatNumber value="${v.ffees}" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
										</c:when>
										<c:otherwise>
											<td>
												${tradeType.buySymbol}
												<fmt:formatNumber value="${v.ffees}" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
										</c:otherwise>
									</c:choose>
									<td>
										${tradeType.buySymbol}
										<fmt:formatNumber value="${v.fprize }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="${cnyDigit}" />
									</td>
									<td>
										${tradeType.sellSymbol}
										<fmt:formatNumber value="${v.fcount-v.fleftcount }" pattern="0.0000" maxIntegerDigits="10"
											maxFractionDigits="4" />
									</td>
									<td>
										${tradeType.buySymbol}
										<fmt:formatNumber value="${v.fsuccessamount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="${cnyDigit}" />
									</td>
									<td>
										${tradeType.buySymbol}
										<fmt:formatNumber value="${v.flast }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="${cnyDigit}" />
									</td>
									<td class="lightgreen5">
										<spring:message code="trade.enum.entruststate${v.fstatus }" />
										<c:if test="${v.fstatus==1 || v.fstatus==2}">
											&nbsp;|&nbsp;<a class="tradecancel opa-link" href="javascript:void(0);" data-value="${v.fid }">
												<spring:message code="comm.cancel" />
											</a>
										</c:if>
										<c:if test="${v.fstatus==3}">
											&nbsp;|<a class="tradelook opa-link" href="javascript:void(0);" data-value="${v.fid}">
												<spring:message code="trade.look" />
											</a>
										</c:if>
									</td>
								</tr>
							</c:forEach>
						</table>
						<c:if test="${!empty(pagin) }">
							<div class="text-right">
								<ul class="pagination">${pagin }
								</ul>
							</div>
						</c:if>
					</div>
					<input type="hidden" value="${currentPage }" id="currentPage">
				</div>
			</div>
		</div>
	</div>
	<div class="modal modal-custom fade" id="entrustsdetail" tabindex="-1" role="dialog"
		aria-labelledby="exampleModalLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title" id="exampleModalLabel"></span>
				</div>
				<div class="modal-body"></div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">
						<spring:message code="trade.close" />
					</button>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/trade/trade.entrust.js?v=20"></script>
</body>
</html>
