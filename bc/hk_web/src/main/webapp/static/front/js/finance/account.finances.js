var finances = {
    submit: function (ele) {
        var finCount = $("#finCount").val();
        var finType = $("#finType").val();
        var tradePwd = $("#tradePwd").val();
        var btcbalance = $("#btcbalance").val();
        var symbol = $("#symbol").val();
        var googleCode = "";
        if (finType == "") {
            util.showerrortips("finError", util.getLan("finance.tips.11"));
            return;
        }
        if (finCount == "") {
            util.showerrortips("finError", util.getLan("finance.tips.12"));
            return;
        }
        if (Number(finCount) < 100) {
            util.showerrortips("finError", util.getLan("finance.tips.13"));
            return;
        }
        if (Number(btcbalance) < Number(finCount)) {
            util.showerrortips("finError", util.getLan("finance.tips.14"));
            return;
        }
        if (tradePwd == "") {
            util.showerrortips("finError", util.getLan("comm.tips.8"));
            return;
        }
        if ($("#googleCode").length > 0) {
            googleCode = $("#googleCode").val();
            if (googleCode == "") {
                util.showerrortips("finError", util.getLan("comm.tips.2"));
                return;
            }
        }
        if (googleCode == "") {
            util.showerrortips("finError", util.getLan("comm.tips.1"));
            return;
        }
        util.hideerrortips("finError");
        var url = "/submit_finances.html";
        var param = {
            symbol: symbol,
            type: finType,
            count: finCount,
            tradepwd: tradePwd,
            googlecode: googleCode
        };
        var callback = function (result) {
            if (result.code == 200) {
                util.layerAlert("", result.msg, 1);
            } else {
                util.showerrortips("finError", result.msg);
            }
        }
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    calcCount: function () {
        var rate = parseFloat($("#finType").find("option:selected").attr("id"));
        var count = $("#finCount").val();
        var html = $("#countTips");
        if (html.length <= 0) {
            html =
                $('<span id="countTips" class="text-danger" style="position: absolute;left: 100%;top: 7px;width: 145px;"></span>');
            $("#finCount").after(html);
        }
        if (count == "") {
            html.remove();
            return;
        }
        var count = parseFloat(count);
        html.html(util.getLan("finance.tips.16", util.numFormat(util.accMul(rate, count), util.DEF_COIN_SCALE)));
    },
    getFinances: function (ele) {
        var url = "/get_finances.html";
        var param = {
            symbol: $(ele).val(),
        };
        var callback = function (result) {
            if (result.code != 200) {
                return;
            }
            $("#btcbalance").val(result.data.total);
            $("#assettotal").html(result.data.total);
            $("#finType option").remove();
            $.each(result.data.typeList, function (key, value) {
                $("#finType")
                    .append("<option value=" + value.id + " id=" + value.rate + ">" + value.name + "</option>");
            });
        }
        util.network({url: url, param: param, success: callback});
    },
    cancel: function (fid) {
        util.layerConfirm(util.getLan("finance.tips.44"), function () {
            var url = "/cancel_finances.html";
            var param = {
                fid: fid
            };
            var callback = function (result) {
                if (result.code == 200) {
                    util.layerAlert("", result.msg, 1);
                } else {
                    util.layerAlert("", result.msg, 2);
                }
            };
            util.network({url: url, param: param, success: callback});
        });
    }
};
$(function () {
    $("#finCount").on("keypress", function (event) {
        return util.goIngKeypress(this, event, util.ENTER_COIN_SCALE);
    }).on("keyup", function () {
        finances.calcCount();
    });
    $("#finSubmit").on("click", function () {
        finances.submit(this);
    });
    $(".recordtitle").on("click", function () {
        util.recordTab($(this));
    });
    $("#finType").on("change", function () {
        finances.calcCount();
    });
    $("#symbol").on("change", function () {
        finances.getFinances(this);
    });
    $(".cancel").on("click", function () {
        finances.cancel($(this).data().fid);
    });
});