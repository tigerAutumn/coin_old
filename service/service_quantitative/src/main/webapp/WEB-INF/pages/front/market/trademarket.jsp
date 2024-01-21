<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/markt/trademarket.css" type="text/css"></link>
<link rel="stylesheet" href="${staticurl }/front/css/markt/loader.css" type="text/css"></link>
	<link href="${staticurl }/front/css/trade/trade.css" rel="stylesheet" type="text/css" media="screen, projection" />
</head>
<body style="background: #1e1e1e;">
	<div id="loader-wrapper">
		<div class="loader-inner ball-scale-ripple-multiple">
			<div></div>
			<div></div>
			<div></div>
		</div>
		<div class="loader-section section-left"></div>
		<div class="loader-section section-right"></div>
	</div>
	<div class="container-full market-head">
		<h1 class="head-logo">
			<a class="marketplist login" href="/"></a>
		</h1>
		<div class="head-nav">
			<c:forEach items="${tradeTypeList }" var="v">
				<a class="${v.id == symbol?'active':'' }" href="/trademarket.html?symbol=${v.id }"> ${v.sellShortName }/${v.buyShortName } </a>
			</c:forEach>
		</div>
		<div class="head-login">
			<c:if test="${userInfo == null  }">
				<input id="login_acc" placeholder="<spring:message code="market.input.username"></spring:message>" />
				<input id="login_pwd" type="password" placeholder="<spring:message code="market.input.password"></spring:message>" />
				<button id="login_sub"><spring:message code="market.login"></spring:message></button>
			</c:if>
			<c:if test="${userInfo != null }">
				<span><spring:message code="market.hello"></spring:message>，${(userInfo.frealname)==null?(userInfo.fnickname):(userInfo.frealname)}</span>
				<span>|</span>
				<a id="login_logout" href="/user/logout.html"><spring:message code="market.logout"></spring:message></a>
			</c:if>
		</div>
	</div>
	<div class="container-full clearfix">
		<div id="marketLeft" class="market-left">
			<div id="marketStart" class="market-start">
				<iframe frameborder="0" border="0" width="100%" height="100%" id="klineFullScreen" src="/kline/fullstart.html?symbol=${symbol }&themename=dark"></iframe>
			</div>
			<c:if test="${userInfo != null }">
				<div id="marketEntruts" class="market-entruts">
					<div id="entrutsHead" class="entruts-head">
						<span class="entruts-head-nav-full" data-show="entrutsCur" data-hide="entrutsHis"><spring:message code="market.current.entrust"></spring:message></span><span class="entruts-head-nav-full" data-show="entrutsHis" data-hide="entrutsCur"><spring:message code="trade.history.entrust"></spring:message></span>
					</div>
					<div class="entruts-data" id="entrutsCur">
						<div class="entruts-data-head">
							<span class="col-1"><spring:message code="trade.entrust.time"></spring:message></span>
							<span class="col-2"><spring:message code="trade.entrust.type"></spring:message></span>
							<span class="col-3"><spring:message code="trade.entrust.price"></spring:message></span>
							<span class="col-3"><spring:message code="trade.entrust.number"></spring:message></span>
							<span class="col-3"><spring:message code="market.entrust.succ"></spring:message></span>
							<span class="col-3"><spring:message code="comm.status"></spring:message></span>
							<span class="col-2"><spring:message code="comm.operation"></spring:message></span>
						</div>
						<div class="entruts-data-data" id="entrutsCurData"></div>
					</div>
					<div class="entruts-data" id="entrutsHis">
						<div class="entruts-data-head">
							<span class="col-1"><spring:message code="trade.entrust.time"></spring:message></span>
							<span class="col-2"><spring:message code="trade.entrust.type"></spring:message></span>
							<span class="col-3"><spring:message code="trade.entrust.price"></spring:message></span>
							<span class="col-3"><spring:message code="trade.entrust.number"></spring:message></span>
							<span class="col-3"><spring:message code="market.entrust.succ"></spring:message></span>
							<span class="col-3"><spring:message code="comm.status"></spring:message></span>
							<span class="col-2"><spring:message code="comm.operation"></spring:message></span>
						</div>
						<div class="entruts-data-data" id="entrutsHisData"></div>
					</div>
				</div>
			</c:if>
		</div>
		<div class="market-right">
			<div id="marketData" class="market-data">
				<div class="market-depth">
					<div class="market-depth-head">
						<span class="depth-des">&nbsp;</span><span class="depth-price"><spring:message code="market.price"/>&nbsp;${tradeType.buyShortName}</span><span class="depth-amount"><spring:message code="comm.number"/></span>
					</div>
					<div class="market-depth-data" id="marketDepthData">
						<div class="market-depth-price">
							<div class="market-depth-sell" id="marketDepthSell"></div>
							<div class="depth-price text-left">
								<spring:message code="market.lastestprice"/>&nbsp;<span id="marketPrice" class="market-font-sell">0.0000</span>
							</div>
							<div class="depth-price right">
								<spring:message code="market.range"/>&nbsp;<span class="market-font-sell" id="marketRose">0%</span>
							</div>
							<div class="market-depth-buy" id="marketDepthBuy"></div>
						</div>
					</div>

				</div>
				<div class="market-success">
					<div class="market-success-head">
						<span class="success-time"><spring:message code="market.time"></spring:message></span><span class="success-price"><spring:message code="market.price"></spring:message>&nbsp;${tradeType.buyShortName}</span><span class="success-amount"><spring:message code="comm.total"></spring:message></span>
					</div>
					<div class="market-success-data" id="marketSuccessData"></div>
				</div>
			</div>
			<c:if test="${userInfo != null }">
				<div id="marketTrade" class="market-trade">
					<div class="trade-table left">
						<div class="trade-tr">
							<span><spring:message code="market.free"/>：<span class="market-font-buy" id="totalCny">0</span>&nbsp${tradeType.buyShortName}
							</span>
						</div>
						<div class="trade-tr">
							<label for="buy-price" class="tr-tips"><spring:message code="comm.buy.price"/>${tradeType.buyShortName}</label>
							<input id="buy-price" />
						</div>
						<div class="trade-tr">
							<label for="buy-amount" class="tr-tips"><spring:message code="comm.buy.number"/>${tradeType.sellShortName }</label>
							<input id="buy-amount" />
						</div>
						<div class="trade-tr tr-boder tr-slider">
							<span id="buyBar" class="col-xs-12 buysellbar">
								<div class="buysellbar-box">
									<div id="buyslider" class="slider" data-role="slider" data-param-marker="marker" data-param-complete="complete" data-param-type="0"></div>
									<div class="slider-points">
										<div class="proportioncircle proportion0" data-points="0"></div>
										<div class="proportioncircle proportion1" data-points="25"></div>
										<div class="proportioncircle proportion2" data-points="50"></div>
										<div class="proportioncircle proportion3" data-points="75"></div>
										<div class="proportioncircle proportion4" data-points="100"></div>
									</div>
								</div>
							</span>
						</div>
						<div class="trade-tr tr-boder">
							<span class="tr-tips"><spring:message code="market.trade.amount"/>：</span><span class="tr-right"><span class="market-font-buy" id="buy-limit">0.0000</span>&nbsp;${tradeType.buyShortName}</span>
						</div>
						<div class="trade-tr tr-btn">
							<button id="buy_sub" class="buy"><spring:message code="comm.buy"/></button>
						</div>
					</div>
					<div class="trade-table right">
						<div class="trade-tr">
							<span><spring:message code="market.free"/>：<span class="market-font-sell" id="totalCoin">0</span>&nbsp${tradeType.sellShortName}
							</span>
						</div>
						<div class="trade-tr">
							<label for="sell-price" class="tr-tips"><spring:message code="comm.sell.price"/>${tradeType.buyShortName }</label>
							<input id="sell-price" />
						</div>
						<div class="trade-tr">
							<label for="sell-amount" class="tr-tips"><spring:message code="comm.sell.number"/>${tradeType.sellShortName}</label>
							<input id="sell-amount" />
						</div>
						<div class="trade-tr tr-boder tr-slider">
							<span id="sellBar" class="col-xs-12 buysellbar">
								<div class="buysellbar-box">
									<div id="sellslider" class="slider" data-role="slider" data-param-marker="marker sell-marker" data-param-complete="complete sell-complete" data-param-type="1"></div>
									<div class="slider-points">
										<div class="proportioncircle proportion0" data-points="0"></div>
										<div class="proportioncircle proportion1" data-points="25"></div>
										<div class="proportioncircle proportion2" data-points="50"></div>
										<div class="proportioncircle proportion3" data-points="75"></div>
										<div class="proportioncircle proportion4" data-points="100"></div>
									</div>
								</div>
							</span>
						</div>
						<div class="trade-tr tr-boder">
							<span class="tr-tips"><spring:message code="market.trade.amount"/>：</span><span class="tr-right"><span class="market-font-sell" id="sell-limit">0.0000</span>&nbsp;${tradeType.buyShortName}</span>
						</div>
						<div class="trade-tr tr-btn">
							<button id="sell_sub" class="sell"><spring:message code="comm.sell"/></button>
						</div>
					</div>
				</div>
			</c:if>
		</div>
	</div>
	<c:if test="${userInfo != null }">
		<div class="modal modal-custom fade" id="tradepass" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
			<div class="modal-dialog" role="document">
				<div class="modal-mark"></div>
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
						<span class="modal-title" id="exampleModalLabel"><spring:message code="comm.trade.pwd"/></span>
					</div>
					<div class="modal-body form-horizontal">
						<div class="form-group">
							<div class="col-xs-3 control-label">
								<span><spring:message code="comm.trade.pwd"/></span>
							</div>
							<div class="col-xs-6 padding-clear">
								<input type="password" class="form-control" id="tradePwd" placeholder="<spring:message code="market.input.trade.password"></spring:message>">
							</div>
						</div>
						<div class="form-group">
							<div class="col-xs-6 padding-clear col-xs-offset-3">
								<span id="errortips" class="error-msg text-danger"></span>
							</div>
						</div>
						<div class="form-group margin-bottom-clear">
							<div class="col-xs-6 padding-clear col-xs-offset-3">
								<button id="modalbtn" type="button" class="btn btn-danger btn-block"><spring:message code="comm.confirm"/></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</c:if>
	<input type="hidden" id="symbol" value="${tradeType.id }">
	<input type="hidden" id="coinshortName" value="${tradeType.sellShortName}">
	<input type="hidden" id="tradeAmount" value="${tradeType.amountOffset }">
	<input type="hidden" id="tradePrice" value="${tradeType.priceOffset }">
	<input id="cnyDigit" type="hidden" value="${cnyDigit }">
	<input id="coinDigit" type="hidden" value="${coinDigit }">
	<input id="tradePassword" type="hidden" value="${tradePassword }">
	<input id="isTelephoneBind" type="hidden" value="${isTelephoneBind }">
	<input id="isopen" type="hidden" value="${needTradePasswd }">
	<input id="tradeType" type="hidden" value="${type }">
	<input id="login" type="hidden" value="${login }">
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.min.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/bootstrap.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/layer/layer.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/comm/util.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/comm/comm.js"></script>
	<script type="text/javascript" src="${staticurl}/front/js/language/language_${requestScope.localeStr}.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.jslider.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/markt/trademarket2.js"></script>
</body>
</html>
