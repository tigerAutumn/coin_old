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
                                        <label for="blankId" class="col-xs-3 control-label">
                                            <spring:message code="financial.deposit.bank"/>
                                        </label>
                                        <div class="col-xs-6">
                                            <select id="blankId" class="form-control">
                                                <c:forEach items="${userBankinfo }" var="v">
                                                    <c:if test="${v.init }">
                                                        <option value="${v.fid }" data-fid="${v.fid}"
                                                                data-bankmodify="${(v.fprov==null && v.fcity==null && v.ftype == 0)? 1 : 2}"
                                                                data-banknumber="${v.fbanknumber}"
                                                                data-bankinfo="${v.fbanktype}"
                                                                value="${v.fid }"><spring:message
                                                                code="account.withdraw.balcktips"
                                                                arguments="${v.fname },${fn:substring(v.fbanknumber,fn:length(v.fbanknumber)-4,fn:length(v.fbanknumber)) }"
                                                                argumentSeparator=","/></option>
                                                    </c:if>
                                                </c:forEach>
                                            </select>
                                            <a href="javascript:void(0)" class="text-primary addtips"
                                               data-toggle="modal" data-target="#userBankInfo">
                                                <spring:message code="account.withdraw.balckadd"/>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="amount" class="col-xs-3 control-label"><spring:message
                                                code="financial.deposit.amount"/></label>
                                        <div class="col-xs-6">
                                            <input id="amount" class="form-control" type="text">
                                            <input type="hidden" value="0.${randomDecimal }" id="random">
                                            <label id="randomShow" for="amount"
                                                   class="control-label randomtips">.${randomDecimal }</label>
                                        </div>
                                    </div>
                                    <div class="form-group ">
                                        <label for="telephone" class="col-xs-3 control-label"><spring:message
                                                code="financial.deposit.phone"/></label>
                                        <div class="col-xs-6">
                                            <input id="telephone" class="form-control" type="text" value="${telephone}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="rechargebtn" class="col-xs-3 control-label"></label>
                                        <div class="col-xs-6">
                                            <button id="rechargebtn" class="btn btn-danger btn-block"><spring:message
                                                    code="financial.confirm.recharge"/></button>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-5 padding-clear text-center">
                                    <a target="_blank" href="/about/about.html?id=17" class="recharge-help"> </a>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-12 padding-clear padding-top-30">
                            <div class="panel panel-tips">
                                <div class="panel-header text-center text-danger">
                                    <span class="panel-title"><spring:message code="financial.recharge.notice"/></span>
                                </div>
                                <div class="panel-body">
                                    <p>&lt <spring:message code="financial.recharge.notice8"/></p>
                                    <p>&lt <spring:message code="financial.recharge.notice9"/></p>
                                    <p>&lt <spring:message code="financial.recharge.notice7"
                                                           arguments="support@hotcoin.pro"/></p>
                                    <p>&lt <spring:message code="financial.recharge.notice2"
                                                           arguments="${minRecharge },${maxRecharge}"
                                                           argumentSeparator=","/></p>
                                    <p>&lt <spring:message code="financial.recharge.notice12"/></p>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-12 padding-clear padding-top-30" id="rechargeTab">
                            <div class="panel border">
                                <div class="panel-heading">
                                    <span class="text-danger"><spring:message
                                            code="financial.cny.recharge.record"/></span>
                                    <span class="pull-right recordtitle" data-type="0" data-value="0">
                                        <spring:message code="comm.shrink"/></span>
                                </div>
                                <div class="panel-body" id="recordbody0">
                                    <table class="table">
                                        <tr>
                                            <td><spring:message code="financial.recharge.id"/></td>
                                            <td><spring:message code="financial.recharge.time"/></td>
                                            <td><spring:message code="financial.recharge.type"/></td>
                                            <td><spring:message code="financial.recharge.money"/></td>
                                            <td><spring:message code="comm.status"/></td>
                                            <td><spring:message code="comm.operation"/></td>
                                        </tr>
                                        <c:forEach items="${page.data}" var="v">
                                            <tr>
                                                <td class="gray">${v.fid }</td>
                                                <td><fmt:formatDate value="${v.fcreatetime }"
                                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                                <td><spring:message code="enum.capital.operation.type${v.ftype}"/></td>
                                                <td>${v.famount }</td>
                                                <td><spring:message
                                                        code="enum.capital.operation.status${v.fstatus}"/></td>
                                                <td class="opa-link"><c:if test="${v.fstatus==2 }">
                                                    <a class="look opa-link" href="javascript:void(0);"
                                                       data-bankid="${v.fsysbankid }" data-amount="${v.famount }"
                                                       data-bankName="${v.fbank }">
                                                        <spring:message code="financial.account.completioninfo"/>
                                                    </a>
                                                </c:if></td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${fn:length(page.data)==0 }">
                                            <tr>
                                                <td class="no-data-tips" colspan="6"><span><spring:message
                                                        code="financial.account.ordernodata"/>
													</span></td>
                                            </tr>
                                        </c:if>
                                    </table>
                                </div>
                            </div>
                            <c:if test="${!empty(page.pagin) }">
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
<div class="modal modal-custom fade" id="userBankInfo" tabindex="-1" role="dialog"
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
						<spring:message code="account.withdraw.modal1"/>
					</span>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group ">
                    <label for="payeeAddr" class="col-xs-3 control-label">
                        <spring:message code="account.withdraw.modal2"/>
                    </label>
                    <div class="col-xs-8">
                        <input id="payeeAddr" class="form-control" type="text" value="${realname }"
                               readonly="readonly"/>
                        <span class="help-block text-danger">
								<spring:message code="account.withdraw.modal3"/>
							</span>
                    </div>
                </div>
                <div class="form-group ">
                    <label for="withdrawAccountAddr" class="col-xs-3 control-label">
                        <spring:message code="account.withdraw.modal4"/>
                    </label>
                    <div class="col-xs-8">
                        <input id="withdrawAccountAddr" class="form-control">
                    </div>
                </div>
                <div class="form-group ">
                    <label for="withdrawAccountAddr2" class="col-xs-3 control-label">
                        <spring:message code="account.withdraw.modal5"/>
                    </label>
                    <div class="col-xs-8">
                        <input id="withdrawAccountAddr2" class="form-control">
                    </div>
                </div>
                <div class="form-group ">
                    <label for="openBankTypeAddr" class="col-xs-3 control-label">
                        <spring:message code="account.withdraw.modal6"/>
                    </label>
                    <div class="col-xs-8">
                        <select id="openBankTypeAddr" class="form-control">
                            <option value="-1">
                                <spring:message code="account.withdraw.modal7"/>
                            </option>
                            <c:forEach items="${systemBankinfoWithdraws }" var="v">
                                <option value="${v.fid }">${v.fcnname}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div id="prov_city" class="form-group withdraw">
                    <label for="prov" class="col-xs-3 control-label">
                        <spring:message code="account.withdraw.modal8"/>
                    </label>
                    <div class="col-xs-8 ">
                        <%--<div class="col-xs-4 padding-right-clear padding-left-clear margin-bottom-15">--%>
                            <%--<select id="prov" class="form-control prov">--%>
                            <%--</select>--%>
                        <%--</div>--%>
                        <%--<div class="col-xs-4 padding-right-clear margin-bottom-15">--%>
                            <%--<select id="city" class="form-control prov">--%>
                            <%--</select>--%>
                        <%--</div>--%>
                        <%--<div class="col-xs-4 padding-right-clear margin-bottom-15">--%>
                            <%--<select id="dist" class="form-control prov">--%>
                            <%--</select>--%>
                        <%--</div>--%>
                        <div class="col-xs-12 padding-right-clear padding-left-clear">
                            <input id="address" class="form-control" type="text"
                                   placeholder="<spring:message code="account.withdraw.modal9" />"/>
                        </div>
                    </div>
                </div>
                <c:if test="${isTelephone }">
                    <div class="form-group">
                        <label for="addressPhoneCode" class="col-xs-3 control-label">
                            <spring:message code="comm.sms"/>
                        </label>
                        <div class="col-xs-8">
                            <input id="addressPhoneCode" class="form-control" type="text">
                            <button id="bindsendmessage" data-msgtype="110" data-tipsid="binderrortips"
                                    class="btn btn-sendmsg">
                                <spring:message code="comm.sms.send"/>
                            </button>
                        </div>
                    </div>
                </c:if>
                <c:if test="${isGoogle }">
                    <div class="form-group">
                        <label for="addressTotpCode" class="col-xs-3 control-label">
                            <spring:message code="comm.google"/>
                        </label>
                        <div class="col-xs-8">
                            <input id="addressTotpCode" class="form-control" type="text">
                        </div>
                    </div>
                </c:if>
                <div class="form-group margin-bottom-clear">
                    <label for="withdrawCnyAddrBtn" class="col-xs-3 control-label"></label>
                    <div class="col-xs-8">
                        <button id="withdrawCnyAddrBtn" class="btn btn-danger btn-block">
                            <spring:message code="account.withdraw.modal10"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="modal modal-custom fade" id="orderInfo" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-mark"></div>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <span class="modal-title">
						<spring:message code="financial.deposit.order.title"/>
                </span>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group">
                    <p class="text-center">
                        <spring:message code="financial.deposit.order.tips1"/></p>
                </div>
                <div class="form-group">
                    <div class="col-xs-5 col-xs-offset-1 text-right top">
                        <label class="control-label">
                            <spring:message code="financial.payee"/>
                        </label>
                    </div>
                    <div class="col-xs-5 top">
                        <span id="bankPayee" class="form-control border-fff"></span>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-5 col-xs-offset-1 text-right">
                        <label class="control-label">
                            <spring:message code="financial.payee.no"/>
                        </label>
                    </div>
                    <div class="col-xs-5">
                        <span id="bankNo" class="form-control border-fff"></span>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-5 col-xs-offset-1 text-right">
                        <label class="control-label">
                            <spring:message code="financial.payee.bank"/>
                        </label>
                    </div>
                    <div class="col-xs-5">
                        <span id="bankAddress" class="form-control border-fff"></span>
                        <span id="bankName" class="hide"></span>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-5 col-xs-offset-1 text-right">
                        <label class="control-label">
                            <spring:message code="financial.payee.money"/>
                        </label>
                    </div>
                    <div class="col-xs-5">
                        <span id="bankAmount" class="form-control border-fff text-danger"></span>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-5 col-xs-offset-1 text-right">
                        <label class="control-label">
                            <spring:message code="financial.payee.remark"/>
                        </label>
                    </div>
                    <div class="col-xs-5">
                        <span id="bankRemark" class="form-control border-fff group-tips"></span>
                        <span class="form-control border-fff group-tips">
                            <spring:message code="financial.deposit.order.tips2"/></span>
                    </div>
                </div>
                <div class="form-group margin-bottom-clear btn-group">
                    <label class="col-xs-3 control-label"></label>
                    <span class="col-xs-6">
                        <button id="jumpLink" class="btn btn-danger btn-block">
                            <spring:message code="financial.deposit.order.tips3"/>
                        </button>
                        <p class="help-block text-center"><spring:message code="financial.deposit.order.tips4"></spring:message></p>
                    </span>
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
