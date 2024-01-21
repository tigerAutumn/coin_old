var trade = {
	cancelEntrustBtc : function(ele, id) {
		var url = "/trade/cny_cancel.html";
		var param = {
			id : id
		};
		var callback = function(data) {
			if (data.code == 200) {
				window.location.reload(true);
			}
		};
		util.network({
			url : url,
			param : param,
			success : callback,
		});
	},
	entrustLog : function(id) {
		var url = "/trade/cny_entrustLog.html";
		var param = {
			id : id
		};
		var callback = function(data) {
			if (data != null && data.result == true) {
				var modal = $("#entrustsdetail");
				modal.find('.modal-title').html(data.title);
				modal.find('.modal-body').html(data.content);
				modal.modal('show');
			}
		};
		util.network({
			url : url,
			param : param,
			success : callback,
		});
	},
};
$(function() {
	$(".tradecancel").on("click", function() {
		var id = $(this).data().value;
		trade.cancelEntrustBtc(this, id);
	});
	$(".tradelook").on("click", function() {
		var id = $(this).data().value;
		trade.entrustLog(id);
	});
});