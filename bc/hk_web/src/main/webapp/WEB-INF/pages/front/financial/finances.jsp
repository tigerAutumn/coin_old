<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea">
					<div class="col-xs-12 rightarea-con">
						<div class="col-xs-7 padding-clear form-horizontal">
							<div class="form-group ">
								<label for="assettotal" class="col-xs-3 control-label"><spring:message code="financial.balance"/></label>
								<div class="col-xs-6">
									<span id="assettotal" class="form-control border-fff">${userWallet.total }</span>
								</div>
							</div>
							<div class="form-group ">
								<label class="col-xs-3 control-label"><spring:message code="financial.asset.type"/></label>
								<div class="col-xs-6">
									<select id="symbol" class="form-control">
										<c:forEach items="${financesCoinMap }" var="v">
											<option value="${v.key }">${v.value }</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<div class="form-group ">
								<label for="finType" class="col-xs-3 control-label"><spring:message code="financial.profit.type"/></label>
								<div class="col-xs-6">
									<select id="finType" class="form-control">
										<c:forEach items="${typeList }" var="v">
											<option value="${v.fid }" id="${v.frate }">${v.fname }</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<div class="form-group ">
								<label for="finCount" class="col-xs-3 control-label"><spring:message code="comm.number"/></label>
								<div class="col-xs-6">
									<input id="finCount" class="form-control" type="text" placeholder="<spring:message code="financial.number.min"/>">
								</div>
							</div>
							<div class="form-group ">
								<label for="tradePwd" class="col-xs-3 control-label"><spring:message code="comm.trade.pwd"/></label>
								<div class="col-xs-6">
									<input id="tradePwd" class="form-control" type="password">
								</div>
							</div>
							<c:if test="${userInfo.fistelephonebind==true }">
								<div class="form-group">
									<label for="phoneCode" class="col-xs-3 control-label"><spring:message code="comm.sms"/></label>
									<div class="col-xs-6">
										<input id="phoneCode" class="form-control" type="text">
										<button id="sendmessage" data-msgtype="114" data-tipsid="finError" class="btn btn-sendmsg"><spring:message code="comm.sms.send"/></button>
									</div>
								</div>
							</c:if>
							<c:if test="${userInfo.fgooglebind==true }">
								<div class="form-group">
									<label for="googleCode" class="col-xs-3 control-label"><spring:message code="comm.google"/></label>
									<div class="col-xs-6">
										<input id="googleCode" class="form-control" type="text">
									</div>
								</div>
							</c:if>
							<div class="form-group">
								<label for="finError" class="col-xs-3 control-label"></label>
								<div class="col-xs-6">
									<span id="finError" class="text-danger"> </span>
								</div>
							</div>
							<div class="form-group">
								<label for="finSubmit" class="col-xs-3 control-label"></label>
								<div class="col-xs-6">
									<button id="finSubmit" class="btn btn-danger btn-block"><spring:message code="comm.submit"/></button>
								</div>
							</div>
						</div>
						<div class="col-xs-12 padding-clear padding-top-30">
							<div class="panel border">
								<div class="panel-heading">
									<span class="text-danger"><spring:message code="financial.record"/></span>
									<span class="pull-right recordtitle" data-type="0" data-value="0"><spring:message code="comm.shrink"/>-</span>
								</div>
								<div class="panel-body" id="recordbody0">
									<table class="table">
										<tr>
											<td class="text-center"><spring:message code="financial.coin"/></td>
											<td class="text-center"><spring:message code="financial.profit.type"/></td>
											<td class="text-right"><spring:message code="comm.number"/></td>
											<td class="text-right"><spring:message code="financial.plan.profit"/></td>
											<td class="text-center"><spring:message code="financial.plan.time"/></td>
											<td class="text-center"><spring:message code="financial.createtime"/></td>
											<td class="text-center"><spring:message code="comm.status"/></td>
											<td class="text-center"><spring:message code="comm.operation"/></td>
										</tr>
										<c:forEach items="${page.data }" varStatus="vs" var="v">
											<tr>
												<td class="text-center">${financesCoinMap[v.fcoinid] }</td>
												<td class="text-center">${v.fname }</td>
												<td class="text-right">
													<fmt:formatNumber value="${v.famount }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
												</td>
												<td class="text-right">
													<fmt:formatNumber value="${v.fplanamount }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
												</td>
												<td class="text-center">
													<fmt:formatDate value="${v.fupdatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
												</td>
												<td class="text-center">
													<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
												</td>
												<td class="text-center">${v.fstate_s}</td>
												<td class="text-center">
													<c:if test="${v.fstate eq 1}">
														<a class="cancel opa-link" href="javascript:void(0);"
														   data-fid="${v.fid }"><spring:message code="comm.cancel"/></a>
													</c:if>
												</td>
											</tr>
										</c:forEach>
										<c:if test="${fn:length(page.data)==0 }">
											<tr>
												<td colspan="7" class="no-data-tips">
													<span><spring:message code="financial.none"/></span>
												</td>
											</tr>
										</c:if>
									</table>
								</div>
							</div>
							<c:if test="${not empty page.pagin }">
								<input type="hidden" value="${page.currentPage }" name="type" id="currentPage"></input>
								<div class="text-right">
									<ul class="pagination">${page.pagin }
									</ul>
								</div>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<input type="hidden" value="${userWallet.total }" id="btcbalance">
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.finances.js"></script>
</body>
</html>
