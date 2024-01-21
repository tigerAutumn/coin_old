var recharge = {
    refRecord: function () {
        var type = $("#rechargeType").val(),symbol = $("#symbol").val();
        var url = "/deposit/cny_record.html?type=" + type + "&symbol=" + symbol + "&random=" + Math.round(Math.random() * 100);
        $("#rechargeTab").load(url, null, function () {
            $(".look").on("click", function () {
                recharge.lookBankInfo(this);
            });
            $(".recordtitle").on("click", function () {
                util.recordTab($(this));
            });
        });
    },
    getCoinAddress: function (that) {
        var symbol = $("#symbol").val();
        var url = "/withdraw/coin_address.html";
        var param = {
            symbol: symbol
        };
        var callback = function (data) {
            if (data.code == 200) {
                window.location.reload(true);
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: that, url: url, param: param, success: callback});
    },
    addUserBankInfo: function (that) {
        var payeeAddr = $("#payeeAddr").val();
        var openBankTypeAddr = $("#openBankTypeAddr").val();
        var withdrawAccount = util.trim($("#withdrawAccountAddr").val());
        var address = util.trim($("#address").val());
        var userBankId = util.trim($("#userBankId").val());
        var prov = util.trim($("#prov").val());
        var city = util.trim($("#city").val() == null ? "" : $("#city").val());
        var dist = util.trim($("#dist").val() == null ? "" : $("#dist").val());
        var totpCode = 0;
        var phoneCode = 0;
        if (payeeAddr == "") {
            util.layerAlert("", util.getLan("finance.tips.1"), 2);
            return;
        }
        if (openBankTypeAddr == -1) {
            util.layerAlert("", util.getLan("finance.tips.2"), 2);
            return;
        }
        var reg = /^(\d{16}|\d{17}|\d{18}|\d{19})$/;
        if (!reg.test(withdrawAccount)) {
            // 银行卡号不合法
            util.layerAlert("", util.getLan("finance.tips.3"), 2);
            return;
        }
        if (withdrawAccount == "" || withdrawAccount.length > 200) {
            util.layerAlert("", util.getLan("finance.tips.3"), 2);
            return;
        }
        var withdrawAccount2 = util.trim($("#withdrawAccountAddr2").val());
        if (withdrawAccount != withdrawAccount2) {
            util.layerAlert("", util.getLan("finance.tips.5"), 2);
            return;
        }
        if (address == "") {
            util.layerAlert("", util.getLan("finance.tips.6"), 2);
            return;
        }
        if (address.length > 300) {
            util.layerAlert("", util.getLan("finance.tips.6"), 2);
            return;
        }
        if ($("#addressTotpCode").length > 0) {
            totpCode = util.trim($("#addressTotpCode").val());
            if (!/^[0-9]{6}$/.test(totpCode)) {
                util.layerAlert("", util.getLan("comm.tips.2"), 2);
                $("#addressTotpCode").val("");
                return;
            }
        }
        if ($("#addressPhoneCode").length > 0) {
            phoneCode = util.trim($("#addressPhoneCode").val());
            if (!/^[0-9]{6}$/.test(phoneCode)) {
                util.layerAlert("", util.getLan("comm.tips.3"), 2);
                $("#addressPhoneCode").val("");
                return;
            }
        }
        var url = "/user/save_bankinfo.html";
        var param = {
            account: withdrawAccount,
            openBankType: openBankTypeAddr,
            totpCode: totpCode,
            phoneCode: phoneCode,
            address: address,
            prov: prov,
            city: city,
            dist: dist,
            payeeAddr: payeeAddr,
            bankId: userBankId
        };
        var callback = function (result) {
            if (result.code === 200) {
                window.location.reload(true);
            } else {
                util.layerAlert("", result.msg, 2);
            }
        };
        util.network({btn: that, url: url, param: param, success: callback});
    },
    submitRechargeOrder: function (ele) {
        var blankId = $("#blankId").val()
            , amount = parseInt($("#amount").val())
            , random = parseFloat($("#random").val())
            , telephone = $("#telephone").val()
            , rechargeType = $("#rechargeType").val()
            , symbol = $("#symbol").val()
            , minRecharge = parseFloat($("#minRecharge").val())
            , maxRecharge = parseFloat($("#maxRecharge").val());
        if (blankId === "") {
            util.layerAlert("", util.getLan("finance.tips.17"), 2);
            return;
        }
        if (isNaN(amount) || amount <= 0) {
            util.layerAlert("", util.getLan("finance.tips.45"), 2);
            return;
        }
        if (amount < minRecharge) {
            util.layerAlert("", util.getLan("finance.tips.18", minRecharge), 2);
            return;
        }
        if (amount > maxRecharge) {
            util.layerAlert("", util.getLan("finance.tips.46", maxRecharge), 2);
            return;
        }
        if (telephone === "" || !util.checkMobile(telephone)) {
            util.layerAlert("", util.getLan("msg.tips.1"), 2);
            return;
        }
        var url = "/deposit/alipay_manual.html"
            , param = {symbol: symbol, amount: (amount + random), phone: telephone, type: rechargeType, ubank: blankId}
            , callback = function (result) {
            if (result.code === 200) {
                var random = Math.round((Math.random() * 80) + 11);
                $("#randomShow").html("." + random);
                $("#random").val("0." + random);
                recharge.refRecord();
                $("#amount").val("");
                $("#orderInfo").on('show.bs.modal', function () {
                    $("#bankPayee").text(result.data.ownerName);
                    $("#bankNo").text(result.data.bankNumber);
                    $("#bankAddress").text(result.data.bankAddress);
                    $("#bankName").text(result.data.rechargeBankName);
                    $("#bankAmount").text(result.data.money);
                    $("#bankRemark").text(result.data.userId);
                });
                $("#orderInfo").modal('show');
            } else {
                util.layerAlert("", result.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    jumpLink: function () {
        var bankName = $("#bankName").text()
            , url = bankUrl[bankName];
        if (typeof url === "undefined") {
            $("#orderInfo").modal('hide');
            return;
        }
        window.open(url, "_blank");
    },
    lookBankInfo: function (ele) {
        var $that = $(ele)
            , bankId = $that.data().bankid
            , amount = $that.data().amount
            , bankName = $that.data().bankname
            , url = "/deposit/getsysbankinfo.html"
            , param = {bankid: bankId}
            , callback = function (result) {
            $("#orderInfo").on('show.bs.modal', function () {
                $("#bankPayee").text(result.data.ownerName);
                $("#bankNo").text(result.data.bankNumber);
                $("#bankAddress").text(result.data.bankName);
                $("#bankName").text(bankName);
                $("#bankAmount").text(amount);
                $("#bankRemark").text(result.data.userId);
            });
            $("#orderInfo").modal('show');
        };
        util.network({url: url, param: param, success: callback});
    }
};
$(function () {
    $(".btn-sendmsg").on("click", function () {
        msg.sendMsgCode($(this).data().msgtype, $(this).data().tipsid, this.id);
    });
    $(".recordtitle").on("click", function () {
        util.recordTab($(this));
    });
    $("#amount").on("keypress", function (event) {
        return util.goIngKeypress(this, event, 0);
    });
    $("#virtualaddress").on("click", function () {
        recharge.getCoinAddress(this);
    });
    $("#withdrawCnyAddrBtn").on("click", function () {
        recharge.addUserBankInfo(this);
    });
    $("#withdrawAccountAddr2").bind("copy cut paste", function (e) {
        return false;
    });
    $("#rechargebtn").on("click", function () {
        recharge.submitRechargeOrder(this);
    });
    $("#jumpLink").on("click", function () {
        recharge.jumpLink();
    });
    $(".look").on("click", function () {
        recharge.lookBankInfo(this);
    })
});