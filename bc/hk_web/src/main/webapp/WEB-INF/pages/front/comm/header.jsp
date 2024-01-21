<%@ page language="java" pageEncoding="UTF-8" %>
<%@include file="include.inc.jsp" %>
<div id="allheader">
    <div class="container-full header clearfix ">
        <div class="container-full header-nav">
            <div class="container">
                <div class="col-xs-12">
                    <div class="navbar">
                        <div class="navbar-header navbar-default">
                            <a class="navbar-brand" href="/">
                                <img alt="${requestScope.constant.webName }" src="${requestScope.constant.logoImage }">
                            </a>
                        </div>
                        <div class="collapse navbar-collapse navbar-right">
                            <ul class="nav navbar-nav ">
                                <li class="${selectMenu=='index'?'active':'' }">
                                    <a href="/"><spring:message code="page.index"/></a>
                                </li>
                                <%--<li class="${selectMenu=='trade'?'active':'' }">--%>
                                    <%--<a href="/trade/cny_coin.html"><spring:message code="page.trade"/></a>--%>
                                <%--</li>--%>

                                <li class="${selectMenu=='market'?'active':'' }">
                                    <a href="/trademarket.html"><spring:message code="page.market"/></a>
                                </li>
                                <li class="${selectMenu=='financial'?'active':'' }">
                                    <a href="/deposit/coin_deposit.html"><spring:message code="page.financial"/></a>
                                </li>
                                <li class="${selectMenu=='security'?'active':'' }">
                                    <a href="/user/security.html"><spring:message code="page.user"/></a>
                                </li>
                            </ul>
                        </div>
                        <c:if test="${userInfo == null  }">
                            <div class="navbar-login">
                                <a href="/user/login.html" class="top-item"><spring:message code="index.login"/></a>
                                <span class="top-item padding-left-clear padding-right-clear">|</span>
                                <a href="/user/phonereg.html" class="top-item"><spring:message
                                        code="index.register"/></a>
                            </div>
                        </c:if>
                        <c:if test="${userInfo != null }">
                            <div class="navbar-login">
								<span class="top-item user-slide">
									<a href="/financial/index.html">Hi,
                                        <c:choose>
                                            <c:when test="${userInfo.frealname eq null}">
                                                ${fn:substring(userInfo.fnickname,0,3)}****${fn:substring(userInfo.fnickname,fn:length(userInfo.fnickname)-4,fn:length(userInfo.fnickname))}
                                            </c:when>
                                            <c:otherwise>
                                                ${userInfo.frealname}
                                            </c:otherwise>
                                        </c:choose>
                                    </a>
									<span class="caret"></span>
									<span class="slide-box">
										<div class="slide-box-mark"></div>
										<div class="slide-box-top">
											<span class="slide-con">
												<span class="loginname">${userInfo.floginname }</span>
												<span class="uid"> UID:${userInfo.fid } </span>
											</span>
											<a href="/user/security.html" class="btn btn-link pull-right slide-link">
												<spring:message code="page.setting"/>&gt;&gt;</a>
										</div>
										<div class="slide-box-con">
                                            <div id="assetsDetail" class="assets-detail">
                                                <ul class="first title clearfix">
                                                    <li class="col-xs-4 padding-left-clear">
                                                        <spring:message code="index.coin"/>
                                                    </li>
                                                    <li class="col-xs-4 text-center">
                                                        <spring:message code="index.free"/>
                                                    </li>
                                                    <li class="col-xs-4 text-center">
                                                        <spring:message code="index.frozen"/>
                                                    </li>
                                                </ul>
                                            </div>
											<div class="assets-btn">
												<a href="/deposit/coin_deposit.html" class="btn btn-danger btn-block">
                                                    <spring:message code="index.recharge.coin"/></a>
											</div>
										</div>
									</span>
								</span>
                                <span class="top-item padding-left-clear padding-right-clear">|</span>
                                <a href="/user/logout.html" class="top-item"><spring:message code="index.logout"/></a>
                            </div>
                        </c:if>
                        <div class="navbar-login clearfix">
                            <div class="lan-tabs">
                                <div class="lan-tab">
                                    <i class="lan-tab-${requestScope.localeStr }"></i>
                                </div>
                                <div class="lan-switch">
                                    <div id="zh_TW" data-lan="${requestScope.localeStr }" class="lan-tab lan-tab-hover">
                                        <span class="lan-des">中文</span>
                                        <i class="lan-tab-zh_TW"></i>
                                    </div>
                                    <div id="en_US" data-lan="${requestScope.localeStr }" class="lan-tab lan-tab-hover">
                                        <span class="lan-des">English</span>
                                        <i class="lan-tab-en_US"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>
