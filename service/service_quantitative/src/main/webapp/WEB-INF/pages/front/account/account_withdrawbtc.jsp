<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/finance/withdraw.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear rightarea withdraw">
					<div class="col-xs-12 rightarea-con">
						<ul class="nav nav-tabs rightarea-tabs">

							<c:forEach items="${cnyList }" var="v">
								<li class="${v.id == coinType.id ? 'active':''}">
									<a href="/withdraw/cny_withdraw.html?symbol=${v.id }">${v.shortname } </a>
								</li>
							</c:forEach>
							<c:forEach items="${coinList }" var="v">
								<li class="${v.id == coinType.id ? 'active':''}">
									<a href="/withdraw/coin_withdraw.html?symbol=${v.id }">
										<spring:message code="account.withdraw.tab2" arguments="${v.shortname }" />
									</a>
								</li>
							</c:forEach>
						</ul>
						<div class="col-xs-12 padding-clear padding-top-30">
							<div class="col-xs-7 padding-clear form-horizontal">
								<div class="form-group ">
									<label for="withdrawAmount" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.balance" arguments="${coinType.shortname }" />
									</label>
									<div class="col-xs-6">
										<span class="form-control border-fff">
											<fmt:formatNumber value="${userWallet.total }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
										</span>
									</div>
								</div>
								<div class="form-group ">
									<label for="withdrawAddr" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.coinaddr" />
									</label>
									<div class="col-xs-9">
										<select id="withdrawAddr" class="form-control">
											<c:forEach items="${withdrawAddress }" var="v">
												<option value="${v.fid }">${v.fremark }-${v.fadderess }</option>
											</c:forEach>
										</select>
										<a href="javascript:void(0)" class="text-primary addtips" data-toggle="modal" data-target="#address">
											<spring:message code="account.withdraw.balckadd" />
										</a>
									</div>
								</div>
								<c:if test="${coinType.shortname eq 'BTC' }">
									<div class="form-group ">
										<label for="btcfee" class="col-xs-3 control-label">
											<spring:message code="account.withdraw.btcfee" />
										</label>
										<div class="col-xs-6">
											<select id="btcfee" class="form-control">
												<option value="0" selected="selected">0.0001</option>
												<option value="1">0.0002</option>
												<option value="2">0.0003</option>
												<option value="3">0.0004</option>
												<option value="4">0.0005</option>
												<option value="5">0.0006</option>
												<option value="6">0.0007</option>
												<option value="7">0.0008</option>
												<option value="8">0.0009</option>
												<option value="9">0.0010</option>
											</select>
										</div>
									</div>
								</c:if>
								<div class="form-group ">
									<label for="withdrawAmount" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.coinamount" />
									</label>
									<div class="col-xs-6">
										<input id="withdrawAmount" class="form-control" type="text">
									</div>
								</div>
								<div class="form-group ">
									<label for="tradePwd" class="col-xs-3 control-label">
										<spring:message code="comm.trade.pwd" />
									</label>
									<div class="col-xs-6">
										<input id="tradePwd" class="form-control" type="password">
									</div>
								</div>

								<c:if test="${fuser.fistelephonebind==true }">
									<div class="form-group">
										<label for="withdrawPhoneCode" class="col-xs-3 control-label">
											<spring:message code="comm.sms" />
										</label>
										<div class="col-xs-6">
											<input id="withdrawPhoneCode" class="form-control" type="text">
											<button id="withdrawsendmessage" data-msgtype="104" data-tipsid="withdrawerrortips" class="btn btn-sendmsg">
												<spring:message code="comm.sms.send" />
											</button>
										</div>
									</div>
								</c:if>

								<c:if test="${fuser.fgooglebind==true }">
									<div class="form-group">
										<label for="withdrawTotpCode" class="col-xs-3 control-label">
											<spring:message code="comm.google" />
										</label>
										<div class="col-xs-6">
											<input id="withdrawTotpCode" class="form-control" type="text">
										</div>
									</div>
								</c:if>
								<div class="form-group">
									<label for="withdrawerrortips" class="col-xs-3 control-label"></label>
									<div class="col-xs-6">
										<span id="withdrawerrortips" class="text-danger">
											<c:if test="${param.success==1}">
												<spring:message code="account.withdraw.success" />
											</c:if>
										</span>
									</div>
								</div>
								<div class="form-group">
									<label for="withdrawBtcButton" class="col-xs-3 control-label"></label>
									<div class="col-xs-6">
										<button id="withdrawBtcButton" class="btn btn-danger btn-block">
											<spring:message code="account.withdraw.submit" />
										</button>
									</div>
								</div>
							</div>
							<div class="col-xs-12 padding-clear padding-top-30">
								<div class="panel panel-tips">
									<div class="panel-header text-center text-danger">
										<span class="panel-title">
											<spring:message code="account.withdraw.tips1" />
										</span>
									</div>
									<div class="panel-body">
										<p>
											<spring:message code="account.withdraw.cointips1" arguments='${withdrawSetting.withdrawMin }' />
										</p>
										<p>
											<spring:message code="account.withdraw.cointips2" />
										</p>
									</div>
								</div>
							</div>
							<div class="col-xs-12 padding-clear padding-top-30">
								<div class="panel border">
									<div class="panel-heading">
										<span class="text-danger">
											<spring:message code="account.withdraw.record1" arguments="${coinType.shortname }" />
										</span>
										<span class="pull-right recordtitle" data-type="0" data-value="0">
											<spring:message code="comm.shrink" />
										</span>
									</div>
									<div class="panel-body" id="recordbody0">
										<table class="table">
											<tr>
												<td>
													<spring:message code="account.withdraw.record3" />
												</td>
												<td>
													<spring:message code="account.withdraw.record14" />
												</td>
												<td>
													<spring:message code="account.withdraw.record15" />
												</td>
												<td>
													<spring:message code="account.withdraw.record6" />
												</td>
												<td>
													<spring:message code="comm.status" />
												</td>
												<td>
													<spring:message code="account.withdraw.coinmodel1" />
												</td>
											</tr>
											<c:forEach items="${page.data }" varStatus="vs" var="v">
												<tr>
													<td>
														<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
													</td>
													<td>
														<c:if test="${v.fstatus==3 }">
															<c:choose>
																<c:when test="${coinType.shortname=='BTC' }">
																	<a href="http://qukuai.com/tx/${v.funiquenumber }" target="_blank">${v.fwithdrawaddress }</a>
																</c:when>
																<c:when test="${coinType.shortname=='LTC' }">
																	<a href="http://qukuai.com/ltc/tx/${v.funiquenumber }" target="_blank">${v.fwithdrawaddress }</a>
																</c:when>
																<c:otherwise>
																	${v.fwithdrawaddress }
																</c:otherwise>
															</c:choose>
														</c:if>
														<c:if test="${v.fstatus!=3 }">
															${v.fwithdrawaddress }
														</c:if>
													</td>
													<td>
														<fmt:formatNumber value="${v.famount }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
													</td>
													<td>
														<fmt:formatNumber value="${v.ffees + v.fbtcfees }" pattern="0.0000" maxFractionDigits="4"
															maxIntegerDigits="10" />
													</td>
													<td>
														<spring:message code="enum.capital.withdraw.status${v.fstatus}" />
													</td>
													<td>
														<c:if test="${v.fstatus==1 }">
															<a class="cancelWithdrawBtc opa-link" href="javascript:void(0);" data-fid="${v.fid }">
																<spring:message code="account.withdraw.record11" />
															</a>
														</c:if>
													</td>
												</tr>
											</c:forEach>
											<c:if test="${fn:length(page.data)==0 }">
												<tr>
													<td colspan="6" class="no-data-tips">
														<span>
															<spring:message code="account.withdraw.record13" />
														</span>
													</td>
												</tr>
											</c:if>
										</table>
									</div>
								</div>
								<c:if test="${not empty page.pagin }">
									<input type="hidden" value="${currentPage }" name="type" id="currentPage"></input>
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
	</div>
	<div class="modal modal-custom fade" id="address" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title" id="exampleModalLabel">
						<spring:message code="account.withdraw.coinaddr" />
					</span>
				</div>
				<div class="modal-body form-horizontal">
					<div class="form-group ">
						<label for="withdrawBtcAddr" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.coinaddr" />
						</label>
						<div class="col-xs-6">
							<input id="withdrawBtcAddr" class="form-control" type="text">
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawBtcRemark" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.coinmodel1" />
						</label>
						<div class="col-xs-6">
							<input id="withdrawBtcRemark" class="form-control" type="" text>
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawBtcPass" class="col-xs-3 control-label">
							<spring:message code="comm.trade.pwd" />
						</label>
						<div class="col-xs-6">
							<input id="withdrawBtcPass" class="form-control" type="password">
						</div>
					</div>
					<c:if test="${fuser.fistelephonebind==true }">
						<div class="form-group">
							<label for="addressPhoneCode" class="col-xs-3 control-label">
								<spring:message code="comm.sms" />
							</label>
							<div class="col-xs-8">
								<input id="addressPhoneCode" class="form-control" type="text">
								<button id="bindsendmessage" data-msgtype="110" data-tipsid="binderrortips" class="btn btn-sendmsg">
									<spring:message code="comm.sms.send" />
								</button>
							</div>
						</div>
					</c:if>

					<c:if test="${fuser.fgooglebind==true }">
						<div class="form-group">
							<label for="withdrawBtcAddrTotpCode" class="col-xs-3 control-label">
								<spring:message code="comm.google" />
							</label>
							<div class="col-xs-6">
								<input id="withdrawBtcAddrTotpCode" class="form-control" type="text">
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label for="diyMoney" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<span id="binderrortips" class="text-danger"></span>
						</div>
					</div>
					<div class="form-group margin-bottom-clear">
						<label for="diyMoney" class="col-xs-3 control-label"></label>
						<div class="col-xs-6">
							<button id="withdrawBtcAddrBtn" class="btn btn-danger btn-block">
								<spring:message code="account.withdraw.modal10" />
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<input type="hidden" id="isEmptyAuth" value="${(fuser.fgooglebind==false )?'1':'0' }">
	<input type="hidden" id="symbol" value="${coinType.id }">
	<input type="hidden" value="${userWallet.total }" id="btcbalance">
	<input type="hidden" value="${coinType.shortname}" id="coinName">
	<input type="hidden" id="max_double" value="${withdrawSetting.withdrawMax }">
	<input type="hidden" id="min_double" value="${withdrawSetting.withdrawMin }">
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=21"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.withdraw.js?v=21"></script>
</body>
</html>
