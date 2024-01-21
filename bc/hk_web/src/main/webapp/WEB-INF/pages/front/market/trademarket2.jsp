<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<title>币币交易</title>
	<%@include file="../comm/link.inc.jsp"%>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.min.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/echarts.min.js?v=20"></script>
	<script type="text/javascript" src="${staticurl }/front/js/plugin/pako.min.js?v=20"></script>

	<link href="${staticurl }/front/css/markt/trade.css?v=13" rel="stylesheet" type="text/css" media="screen, projection" />
	<link href="${staticurl }/front/css/markt/trade2.css?v=13" rel="stylesheet" type="text/css" media="screen, projection" />

</head>
<body>
<div class="main">
	<%@include file="../comm/header.jsp"%>
	<div class="top">
		<%--<ul class="top-section clear">--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=ltc_btc">LTC/BTC</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=bch_btc">BCH/BTC</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=eth_btc">ETH/BTC</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=etc_btc">ETC/BTC</a>--%>
			<%--</li>--%>

			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=btm_btc">BTM/BTC</a>--%>
			<%--</li>--%>

			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=ltc_usdt">LTC/USDT</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=bch_usdt">BCH/USDT</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=btc_usdt">BTC/USDT</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=eth_usdt">ETH/USDT</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=etc_usdt">ETC/USDT</a>--%>
			<%--</li>--%>
			<%--<li class="top-section-item fl">--%>
				<%--<a href="/trademarket.html?sb=btm_usdt">BTM/USDT</a>--%>
			<%--</li>--%>

		<%--</ul>--%>
	</div>
	<div class="content clear">
		<div class="box leftbar">
			<div class="header-text header-shadow">交易市场</div>
			<div class="coin-tab">
				<c:forEach items="${typeMap }" var="v">
					<span class="${v.key eq type ? 'tab-active':''}">${v.value}</span>
				</c:forEach>
			</div>
			<div class="coin-list-title">
				<span>币种</span>
				<span>最新价</span>
				<span>涨幅</span>
			</div>
			<div class="coin-list">

				<c:forEach items="${tradeTypeListMap}" var="v">
					<ul class="coin-list-item" style="display:${v.key eq type ? 'block' : 'none'};">
						<c:forEach items="${v.value}" var="treadItem" varStatus="vs">
							<%--<c:if test="${treadItem.type eq v.key}">--%>
								<%--<a href="/trade/cny_coin.html?tradeId=${treadItem.id}&type=${treadItem.type}">--%>
									<%--<i class="lefticon" style="background: url('${treadItem.sellWebLogo}') no-repeat 0 0;background-size:100% 100%;"></i>--%>
									<%--<span>${treadItem.sellShortName } / ${treadItem.buyShortName}</span>--%>
								<%--</a>--%>
							<%--</c:if>--%>
							<li data-symbol='${treadItem.sellShortName}_${treadItem.buyShortName}' data-symbol-id="${treadItem.id}" type="${treadItem.type}" symbol="${treadItem.id}">
								<div data-status="${treadItem.status eq 1}" class="trade-cell ${treadItem.sellShortName}_${treadItem.buyShortName} ${treadItem.sellShortName}${treadItem.buyShortName}">
									<span class="trade-symbol">${treadItem.sellShortName}</span>
									<span class="trade-price"></span>
									<span class="trade-rate"></span>
								</div>
							</li>
						</c:forEach>
					</ul>
				</c:forEach>

				<%--<ul class="coin-list-item" style="display: none;">--%>
					<%--<li data-symbol='btc_usdt'>--%>
						<%--<div class="trade-cell btc_usdt">--%>
							<%--<span class="trade-symbol">BTC</span>--%>
							<%--<span class="trade-price"></span>--%>
							<%--<span class="trade-rate"></span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='bch_usdt'>--%>
						<%--<div class="trade-cell bch_usdt">--%>
							<%--<span class="trade-symbol">BCH</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='ltc_usdt'>--%>
						<%--<div class="trade-cell ltc_usdt">--%>
							<%--<span class="trade-symbol">LTC</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='eth_usdt'>--%>
						<%--<div class="trade-cell eth_usdt">--%>
							<%--<span class="trade-symbol">ETH</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='etc_usdt'>--%>
						<%--<div class="trade-cell etc_usdt">--%>
							<%--<span class="trade-symbol">ETC</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='btm_usdt'>--%>
						<%--<div class="trade-cell btm_usdt">--%>
							<%--<span class="trade-symbol">BTM</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
				<%--</ul>--%>

				<%--<ul class="coin-list-item" style="display: none;">--%>
					<%--<li data-symbol='bch_btc'>--%>
						<%--<div class="trade-cell bch_btc">--%>
							<%--<span class="trade-symbol">BCH</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='eth_btc'>--%>
						<%--<div class="trade-cell eth_btc">--%>
							<%--<span class="trade-symbol">ETH</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='etc_btc'>--%>
						<%--<div class="trade-cell etc_btc">--%>
							<%--<span class="trade-symbol">ETC</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
					<%--<li data-symbol='btm_btc'>--%>
						<%--<div class="trade-cell btm_btc">--%>
							<%--<span class="trade-symbol">BTM</span>--%>
							<%--<span class="trade-price">0.0000</span>--%>
							<%--<span class="trade-rate">0%</span>--%>
						<%--</div>--%>
					<%--</li>--%>
				<%--</ul>--%>
			</div>
		</div>
		<div class="rightbar">
			<div class="box chart">
				<div class="chart-head header-shadow">
					<ul class="ticker-wrap clear">
                        <li class="fl" id="tip-sell-buy">${sell}/${buy}</li>
						<li class="fl" id="tip-price"></li>
						<li class="fl" id="tip-cny"></li>
						<li class="fl" id="zf"> <span></span></li>
						<li class="fl" id="high"></li>
						<li class="fl" id="low"></li>
						<li class="fl" id="vol"></li>
					</ul>
				</div>
				<div class="chart-panel">
					<ul class="clear">
						<li>1min</li>
						<li>5min</li>
						<li class="time-selected">15min</li>
						<li>30min</li>
						<li>60min</li>
						<li>1day</li>
					</ul>
				</div>
				<div class="chart-wrap">
					<div class="chart-box" id="chart-box">
					</div>
					<div class="chart-legend n" style="display: none;">
								<span class="chart-legend-item-wrap">
									<span class="chart-legend-item-title">开= </span>
									<span class="chart-legend-item-value"></span>
								</span>

						<span class="chart-legend-item-wrap">
									<span class="chart-legend-item-title">高= </span>
									<span class="chart-legend-item-value"></span>
								</span>

						<span class="chart-legend-item-wrap">
									<span class="chart-legend-item-title">低= </span>
									<span class="chart-legend-item-value"></span>
								</span>

						<span class="chart-legend-item-wrap">
									<span class="chart-legend-item-title">收= </span>
									<span class="chart-legend-item-value"></span>
								</span>
					</div>
				</div>
			</div>

			<div class="content-bottom">
				<div class="trade-depth-container box">
					<div class="header-text header-shadow">立即交易</div>
					<div class="trader clear">
						<div class="panel">
							<div class="panel-content">
								<div class=trade-group>
								<span>
									<spring:message code="trade.free" />
								</span>
									<span id="tradeBuyInt" class="redtips font-size-18">0</span>
									<span id="tradeBuyDig" class="redtips">.0000</span>
									<span class="redtips">${tradeType.buyShortName }</span>
								</div>

								<div class="input-text">
									<span class="label">买入价</span>
									<label>
										<input id="tradebuyprice" autocomplete="off" type="text" name="price" maxlength="14">
										<span class="upper unit">${buy}</span>
									</label>
									<p class="math-price" >
										<em id="tradebuyprice-cny">≈0.00 cny</em>
									</p>
								</div>
								<div class="input-text">
									<span class="label">买入量</span>
									<label>
										<input id="tradebuyamount" autocomplete="off" type="text" name="price" maxlength="14">
										<span class="upper unit">${sell}</span>
									</label>
								</div>
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
								<div class="total" id="buy-total">
									<p>交易额 <span id="tradebuyTurnover">0.00000000 ${buy}</span>
									<p id="buy-errortips">
								</div>
								<div class="submit">
									<button id="buybtn" class="btn-buy">买入${sell}</button>
								</div>
							</div>
						</div>
						<div class="panel">
							<div class="panel-content">

								<div class="trade-group">
								<span>
									<spring:message code="trade.free" />
								</span>
									<span id="tradeCoinInt" class="greentips font-size-18">0</span>
									<span id="tradeCoinDig" class="greentips">.0000</span>
									<span class="greentips">${tradeType.sellShortName }</span>
								</div>
								<div class="input-text">
									<span class="label">卖出价</span>
									<label>
										<input id="tradesellprice" autocomplete="off" type="text" name="price" maxlength="14">
										<span class="upper unit">${buy}</span>
									</label>
									<p class="math-price">
										<em id="tradesellprice-cny">≈ 0.00 cny</em>
									</p>
								</div>
								<div class="input-text">
									<span class="label">卖出量</span>
									<label>
										<input id="tradesellamount" autocomplete="off" type="text" name="price" maxlength="14">
										<span class="upper unit">${sell}</span>
									</label>
								</div>
								<div id="sellBar" class="trade-bar">
									<div class="trade-barbox">
										<div id="sellslider" class="slider" data-role="slider" data-param-marker="marker" data-param-complete="complete"
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

								<div class="total" id="buy_total">
									<p>交易额 <span id="tradesellTurnover">0.00000000 ${buy}</span></p>
									 <p id="sell-errortips"></p>
								</div>
								<div class="submit">
									<button id="sellbtn" class="btn-buy">卖出${sell}</button>
								</div>
							</div>
						</div>
					</div>
				</div>

				<div class="mark-depth box">
					<div class="header-text header-shadow"> <span></span></div>
					<div class="mark-mod">
						<dl>
							<dt>
								<div class="inner">
									<span class="title"> </span>
									<span id="inner-price">价格(${buy})</span>
									<span id="inner-amount">数量(${sell})</span>
									<span id="inner-sum">合计(${buy})</span>
								</div>
							</dt>
						</dl>
						<dl>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 8</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 7</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 6</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 5</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 4</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 3</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 2</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-sell">卖 1</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
						</dl>
						<div class="hl-hr">
							<hr>
							</hr>
						</div>
						<dl>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 1</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 2</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 3</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 4</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 5</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 6</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 7</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
							<dd class="cell">
								<div class="inner">
									<span class="title color-buy">买 8</span>
									<span></span>
									<span></span>
									<span></span>
								</div>
							</dd>
						</dl>
					</div>
				</div>
			</div>

			<div class="trade-body">
				<div class="panel1 box">
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
								<a class="btn btn-link" href="/trade/cny_entrust.html?symbol=${tradeType.id }&status=0">
									<spring:message code="trade.more" />
								</a>
							</c:if>
						</div>
					</div>
				</div>
				<div class="panel1 box">
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
								<a class="btn btn-link" href="/trade/cny_entrust.html?symbol=${tradeType.id }&status=1">
									<spring:message code="trade.more" />
								</a>
							</c:if>
						</div>
					</div>
				</div>
			</div>

			<div class="market-depth box">
				<div class="header-text header-shadow">深度图</div>
				<div id="depth-chart" class="depth-chart" style="width: 100%;height:472px">
				</div>
			</div>
			<div class="trade-real-container box">
				<div class="header-text header-shadow">实时成交</div>
				<div class="trade-bd">
					<div class="market-trades-list" id="market-trades-list">
						<dl class="market-trades-time">
							<dt>时间</dt>
						</dl>
						<dl class="market-trades-type">
							<dt>方向</dt>
						</dl>
						<dl class="market-trades-price">
							<dt>价格(${buy})</dt>
						</dl>
						<dl class="market-trades-amount">
							<dt>数量(${sell})</dt>
						</dl>
					</div>
				</div>
			</div>


		</div>
	</div>
