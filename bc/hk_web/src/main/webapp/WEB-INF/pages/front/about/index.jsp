<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="../comm/include.inc.jsp"%>
<!doctype html>
<html>
<head>
<%@include file="../comm/link.inc.jsp"%>
<link rel="stylesheet" href="${staticurl }/front/css/user/about.css" type="text/css"></link>
</head>
<body>
	<%@include file="../comm/header.jsp"%>
	<div class="container-full">
		<div class="container about">
			<div class="col-xs-12 padding-top-30">
				<div class="col-xs-2 padding-left-clear">
					<span class="title"><spring:message code="about.index.title" /></span>
					<div class="panel-group" role="tablist">
						<c:forEach items="${fabouttypes }" var="v" varStatus="vs">
							<div class="panel panel-default">
								<div class="panel-heading" role="tab" id="collapseListGroupHeading1">
									<h4 class="panel-title">
										<span id="icon${vs.index }" class="item-icon">${fabout.fabouttype==v.fid?'-':'+' }</span>
										<a class="collapsed" role="button" data-toggle="collapse" href="#collapse${vs.index }"> ${v.ftitle } </a>
									</h4>
								</div>
								<div id="collapse${vs.index }" data-icon="icon${vs.index }" class="panel-collapse collapse ${fabout.fabouttype==v.fid?'in':'' }" role="tabpanel">
									<ul class="list-group">
										<c:forEach items="${v.child }" var="a" varStatus="as">
											<li class="list-group-item ${a.key==fabout.fid?'active':'' }">
												<span class="item-icon">&lt</span>
												<a href="/about/about.html?id=${a.key }">${a.value }</a>
											</li>
										</c:forEach>
									</ul>
								</div>
							</div>
						</c:forEach>
					</div>
				</div>
				<div class="col-xs-10 padding-bottom-50">
					<div class="col-xs-12 bg-white split">
						<h4>${fabout.ftitle }</h4>
					</div>
					<div class="col-xs-12 bg-white">${fabout.fcontent }</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="../comm/footer.jsp"%>
	<script type="text/javascript">
		$(function() {
			$('.collapse').on('hide.bs.collapse', function(ele) {
				$("#"+$(ele.currentTarget).data().icon).html("+");
				return true;
			})
			$('.collapse').on('show.bs.collapse', function(ele) {
				$("#"+$(ele.currentTarget).data().icon).html("-");
				return true;
			})
		})
	</script>
</body>
</html>
