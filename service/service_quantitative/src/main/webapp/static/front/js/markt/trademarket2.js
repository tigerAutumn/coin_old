var trade = {
    check: 1,
    cnyDigit: $("#cnyDigit").val(),
    coinDigit: $("#coinDigit").val(),
    toFixed: parseInt($("#tradeType").val()) === 1 ? 6 : 8,
    charts: '',
    btcPrice: 0,
    numVerify: function (tradeType) {
        if (tradeType == 0) {
            var userCnyBalance = $("#tradebuyprice").val();
            if (userCnyBalance == "") {
                userCnyBalance = 0;
            }
            var tradebuyAmount = $("#tradebuyamount").val();
            if (tradebuyAmount == "") {
                tradebuyAmount = 0;
            }
            var tradeTurnover = util.accMul(userCnyBalance, tradebuyAmount);
            var tradecnymoney = Number($("#tradeBuyCoin").val());
            if (tradeTurnover > tradecnymoney) {
                util.showerrortips("buy-errortips", util.getLan("comm.tips.9"));
                return;
            }
            $("#tradebuyTurnover").html(new Number(tradeTurnover).toFixed(trade.toFixed));
            trade.setConvertPrice("tradebuyTurnoverTips",tradeTurnover);
            util.hideerrortips("buy-errortips");
        } else {
            var usersellCnyBalance = $("#tradesellprice").val();
            if (usersellCnyBalance == "") {
                usersellCnyBalance = 0;
            }
            var tradesellAmount = $("#tradesellamount").val();
            if (tradesellAmount == "") {
                tradesellAmount = 0;
            }
            var tradeTurnover = util.accMul(usersellCnyBalance, tradesellAmount);
            var trademtccoin = Number($("#tradeSellCoin").val());
            if (tradesellAmount > trademtccoin) {
                util.showerrortips("sell-errortips", util.getLan("comm.tips.9"));
                return;
            }
            $("#tradesellTurnover").html(new Number(tradeTurnover).toFixed(trade.toFixed));
            trade.setConvertPrice("tradesellTurnoverTips",tradeTurnover);
            util.hideerrortips("sell-errortips");
        }
    },
    submitTradePwd: function () {
        var tradePwd = $("#tradePwd").val();
        if (tradePwd != "") {
            $("#tradePwd").val(tradePwd);
            $("#isopen").val("false");
        }
        $('#tradepass').modal('hide');
        var tradeType = $("#tradeType").val();
        trade.saveCoinTrade(tradeType, false);
    },
    saveCoinTrade: function (tradeType, flag) {
        if ($("#login").val() == "false") {
            window.location.href = "/user/login.html?forwardUrl=" + window.location.pathname;
            return;
        }
        var errorele = "";
        if (tradeType == 0) {
            errorele = "buy-errortips";
        } else {
            errorele = "sell-errortips";
        }
        var tradePassword = $("#tradePassword").val();
        if (tradePassword == "true") {
            util.layerAlert("", util.getLan("comm.tips.17"), 2);
            return;
        }
        var isTelephoneBind = $("#isTelephoneBind").val();
        if (isTelephoneBind == "false") {
            util.layerAlert("", util.getLan("comm.tips.1"), 2);
            return;
        }
        var symbol = $("#symbol").val();
        var sellCoinName = $("#sellShortName").val();
        var buyCoinName = $("#buyShortName").val();
        var tradeAmount = 0;
        var tradePrice = 0;
        if (tradeType == 0) {
            tradeAmount = $("#tradebuyamount").val();
            tradePrice = $("#tradebuyprice").val();
        } else {
            tradeAmount = $("#tradesellamount").val();
            tradePrice = $("#tradesellprice").val();
        }
        var limited = 0;
        if (tradeType == 0) {
            var tradeTurnover = util.accMul(tradeAmount, tradePrice);
            if ($("#tradeBuyCoin").length > 0 && Number($("#tradeBuyCoin").val()) < Number(tradeTurnover)) {
                util.showerrortips(errorele, util.getLan("comm.tips.9"));
                return;
            }
        } else {
            if ($("#tradeSellCoin").length > 0 && Number($("#tradeSellCoin").val()) < Number(tradeAmount)) {
                util.showerrortips(errorele, util.getLan("comm.tips.9"));
                return;
            }
        }
        var minCount = $("#minCount").val();
        var maxCount = $("#maxCount").val();
        var minPrice = $("#minPrice").val();
        var maxPrice = $("#maxPrice").val();
        var reg = new RegExp("^[0-9]+\.{0,1}[0-9]{0,8}$");
        if (!reg.test(tradeAmount)) {
            util.showerrortips(errorele, util.getLan("trade.tips.7"), false, 1);
            return;
        }
        if (parseFloat(tradeAmount) < parseFloat(minCount) && parseFloat(minCount) > 0) {
            util.showerrortips(errorele, util.getLan("trade.tips.8", minCount, sellCoinName), false, 1);
            return;
        }
        if (parseFloat(tradeAmount) > parseFloat(maxCount) && parseFloat(maxCount) > 0) {
            util.showerrortips(errorele, util.getLan("trade.tips.9", maxCount, sellCoinName), false, 1);
            return;
        }
        if (!reg.test(tradePrice)) {
            util.showerrortips(errorele, util.getLan("trade.tips.10"), false, 1);
            return;
        }
        if (parseFloat(tradePrice) < parseFloat(minPrice) && parseFloat(minPrice) > 0) {
            util.showerrortips(errorele, util.getLan("trade.tips.11", minPrice, buyCoinName), false, 1);
            return;
        }
        if (parseFloat(tradePrice) > parseFloat(maxPrice) && parseFloat(maxPrice) > 0) {
            util.showerrortips(errorele, util.getLan("trade.tips.12", maxPrice, buyCoinName), false, 1);
            return;
        }
        var isopen = $("#isopen").val();
        if (isopen == "true" && flag) {
            $("#tradeType").val(tradeType);
            $('#tradepass').modal({backdrop: 'static', keyboard: false, show: true});
            return;
        }
        var tradePwd = "";
        if ($("#tradePwd").length > 0) {
            tradePwd = util.trim($("#tradePwd").val());
        }
        if (tradePwd == "" && isopen == "true") {
            util.showerrortips(errorele, util.getLan("comm.tips.8"));
            $("#isopen").val("true");
            return;
        } else {
            util.hideerrortips(errorele);
        }
        util.hideerrortips(errorele);
        var url = "";
        if (tradeType == 0) {
            url = "/trade/cny_buy.html";
        } else {
            url = "/trade/cny_sell.html";
        }
        tradePwd = isopen == "true" ? "" : tradePwd;
        var param = {
            tradeAmount: tradeAmount,
            tradePrice: tradePrice,
            tradePwd: tradePwd,
            symbol: symbol,
            limited: limited
        };
        var btntext = "";
        var btn = "";
        if (tradeType == 0) {
            btn = $("#buybtn");
            btntext = btn.html();
            btn.html(util.getLan("trade.tips.14"));
        } else {
            btn = $("#sellbtn");
            btntext = btn.html();
            btn.html(util.getLan("trade.tips.15"));
        }
        var callback = function (data) {
            btn.html(btntext);
            util.showerrortips(errorele, data.msg);
            if (data.code == 10003 || data.code == 10116 || data.code == 10008 || data.code == 10009) {
                $("#isopen").val("true");
            }
        };
        util.network({btn: btn[0], url: url, param: param, success: callback, enter: true});
    },
    cancelEntrustBtc: function (ele, id) {
        var url = "/trade/cny_cancel.html";
        var param = {
            id: id
        };
        var callback = function (data) {
            if (data.code == 200) {
                if (window.location.pathname.indexOf("cny_coin") >= 0) {
                    $(ele).remove();
                } else {
                    //window.location.reload(true);
                    $(ele).remove();
                }
            }
        };
        util.network({url: url, param: param, success: callback});
    },
    antoTurnover: function (price, money, mtcNum, tradeType) {
        if (tradeType == 0) {
            $("#tradebuyprice").val(util.numFormat(price, trade.cnyDigit));
            $("#tradebuyamount").val(util.numFormat(mtcNum, trade.coinDigit));
            var tradeTurnover = util.accMul(price, mtcNum);
            var tradecnymoney = Number($("#tradeBuyCoin").val());
            if (parseFloat(tradeTurnover) > parseFloat(tradecnymoney)) {
                util.showerrortips("buy-errortips", util.getLan("comm.tips.9"));
                return;
            }
            $("#tradebuyTurnover").html(new Number(tradeTurnover).toFixed(trade.toFixed));
            trade.setConvertPrice("tradebuyTurnoverTips",tradeTurnover);
            util.hideerrortips("buy-errortips");
        } else {
            $("#tradesellprice").val(util.numFormat(price, trade.cnyDigit));
            $("#tradesellamount").val(util.numFormat(mtcNum, trade.coinDigit));
            var tradeTurnover = util.accMul(price, mtcNum);
            var trademtccoin = util.numFormat(Number($("#tradeSellCoin").val()), trade.coinDigit);
            if (parseFloat(mtcNum) > parseFloat(trademtccoin)) {
                util.showerrortips("sell-errortips", util.getLan("comm.tips.9"));
                return;
            }
            $("#tradesellTurnover").html(new Number(tradeTurnover).toFixed(trade.toFixed));
            trade.setConvertPrice("tradesellTurnoverTips",tradeTurnover);
            util.hideerrortips("sell-errortips");
        }
    },
    entrustInfoTab: function (ele) {
        var type = ele.data().type;
        var title = "";
        var value = ele.data().value;
        if (value == 0) {
            value = 1;
            title = util.getLan("comm.tips.15", "&nbsp;+");
            $("#panelBody" + type).slideUp(300);
        } else {
            value = 0;
            title = util.getLan("comm.tips.16", "&nbsp;-");
            $("#panelBody" + type).slideDown(300);
        }
        ele.data().value = value;
        ele.html(title);
    },
    entrustLog: function (id) {
        var url = "/trade/cny_entrustLog.html";
        var param = {
            id: id
        };
        var callback = function (data) {
            if (data != null && data.result == true) {
                var modal = $("#entrustsdetail");
                modal.find('.modal-title').html(data.title);
                modal.find('.modal-body').html(data.content);
                modal.modal('show');
            }
        };
        util.network({url: url, param: param, success: callback});
    },
    onPortion: function (portion, tradeType) {
        portion = util.accDiv(portion, 100);
        if (tradeType == 0) {
            var money = Number($("#tradebuyprice").val());
            var tradecnymoney = Number($("#tradeBuyCoin").val());
            var mtcNum = util.accDiv(tradecnymoney, money);
            mtcNum = util.accMul(mtcNum, portion);
            var portionMoney = util.accMul(money, mtcNum);
            this.antoTurnover(money, portionMoney, mtcNum, tradeType);
        } else {
            var money = Number($("#tradesellprice").val());
            var trademtccoin = Number($("#tradeSellCoin").val());
            mtcNum = util.accMul(trademtccoin, portion);
            var portionMoney = util.accMul(money, mtcNum);
            this.antoTurnover(money, portionMoney, mtcNum, tradeType);
        }
    },
    lastprice: 0,
    fristprice: true,
    autoRefresh: function () {
        var symbol = $("#symbol").val();
        var coinshortName = $("#sellShortName").val();
        if (symbol == "" || typeof (symbol) == "undefined" || coinshortName == "" || typeof (coinshortName)
            == "undefined") {
            return;
        }
        var globalurl = util.globalurl[coinshortName];
        if (typeof (globalurl) == "undefined") {
            globalurl = util.globalurl["DEFAULT"];
        }
        var url = globalurl.market;
        var buysellcount = 6;
        var successcount = 0;
        url = util.strFormat(url, symbol, buysellcount, successcount);
        var priceOffset = function (value, type) {
            var offset = $("#tradePrice").val().split("#");
            if (offset.length == 2 && globalurl.isremote) {
                if (type == 0) {
                    return util.accSub(value, offset[0]);
                }
                if (type == 1) {
                    return util.accAdd(value, offset[1]);
                }
            }
            return value;
        };
        var amountOffset = function (value, type) {
            var offset = $("#tradeAmount").val().split("#");
            if (offset.length == 2 && globalurl.isremote) {
                if (type == 0) {
                    return util.accMul(value, offset[0]);
                }
                if (type == 1) {
                    return util.accMul(value, offset[1]);
                }
            }
            return value;
        };
        var marketCallback = function (result) {
            var data = "";
            if (globalurl.isremote) {
                data = result;
            } else {
                if (result.code != 200) {
                    return;
                }
                data = result.data;
            }
            $.each(data.buys, function (key, value) {
                if (key >= buysellcount) {
                    return;
                }
                var buyele = $("#buy" + key);
                if (buyele.length == 0) {
                    $("#buybox").append('<li id="buy' + key
                        + '"  class="list-group-item clearfix buysellmap" data-type="1" data-money="'
                        + util.numFormat(priceOffset(Number(value.price), 0), trade.cnyDigit)
                        + '" data-num="' + util.numFormat(amountOffset(Number(value.amount), 0),
                            trade.coinDigit) + '">'
                        + '<span class="col-xs-2 redtips padding-clear">' + util.getLan("trade.tips.3",
                            key + 1)
                        + '</span>' + '<span class="col-xs-5 text-right padding-clear">'
                        + util.numFormat(priceOffset(Number(value.price), 0), trade.cnyDigit)
                        + '</span>' + '<span class="col-xs-5 redtips text-right padding-clear">'
                        + util.numFormat(amountOffset(Number(value.amount), 0), trade.coinDigit)
                        + '</span></li>');
                } else {
                    buyele.data().money = util.numFormat(priceOffset(Number(value.price), 0), trade.cnyDigit);
                    buyele.data().num = util.numFormat(amountOffset(Number(value.amount), 0), trade.coinDigit);
                    buyele.children()[1].innerHTML =
                        util.numFormat(priceOffset(Number(value.price), 0), trade.cnyDigit);
                    buyele.children()[2].innerHTML =
                        util.numFormat(amountOffset(Number(value.amount), 0), trade.coinDigit);
                }
            });
            for (var i = data.buys.length; i < buysellcount; i++) {
                $("#buy" + i).remove();
            }
            $.each(data.sells, function (key, value) {
                if (key >= buysellcount) {
                    return;
                }
                var sellele = $("#sell" + key);
                if (sellele.length == 0) {
                    $("#sellbox").prepend('<li id="sell' + key
                        + '"  class="list-group-item clearfix buysellmap" data-type="0" data-money="'
                        + util.numFormat(priceOffset(Number(value.price), 1), trade.cnyDigit)
                        + '" data-num="' + util.numFormat(amountOffset(Number(value.amount), 1),
                            trade.coinDigit) + '">'
                        + '<span class="col-xs-2 greentips padding-clear">' + util.getLan(
                            "trade.tips.4", key + 1) + '</span>'
                        + '<span class="col-xs-5 text-right padding-clear">' + util.numFormat(
                            priceOffset(Number(value.price), 1), trade.cnyDigit) + '</span>'
                        + '<span class="col-xs-5 greentips text-right padding-clear">'
                        + util.numFormat(amountOffset(Number(value.amount), 1), trade.coinDigit)
                        + '</span></li>');
                } else {
                    sellele.data().money = util.numFormat(priceOffset(Number(value.price), 1), trade.cnyDigit);
                    sellele.data().num = util.numFormat(amountOffset(Number(value.amount), 1), trade.coinDigit);
                    sellele.children()[1].innerHTML =
                        util.numFormat(priceOffset(Number(value.price), 1), trade.cnyDigit);
                    sellele.children()[2].innerHTML =
                        util.numFormat(amountOffset(Number(value.amount), 1), trade.coinDigit);
                }
            });
            for (var i = data.sells.length; i < buysellcount; i++) {
                $("#sell" + i).remove();
            }
            if (trade.fristprice) {
                if (data.buys.length <= 0) {
                    $("#tradesellprice").val(util.numFormat(Number(data.p_new), trade.cnyDigit));
                    trade.setConvertPrice("tradesellpriceTips",data.p_new);
                } else {
                    $("#tradesellprice").val(util.numFormat(Number(data.buys[0].price), trade.cnyDigit));
                    trade.setConvertPrice("tradesellpriceTips",data.buys[0].price);
                }
                if (data.sells.length <= 0) {
                    $("#tradebuyprice").val(util.numFormat(Number(data.p_new), trade.cnyDigit));
                    var val =  $("#tradebuyprice").val();
                    var cny =  parseFloat(calculateCNY(val,sellBuy[1])).toFixed(2);
                    $("#tradebuyprice-cny").html("≈ "+cny+" CNY");
                    trade.setConvertPrice("tradebuypriceTips",data.p_new);
                } else {
                    $("#tradebuyprice").val(util.numFormat(Number(data.sells[0].price), trade.cnyDigit));
                    trade.setConvertPrice("tradebuypriceTips",data.sells[0].price);
                }
                var val =  $("#tradebuyprice").val();
                var cny =  parseFloat(calculateCNY(val,sellBuy[1])).toFixed(2);
                $("#tradebuyprice-cny").html("≈ "+cny+" CNY");

                var val2 =  $("#tradesellprice").val();
                var cny2 =  parseFloat(calculateCNY(val2,sellBuy[1])).toFixed(2);
                $("#tradesellprice-cny").html("≈ "+cny2+" CNY");

                trade.fristprice = false;
            }
            $(".buysellmap").on("click", function () {
                var data = $(this).data();
                var type = data.type;
                var money = data.money;
                var num = data.num;
                var tradeAmount = "";
                if (type == 0) {
                    tradeAmount = $("#tradebuyamount").val();
                } else {
                    tradeAmount = $("#tradesellamount").val();
                }
                if (tradeAmount == "") {
                    tradeAmount = 0;
                }
                trade.antoTurnover(money, 0, tradeAmount, type);
            });

            var p_new = util.numFormat(Number(data.p_new), trade.cnyDigit);
            $("#lastprice").html(data.symbol + p_new);
            $("#labLastPrice").html(p_new);
            $("#buyFirst").html(util.numFormat(Number(data.buy), util.DEF_CNY_SCALE));
            $("#sellFirst").html(util.numFormat(Number(data.sell), util.DEF_CNY_SCALE));
            $("#volume").html(util.numFormat(Number(data.total), util.DEF_CNY_SCALE));
            if (p_new > trade.lastprice) {
                $("#lastprice").parent().removeClass("greentips").addClass("redtips");
                $("#lastpriceicon").removeClass("green").addClass("red");
                $("#labLastPrice").parent().removeClass("greentips").addClass("redtips");
                $("#labDirection").removeClass("down").addClass("up");
            } else if (p_new < trade.lastprice) {
                $("#lastprice").parent().removeClass("redtips").addClass("greentips");
                $("#lastpriceicon").removeClass("red").addClass("green");
                $("#labLastPrice").parent().removeClass("redtips").addClass("greentips");
                $("#labDirection").removeClass("up").addClass("down");
            }
            trade.lastprice = p_new;
        };
        util.network({url: url, method: "get", success: marketCallback});
        if ($("#login").val() == "true") {
            url = "/real/userassets.html";
            var splitNum = function (num, digit) {
                if (typeof (digit) == 'undefined') {
                    digit = 4;
                }
                if (num == null) {
                    return null;
                }
                num = num + '';
                var end = "";
                var start = "";
                var money = [0, 0];
                if (num.toString().split(".") != null && num.toString().split(".")[1] != null) {
                    start = num.toString().split(".")[0];
                    end = num.toString().split(".")[1];
                    if (end.length > digit) {
                        end = end.substring(0, digit);
                    } else if (end.length < digit) {
                        var endCount = digit - end.length;
                        for (var i = 0; i < endCount; i++) {
                            end += "0";
                        }
                    }
                    money = [start, end]
                } else {
                    start = num;
                    for (var i = 0; i < digit; i++) {
                        end += "0";
                    }
                    money = [start, end]
                }
                return money;
            };
            var param = {
                tradeid: symbol
            };
            var assetsCallback = function (data) {
                if (data.code != 200) {
                    return;
                }
                data = data.data;
                if (data != "") {
                    var buyCoin = data.buyCoin;
                    var buyCoinSplit = splitNum(Number(buyCoin.total), util.DEF_CNY_SCALE);
                    $("#tradeBuyInt").html(buyCoinSplit[0]);
                    $("#tradeBuyDig").html("." + buyCoinSplit[1]);
                    $("#tradeBuyCoin").val(util.numFormat(Number(buyCoin.total), util.DEF_CNY_SCALE));
                    var sellCoin = data.sellCoin;
                    var sellCoinSplit = splitNum(Number(sellCoin.total), util.DEF_COIN_SCALE);
                    $("#tradeCoinInt").html(sellCoinSplit[0]);
                    $("#tradeCoinDig").html("." + sellCoinSplit[1]);
                    $("#tradeSellCoin").val(util.numFormat(Number(sellCoin.total), util.DEF_COIN_SCALE));

                    // if ($("#headerscore").length > 0) {
                    //     $("#headerscore").html(util.numFormat(Number(data.score), 0));
                    // }
                    // if ($("#headercoin" + buyCoin.id).length > 0) {
                    //     var coinchild = $("#headercoin" + buyCoin.id).children();
                    //     coinchild[1].innerHTML = util.numFormat(Number(buyCoin.total, trade.coinDigit));
                    //     coinchild[2].innerHTML = util.numFormat(Number(buyCoin.frozen, trade.coinDigit));
                    //     coinchild[3].innerHTML = util.numFormat(Number(buyCoin.borrow, trade.coinDigit));
                    // }
                    // if ($("#headercoin" + sellCoin.id).length > 0) {
                    //     var coinchild = $("#headercoin" + sellCoin.id).children();
                    //     coinchild[1].innerHTML = util.numFormat(Number(sellCoin.total, trade.coinDigit));
                    //     coinchild[2].innerHTML = util.numFormat(Number(sellCoin.frozen, trade.coinDigit));
                    //     coinchild[3].innerHTML = util.numFormat(Number(sellCoin.borrow, trade.coinDigit));
                    // }
                }
            };
            util.network({url: url, param: param, success: assetsCallback});
            var entrutsUrl = "/real/getEntruts.html";
            var entrutsParam = {
                symbol: symbol,
                count: 5
            };
            var entrutsCallbcack = function (result) {
                if (result.code == 200) {
                    var entrutsCur = "";
                    $.each(result.data.entrutsCur, function (key, value) {
                        entrutsCur += '<article>';
                        entrutsCur += '<span class="col-1">' + value.time + '</span>';
                        entrutsCur +=
                            '<span class="col-2 ' + (value.type == 0 ? 'text-success' : 'text-danger') + '">'
                            + value.types + '</span>';
                        entrutsCur += '<span class="col-2">' + value.source + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.price,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + value.sellsymbol + " " + util.numFormat(value.count,
                            trade.coinDigit)
                            + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.last,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + value.sellsymbol + " " + util.numFormat(value.leftcount,
                            trade.coinDigit)
                            + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.successamount,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsCur +=
                            '<span class="col-3">' + (value.type == 1 ? value.buysymbol : value.sellsymbol) + " "
                            + util.numFormat(value.fees, util.DEF_FEE_SCALE) + '</span>';
                        entrutsCur += '<span class="col-4">' + value.status + '</span>';
                        entrutsCur += '<span class="col-4">';
                        entrutsCur +=
                            '<a class="tradecancel opa-link" href="javascript:void(0);" data-fid="' + value.id + '">'
                            + util.getLan("trade.tips.5") + '</a>';
                        entrutsCur += '</span>';
                        entrutsCur += '</article>';
                    });
                    if (entrutsCur == "") {
                        $("#entrutsCurData").html(
                            '<span style="height: 100%;width: 100%;text-align: center;padding-top: 20px;">'
                            + util.getLan("trade.tips.6") + '</span>');
                        $("#entrutsCurFot").hide();
                    } else {
                        $("#entrutsCurData").html("").append(entrutsCur);
                        $(".tradecancel").on("click", function () {
                            var id = $(this).data().fid;
                            trade.cancelEntrustBtc(this, id);
                        });
                        if (result.data.entrutsCur.length == 5) {
                            $("#entrutsCurFot").show();
                        } else {
                            $("#entrutsCurFot").hide();
                        }
                    }
                    var entrutsHis = "";
                    $.each(result.data.entrutsHis, function (key, value) {
                        entrutsHis += '<article>';
                        entrutsHis += '<span class="col-1">' + value.time + '</span>';
                        entrutsHis +=
                            '<span class="col-2 ' + (value.type == 0 ? 'text-success' : 'text-danger') + '">'
                            + value.types + '</span>';
                        entrutsHis += '<span class="col-2">' + value.source + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.price,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + value.sellsymbol + " " + util.numFormat(value.count,
                            trade.coinDigit)
                            + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.last,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + value.sellsymbol + " " + util.numFormat(value.leftcount,
                            trade.coinDigit)
                            + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + value.buysymbol + " " + util.numFormat(value.successamount,
                            trade.cnyDigit)
                            + '</span>';
                        entrutsHis +=
                            '<span class="col-3">' + (value.type == 1 ? value.buysymbol : value.sellsymbol) + " "
                            + util.numFormat(value.fees, util.DEF_FEE_SCALE) + '</span>';
                        entrutsHis += '<span class="col-4">' + value.status + '</span>';
                        entrutsHis += '<span class="col-4">';
                        entrutsHis +=
                            (value.successamount > 0
                                ? ('<a class="tradelook opa-link" href="javascript:void(0);" data-value="' + value.id
                                    + '">' + util.getLan("trade.tips.16") + '</a>') : '');
                        entrutsHis += '</span>';
                        entrutsHis += '</article>';
                    });
                    if (entrutsHis == "") {
                        $("#entrutsHisData").html(
                            '<span style="height: 100%;width: 100%;text-align: center;padding-top: 20px;">'
                            + util.getLan("trade.tips.6") + '</span>');
                        $("#entrutsHisFot").hide();
                    } else {
                        $("#entrutsHisData").html("").append(entrutsHis);
                        $(".tradelook").on("click", function () {
                            var id = $(this).data().value;
                            trade.entrustLog(id);
                        });
                        if (result.data.entrutsHis.length == 5) {
                            $("#entrutsHisFot").show();
                        } else {
                            $("#entrutsHisFot").hide();
                        }
                    }
                }
            };
            util.network({url: entrutsUrl, param: entrutsParam, success: entrutsCallbcack});
        }
        window.setTimeout(function () {
            trade.autoRefresh();
        }, 2000);
    },
    closepriceclock: function (obj) {
        var id = $(obj).attr("id").replace("switchbtn_", "");
        var url = "/user/update_user_priceclock.html";
        var highprice = $("#highprice_" + id).val();
        var lowprice = $("#lowprice_" + id).val();
        var param = {
            "fcoinid": id,
            "fmaxprice": highprice,
            "fminprice": lowprice,
            "fisopen": 0
        };
        var callback = function (data) {
            if (data.code == -1) {
                util.layerAlert("", data.msg, 2);
                return;
            } else {
                $(obj).removeClass("openbtn");
                $(obj).addClass("closebtn");

                $("#highprice_" + id).attr("readonly", false);
                $("#lowprice_" + id).attr("readonly", false);

                $(obj).attr("data-toggle", "modal");
                $(obj).attr("data-target", "#priceclock");

                return;
            }
        };
        util.network({url: url, param: param, success: callback});
    },
    openpriceclock: function (obj) {
        var id = $(obj).attr("id").replace("confirmPriceClock_", "");
        var url = "/user/update_user_priceclock.html";
        var highprice = $("#highprice_" + id).val();
        var lowprice = $("#lowprice_" + id).val();
        if (highprice == null || highprice == "" || lowprice == null || lowprice == "") {
            return false;
        }
        var param = {
            "fcoinid": id,
            "fmaxprice": highprice,
            "fminprice": lowprice,
            "fisopen": 1
        };
        var callback = function (data) {
            if (data.code == -1) {
                util.layerAlert("", data.msg, 2);
                var modal = $("#priceclock");
                modal.modal("hide");
                return;
            } else {
                $(".switchbtn").addClass("openbtn");
                $(".switchbtn").removeClass("closebtn");

                $("#highprice_" + id).attr("readonly", true);
                $("#lowprice_" + id).attr("readonly", true);

                $(".switchbtn").removeAttr("data-toggle");
                $(".switchbtn").removeAttr("data-target");

                var modal = $("#priceclock");
                modal.modal("hide");

                $(".openbtn").on("click", function () {
                    trade.closepriceclock(this);
                });
                return;
            }
        };
        util.network({url: url, param: param, success: callback});
    },

    getTime: function (date, isremote) {
        var etime = date;
        if (isremote) {
            var timetype = [4, 2, 2, 2, 2, 2, 3];
            var timearr = [0, 0, 0, 0, 0, 0, 0];
            var time = date;
            var subindex = 0;
            for (ii = 0; ii < timetype.length; ii++) {
                timearr[ii] = time.substring(subindex, subindex + timetype[ii]);
                subindex += timetype[ii];
            }
            etime =
                Math.round(
                    new Date(timearr[0], parseInt(timearr[1]) - 1, timearr[2], timearr[3], timearr[4], timearr[5],
                        timearr[6]).getTime());
        }
        return etime;
    },
    getBtcPrice: function () {
        if (parseInt($("#tradeType").val()) !== 2) {
            return;
        }
        var url = "https://huobi.huobi.com/staticmarket/ticker_btc_json.js"
            , callback = function (result) {
            if (result === null || typeof result === "undefined") {
                return;
            }
            trade.btcPrice = result.ticker.last;
        };
        util.network({url: url, success: callback, method: "get"});
        window.setTimeout(function () {
            trade.getBtcPrice();
        }, 5000);
    },
    setConvertPrice:function(id,value){
        if (parseInt($("#tradeType").val()) === 2) {
            if (value != "" && value != "0") {
                $("#"+id).text(" ≈ " + util.numFormat(util.accMul(trade.btcPrice, value), 2) + " CNY");
            }
        }
    }

};

