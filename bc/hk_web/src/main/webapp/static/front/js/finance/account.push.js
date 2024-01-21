var push = {
	submit : function(ele) {
		var pushUid = $("#pushUid").val();
		var pushCoinId = $("#pushCoinId").val();
		var pushCount = $("#pushCount").val();
		var pushPrice = $("#pushPrice").val();
		var tradePwd = $("#tradePwd").val();
		var btcbalance = $("#btcbalance").val();
		var googleCode = "";
		if (pushUid == "") {
			util.showerrortips("pushError", util.getLan("finance.tips.41"));
			return;
		}
		if (pushCoinId == "") {
			util.showerrortips("pushError",util.getLan("finance.tips.33"));
			return;
		}
		if (pushCount == "") {
			util.showerrortips("pushError", util.getLan("finance.tips.34"));
			return;
		}
		if (Number(pushCount) < 100) {
			util.showerrortips("pushError", util.getLan("finance.tips.35"));
			return;
		}
		if (Number(btcbalance) < Number(pushCount)) {
			util.showerrortips("pushError", util.getLan("finance.tips.36"));
			return;
		}
		if (pushPrice == "") {
			util.showerrortips("pushError", util.getLan("finance.tips.37"));
			return;
		}
		if (Number(pushPrice) < 0.0001) {
			util.showerrortips("pushError", util.getLan("finance.tips.38"));
			return;
		}
		if (tradePwd == "") {
			util.showerrortips("pushError", util.getLan("finance.tips.39"));
			return;
		}
		if ($("#googleCode").length > 0) {
			googleCode = $("#googleCode").val();
			if (googleCode == "") {
				util.showerrortips("pushError", util.getLan("comm.tips.2"));
				return;
			}
		}
		if (googleCode == "") {
			util.showerrortips("pushError", util.getLan("comm.tips.17"));
			return;
		}
		util.hideerrortips("pushError");
		var url = "/submit_push.html";
		var param = {
			pushcoinid : pushCoinId,
			pushuid : pushUid,
			pushcount : pushCount,
			pushprice : pushPrice,
			tradepwd : tradePwd,
			googlecode : googleCode,
		}
		var callback = function(result) {
			if (result.code == 200) {
				util.layerAlert("", result.msg, 1);
			} else {
				util.showerrortips("pushError", result.msg);
			}
		}
		util.network({
			btn : ele,
			url : url,
			param : param,
			success : callback,
		});
	},
	cancel : function(pushid) {
		util.layerConfirm(util.getLan("finance.tips.40"), function() {
			var url = "/cancel_push.html";
			var param = {
				pushid : pushid
			};
			var callback = function(result) {
				if (result.code == 200) {
					util.layerAlert("", result.msg, 1);
				} else {
                    util.layerAlert("", result.msg, 2);
				}
			};
			util.network({
				url : url,
				param : param,
				success : callback,
			});
		});
	},
	confirmShow : function(ele) {
		var $that = $(ele);
		var $data = $that.data();
		var pushid = $data.pushid;
		var coins = $data.coins;
		var count = $data.count;
		var price = $data.price;
		var amount = $data.amount;
		$("#pushModalCoin").html(coins);
		$("#pushModalPrice").html(price);
		$("#pushModalCount").html(count);
		$("#pushModalAmount").html(amount);
		$("#pushModalid").val(pushid);
		$('#pushModal').modal({
			backdrop : 'static',
			keyboard : false,
			show : true
		});
	},
	confirm : function() {
		var pushid = $("#pushModalid").val();
		var password = $("#pushModalPassword").val();
		var url = "/confirm_push.html";
		var param = {
			pushid : pushid,
			password : password
		};
		var callback = function(result) {
			if (result.code == 200) {
				util.layerAlert("", result.msg, 1);
			} else {
				util.showerrortips("pushModalError", result.msg);
			}
		};
		util.network({
			url : url,
			param : param,
			success : callback,
		});
	},
    getUserBalance: function (ele) {
        var url = "/push/userbalance.html";
        var param = {coinid: ele.value};
        var callback = function (result) {
            if (result.code === 200) {
                $("#btcbalance").val(result.data.total);
                $("#assettotal").html(result.data.total);
            } else {
                util.layerAlert("", result.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    }
}
$(function() {
	$("#pushPrice").on("keypress", function(event) {
		return util.goIngKeypress(this, event, util.ENTER_COIN_SCALE);
	});
	$("#pushCount").on("keypress", function(event) {
		return util.goIngKeypress(this, event, util.ENTER_COIN_SCALE);
	});
	$("#pushSubmit").on("click", function() {
		push.submit(this);
	});
	$(".cancelPush").on("click", function() {
		var $that = $(this);
		push.cancel($that.data().pushid);
	});
	$(".payPush").on("click", function() {
		push.confirmShow(this);
	});
	$("#pushModalBtn").on("click", function() {
		push.confirm(this);
	});
	$(".recordtitle").on("click", function() {
		util.recordTab($(this));
	});
    $("#pushCoinId").on("change", function () {
        push.getUserBalance(this);
    });
});