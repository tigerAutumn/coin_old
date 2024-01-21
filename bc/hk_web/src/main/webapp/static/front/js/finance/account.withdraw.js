var withdraw = {
    btc : {
        addCoinAddress : function(that) {
            var coinName = $("#coinName").val();
            var withdrawAddr = util.trim($("#withdrawBtcAddr").val());
            var withdrawRemark = util.trim($("#withdrawBtcRemark").val());
            var withdrawBtcPass = util.trim($("#withdrawBtcPass").val());
            var withdrawBtcAddrTotpCode = 0;
            var addressPhoneCode = 0;
            var symbol = $("#symbol").val();
            if (withdrawAddr == "") {
                util.showerrortips("binderrortips", util.getLan("finance.tips.24"));
                return;
            }
            if ($("#withdrawBtcAddrTotpCode").length > 0) {
                withdrawBtcAddrTotpCode = util.trim($("#withdrawBtcAddrTotpCode").val());
                if (!/^[0-9]{6}$/.test(withdrawBtcAddrTotpCode)) {
                    util.showerrortips("binderrortips", util.getLan("comm.tips.2"));
                    $("#withdrawBtcAddrTotpCode").val("");
                    return;
                }
            }
            if ($("#addressPhoneCode").length > 0) {
                addressPhoneCode = util.trim($("#addressPhoneCode").val());
                if (!/^[0-9]{6}$/.test(addressPhoneCode)) {
                    util.showerrortips("binderrortips", util.getLan("comm.tips.3"));
                    $("#addressPhoneCode").val("");
                    return;
                }
            }
            util.hideerrortips("binderrortips");
            var url = "/user/save_withdraw_address.html";
            var param = {
                withdrawAddr : withdrawAddr,
                totpCode : withdrawBtcAddrTotpCode,
                phoneCode : addressPhoneCode,
                symbol : symbol,
                password : withdrawBtcPass,
                remark : withdrawRemark
            };
            var callback = function(result) {
                if (result.code == 200) {
                    window.location.reload(true);
                } else {
                    util.showerrortips("binderrortips", result.msg);
                }
            };
            util.network({
                             btn : that,
                             url : url,
                             param : param,
                             success : callback,
                         });
        },
        saveCoinWithdraw : function(that) {
            var coinName = $("#coinName").val();
            var withdrawAddr = util.trim($("#withdrawAddr").val());
            var withdrawAmount = util.trim($("#withdrawAmount").val());
            var tradePwd = util.trim($("#tradePwd").val());
            var max_double = parseFloat(util.trim($("#max_double").val()));
            var min_double = parseFloat(util.trim($("#min_double").val()));
            var totpCode = 0;
            var phoneCode = 0;
            var btcfee = 0;
            var withdrawMemo ="";
            var symbol = $("#symbol").val();
            if ($("#btcbalance").length > 0 && $("#btcbalance").val() == 0) {
                util.showerrortips("withdrawerrortips", util.getLan("comm.tips.9"));
                return;
            }
            if (withdrawAddr == "") {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.24"));
                return;
            }
            var reg = new RegExp("^[0-9]+\.{0,1}[0-9]{0,8}$");
            if (!reg.test(withdrawAmount)) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.25"));
                return;
            }
            if (parseFloat(withdrawAmount) < parseFloat(min_double) && parseFloat(min_double) != 0) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.26", min_double, coinName));
                return;
            }
            if (parseFloat(withdrawAmount) > parseFloat(max_double) && parseFloat(max_double) != 0) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.27", max_double, coinName));
                return;
            }
            if (tradePwd == "") {
                util.showerrortips("withdrawerrortips", util.getLan("comm.tips.8"));
                return;
            }
            if ($("#withdrawTotpCode").length > 0) {
                totpCode = util.trim($("#withdrawTotpCode").val());
                if (!/^[0-9]{6}$/.test(totpCode)) {
                    util.showerrortips("withdrawerrortips", util.getLan("comm.tips.2"));
                    return;
                }
            }
            if ($("#withdrawPhoneCode").length > 0) {
                phoneCode = util.trim($("#withdrawPhoneCode").val());
                if (!/^[0-9]{6}$/.test(phoneCode)) {
                    util.showerrortips("withdrawerrortips", util.getLan("comm.tips.3"));
                    return;
                }
            }
            if (totpCode == 0 && phoneCode == 0) {
                util.showerrortips("withdrawerrortips", util.getLan("comm.tips.1"));
                return;
            }
            if ($("#btcfee").length > 0) {
                btcfee = util.trim($("#btcfee").val());
            }
            if ($("#withdrawMemo").length > 0) {
                withdrawMemo = util.trim($("#withdrawMemo").val());
            }
            util.hideerrortips("withdrawerrortips");
            var url = "/withdraw/coin_manual.html";
            var param = {
                withdrawAddr : withdrawAddr,
                withdrawAmount : withdrawAmount,
                tradePwd : tradePwd,
                totpCode : totpCode,
                phoneCode : phoneCode,
                symbol : symbol,
                btcfeesIndex : btcfee,
                memo:withdrawMemo
            };
            var callback = function(result) {
                if (result.code == 200) {
                    util.layerAlert("", util.getLan("finance.tips.28"), 1);
                } else {
                    util.showerrortips("withdrawerrortips", result.msg);
                }
            };
            util.network({
                             btn : that,
                             url : url,
                             param : param,
                             success : callback,
                         });
        },
        cancelCoinWithdraw : function(id) {
            util.layerConfirm(util.getLan("finance.tips.29"), function() {
                var url = "/withdraw/coin_cancel.html";
                var param = {
                    id : id
                };
                var callback = function(data) {
                    window.location.reload(true);
                };
                util.network({
                                 url : url,
                                 param : param,
                                 success : callback,
                             });
            });
        }
    },
    cny : {
        addCnyAddress : function(that) {
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
                util.showerrortips("binderrortips", util.getLan("finance.tips.1"));
                return;
            }
            if (openBankTypeAddr == -1) {
                util.showerrortips("binderrortips", util.getLan("finance.tips.2"));
                return;
            }
            var reg = /^(\d{16}|\d{17}|\d{18}|\d{19})$/;
            if (!reg.test(withdrawAccount)) {
                // 银行卡号不合法
                util.showerrortips("binderrortips", util.getLan("finance.tips.3"));
                return;
            }
            if (withdrawAccount == "" || withdrawAccount.length > 200) {
                util.showerrortips("binderrortips", util.getLan("finance.tips.3"));
                return;
            }
            var withdrawAccount2 = util.trim($("#withdrawAccountAddr2").val());
            if (withdrawAccount != withdrawAccount2) {
                util.showerrortips("binderrortips", util.getLan("finance.tips.5"));
                return;
            }
            if ((prov == "" || prov == "请选择") || (city == "" || city == "请选择") || address == "") {
                util.showerrortips("binderrortips", util.getLan("finance.tips.6"));
                return;
            }
            if (address.length > 300) {
                util.showerrortips("binderrortips", util.getLan("finance.tips.6"));
                return;
            }
            if ($("#addressTotpCode").length > 0) {
                totpCode = util.trim($("#addressTotpCode").val());
                if (!/^[0-9]{6}$/.test(totpCode)) {
                    util.showerrortips("binderrortips", util.getLan("comm.tips.2"));
                    $("#addressTotpCode").val("");
                    return;
                }
            }
            if ($("#addressPhoneCode").length > 0) {
                phoneCode = util.trim($("#addressPhoneCode").val());
                if (!/^[0-9]{6}$/.test(phoneCode)) {
                    util.showerrortips("binderrortips", util.getLan("comm.tips.3"));
                    $("#addressPhoneCode").val("");
                    return;
                }
            }
            util.hideerrortips("binderrortips");
            var url = "/user/save_bankinfo.html";
            var param = {
                account : withdrawAccount,
                openBankType : openBankTypeAddr,
                totpCode : totpCode,
                phoneCode : phoneCode,
                address : address,
                prov : prov,
                city : city,
                dist : dist,
                payeeAddr : payeeAddr,
                bankId : userBankId
            };
            var callback = function(result) {
                if (result.code == 200) {
                    window.location.reload(true);
                } else {
                    util.showerrortips("binderrortips", result.msg);
                }
            };
            util.network({
                             btn : that,
                             url : url,
                             param : param,
                             success : callback,
                         });
        },
        addCnyAddressModifyInfo : function (ele) {
            $("#withdrawAccountAddr").val("").removeAttr("readonly");
            $("#withdrawAccountAddr2").val("").removeAttr("readonly");
            $("#openBankTypeAddr").val(-1).removeAttr("disabled");
            $("#exampleModalLabel").text(util.getLan("finance.tips.43"));
            $("#userBankId").val(0);
        },
        modifyCnyAddressModifyInfo : function (ele) {
            var selectOptaion = ele;
            var modifyFlag = selectOptaion.bankmodify;
            if(modifyFlag === 1 || modifyFlag === "1"){
                $("#withdrawAccountAddr").val(selectOptaion.banknumber).attr("readonly","readonly");
                $("#withdrawAccountAddr2").val(selectOptaion.banknumber).attr("readonly","readonly");
                $("#openBankTypeAddr").val(selectOptaion.bankinfo).attr("disabled","disabled");
                $("#exampleModalLabel").text(util.getLan("finance.tips.42"));
                $("#userBankId").val(selectOptaion.fid);
                $('#withdrawCnyAddress').modal('show');
                return true;
            }
            return false;
        },
        saveCnyWithdraw : function(ele) {
            var selectOptaion = $("#withdrawBlank").find("option:selected").data();
            if(selectOptaion.bankmodify === 1 || selectOptaion.bankmodify === "1"){
                withdraw.cny.modifyCnyAddressModifyInfo(selectOptaion);
                return false;
            }
            var withdrawBlank = $("#withdrawBlank").val();
            var balance = util.trim($("#withdrawBalance").val());
            var tradePwd = util.trim($("#tradePwd").val());
            var symbol = $("#symbol").val();
            var totpCode = 0;
            var phoneCode = 0;
            var min = $("#min_double").val();
            var max = $("#max_double").val();
            var reg = new RegExp("^[0-9]+\.{0,1}[0-9]{0,8}$");
            if (!reg.test(balance)) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.25"));
                return;
            }
            if (parseFloat(balance) < parseFloat(min) && parseFloat(min) != 0) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.30", min));
                return;
            }
            if (parseFloat(balance) > parseFloat(max) && parseFloat(max) != 0) {
                util.showerrortips("withdrawerrortips", util.getLan("finance.tips.31", max));
                return;
            }
            if (tradePwd == "" || tradePwd.length > 200) {
                util.showerrortips("withdrawerrortips", util.getLan("comm.tips.8"));
                return;
            }
            if ($("#withdrawTotpCode").length > 0) {
                totpCode = util.trim($("#withdrawTotpCode").val());
                if (!/^[0-9]{6}$/.test(totpCode)) {
                    util.showerrortips("withdrawerrortips", util.getLan("comm.tips.2"));
                    return;
                }
            }
            if ($("#withdrawPhoneCode").length > 0) {
                phoneCode = util.trim($("#withdrawPhoneCode").val());
                if (!/^[0-9]{6}$/.test(phoneCode)) {
                    util.showerrortips("withdrawerrortips", util.getLan("comm.tips.3"));
                    return;
                }
            }
            if (totpCode == 0 && phoneCode == 0) {
                util.showerrortips("withdrawerrortips", util.getLan("comm.tips.1"));
                return;
            }
            util.hideerrortips("withdrawerrortips");
            var url = "/withdraw/cny_manual.html";
            var param = {
                tradePwd : tradePwd,
                withdrawBalance : balance,
                phoneCode : phoneCode,
                totpCode : totpCode,
                withdrawBlank : withdrawBlank,
                symbol : symbol
            };
            var callback = function(result) {
                if (result.code == 200) {
                    util.layerAlert("", util.getLan("finance.tips.28"), 1);
                } else {
                    util.showerrortips("withdrawerrortips", result.msg);
                }
            };
            util.network({
                             btn : ele,
                             url : url,
                             param : param,
                             success : callback,
                         });
        },
        cancelCnyWithdraw : function(outId) {
            util.layerConfirm(util.getLan("finance.tips.29"), function() {
                var url = "/withdraw/cny_cancel.html";
                var param = {
                    id : outId
                };
                var callback = function(data) {
                    window.location.reload(true);
                };
                util.network({
                                 url : url,
                                 param : param,
                                 success : callback,
                             });
            });
        },
        calculateFeesRate : function() {
            var amount = $("#withdrawBalance").val();
            var feesRate = $("#feesRate").val();
            if (amount == "") {
                amount = 0;
            }
            var feeamt = util.numFormat(util.accMul(amount, feesRate), util.DEF_FEE_SCALE);
            $("#free").html(feeamt);
            $("#amount").html(util.numFormat(parseFloat(amount) - parseFloat(feeamt), util.ENTER_CNY_SCALE));
        }
    }
};
$(function() {
    $(".btn-sendmsg").on("click", function() {
        email.sendMsgCode($(this).data().msgtype, $(this).data().tipsid, this.id);
    });
    $("#withdrawBtcAddrBtn").on("click", function() {
        withdraw.btc.addCoinAddress(this);
    });
    $("#withdrawBtcButton").on("click", function() {
        withdraw.btc.saveCoinWithdraw(this);
    });
    $("#withdrawAmount").on("keypress", function(event) {
        if($("#coinName").val()=="BTK"){
            return util.goIngKeypress(this, event, 0);
        }else{
            return util.goIngKeypress(this, event, util.ENTER_COIN_SCALE);
        }
    });
    $(".cancelWithdrawBtc").on("click", function(event) {
        withdraw.btc.cancelCoinWithdraw($(this).data().fid);
    });

    $(".recordtitle").on("click", function() {
        util.recordTab($(this));
    });
    $("#withdrawCnyAddrBtn").on("click", function() {
        withdraw.cny.addCnyAddress(this);
    });
    $("#withdrawBalance").on("keypress", function(event) {
        return util.goIngKeypress(this, event, util.ENTER_CNY_SCALE);
    }).on("keyup", function() {
        withdraw.cny.calculateFeesRate();
    });
    $("#withdrawCnyButton").on("click", function(event) {
        withdraw.cny.saveCnyWithdraw(this);
    });
    $(".cancelWithdrawcny").on("click", function(event) {
        withdraw.cny.cancelCnyWithdraw($(this).data().fid);
    });
    $("#withdrawAccountAddr2").bind("copy cut paste", function(e) {
        return false;
    });
    $("#withdrawBlank").on("change",function (event) {
        withdraw.cny.modifyCnyAddressModifyInfo($(this).find("option:selected").data());
    })
    $('#withdrawCnyAddress').on('hidden.bs.modal', function (event) {
        withdraw.cny.addCnyAddressModifyInfo();
    })
});