$(function () {
    //trade.getBtcPrice();
    $("#tradebuyprice").on("keyup", function () {
        trade.numVerify(0);
        trade.setConvertPrice("tradebuypriceTips",this.value);
    }).on("keypress", function (event) {
        return util.goIngKeypress(this, event, trade.cnyDigit);
    }).on("click", function () {
        this.focus();
        this.select();
    });
    $("#tradesellprice").on("keyup", function () {
        trade.numVerify(1);
        trade.setConvertPrice("tradesellpriceTips",this.value);
    }).on("keypress", function (event) {
        return util.goIngKeypress(this, event, trade.cnyDigit);
    }).on("click", function () {
        this.focus();
        this.select();
    });
    $("#tradebuyamount").on("keyup", function () {
        trade.numVerify(0);
    }).on("keypress", function (event) {
        return util.goIngKeypress(this, event, trade.coinDigit);
    }).on("click", function () {
        this.focus();
        this.select();
    });
    $("#tradesellamount").on("keyup", function () {
        trade.numVerify(1);
    }).on("keypress", function (event) {
        return util.goIngKeypress(this, event, trade.coinDigit);
    }).on("click", function () {
        this.focus();
        this.select();
    });
    $("#buybtn").on("click", function () {
        trade.saveCoinTrade(0, true);
    });
    $("#sellbtn").on("click", function () {
        trade.saveCoinTrade(1, true);
    });
    $("#modalbtn").on("click", function () {
        trade.submitTradePwd();
    });
    $(".buysellmap").on("click", function () {
        var data = $(this).data();
        var type = data.type;
        var money = data.money;
        var num = data.num;
        var tradeAmount = "";
        if (type == 0) {
            tradeAmount = $("#tradebuyamount").val();
        } else {
            tradeAmount = $("#tradesellamount").val();
        }
        if (tradeAmount == "") {
            tradeAmount = 0;
        }
        trade.antoTurnover(money, 0, tradeAmount, type);
    });
    $(".panel-open").on("click", function () {
        trade.entrustInfoTab($(this));
    });
    $("#buyslider").on("change", function (e, val) {
        trade.onPortion(val, 0);
    });
    $("#sellslider").on("change", function (e, val) {
        trade.onPortion(val, 1);
    });
    $('#tradepass').on('shown.bs.modal', function (e) {
        util.callbackEnter(trade.submitTradePwd);
    });
    $('#tradepass').on('hidden.bs.modal', function (e) {
        document.onkeydown = function () {
        };
    });
    $(".tradecancel").on("click", function () {
        var id = $(this).data().value;
        trade.cancelEntrustBtc(this, id);
    });
    $(".tradelook").on("click", function () {
        var id = $(this).data().value;
        trade.entrustLog(id);
    });
    $(".openbtn").on("click", function () {
        trade.closepriceclock(this);
    });
    $(".confirmPriceClock").on("click", function () {
        trade.openpriceclock(this);
    });

    $(".priceinput").on("keypress", function (event) {
        return util.goIngKeypress(this, event, util.ENTER_CNY_SCALE);
    });
    trade.autoRefresh();
});