<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/finance/accountrecord.css" type="text/css"></link>
<link rel="stylesheet" href="${staticurl }/front/js/plugin/layer/skin/bitdate/bitDate.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear padding-top-30 rightarea record">
					<div class="col-xs-12 rightarea-con">
						<div class="form-group">
							<span>
								<spring:message code="financial.begin.time" />
								：
							</span>
							<input class="databtn datainput" id="begindate" value="${begindate }" readonly="readonly"></input>
							<span>
								<spring:message code="financial.to.time" />
							</span>
							<input class="databtn datainput" id="enddate" value="${enddate }" readonly="readonly"></input>
							<span class="databtn datatime ${(datetype==1)?'datatime-sel':'' }" data-type="1">
								<spring:message code="financial.today" />
							</span>
							<span class="databtn datatime ${(datetype==2)?'datatime-sel':'' }" data-type="2">
								7
								<spring:message code="financial.day" />
							</span>
							<span class="databtn datatime ${(datetype==3)?'datatime-sel':'' }" data-type="3">
								15
								<spring:message code="financial.day" />
							</span>
							<span class="databtn datatime ${(datetype==4)?'datatime-sel':'' }" data-type="4">
								30
								<spring:message code="financial.day" />
							</span>
							<input type="hidden" id="datetype" value="0">
						</div>
						<div class="form-group">
							<span>
								<spring:message code="financial.operation.type" />
								：
							</span>
							<select class="form-control typeselect" id="recordType">
								<c:forEach items="${filters }" var="v">
									<c:choose>
										<c:when test="${select==v.value}">
											<option value="${v.key }" selected="selected">${v.value }</option>
										</c:when>
										<c:otherwise>
											<option value="${v.key }">${v.value }</option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</select>
						</div>
						<table class="table table-striped">
							<c:choose>
								<c:when test="${recordType==1 }">
									<%--人民币充值、提现--%>
									<tr class="bg-gary">
										<th width="200">
											<spring:message code="financial.account.recordtradingtime" />
										</th>
										<th width="160">
											<spring:message code="financial.account.recordtype" />
										</th>
										<th width="120"><spring:message code="comm.money"/></th>
										<th width="120">
											<spring:message code="financial.account.recordfee" />
										</th>
										<th width="120">
											<spring:message code="comm.status" />
										</th>
									</tr>
									<c:forEach items="${list.data }" var="v">
										<tr>
											<td>
												<%--<fmt:formatDate value="${type == 1 ? v.gmtModified : v.fcreatetime}" pattern="yyyy-MM-dd HH:mm:ss" />--%>
												<fmt:formatDate value="${v.fcreatetime}" pattern="yyyy-MM-dd HH:mm:ss" />
											</td>
											<td>${select }</td>
											<td class="red">
												<%--<fmt:formatNumber value="${type == 1 ? v.amount : v.famount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />--%>
												<fmt:formatNumber value="${v.famount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td>
												<fmt:formatNumber value="${type == 1 ? 0: v.ffees }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td style="">
												<%--<c:if test="${type == 2}">--%>
													<spring:message code="enum.financial.account.status${v.ftype}${v.fstatus}" />
												<%--</c:if>
												<c:if test="${type == 1}">
													<spring:message code="financial.agency.recharge.status${v.status}"/>
												</c:if>--%>
											</td>
										</tr>
									</c:forEach>
								</c:when>
								<c:when test="${recordType==2 }">
									<%--网络流通币充值、提现--%>
									<tr class="bg-gary">
										<th width="200">
											<spring:message code="financial.account.recordtradingtime" />
										</th>
										<th width="160">
											<spring:message code="financial.account.recordtype" />
										</th>
										<th width="120"><spring:message code="comm.number"/></th>
										<th width="120">
											<spring:message code="financial.account.recordfee" />
										</th>
										<th width="120">
											<spring:message code="comm.status" />
										</th>
									</tr>
									<c:forEach items="${list.data }" var="v">
										<tr>
											<td>
												<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
											</td>
											<td>${select }</td>
											<td class="red">
												<fmt:formatNumber value="${v.famount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td>
												<fmt:formatNumber value="${v.ffees + v.fbtcfees }" pattern="0.0000" maxIntegerDigits="10"
													maxFractionDigits="4" />
											</td>
											<td style="">
												<spring:message code="enum.financial.account.status${v.ftype}${v.fstatus}" />
											</td>
										</tr>
									</c:forEach>
								</c:when>
								<c:otherwise>
									<%--交易--%>
									<tr class="bg-gary">
										<th width="200">
											<spring:message code="financial.account.recordtradingtime" />
										</th>
										<th width="160">
											<spring:message code="financial.account.recordtype" />
										</th>
										<th width="150"><spring:message code="comm.number"/></th>
										<th width="120"><spring:message code="comm.money"/></th>
										<th width="120">
											<spring:message code="financial.money" />
										</th>
										<th width="120">
											<spring:message code="financial.account.recordfee" />
										</th>
										<th width="150">
											<spring:message code="financial.account.recordprice" />
										</th>
										<th width="120">
											<spring:message code="comm.status" />
										</th>
									</tr>
									<c:forEach items="${list.data }" var="v">
										<tr>
											<td>
												<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
											</td>
											<td>${select }</td>
											<td class="red">
												<fmt:formatNumber value="${v.fcount }" pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td class="red">
												<fmt:formatNumber value="${v.famount }" pattern="￥0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td>
												<c:choose>
													<c:when test="${v.ftype==0 }">￥${v.ffees }</c:when>
													<c:otherwise>${v.ffees }</c:otherwise>
												</c:choose>
											</td>
											<td>
												￥
												<fmt:formatNumber value="${((v.fcount-v.fleftcount)==0)?0:(v.famount/(v.fcount-v.fleftcount)) }"
													pattern="0.0000" maxIntegerDigits="10" maxFractionDigits="4" />
											</td>
											<td style="">${v.fstatus_s}</td>
										</tr>
									</c:forEach>
								</c:otherwise>
							</c:choose>
							<c:if test="${fn:length(list.data)==0 }">
								<tr>
									<td colspan="7" class="no-data-tips">
										<span>
											<spring:message code="financial.account.recordnodata" />
										</span>
									</td>
								</tr>
							</c:if>
							</tbody>
						</table>
						<c:if test="${!empty(list.pagin) }">
							<div class="text-right">
								<ul class="pagination">${list.pagin }
								</ul>
							</div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/layer/bitDate.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.record.js"></script>
</body>
</html>
