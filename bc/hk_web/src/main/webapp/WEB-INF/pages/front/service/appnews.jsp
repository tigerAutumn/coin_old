<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path;
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<meta name="format-detection" content="telephone=no">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<script>
	(function(doc, win) {
		var docEl = doc.documentElement, isIOS = navigator.userAgent.match(/iphone|ipod|ipad/gi), dpr = isIOS ? Math.min(win.devicePixelRatio, 3) : 1, dpr = window.top === window.self ? dpr : 1, //被iframe引用时，禁止缩放
		dpr = 1, // 首页引用IFRAME，强制为1
		scale = 1 / dpr, resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize';
		docEl.dataset.dpr = win.devicePixelRatio;
		if (navigator.userAgent.match(/iphone/gi) && screen.width == 375 && win.devicePixelRatio == 2) {
			docEl.classList.add('iphone6')
		}
		if (navigator.userAgent.match(/iphone/gi) && screen.width == 414 && win.devicePixelRatio == 3) {
			docEl.classList.add('iphone6p')
		}
		var metaEl = doc.createElement('meta');
		metaEl.name = 'viewport';
		metaEl.content = 'initial-scale=' + scale + ',maximum-scale=' + scale + ', minimum-scale=' + scale;
		docEl.firstElementChild.appendChild(metaEl);
		var recalc = function() {
			var width = docEl.clientWidth;
			if (width / dpr > 640) {
				width = 640 * dpr;
			}
			docEl.style.fontSize = 100 * (width / 640) + 'px';
		};
		recalc()
		if (!doc.addEventListener)
			return;
		win.addEventListener(resizeEvt, recalc, false);
	})(document, window);
</script>
<link rel="stylesheet" href="${staticurl }/front/css/service/appnews.css" type="text/css"></link>
</head>
<body>
	<div class="container-fluid">
		<%--<div id="myCarousel" class="index_carousel ">
			<ol class="index_carousel-indicators ">
				<li data-target="#myCarousel" data-slide-to="0" class="active" style="display:block;"></li>
				<li data-target="#myCarousel" data-slide-to="1" class="" style="display:block;"></li>
			</ol>
			<div class="index_carousel-inner ">
				<div class="item active hand" style="background-image: url(${staticurl }/front/images/service/banner1.png);background-size:100% 100%;"></div>
				<div class="item " style="background-image: url(${staticurl }/front/images/service/banner2.jpg);background-size:100% 100%;"></div>
			</div>
		</div>--%>
		<!-- 新闻列表 -->
		<div id="newsitem" class="news-item">
			<c:forEach items="${farticles}" var="news">
				<a href="/service/appnew.html?id=${news.fid}">
					<div class="media clearfix">
						<div class="media-body">
							<span class="news-title">
								<span>${news.ftitle}</span>
							</span>
							<p class="date">
								<i class="icon"></i>
								官方公告
								<i class="split"></i>
								<fmt:formatDate value="${news.fcreatedate }" pattern="yyyy.MM.dd" />
							</p>
						</div>
						<div class="media-right">
							<c:if test="${news.findeximg==null }">
								<img alt="" src="${staticurl}/front/images/service/defaultImg.png">
							</c:if>
							<c:if test="${news.findeximg!=null }">
								<img alt="" src="${news.findeximg }">
							</c:if>
						</div>
					</div>
				</a>
			</c:forEach>
		</div>
		<c:if test="${nextpage>0 }">
			<div id="newsmore" class="news-more">
				<span class="more">加载更多>></span>
				<input id="nextpage" type="hidden" value="${nextpage }" />
				<input id="id" type="hidden" value="${id }" />
			</div>
		</c:if>
	</div>
</body>
<script type="text/javascript" src="${staticurl }/front/js/plugin/jquery.min.js"></script>
<script type="text/javascript" src="${staticurl }/front/js/service/appnews.js"></script>
</html>