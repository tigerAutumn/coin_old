<%@ page language="java" pageEncoding="UTF-8" %>
<%@ include file="../comm/include.inc.jsp" %>
<!doctype html>
<html>
<head>
    <%@include file="../comm/link.inc.jsp" %>
    <link rel="stylesheet" href="${staticurl }/front/css/finance/recharge.css" type="text/css"/>
</head>
<body>
<%@include file="../comm/header.jsp" %>
<div class="container-full padding-top-40">
    <div class="container displayFlex">
        <div class="col-xs-2 leftmenu">
            <%@include file="../comm/left_menu.jsp" %>
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
                            <li>
                                <a href="/deposit/coin_deposit.html?symbol=${v.id }">${v.shortname } </a>
                            </li>
                        </c:forEach>
                    </ul>
                    <div class="col-xs-12 padding-clear padding-top-40">
                        <div class="recharge-box clearfix padding-top-30">
                            <div class="col-xs-12 padding-top-30">
                                <div id="rechage1" class="col-xs-7 padding-clear form-horizontal">
                                    <div class="form-group ">
                                        <label for="agency1" class="col-xs-3 control-label"><spring:message code="deposit.proxy.one"></spring:message></label>
                                        <div class="col-xs-6">
                                            <a id="agency1" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=3384327078&site=qq&menu=yes">
                                                <i class="icon-qq"></i>3384327078</a>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="agency2" class="col-xs-3 control-label"><spring:message code="deposit.proxy.two"></spring:message></label>
                                        <div class="col-xs-6">
                                            <a id="agency2" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=2742369241&site=qq&menu=yes">
                                                <i class="icon-qq"></i>2742369241</a>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="agency3" class="col-xs-3 control-label"><spring:message code="deposit.proxy.three"></spring:message></label>
                                        <div class="col-xs-6">
                                            <a id="agency3" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=1022265830&site=qq&menu=yes">
                                                <i class="icon-qq"></i>1022265830</a>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="agency4" class="col-xs-3 control-label"><spring:message code="deposit.proxy.four"></spring:message></label>
                                        <div class="col-xs-6">
                                            <a id="agency4" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=1874078024&site=qq&menu=yes">
                                                <i class="icon-qq"></i>1874078024</a>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="agency4" class="col-xs-3 control-label"><spring:message code="deposit.proxy.five"></spring:message></label>
                                        <div class="col-xs-6">
                                            <a id="agency5" class="link-box" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=3342023446&site=qq&menu=yes">
                                                <i class="icon-qq"></i>3342023446</a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-5 padding-clear text-center">
                                    <a target="_blank" href="/about/about.html?id=17" class="recharge-help"> </a>
                                </div>
                            </div>
                            <p>
                                <spring:message code="deposit.note.title"></spring:message>
                            </p>
                           <p>
                               <spring:message code="deposit.note"></spring:message>
                           </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="minRecharge" value="${minRecharge }"/>
<input type="hidden" id="maxRecharge" value="${maxRecharge }"/>
<input type="hidden" id="symbol" value="${coinType.id }"/>
<input type="hidden" id="rechargeType" value="${rechargeType }"/>
<%@include file="../comm/footer.jsp" %>
<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/finance/bankUrl.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/finance/account.recharge.js"></script>
<%--<script type="text/javascript" src="${staticurl }/front/js/finance/city.min.js"></script>--%>
<%--<script type="text/javascript" src="${staticurl }/front/js/finance/jquery.cityselect.js"></script>--%>
</body>
</html>