</div>

<!-- 交易密码 -->
<%--<c:if test="${login }">--%>
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
<%--</c:if>--%>

<div class="modal modal-custom fade" id="entrustsdetail" tabindex="-1" role="dialog"
	 aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document" style="color: black">
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

<input type="hidden" id="sellShortName" value="${tradeType.sellShortName}">
<input type="hidden" id="buyShortName" value="${tradeType.buyShortName}">
<input type="hidden" name="" id="hide-symbol" value="${sb}"> <!-- btc_usdt -->
<input type="hidden" id="symbol" value="${tradeType.id}">
<input type="hidden" id="coinshortName" value="${tradeType.sellShortName}">
<input type="hidden" id="tradeAmount" value="${tradeType.amountOffset}">
<input type="hidden" id="tradePrice" value="${tradeType.priceOffset}">
<input id="cnyDigit" type="hidden" value="${cnyDigit}">
<input id="coinDigit" type="hidden" value="${coinDigit}">
<input id="tradePassword" type="hidden" value="${tradePassword}">
<input id="isTelephoneBind" type="hidden" value="${isTelephoneBind}">
<input id="isopen" type="hidden" value="${needTradePasswd}">
<input id="tradeType" type="hidden" value="${type}">
<input id="login" type="hidden" value="${login}">
<input id="isPlatformStatus" type="hidden" value="${isPlatformStatus}">
<input id="tradeBuyCoin" type="hidden" value="0">
<input id="tradeSellCoin" type="hidden" value="0">

<%@include file="../comm/footer.jsp"%>

<script type="text/javascript" src="${staticurl }/front/js/comm/ExchangeRate.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/comm/util.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/comm/comm.js?v=20"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/bootstrap.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/layer/layer.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/language/language_${requestScope.localeStr}.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/markt/HandleMarket.js?v=24"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.jslider.js?v=24"></script>
<script type="text/javascript" src="${staticurl }/front/js/markt/trademarket2.js?v=23"></script>
</body>

<script type="text/javascript"></script>
</html>