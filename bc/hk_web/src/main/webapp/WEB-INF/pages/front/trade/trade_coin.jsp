<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
	<%@include file="../comm/link.inc.jsp"%>
	<link href="${staticurl }/front/css/trade/trade.css" rel="stylesheet" type="text/css" media="screen, projection" />
</head>
<body>
<%@include file="../comm/header.jsp"%>
<div class="container-full coin-list">
	<div class="container coin-list-box">
		<c:forEach items="${typeMap }" var="v">
			<div class="coin-item-box ${v.key eq type ? 'active':''}">
				<span><spring:message code="trade.enum.tradetype${v.key }" /></span><i class="arrows"></i>
				<div class="list-box">
					<div class="top-box"></div>
					<div class="con-box">
						<c:forEach items="${tradeTypeList }" var="treadItem" varStatus="vs">
							<c:if test="${treadItem.type eq v.key}">
								<a href="/trade/cny_coin.html?tradeId=${treadItem.id}&type=${treadItem.type}">
									<i class="lefticon" style="background: url('${treadItem.sellWebLogo}') no-repeat 0 0;background-size:100% 100%;"></i>
									<span>${treadItem.sellShortName } / ${treadItem.buyShortName}</span>
								</a>
							</c:if>
						</c:forEach>
					</div>
				</div>
			</div>
			<div class="split-box"></div>
		</c:forEach>
	</div>
