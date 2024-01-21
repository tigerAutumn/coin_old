<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/finance/accountrecord.css" type="text/css"></link>
<link rel="stylesheet" href="${staticurl }/front/js/plugin/layer/skin/bitdate/bitDate.css" type="text/css"></link>

	<style type="text/css">
		#intro-url,#copy-intro{
			outline: none;
			text-decoration: none;
		}

		#intro-url{
			border-radius: 4px;
			height: 20px;
			font-weight: 400;
			border:1px white solid;
			font-size: 20px;
		}

		#copy-intro{
			cursor: pointer;
			color: #f50d0d;
			font-weight: 400;
			font-size: 20px;
		}
	</style>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full padding-top-40">
		<div class="container displayFlex">
			<div class="col-xs-2 leftmenu">
				<%@include file="../comm/left_menu.jsp"%>
			</div>
			<div class="col-xs-10 padding-right-clear">
				<div class="col-xs-12 padding-right-clear padding-left-clear padding-top-30 rightarea record">
					<input id="intro-url" value="${introurl}">
					<button id="copy-intro" data-clipboard-target="#intro-url" >复制推广链接</button>
					<div class="col-xs-12 rightarea-con">
						<table class="table table-striped">
								<tr class="bg-gary">
									<th width="200">
										<spring:message code="financial.account.commissiontime" />
									</th>
									<th width="160">
										<spring:message code="financial.account.commissionamount" />
									</th>
									<th width="120">
										<spring:message code="financial.account.commissioncoin" />
									</th>
									<th width="120">
										<spring:message code="comm.status" />
									</th>
								</tr>
							<c:forEach items="${list.data }" var="v">
										<tr>
											<td>
												<fmt:formatDate value="${v.createtime }" pattern="yyyy-MM-dd HH:mm:ss" />
											</td>
											<td >
													${v.amount }
											</td>
											<td>
													${v.coinname }
											</td>
											<td style="">${v.status_s}</td>
										</tr>
							</c:forEach>

							<c:if test="${fn:length(list.data)==0 }">
								<tr>
									<td colspan="7" class="no-data-tips">
										<span>
											<spring:message code="financial.account.commissionnodata" />
										</span>
									</td>
								</tr>
							</c:if>
							</tbody>
						</table>
						<c:if test="${!empty(list.pagin) }">
							<div class="text-right">
								<ul class="pagination">${list.pagin }
								</ul>
							</div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="${staticurl}/front/js/plugin/jquery.min.js"></script>
	<script type="text/javascript" src="${staticurl }/front/js/user/clipboard.min.js?v=1"></script>
	<%@include file="../comm/footer.jsp"%>

	<script>
       $(function(){
           var clipboard = new Clipboard('#copy-intro');
           clipboard.on('success', function(e) {
               alert('复制成功');
           });
           clipboard.on('error', function(e) {
           });
	   });
	</script>

</body>
</html>
