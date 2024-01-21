<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/finance/recharge.css?v=22" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea recharge">
					<div class="col-xs-12 rightarea-con">
						<ul class="nav nav-tabs rightarea-tabs">
							<c:forEach items="${cnyList }" var="v">
								<li class="${v.id == coinType.id ? 'active':''}">
									<a href="/deposit/cny_deposit.html?symbol=${v.id }">${v.shortname } </a>
								</li>
							</c:forEach>
							<c:forEach items="${coinList }" var="v">
								<li class="${v.id == coinType.id ? 'active':''}">
									<a href="/deposit/coin_deposit.html?symbol=${v.id }">${v.shortname } </a>
								</li>
							</c:forEach>
						</ul>
						<c:choose>
							<c:when test="${coinType.shortname eq 'GSET'}">
								<div class="col-xs-12 padding-top-30">
									<div id="rechage1" class="col-xs-7 padding-clear form-horizontal">
										<div class="form-group ">
											<label for="agency1" class="col-xs-3 control-label">代理充值1</label>
											<div class="col-xs-6">
												<a id="agency1" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=3342023446&site=qq&menu=yes">
													<i class="icon-qq"></i>3342023446</a>
											</div>
										</div>
										<div class="form-group ">
											<label for="agency2" class="col-xs-3 control-label">代理充值2</label>
											<div class="col-xs-6">
												<a id="agency2" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=1872736610&site=qq&menu=yes">
													<i class="icon-qq"></i>1872736610</a>
											</div>
										</div>
										<div class="form-group ">
											<label for="agency3" class="col-xs-3 control-label">代理充值3</label>
											<div class="col-xs-6">
												<a id="agency3" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=3239882620&site=qq&menu=yes">
													<i class="icon-qq"></i>3239882620</a>
											</div>
										</div>
										<div class="form-group ">
											<label for="agency4" class="col-xs-3 control-label">代理充值4</label>
											<div class="col-xs-6">
												<a id="agency4" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=3570184720&site=qq&menu=yes">
													<i class="icon-qq"></i>3570184720</a>
											</div>
										</div>
									</div>
									<div class="col-xs-5 padding-clear text-center">
										<a target="_blank" href="/about/about.html?id=17" class="recharge-help"> </a>
									</div>
								</div>
							</c:when>
							<c:when test="${coinType.shortname eq 'GXS'}">
								<div class="col-xs-12 padding-clear padding-top-30">
									<div class="col-xs-8 col-xs-offset-2 padding-clear rechage-gxs">
										<div class="form-group clearfix">
											<p class="col-xs-12 text-center">
												<spring:message code="recharge.gxs.2" />
											</p>
										</div>
										<div class="form-group clearfix">
											<span class="col-xs-4 top">
												<spring:message code="recharge.gxs.1" />
											</span>
											<span class="col-xs-8 top">
													${rechargeAddress.fadderess}
											</span>
										</div>
										<div class="form-group clearfix">
											<span class="col-xs-4">
												MEMO
											</span>
											<span class="col-xs-8">
													${rechargeAddress.fuid}
											</span>
										</div>
									</div>
								</div>
								<div class="col-xs-12 padding-clear padding-top-30">
									<div class="panel panel-tips">
										<div class="panel-header text-center text-danger">
										<span class="panel-title"><spring:message
												code="financial.recharge.notice"/></span>
										</div>
										<div class="panel-body">
											<p>&lt <spring:message code="financial.recharge.notice4"/>support@hotcoin.top</p>
											<p>&lt <spring:message code="financial.recharge.notice5"
																   arguments="${coinType.shortname }"/></p>
											<p>&lt <spring:message code="financial.recharge.notice6"
																   arguments="${coinType.shortname eq 'BL'?'0.05':'0.0001'},${coinType.shortname }"
																   argumentSeparator=","/></p>
										</div>
									</div>
								</div>
							</c:when>
							<c:otherwise>
								<div class="col-xs-12 padding-clear padding-top-30 recharge-qrcodetext">
									<div class="col-xs-2 text-right">
										<span style="color:#fff;"><spring:message code="financial.recharge.address"/></span>
									</div>
									<div class="col-xs-7 recharge-qrcodecon ${rechargeAddress!=null?'address':''}">
										<c:if test="${not empty rechargeAddress}">
											<span class="code-txt">${rechargeAddress.fadderess}</span>
											<span class="code-box">
												<span class="qrcode" id="qrcode"></span>
											</span>
										</c:if>
										<c:if test="${empty rechargeAddress}">
											<button class="btn btn-danger" id="virtualaddress"><spring:message code="financial.recharge.get.address"/></button>
										</c:if>
									</div>
								</div>
								<div class="col-xs-12 padding-clear padding-top-30">
									<div class="panel panel-tips">
										<div class="panel-header text-center text-danger">
											<span class="panel-title"><spring:message code="financial.recharge.notice"/></span>
										</div>
										<div class="panel-body">
											<p>&lt <spring:message code="financial.recharge.notice4"/>support@hotcoin.top</p>
											<p>&lt <spring:message code="financial.recharge.notice5" arguments="${coinType.shortname }"/></p>
											<p>&lt <spring:message code="financial.recharge.notice6" arguments="0.001${coinType.shortname },${coinType.shortname }" argumentSeparator=","/></p>
										</div>
									</div>
								</div>
							</c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${coinType.shortname != 'GSET'}">
							<div class="col-xs-12 padding-clear padding-top-30">
								<div class="panel border">
									<div class="panel-heading">
										<span class="text-danger"><spring:message code="financial.last10.recharge.record"/></span>
									</div>
									<div class="panel-body" id="recordbody0">
										<table class="table">
											<tr>
												<td><spring:message code="financial.recharge.update"/></td>
												<td><spring:message code="financial.recharge.address"/></td>
												<td><spring:message code="financial.recharge.number"/></td>
												<td><spring:message code="financial.recharge.confirmations"/></td>
												<td><spring:message code="comm.status"/></td>
											</tr>
											<c:if test="${fn:length(page.data)==0 }">
												<tr>
													<td class="no-data-tips" colspan="5">
														<span>
															<spring:message code="financial.account.rechargebtcnodata" />
														</span>
													</td>
												</tr>
											</c:if>
											<c:forEach items="${page.data }" var="v" varStatus="vs">
												<tr>
													<td width="150">
														<fmt:formatDate value="${v.fupdatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
													</td>
													<td width="330">
														<c:choose>
															<c:when test="${coinType.shortname=='BTC' }">
																<a href="http://qukuai.com/address/${v.frechargeaddress }" target="_blank">${v.frechargeaddress }</a>
															</c:when>
															<c:when test="${coinType.shortname=='LTC' }">
																<a href="http://qukuai.com/ltc/address/${v.frechargeaddress }" target="_blank">${v.frechargeaddress }</a>
															</c:when>
															<c:otherwise>${v.frechargeaddress }</c:otherwise>
														</c:choose>
													</td>
													<td width="100">
														<fmt:formatNumber pattern="0.0000" value="${v.famount }" maxFractionDigits="4"></fmt:formatNumber>
													</td>
													<td width="80">${v.fconfirmations }</td>
													<td>	<c:if test="${v.fstatus eq 3 }"><spring:message code="financial.recharge.succ"/></c:if>
														<c:if test="${v.fstatus ne 3 }"><spring:message code="financial.recharge.wait"/></c:if>
													</td>
												</tr>
											</c:forEach>
										</table>
									</div>
								</div>
							</div>
							</c:when>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<input type="hidden" id="symbol" value="${coinType.id }">
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.recharge.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.qrcode.min.js"></script>
	<script type="text/javascript">
		jQuery(document).ready(function() {
			if (navigator.userAgent.indexOf("MSIE") > 0) {
				jQuery('#qrcode').qrcode({
					text : '${rechargeAddress.fadderess}',
					width : "149",
					height : "143",
					render : "table"
				});
			} else {
				jQuery('#qrcode').qrcode({
					text : '${rechargeAddress.fadderess}',
					width : "149",
					height : "143"
				});
			}
		});
	</script>
</body>
</html>