</div>
<div class="container-full padding-top-40">
	<div class="container trade">
		<div class="tread-detail-box ">
			<div class="left-box">
				<i class="lefticon" style="background: url('${systemTradeType.sellWebLogo}') no-repeat 0 0;background-size:100% 100%;"></i>
				<span class="shortName">${systemTradeType.sellShortName}</span>
				<span class="price" id="labLastPrice">0</span>
				<i class="tendency up" id="labDirection"></i>
				<div id="${v.sellShortName }_plot" style="width: 188px; height: 60px; display: inline-block; float: left; margin-top: 10px;"></div>
			</div>
			<div class="right-box">
				<div class="item-box">
					<p class="name"><spring:message code="tread.BuyFirst" /></p>
					<p class="cont" id="buyFirst">0</p>
				</div>
				<div class="item-box">
					<p class="name"><spring:message code="tread.SellFirst" /></p>
					<p class="cont" id="sellFirst">0</p>
				</div>
				<div class="item-box">
					<p class="name"><spring:message code="tread.Volume" /></p>
					<p class="cont" id="volume">0</p>
				</div>
			</div>
		</div>
		<div class="trade-body clearfix">
			<div class="tabbable">
				<div class="trade-area">
					<div class="trade-buysell">
						<div class=trade-group>
								<span>
									<spring:message code="trade.free" />
								</span>
							<span id="tradeBuyInt" class="redtips font-size-18">0</span>
							<span id="tradeBuyDig" class="redtips">.0000</span>
							<span class="redtips">${systemTradeType.buyShortName }</span>
						</div>
						<div class="trade-group">
							<label for="tradebuyprice" class="trade-inputtips">
								<spring:message code="comm.buy.price" />${systemTradeType.sellShortName }/${systemTradeType.buyShortName }</label>
							<input id="tradebuyprice" class="trade-input" />
						</div>
						<c:if test="${type eq 2}">
							<div class="trade-group clearfix">
								<span class="trade-price-tips" id="tradebuypriceTips">≈ 0.00 CNY</span>
							</div>
						</c:if>
						<div class="trade-group">
							<label for="tradebuyamount" class="trade-inputtips">
								<spring:message code="comm.buy.number" />${systemTradeType.sellShortName }</label>
							<input id="tradebuyamount" class="trade-input" />
						</div>
						<div class="trade-group">
							<div id="buyBar" class="trade-bar">
								<div class="trade-barbox">
									<div id="buyslider" class="slider" data-role="slider" data-param-marker="marker" data-param-complete="complete"
										 data-param-type="0" data-param-markertop="marker-top"></div>
									<div class="slider-points">
										<div class="proportioncircle proportion0" data-points="0">0%</div>
										<div class="proportioncircle proportion1" data-points="20">20%</div>
										<div class="proportioncircle proportion2" data-points="40">40%</div>
										<div class="proportioncircle proportion3" data-points="60">60%</div>
										<div class="proportioncircle proportion4" data-points="80">80%</div>
										<div class="proportioncircle proportion5" data-points="100">100%</div>
									</div>
								</div>
							</div>
						</div>
						<div class="trade-group">
								<span class="trade-input">
									<label class="trade-inputtips">
										<spring:message code="trade.amount" />
									</label>
									<span id="tradebuyTurnover">0</span>
									${systemTradeType.buyShortName }
								</span>
						</div>
						<div class="trade-group">
							<span id="buy-errortips" class="text-danger trade-error"></span>
							<c:if test="${type eq 2}">
								<span class="trade-price-tips" id="tradebuyTurnoverTips">≈ 0.00 CNY</span>
							</c:if>
							<button id="buybtn" class="btn btn-danger btn-block trade-btn">
								<spring:message code="comm.buy" />${systemTradeType.sellShortName }</button>
						</div>
						<div class="trade-group">
							<a href="/deposit/coin_deposit.html" class="text-danger">
								<spring:message code="trade.recharge" />
								<i class="arrow-icon-small red"></i>
							</a>
						</div>
					</div>
					<div class="trade-disk"></div>
					<div class="trade-buysell">
						<div class="trade-group">
								<span>
									<spring:message code="trade.free" />
								</span>
							<span id="tradeCoinInt" class="greentips font-size-18">0</span>
							<span id="tradeCoinDig" class="greentips">.0000</span>
							<span class="greentips">${systemTradeType.sellShortName }</span>
						</div>
						<div class="trade-group">
							<label for="tradesellprice" class="trade-inputtips">
								<spring:message code="comm.sell.price" />${systemTradeType.sellShortName }/${systemTradeType.buyShortName }</label>
							<input id="tradesellprice" class="trade-input" />
						</div>
						<c:if test="${type eq 2}">
							<div class="trade-group clearfix">
								<span class="trade-price-tips" id="tradesellpriceTips">≈ 0.00 CNY</span>
							</div>
						</c:if>
						<div class="trade-group">
							<label for="tradesellamount" class="trade-inputtips">
								<spring:message code="comm.sell.number" />${systemTradeType.sellShortName }</label>
							<input id="tradesellamount" class="trade-input" />
						</div>
						<div class="trade-group">
							<div id="buyBar" class="trade-bar">
								<div class="trade-barbox">
									<div id="sellslider" class="slider" data-role="slider" data-param-marker="marker sell-marker"
										 data-param-complete="complete" data-param-type="1" data-param-markertop="marker-top"></div>
									<div class="slider-points">
										<div class="proportioncircle proportion0" data-points="0">0%</div>
										<div class="proportioncircle proportion1" data-points="20">20%</div>
										<div class="proportioncircle proportion2" data-points="40">40%</div>
										<div class="proportioncircle proportion3" data-points="60">60%</div>
										<div class="proportioncircle proportion4" data-points="80">80%</div>
										<div class="proportioncircle proportion5" data-points="100">100%</div>
									</div>
								</div>
							</div>
						</div>
						<div class="trade-group">
							<div class="trade-input">
								<label class="trade-inputtips">
									<spring:message code="trade.amount" />
								</label>
								<span id="tradesellTurnover">0</span>
								${systemTradeType.buyShortName }
							</div>
						</div>
						<div class="trade-group">
							<span id="sell-errortips" class="text-danger trade-error"></span>
							<c:if test="${type eq 2}">
								<span class="trade-price-tips" id="tradesellTurnoverTips">≈ 0.00 CNY</span>
							</c:if>
							<button id="sellbtn" class="btn btn-success btn-block trade-btn">
								<spring:message code="comm.sell" />${systemTradeType.sellShortName }</button>
						</div>
						<div class="trade-group">
							<a href="/deposit/coin_deposit.html?symbol=${systemTradeType.id }" class="text-success">
								<spring:message code="trade.recharge" />
								<i class="arrow-icon-small green"></i>
							</a>
						</div>
					</div>
				</div>
				<div class="trade-depth">
					<div class="trade-price">
						<spring:message code="comm.price" />
						<span id="lastprice" class="last-price">0</span>
						<span id="lastpriceicon" class="arrow-icon-big"></span>
					</div>
					<div class="list-group-box">
						<ul id="sellbox" class="list-group first-child"></ul>
					</div>
					<ul id="buybox" class="list-group "></ul>
				</div>

			</div>
		</div>
		<div class="trade-body">
			<div class="panel">
				<div class="panel-heading">
					<spring:message code="trade.current.entrust" />
					<span class="pull-right panel-open" data-type="0" data-value="0">
							<spring:message code="comm.unfurled" />
					</span>
				</div>
				<div class="panel-body trade-entruts" id="panelBody0">
					<div class="entruts-head">
							<span class="col-1">
								<spring:message code="trade.entrust.time" />
							</span>
						<span class="col-2">
								<spring:message code="trade.entrust.type" />
							</span>
						<span class="col-2">
								<spring:message code="trade.entrust.source" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.price" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.number" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.avgprice" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.succnumber" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.succprice" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.fee" />
							</span>
						<span class="col-4">
								<spring:message code="comm.status" />
							</span>
						<span class="col-4">
								<spring:message code="comm.operation" />
							</span>
					</div>
					<div class="entruts-body" id="entrutsCurData"></div>
					<div class="entruts-fot" id="entrutsCurFot">
						<c:if test="${!login }">
							<a class="btn btn-link" href="/user/login.html?forwardUrl=/trade/cny_coin.html">
								<spring:message code="trade.login" />
							</a>
						</c:if>
						<c:if test="${login }">
							<a class="btn btn-link" href="/trade/cny_entrust.html?symbol=${systemTradeType.id }&status=0">
								<spring:message code="trade.more" />
							</a>
						</c:if>
					</div>
				</div>
			</div>
			<div class="panel">
				<div class="panel-heading ">
					<spring:message code="trade.history.entrust" />
					<span class="pull-right panel-open" data-type="1" data-value="0">
							<spring:message code="comm.unfurled" />
						</span>
				</div>
				<div class="panel-body trade-entruts" id="panelBody1">
					<div class="entruts-head">
							<span class="col-1">
								<spring:message code="trade.entrust.time" />
							</span>
						<span class="col-2">
								<spring:message code="trade.entrust.type" />
							</span>
						<span class="col-2">
								<spring:message code="trade.entrust.source" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.price" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.number" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.avgprice" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.succnumber" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.succprice" />
							</span>
						<span class="col-3">
								<spring:message code="trade.entrust.fee" />
							</span>
						<span class="col-4">
								<spring:message code="comm.status" />
							</span>
						<span class="col-4">
								<spring:message code="comm.operation" />
							</span>
					</div>
					<div class="entruts-body" id="entrutsHisData"></div>
					<div class="entruts-fot" id="entrutsHisFot">
						<c:if test="${!login }">
							<a class="btn btn-link" href="/user/login.html?forwardUrl=/trade/cny_coin.html">
								<spring:message code="trade.login" />
							</a>
						</c:if>
						<c:if test="${login }">
							<a class="btn btn-link" href="/trade/cny_entrust.html?symbol=${systemTradeType.id }&status=1">
								<spring:message code="trade.more" />
							</a>
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="modal modal-custom fade" id="tradepass" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
		<div class="modal-mark"></div>
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<span class="modal-title" id="exampleModalLabel">
						<spring:message code="comm.trade.pwd" />
					</span>
			</div>
			<div class="modal-body form-horizontal">
				<div class="form-group">
					<div class="col-xs-3 control-label">
							<span>
								<spring:message code="comm.trade.pwd" />
								：
							</span>
					</div>
					<div class="col-xs-6 padding-clear">
						<input type="password" class="form-control" id="tradePwd"
							   placeholder="<spring:message code="trade.input.password"/>">
					</div>
				</div>
				<div class="form-group">
					<div class="col-xs-6 padding-clear col-xs-offset-3">
						<span id="errortips" class="error-msg text-danger"></span>
					</div>
				</div>
				<div class="form-group margin-bottom-clear">
					<div class="col-xs-6 padding-clear col-xs-offset-3">
						<button id="modalbtn" type="button" class="btn btn-danger btn-block">
							<spring:message code="comm.confirm" />
						</button>
					</div>
				</div>
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
<c:if test="${login }">
	<div class="modal modal-custom fade" id="priceclock" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-mark"></div>
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<span class="modal-title" id="exampleModalLabel">
							<spring:message code="trade.price.clock" />
						</span>
				</div>
				<div class="modal-body body-flex">
					<div class="price-text-warning">
						<spring:message code="trade.price.clock.desc" />
					</div>
					<div class="price-body">
						<div class="highprice">
							<div class="priceup-open">
								<div class="price-text">
										<span>
											<spring:message code="trade.price.clock.max" />
										</span>
								</div>
							</div>
							<div class="price-div">
								<input type="text" class="priceinput" value="${priceclock.fmaxprice }" id="highprice_${systemTradeType.id}" />
							</div>
						</div>
						<div class="lowprice">
							<div class="pricedown-open">
								<div class="price-text">
										<span>
											<spring:message code="trade.price.clock.min" />
										</span>
								</div>
							</div>
							<div class="price-div">
								<input type="text" class="priceinput" value="${priceclock.fminprice }" id="lowprice_${systemTradeType.id}" />
							</div>
						</div>
					</div>
					<div class="confirmPriceClock" id="confirmPriceClock_${systemTradeType.id}">
						<spring:message code="comm.confirm" />
					</div>
				</div>
			</div>
		</div>
	</div>
