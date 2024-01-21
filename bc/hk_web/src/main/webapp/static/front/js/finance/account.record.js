var  record= {
	search : function(begindate, enddate) {
		var url = $("#recordType").val();
		var datetype = $("#datetype").val();
		begindate = begindate ? begindate : $("#begindate").val();
		enddate = enddate ? enddate : $("#enddate").val();
		if (datetype > 0) {
			url=url + "&datetype=" + datetype;
		} else {
			url=url + "&datetype=" + datetype + "&begindate=" + begindate + "&enddate=" + enddate;
		}
		window.location.href = url;
		
	}
};
$(function() {
	bitDates({input:[$('#begindate'),$('#enddate')]},function(){
		record.search();
	});
	$("#begindate").on("input",function() {
		record.search();
	});
	$("#enddate").on("input",function() {
		record.search();
	});
	$(".datatime").click(function() {
		$("#datetype").val($(this).data().type);
		record.search();
	});
	$("#recordType").change(function() {
		record.search();
	});
});