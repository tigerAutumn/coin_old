<%@ page language="java" pageEncoding="UTF-8" %>
<%@ include file="comm/include.inc.jsp" %>
<!doctype html>
<html>
<head>
    <%@include file="comm/link.inc.jsp" %>
    <link rel="stylesheet" href="${staticurl}/front/css/index/index.css?v=22" type="text/css"></link>
    <script type="text/javascript" src="${staticurl }/front/js/plugin/echarts.min.js"></script>
    <script type="text/javascript" src="${staticurl }/front/js/plugin/pako.min.js"></script>
</head>
<body class="gray-bg">
<%@include file="comm/header.jsp" %>
<div class="container-full " style="position: relative">

    <div id="shuffling" class="carousel slide" data-ride="carousel">
        <ol class="carousel-indicators">
            <li data-target="#shuffling" data-slide-to="0" class="active"></li>
            <li data-target="#shuffling" data-slide-to="1"></li>
            <li data-target="#shuffling" data-slide-to="2"></li>
            <li data-target="#shuffling" data-slide-to="3"></li>
            <li data-target="#shuffling" data-slide-to="4"></li>
        </ol>
        <div class="carousel-inner">
            <a class="item active" target="_blank" href="${requestScope.constant.bigImage1url}"
               style="width: 100%;">
                <img src="${requestScope.constant.bigImage1 }" style=" width: 100%;"/>
            </a>
            <a class="item" target="_blank" href="${requestScope.constant.bigImage2url}"
               style=" width: 100%;">
                <img src="${requestScope.constant.bigImage2 }" style=" width: 100%;"/>
            </a>
            <a class="item" target="_blank" href="${requestScope.constant.bigImage3url}"
               style=" width: 100%;">
                <img src="${requestScope.constant.bigImage3 }" style=" width: 100%;"/>
            </a>
            <a class="item" target="_blank" href="${requestScope.constant.bigImage4url}"
               style=" width: 100%;">
                <img src="${requestScope.constant.bigImage4 }" style=" width: 100%;"/>
            </a>
            <a class="item" target="_blank" href="${requestScope.constant.bigImage5url}"
               style=" width: 100%;">
                <img src="${requestScope.constant.bigImage5 }" style=" width: 100%;"/>
            </a>
        </div>
    </div>
</div>
<div class="container-full">
    <div class="container news-box">
        <div class="new-item-box news-type">
            <div class="left">
                <span class="news-title">

                    <spring:message code="index.news.title"/>
                </span>
                <i class="news-arrows"></i>
            </div>
            <div class="right"></div>
        </div>
        <div class="new-item-box split-box"></div>
        <div class="new-item-box news-dis">
            <a href="/notice/detail.html?id=${headerarticles[0].value[0].fid}"><i class="right-arrows"></i>${headerarticles[0].value[0].fcontent_indexShort}</a>
        </div>
        <div class="new-item-box split-box"></div>
        <div class="new-item-box news-dis">
            <a href="/notice/detail.html?id=${headerarticles[0].value[1].fid}"><i class="right-arrows"></i>${headerarticles[0].value[1].fcontent_indexShort}</a>
        </div>
        <div class="new-item-box split-box"></div>
        <div class="new-item-box news-dis">
            <a href="/notice/detail.html?id=${headerarticles[0].value[2].fid}"><i class="right-arrows"></i>${headerarticles[0].value[2].fcontent_indexShort}</a>
        </div>
        <div class="new-item-box split-box"></div>
        <a href="/notice/index.html" class="new-item-box more-box">
            <span>MORE</span>
            <i class="icon"></i>
        </a>
    </div>
</div>
<div class="container-full">
    <div class="container market market-top">
        <div class="trade-navs">
            <div class="trade-tabs">
                <c:forEach items="${typeMap }" var="v" varStatus="vs">
                    <span data-market="marketType${v.key }" class="trade-tab ${v.key == typeFirst ? 'active' : '' }">
                        <spring:message code="trade.enum.tradetype${v.key }"/>
                        <i class="trigon"></i>
                    </span>
                </c:forEach>
            </div>
        </div>
        <article class="title">
            <div class="container market-item market-title">
                <span class="coin-name"><spring:message code="index.coin"/></span>
                <span class="coin-price"><spring:message code="index.lastestprice"/></span>
                <span class="coin-buy"><spring:message code="index.high.price"/></span>
                <span class="coin-sell"><spring:message code="index.low.price"/></span>
                <span class="coin-vol"><spring:message code="index.coinamount"/></span>
                <span class="coin-chg"><spring:message code="index.range"/></span>
                <span class="coin-trend"><spring:message code="index.price.trend"/></span>
            </div>
        </article>
    </div>
</div>
<div class="container-full">
    <c:forEach items="${typeMap }" var="v" varStatus="vs">
        <div class="container market market-con" id="marketType${v.key}"
             style="${v.key == typeFirst ? '' : 'display:none'}">
            <!-- 币种 -->
        </div>
    </c:forEach>
</div>
<div class="container-fluid service">
    <div class="row">
        <div class="container">
            <div class="row">
                <div class="col-xs-12 service-list">
                    <div class="col-xs-3 service-item">
                        <div class=" service-item-img">
                            <img src="${staticurl }/front/images/index/demo_01.jpg" alt="" />
                        </div>
                        <div class=" service-item-text">
                            <h3> <spring:message code="index.security.title"></spring:message> </h3>
                            <p><spring:message code="index.security.desc"></spring:message></p>
                        </div>
                    </div>
                    <div class="col-xs-3 service-item">
                        <div class=" service-item-img">
                            <img src="${staticurl }/front/images/index/demo_02.jpg" alt="" />
                        </div>
                        <div class=" service-item-text">
                            <h3> <spring:message code="index.service.title"></spring:message> </h3>
                            <p><spring:message code="index.service.desc"></spring:message></p>
                        </div>
                    </div>
                    <div class="col-xs-3 service-item">
                        <div class=" service-item-img">
                            <img src="${staticurl }/front/images/index/demo_03.jpg" alt="" />
                        </div>
                        <div class=" service-item-text">
                            <h3> <spring:message code="index.fast.title"></spring:message> </h3>
                            <p><spring:message code="index.fast.desc"></spring:message></p>
                        </div>
                    </div>
                    <div class="col-xs-3 service-item">
                        <div class=" service-item-img">
                            <img src="${staticurl }/front/images/index/demo_04.jpg" alt="" />
                        </div>
                        <div class=" service-item-text">
                            <h3> <spring:message code="index.fiery.title"></spring:message> </h3>
                            <p><spring:message code="index.fiery.desc"></spring:message></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="comm/footer.jsp" %>
<script type="text/javascript" src="${staticurl }/front/js/index/index.js?v=21"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.cookie.js"></script>
</body>
</html>