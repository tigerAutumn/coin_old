var activity = {
	exchange : function(that) {
		var code = util.trim($("#exchangeCode").val());
		var patrn = /[a-zA-Z0-9]+/;
		if (code == "" || code.length != 16 || !patrn.exec(code)) {
			desc = util.getLan("activity.tips.1");
			util.layerAlert("", desc, 2);
			return;
		}
		url = "/activity/exchange.html";
		var param = {
			code : code
		};
		var callback = function(data) {
			if (data.code == 200) {
				util.layerAlert("", data.msg, 1);
			} else {
				util.layerAlert("", data.msg, 2);
			}
		};
		util.network({
			btn : that,
			url : url,
			param : param,
			success : callback,
		});
	},

};
$(function() {
	$("#exchange").click(function() {
		activity.exchange(this);
	});
	$(".rewordtitle").on("click", function() {
		util.recordTab($(this));
	});
});