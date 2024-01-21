<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="include.inc.jsp"%>
<div id="allFooter" class="container-full footer">
	<div class="container footer-top">
		<div class="row col-xs-12 text-center">
			<div class="footer-left-top">
				<a target="_blank" href="/about/about.html?id=53"><spring:message code="footer.connect"/></a>
				<span class="plist max">|</span>
				<a target="_blank" href="/about/about.html?id=2"><spring:message code="footer.us"/></a>
				<span class="plist max">|</span>
				<a target="_blank" href="/about/about.html?id=5"><spring:message code="footer.law"/></a>
				<span class="plist max">|</span>
				<a target="_blank" href="/about/about.html?id=1"><spring:message code="footer.help"/></a>
				<span class="plist max">|</span>
				<a target="_blank" href="/about/about.html?id=57"><spring:message code="footer.cooperation"/></a>
				<span class="plist max">|</span>
				<a href="/online_help/index.html"><spring:message code="footer.online.question"/></a>
				<span class="plist max">|</span>
				<a target="_blank" href="/about/about.html?id=3"><spring:message code="footer.fee"/></a>
			</div>
		</div>
	</div>
	<div class="container">
		<c:forEach items="${ffriendlinks}" var="v">
		<div class="row">
			<div class="col-xs-12 footer-list">
				<ul class="clearfix">
					<li>
						<a style="cursor: pointer" href="${v.furl}">${v.fdescription}</a>
					</li>
				</ul>
			</div>
		</div>
		</c:forEach>
	</div>
	<div class="container">
		<div class="footer-left">
			<i class="footer-logo">
			</i>
		</div>

		<div class="footer-right">
			<span>©2017-2018 hotcoin.top</span>
		</div>
	</div>
</div>
<script type="text/javascript" src="${staticurl}/front/js/plugin/jquery.min.js"></script>
<script type="text/javascript" src="${staticurl}/front/js/plugin/bootstrap.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/plugin/layer/layer.js"></script>
<script type="text/javascript" src="${staticurl}/front/js/comm/util.js"></script>
<script type="text/javascript" src="${staticurl}/front/js/comm/comm.js"></script>
<script type="text/javascript" src="${staticurl}/front/js/language/language_${requestScope.localeStr}.js"></script>
