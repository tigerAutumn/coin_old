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
									<a href="/withdraw/cny_withdraw.html?symbol=${v.id }">
										<spring:message code="account.withdraw.tab1" arguments="${v.shortname }" />
									</a>
								</li>
							</c:forEach>
							<c:forEach items="${coinList }" var="v">
								<li>
									<a href="/withdraw/coin_withdraw.html?symbol=${v.id }">
										<spring:message code="account.withdraw.tab2" arguments="${v.shortname }" />
									</a>
								</li>
							</c:forEach>
						</ul>
						<div class="col-xs-12 padding-clear padding-top-30">
							<div class="col-xs-7 padding-clear form-horizontal">
								<div class="form-group ">
									<label for="diyMoney" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.balance" arguments="${coinType.shortname }" />
									</label>
									<div class="col-xs-6">
										<span class="form-control border-fff">
											<fmt:formatNumber value="${wallet.total }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
										</span>
									</div>
								</div>
								<div class="form-group ">
									<label for="withdrawBlank" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.balck" />
									</label>
									<div class="col-xs-6">
										<select id="withdrawBlank" class="form-control">
											<c:forEach items="${fbankinfoWithdraw }" var="v">
												<c:if test="${v.init }">
													<option value="${v.fid }" data-fid="${v.fid}" data-bankmodify="${(v.fprov==null && v.fcity==null && v.ftype == 0)? 1 : 2}"
															data-banknumber="${v.fbanknumber}" data-bankinfo="${v.fbanktype}"
															value="${v.fid }"><spring:message code="account.withdraw.balcktips"
															arguments="${v.fbanktype_s },${fn:substring(v.fbanknumber,fn:length(v.fbanknumber)-4,fn:length(v.fbanknumber)) }"
															argumentSeparator="," /></option>
												</c:if>
											</c:forEach>
										</select>
										<a href="javascript:void(0)" class="text-primary addtips" data-toggle="modal" data-target="#withdrawCnyAddress">
											<spring:message code="account.withdraw.balckadd" />
										</a>
									</div>
								</div>
								<div class="form-group ">
									<label for="withdrawBalance" class="col-xs-3 control-label">
										<spring:message code="account.withdraw.amount" />
									</label>
									<div class="col-xs-6">
										<input id="withdrawBalance" class="form-control" type="text">
										<span class="amounttips">
											<span>
												<spring:message code="account.withdraw.fee" />
												&nbsp;&nbsp;&nbsp;
												<span id="free">0</span>
												<span>${coinType.shortname }</span>
											</span>
											<span>
												<spring:message code="account.withdraw.real" />
												<span id="amount" class="text-danger">0</span>
												<span class="text-danger">${coinType.shortname }</span>
											</span>
										</span>
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
									<label for="withdrawCnyButton" class="col-xs-3 control-label"></label>
									<div class="col-xs-6">
										<button id="withdrawCnyButton" class="btn btn-danger btn-block">
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
											<spring:message code="account.withdraw.tips2"
												arguments='${withdrawSetting.withdrawMin }' />
										</p>
										<p>
											<spring:message code="account.withdraw.tips3" />
										</p>
										<p>
											<spring:message code="account.withdraw.tips4" />
										</p>
										<p>
											<spring:message code="account.withdraw.tips5"
												arguments='${withdrawSetting.withdrawMax }' />
										</p>
									</div>
								</div>
							</div>
							<div class="col-xs-12 padding-clear padding-top-30">
								<div class="panel border">
									<div class="panel-heading">
										<span class="text-danger">
											<spring:message code="account.withdraw.record1" arguments="${coinType.shortname }"/>
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
													<spring:message code="account.withdraw.record4" />
												</td>
												<td>
													<spring:message code="account.withdraw.record5" />
												</td>
												<td>
													<spring:message code="account.withdraw.record6" />
												</td>
												<td>
													<spring:message code="account.withdraw.record7" />
												</td>
												<td>
													<spring:message code="account.withdraw.record8" />
												</td>
												<td>
													<spring:message code="comm.status" />
												</td>
											</tr>
											<c:forEach items="${page.data }" var="v" varStatus="vs">
												<tr>
													<td>
														<fmt:formatDate value="${v.fcreatetime }" pattern="yyyy-MM-dd HH:mm:ss" />
													</td>
													<td>
														<spring:message code="account.withdraw.record10" />
													</td>
													<td>
														<fmt:formatNumber value="${v.famount }" pattern="￥0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
													</td>
													<td>
														<fmt:formatNumber value="${v.ffees }" pattern="￥0.0000" maxFractionDigits="4" maxIntegerDigits="10" />
													</td>
													<td><spring:message code="account.withdraw.balcktips"
															arguments="${v.fbank },${fn:substring(v.faccount,fn:length(v.faccount)-4,fn:length(v.faccount)) }"
															argumentSeparator="," /></td>
													<td>
														<font color="red">${v.fid }</font>
													</td>
													<td>
														<c:choose>
															<c:when test="${v.fstatus==2}">
																<span title='<spring:message code="account.withdraw.record12" />'>
															</c:when>
															<c:otherwise>
																<span title=''>
															</c:otherwise>
														</c:choose>
														<spring:message code="enum.capital.withdraw.status${v.fstatus}"/>
														<c:if test="${v.fstatus==1 }">
										&nbsp;|&nbsp;<a class="cancelWithdrawcny opa-link" data-fid="${v.fid }" href="javascript:void(0)">
																<spring:message code="account.withdraw.record11" />
															</a>
														</c:if>
														</span>
													</td>
												</tr>
											</c:forEach>
											<c:if test="${fn:length(page.data)==0 }">
												<tr>
													<td colspan="7" class="no-data-tips">
														<span>
															<spring:message code="account.withdraw.record13" />
														</span>
													</td>
												</tr>
											</c:if>
										</table>
									</div>
								</div>
								<c:if test="${!empty(page.pagin) }">
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
	<div class="modal modal-custom fade" id="withdrawCnyAddress" tabindex="-1" role="dialog"
		aria-labelledby="exampleModalLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
                <input type="hidden" id="userBankId" value="0">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title" id="exampleModalLabel">
						<spring:message code="account.withdraw.modal1" />
					</span>
				</div>
				<div class="modal-body form-horizontal">
					<div class="form-group ">
						<label for="payeeAddr" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.modal2" />
						</label>
						<div class="col-xs-8">
							<input id="payeeAddr" class="form-control" type="text" value="${fuser.frealname }" readonly="readonly" />
							<span class="help-block text-danger">
								<spring:message code="account.withdraw.modal3" />
							</span>
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawAccountAddr" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.modal4" />
						</label>
						<div class="col-xs-8">
							<input id="withdrawAccountAddr" class="form-control" type="" text>
						</div>
					</div>
					<div class="form-group ">
						<label for="withdrawAccountAddr2" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.modal5" />
						</label>
						<div class="col-xs-8">
							<input id="withdrawAccountAddr2" class="form-control" type="" text>
						</div>
					</div>
					<div class="form-group ">
						<label for="openBankTypeAddr" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.modal6" />
						</label>
						<div class="col-xs-8">
							<select id="openBankTypeAddr" class="form-control">
								<option value="-1">
									<spring:message code="account.withdraw.modal7" />
								</option>
								<c:forEach items="${bankTypes }" var="v">
									<option value="${v.fid }">${v.fcnname}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<div id="prov_city" class="form-group withdraw">
						<label for="prov" class="col-xs-3 control-label">
							<spring:message code="account.withdraw.modal8" />
						</label>
						<div class="col-xs-8 ">
							<div class="col-xs-4 padding-right-clear padding-left-clear margin-bottom-15">
								<select id="prov" class="form-control prov">
								</select>
							</div>
							<div class="col-xs-4 padding-right-clear margin-bottom-15">
								<select id="city" class="form-control prov">
								</select>
							</div>
							<div class="col-xs-4 padding-right-clear margin-bottom-15">
								<select id="dist" class="form-control prov">
								</select>
							</div>
							<div class="col-xs-12 padding-right-clear padding-left-clear">
								<input id="address" class="form-control" type="text"
									placeholder="<spring:message code="account.withdraw.modal9" />" />
							</div>
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
							<label for="addressTotpCode" class="col-xs-3 control-label">
								<spring:message code="comm.google" />
							</label>
							<div class="col-xs-8">
								<input id="addressTotpCode" class="form-control" type="text">
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
						<label for="withdrawCnyAddrBtn" class="col-xs-3 control-label"></label>
						<div class="col-xs-8">
							<button id="withdrawCnyAddrBtn" class="btn btn-danger btn-block">
								<spring:message code="account.withdraw.modal10" />
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<input id="feesRate" type="hidden" value="${withdrawSetting.withdrawFee }">
	<input id="userBalance" type="hidden" value="${wallet.total}">
	<input type="hidden" id="max_double"
		value="<fmt:formatNumber pattern="0.0000" value="${withdrawSetting.withdrawMax }" maxFractionDigits="4"></fmt:formatNumber>">
	<input type="hidden" id="min_double"
		value="<fmt:formatNumber pattern="0.0000" value="${withdrawSetting.withdrawMin }" maxFractionDigits="4"></fmt:formatNumber>">
	<input type="hidden" id="symbol" value="${coinType.id }">
	<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js?v=1"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/account.withdraw.js?v=1"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/city.min.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/finance/jquery.cityselect.js"></script>
</body>
</html>
