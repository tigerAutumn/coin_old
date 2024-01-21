<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<link rel="stylesheet" href="${staticurl }/front/css/finance/accountassets.css" type="text/css"></link>
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
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea assets">
					<div class="col-xs-12 rightarea-con">
						<ul class="nav nav-tabs rightarea-tabs">
							<c:forEach items="${coinTypeList }" var="v">
								<li class="${v.id==coinType.id?'active':'' }">
									<c:choose>
										<c:when test="${v.id==coinType.id }">
											<a href="javascript:void(0)">${v.shortname } </a>
										</c:when>
										<c:otherwise>
											<a href="/financial/accountcoin.html?symbol=${v.id }">${v.shortname } </a>
										</c:otherwise>
									</c:choose>
								</li>
							</c:forEach>
						</ul>
						<div class="col-xs-12 padding-clear padding-top-30">
							<c:forEach items="${fvirtualaddressWithdraws }" var="v" varStatus="vs">
								<div class="col-xs-6">
									<div class="coin-item">
										<div class="coin-item-top">
											<div class="col-xs-11">
												<p>${v.fadderess }</p>
											</div>
											<div class="col-xs-1 text-center padding-clear">
												<span class="coin-item-del" data-fid="${v.fid }"></span>
											</div>
										</div>
										<div class="coin-item-bot">
											<div class="col-xs-12">
												<span class="coin-item-code">
													<span class="addresscode" data-text=${v.fremark } data-fid=${v.fid }>
														<span class="qrcode" id="qrcode${v.fid }"></span>
													</span>
													${v.fremark }
												</span>
											</div>
										</div>
									</div>
								</div>
							</c:forEach>
							<div class="col-xs-6">
								<div class="bank-add coin-add text-center" data-toggle="modal" data-target="#addCoinAddress">
									<span class="icon"></span>
									<br />
									<span><spring:message code="financial.append.address"/></span>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="modal modal-custom fade" id="addCoinAddress" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title" id="exampleModalLabel"><spring:message code="financial.withdraw"/></span>
				</div>
				<div class="modal-body form-horizontal">
					<div class="form-group ">
						<label for="withdrawBtcAddr" class="col-xs-3 control-label"><spring:message code="financial.withdraw"/></label>
						<div class="col-xs-8">
							<input id="withdrawBtcAddr" class="form-control" type="text">
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawBtcRemark" class="col-xs-3 control-label"><spring:message code="financial.remark"/></label>
						<div class="col-xs-8">
							<input id="withdrawBtcRemark" class="form-control" type="" text>
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawBtcPass" class="col-xs-3 control-label"><spring:message code="comm.trade.pwd"/></label>
						<div class="col-xs-8">
							<input id="withdrawBtcPass" class="form-control" type="password">
						</div>
					</div>
					<c:if test="${fuser.fistelephonebind==true }">
						<div class="form-group">
							<label for="withdrawBtcAddrPhoneCode" class="col-xs-3 control-label"><spring:message code="comm.sms"/></label>
							<div class="col-xs-8">
								<input id="withdrawBtcAddrPhoneCode" class="form-control" type="text">
								<button id="bindsendmessage" data-msgtype="108" data-tipsid="binderrortips" class="btn btn-sendmsg"><spring:message code="comm.sms.send"/></button>
							</div>
						</div>
					</c:if>
					<c:if test="${fuser.fgooglebind==true }">
						<div class="form-group">
							<label for="withdrawBtcAddrTotpCode" class="col-xs-3 control-label"><spring:message code="comm.google"/></label>
							<div class="col-xs-8">
								<input id="withdrawBtcAddrTotpCode" class="form-control" type="text">
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label for="binderrortips" class="col-xs-3 control-label"></label>
						<div class="col-xs-8">
							<span id="binderrortips" class="text-danger"></span>
						</div>
					</div>
					<div class="form-group margin-bottom-clear">
						<label for="withdrawBtcAddrBtn" class="col-xs-3 control-label"></label>
						<div class="col-xs-8">
							<button id="withdrawBtcAddrBtn" class="btn btn-danger btn-block"><spring:message code="comm.submit"/></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<input type="hidden" id="symbol" value="${coinType.id }">
	<input type="hidden" value="${coinType.shortname}" id="coinName" name="coinName">
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.qrcode.min.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.assets.js"></script>
</body>
</html>
