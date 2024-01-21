<%@ page language="java" pageEncoding="UTF-8"%>
<%@include file="include.inc.jsp"%>
<c:if test="${selectMenu == 'trade'}">
	<ul class="nav nav-pills nav-stacked ">
		<c:forEach items="${tradeTypeList }" var="v" varStatus="vs">
			<c:if test="${v.type eq type}">
				<span class="leftmenu-title leftmenu-folding ${vs.index==0? 'top':'' }" data-folding="trademenu${v.id }">
					<i class="lefticon" style="background: url('${v.sellWebLogo}') no-repeat 0 0;background-size:100% 100%;"></i>
					${v.sellShortName } / ${v.buyShortName}
				</span>
				<c:if test="${v.isShare }">
					<li class="${(tradeType.id==v.id && param.status eq null)? 'active':'' } trademenu${v.id }"
						style="display:${(param.tradeId==v.id || (leftMenu=='entrust' && param.symbol==v.id))?'':'none' }">
						<a href="/trade/cny_coin.html?tradeId=${v.id }&type=${v.type}">
							<spring:message code="leftmenu.buyorsell"/>
						</a>
					</li>
					<li class="${(leftMenu=='entrust' && param.status==0 && param.symbol==v.id)?'active':'' } trademenu${v.id }"
						style="display:${(param.tradeId==v.id || (leftMenu=='entrust' && param.symbol==v.id))?'':'none' }">
						<a href="/trade/cny_entrust.html?symbol=${v.id }&status=0">
							<spring:message code="leftmenu.entrust"/>
						</a>
					</li>
					<li class="${(leftMenu=='entrust' && param.status==1 && param.symbol==v.id)?'active':'' } trademenu${v.id }"
						style="display:${(param.tradeId==v.id || (leftMenu=='entrust' && param.symbol==v.id))?'':'none' }">
						<a href="/trade/cny_entrust.html?symbol=${v.id }&status=1">
							<spring:message code="leftmenu.tradingrecord"/>
						</a>
					</li>
				</c:if>
			</c:if>
		</c:forEach>
	</ul>
</c:if>
<c:if test="${selectMenu == 'financial' }">
	<ul class="nav nav-pills nav-stacked">
		<span class="leftmenu-title top">
			<i class="lefticon financial"></i>
			<spring:message code="financial.manage"/>
		</span>
		<li class="${leftMenu=='recharge'?'active':'' }">
			<a href="/deposit/coin_deposit.html"> <spring:message code="financial.recharge.menu"/></a>
		</li>
		<li class="${leftMenu=='withdraw'?'active':'' }">
			<a href="/withdraw/coin_withdraw.html"> <spring:message code="financial.withdraw.menu"/></a>
		</li>
		<li class="${leftMenu=='financial'?'active':'' }">
			<a href="/financial/index.html"> <spring:message code="financial.asset.menu"/></a>
		</li>
		<li class="${leftMenu=='record'?'active':'' }">
			<a href="/financial/record.html"> <spring:message code="financial.bill.menu"/></a>
		</li>
		<li class="${leftMenu=='accountassets'?'active':'' }">
			<a href="/financial/accountcoin.html"> <spring:message code="financial.bank.menu"/></a>
		</li>
		<li class="${leftMenu=='finances'?'active':'' }">
			<a href="/financial/finances.html"><spring:message code="financial.finances.menu"/></a>
		</li>
		<li class="${leftMenu=='commission'?'active':'' }">
			<a href="/financial/commission.html"><spring:message code="financial.finances.income"/></a>
		</li>
	</ul>
</c:if>
<c:if test="${selectMenu == 'security'}">
	<ul class="nav nav-pills nav-stacked">
		<span class="leftmenu-title top">
			<i class="lefticon security"></i>
			<spring:message code="security.center"/>
		</span>
		<li class="${leftMenu=='security'?'active':'' }">
			<a href="/user/security.html"> <spring:message code="security.setting"/></a>
		</li>
		<li class="${leftMenu=='userlog'?'active':'' }">
			<a href="/user/user_loginlog.html"> <spring:message code="security.login.record"/></a>
		</li>
		<%--<span class="leftmenu-title">
			<i class="lefticon user"></i>
			<spring:message code="user.center"/>
		</span>--%>
		<li class="${leftMenu=='activity'?'active':'' }">
			<a href="/activity/index.html"><spring:message code="security.recharge.code"/></a>
		</li>
	</ul>
</c:if>