</c:if>
<%@include file="../comm/footer.jsp"%>
<input type="hidden" id="sellShortName" value="${systemTradeType.sellShortName}">
<input type="hidden" id="buyShortName" value="${systemTradeType.buyShortName}">
<input id="symbol" type="hidden" value="${systemTradeType.id }">
<input id="isopen" type="hidden" value="${needTradePasswd }">
<input id="tradeType" type="hidden" value="${type}">
<input id="tradePassword" type="hidden" value="${tradePassword }">
<input id="isTelephoneBind" type="hidden" value="${isTelephoneBind }">
<input id="tradeAmount" type="hidden" value="${systemTradeType.amountOffset }">
<input id="tradePrice" type="hidden" value="${systemTradeType.priceOffset }">
<input id="tradeBuyCoin" type="hidden" value="0">
<input id="tradeSellCoin" type="hidden" value="0">
<input id="cnyDigit" type="hidden" value="${cnyDigit }">
<input id="coinDigit" type="hidden" value="${coinDigit }">
<input id="login" type="hidden" value="${login }">
<input id="minCount" type="hidden" value="${systemTradeType.minCount}">
<input id="maxCount" type="hidden" value="${systemTradeType.maxCount}">
<input id="minPrice" type="hidden" value="${systemTradeType.minPrice}">
<input id="maxPrice" type="hidden" value="${systemTradeType.maxPrice}">
<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.jslider.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/highstock.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/trade/trade.js?v=20"></script>
</body>
</html>
