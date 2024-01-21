<%@ page language="java" pageEncoding="UTF-8" %>
<%@ include file="../comm/include.inc.jsp" %>
<!doctype html>
<html>
<head>
    <%@include file="../comm/link.inc.jsp" %>
</head>
<body>
<%@include file="../comm/header.jsp" %>
<div class="container-full body-main">
    <div class="container displayFlex">
        <div class="col-xs-2 leftmenu">
            <%@include file="../comm/left_menu.jsp" %>
        </div>
        <div class="col-xs-10 padding-right-clear">
            <div class="col-xs-12 padding-right-clear padding-left-clear rightarea">
                <div class="col-xs-12 rightarea-con">
                    <div class="col-xs-7 padding-clear form-horizontal">
                        <div class="form-group ">
                            <label class="col-xs-3 control-label"><spring:message code="financial.balance"/></label>
                            <div class="col-xs-6">
                                <span id="assettotal" class="form-control border-fff">${coinWallet.total }</span>
                            </div>
                        </div>
                        <div class="form-group ">
                            <label for="pushUid" class="col-xs-3 control-label"><spring:message code="financial.push.uid"/></label>
                            <div class="col-xs-6">
                                <input id="pushUid" class="form-control" type="text">
                            </div>
                        </div>
                        <div class="form-group ">
                            <label for="pushCoinId" class="col-xs-3 control-label"><spring:message code="financial.push.type"/></label>
                            <div class="col-xs-6">
                                <select id="pushCoinId" class="form-control">
                                    <c:forEach items="${pushCoinMap }" var="coin">
                                        <option value="${coin.key }">${coin.value }</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group ">
                            <label for="pushCount" class="col-xs-3 control-label"><spring:message code="comm.number"/></label>
                            <div class="col-xs-6">
                                <input id="pushCount" class="form-control" type="text" placeholder="<spring:message code="financial.number.min"/>">
                            </div>
                        </div>
                        <div class="form-group ">
                            <label for="pushPrice" class="col-xs-3 control-label"><spring:message code="financial.push.price"/></label>
                            <div class="col-xs-6">
                                <input id="pushPrice" class="form-control" type="text">
                            </div>
                        </div>
                        <div class="form-group ">
                            <label for="tradePwd" class="col-xs-3 control-label"><spring:message code="comm.trade.pwd"/></label>
                            <div class="col-xs-6">
                                <input id="tradePwd" class="form-control" type="password">
                            </div>
                        </div>
                        <c:if test="${userInfo.fistelephonebind==true }">
                            <div class="form-group">
                                <label for="phoneCode" class="col-xs-3 control-label"><spring:message code="comm.sms"/></label>
                                <div class="col-xs-6">
                                    <input id="phoneCode" class="form-control" type="text">
                                    <button id="sendmessage" data-msgtype="113" data-tipsid="pushError"
                                            class="btn btn-sendmsg"><spring:message code="comm.sms.send"/>
                                    </button>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${userInfo.fgooglebind==true }">
                            <div class="form-group">
                                <label for="googleCode" class="col-xs-3 control-label"><spring:message code="comm.google"/></label>
                                <div class="col-xs-6">
                                    <input id="googleCode" class="form-control" type="text">
                                </div>
                            </div>
                        </c:if>
                        <div class="form-group">
                            <label for="pushError" class="col-xs-3 control-label"></label>
                            <div class="col-xs-6">
                                <span id="pushError" class="text-danger"> </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="pushSubmit" class="col-xs-3 control-label"></label>
                            <div class="col-xs-6">
                                <button id="pushSubmit" class="btn btn-danger btn-block"><spring:message code="financial.push.submit"/></button>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-12 padding-clear padding-top-30">
                        <div class="panel border">
                            <div class="panel-heading">
                                <span class="text-danger"><spring:message code="financial.record"/></span>
                                <span class="pull-right recordtitle" data-type="0" data-value="0"><spring:message code="comm.shrink"/></span>
                            </div>
                            <div class="panel-body" id="recordbody0">
                                <table class="table">
                                    <tr>
                                        <td><spring:message code="financial.push.record.01"/></td>
                                        <td><spring:message code="financial.push.record.02"/></td>
                                        <td><spring:message code="financial.push.record.03"/></td>
                                        <td class="text-right"><spring:message code="comm.number"/></td>
                                        <td class="text-right"><spring:message code="financial.push.price"/></td>
                                        <td class="text-right"><spring:message code="comm.money"/></td>
                                        <td class="text-center"><spring:message code="financial.push.record.04"/></td>
                                        <td><spring:message code="comm.status"/></td>
                                        <td><spring:message code="comm.operation"/></td>
                                    </tr>
                                    <c:forEach items="${page.data }" varStatus="vs" var="v">
                                        <tr>
                                            <td>
                                                <c:if test="${v.fuid == userInfo.fshowid}">
                                                    <spring:message code="enum.financial.transfer.type1"/>
                                                </c:if>
                                                <c:if test="${v.fuid != userInfo.fshowid}">
                                                    <spring:message code="enum.financial.transfer.type0"/>
                                                </c:if>
                                            </td>
                                            <td>${v.fuid eq userInfo.fshowid ? v.fpushuid : v.fuid }</td>
                                            <td>${v.fcoin_s }</td>
                                            <td class="text-right">
                                                <fmt:formatNumber value="${v.fcount }" pattern="0.0000"
                                                                  maxFractionDigits="4" maxIntegerDigits="10"/>
                                            </td>
                                            <td class="text-right">
                                                <fmt:formatNumber value="${v.fprice }" pattern="0.0000"
                                                                  maxFractionDigits="4" maxIntegerDigits="10"/>
                                            </td>
                                            <td class="text-right">
                                                <fmt:formatNumber value="${v.famount }" pattern="0.0000"
                                                                  maxFractionDigits="4" maxIntegerDigits="10"/>
                                            </td>
                                            <td class="text-center">
                                                <fmt:formatDate value="${v.fcreatetime }"
                                                                pattern="yyyy-MM-dd HH:mm:ss"/>
                                            </td>
                                            <td>${v.fstate_s}</td>
                                            <td>
                                                <c:if test="${v.fstate eq 1 && v.fuid eq userInfo.fshowid}">
                                                    <a class="cancelPush opa-link" href="javascript:void(0);"
                                                       data-pushid="${v.fid }"> <spring:message code="comm.cancel"/> </a>
                                                </c:if>
                                                <c:if test="${v.fstate eq 1 && v.fuid ne userInfo.fshowid}">
                                                    <a class="payPush opa-link" href="javascript:void(0);"
                                                       data-pushid="${v.fid }" data-coins="${v.fcoin_s }"
                                                       data-count="<fmt:formatNumber value="${v.fcount }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />"
                                                       data-price="<fmt:formatNumber value="${v.fprice }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />"
                                                       data-amount="<fmt:formatNumber value="${v.famount }" pattern="0.0000" maxFractionDigits="4" maxIntegerDigits="10" />"
                                                       > <spring:message code="financial.push.record.05"/> </a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${fn:length(page.data)==0 }">
                                        <tr>
                                            <td colspan="9" class="no-data-tips">
                                                <span><spring:message code="financial.push.record.06"/> </span>
                                            </td>
                                        </tr>
                                    </c:if>
                                </table>
                            </div>
                        </div>
                        <c:if test="${not empty page.pagin }">
                            <input type="hidden" value="${page.currentPage }" name="type" id="currentPage"/>
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
<div class="modal modal-custom fade" id="pushModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-mark"></div>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close btn-modal" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <span class="modal-title" id="exampleModalLabel"><spring:message code="financial.push.modal.01"/></span>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group clearfix">
                    <div class="col-xs-4 control-label">
                        <span><spring:message code="financial.push.modal.02"/></span>
                    </div>
                    <div class="col-xs-8">
                        <span id="pushModalCoin" class="form-control border-fff"></span>
                    </div>
                </div>
                <div class="form-group clearfix">
                    <div class="col-xs-4 control-label">
                        <span><spring:message code="financial.push.modal.03"/></span>
                    </div>
                    <div class="col-xs-8">
                        <span id="pushModalPrice" class="form-control border-fff"></span>
                    </div>
                </div>
                <div id="coinshow" class="form-group clearfix">
                    <div class="col-xs-4 control-label">
                        <span><spring:message code="financial.push.modal.04"/></span>
                    </div>
                    <div class="col-xs-8">
                        <span id="pushModalCount" class="form-control border-fff"></span>
                    </div>
                </div>
                <div id="cnyshow" class="form-group clearfix">
                    <div class="col-xs-4 control-label">
                        <span><spring:message code="financial.push.modal.05"/></span>
                    </div>
                    <div class="col-xs-8">
                        <span id="pushModalAmount" class="form-control border-fff"></span>
                    </div>
                </div>
                <div class="form-group clearfix">
                    <div class="col-xs-4 control-label">
                        <span><spring:message code="comm.trade.pwd"/></span>
                    </div>
                    <div class="col-xs-5">
                        <input type="password" class="form-control" id="pushModalPassword" placeholder="<spring:message code="market.input.trade.password"/>"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-xs-4 control-label"></div>
                    <div class="col-xs-8 padding-left-clear">
                        <span id="pushModalError" class="text-danger"></span>
                    </div>
                </div>
                <div class="form-group margin-bottom-clear">
                    <div class="col-xs-4 control-label"></div>
                    <div class="col-xs-5 padding-left-clear">
                        <input type="hidden" class="form-control" id="pushModalid"/>
                        <button id="pushModalBtn" type="button" class="btn btn-danger btn-block"><spring:message code="comm.confirm.submit"/></button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<input type="hidden" value="${coinWallet.total }" id="btcbalance" name="btcbalance">
<%@include file="../comm/footer.jsp" %>
<script type="text/javascript" src="${staticurl }/front/js/comm/msg.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/finance/account.push.js"></script>
</body>
</html>
