var login = {
    lastPriceList : {},
    login: function (ele) {
        var url = "/login.html";
        var uName = $("#indexLoginName").val();
        var pWord = $("#indexLoginPwd").val();
        var longLogin = 0;
        if (util.checkEmail(uName)) {
            longLogin = 1;
        }
        var des = util.isPassword(pWord);
        if (des != "") {
            util.layerAlert("", des);
            return;
        }
        var param = {
            loginName: uName,
            password: pWord,
            type: longLogin
        };
        var callback = function (data) {
            if (data.code == 200) {
                if ($("#forwardUrl").length > 0 && $("#forwardUrl").val() != "") {
                    var forward = $("#forwardUrl").val();
                    forward = decodeURI(forward);
                    window.location.href = forward;
                } else {
                    var whref = document.location.href;
                    if (whref.indexOf("#") != -1) {
                        whref = whref.substring(0, whref.indexOf("#"));
                    }
                    window.location.href = whref;
                }
            } else {
                util.layerAlert("", data.msg, 2);
                $("#indexLoginPwd").val("");
            }
        }
        ele = ele || $("#loginbtn")[0];
        util.network({btn: ele, url: url, param: param, success: callback, enter: true});
    },
    newsHover: function (ele) {
        $(".news-items").removeClass("active");
        $(".news-items").stop().animate({width: "345px"}, 50);
        $(ele).stop().animate({width: "450px"}, 50);
        $(ele).addClass("active");
    },
    aotoMarket: function () {
        var green = "#72c02c";
        var red = "#e74c3c";
        var url = "/real/indexmarket.html";
        var callback = function (result) {
            if (result.code != 200) {
                return;
            }

            var marketObject = {};
            $.each(result.data, function (index, data) {
                var market = "";
                var color = red;
                /*if(typeof(login.lastPriceList[data.tradeId]) !== "undefined" && login.lastPriceList[data.tradeId] < data.price){
                    color = green;
                }*/
                if(Number(data.rose)>=0){
                    color = green;
                }

                login.lastPriceList[data.tradeId] = data.rose;
                if (index % 2 == 1) {
                    market += "<article class='child-market'>";
                } else {
                    market += "<article class='child-market market-color'>";
                }
                market += "<div class=\"container market-item\">";
                market +=
                    "<span class=\"coin-name\"><img class='coin-logo' src='" + data.image + "' />" + data.sellname
                    + "</span>";
                market +=
                    "<span class=\"coin-price\">" + data.buysymbol + " " + util.numFormat(data.price, data.cnyDigit)
                    + "</span>";
                market +=
                    "<span class=\"coin-buy\">" + data.buysymbol + " " + util.numFormat(data.buy, data.cnyDigit)
                    + "</span>";
                market +=
                    "<span class=\"coin-sell\">" + data.buysymbol + " " + util.numFormat(data.sell, data.cnyDigit)
                    + "</span>";
                market +=
                    "<span class=\"coin-vol\">" + util.numFormat(data.total, data.coinDigit) + " " + data.sellsymbol
                    + "</span>";
                market += "<span class=\"coin-chg\" style='color:"+color+"'>" + data.rose + "%</span>";
                market += "<a href='/trademarket.html?symbol="+data.treadId+"&type="+data.type+"&sb="+data.sellShortName+"_"+data.buyShortName+"'>"+util.getLan("index.go.trade")+"</a>";
                market += "</div>";
                market += "</article>";
                marketObject[data.type] =
                    (typeof marketObject[data.type] === "undefined" ? "" : marketObject[data.type]) + market;
            });
            $(".child-market", ".market").remove();
            for (var type in marketObject) {
                $("#marketType" + type).append(marketObject[type]);
            }
        };
        util.network({url: url, param: {}, success: callback});
        setTimeout(login.aotoMarket, 5000);
    },
    aotoMarket2: function () {
        var green = "#72c02c";
        var red = "#e74c3c";
        var url = "/real/indexmarket.html";
        var callback = function (result) {
            if (result.code != 200) {
                return;
            }

            var marketObject = {};
            $.each(result.data, function (index, data) {
                var market = "";
                var color = red;
                /*if(typeof(login.lastPriceList[data.tradeId]) !== "undefined" && login.lastPriceList[data.tradeId] < data.price){
                    color = green;
                }*/
                if(Number(data.rose)>=0){
                    color = green;
                }

                login.lastPriceList[data.tradeId] = data.rose;
                if (index % 2 == 1) {
                    market += "<article data-symbol='"+data.treadId+"' data-status='"+data.status+"' class='child-market "+data.sellShortName+data.buyShortName+"'>";
                } else {
                    market += "<article data-symbol='"+data.treadId+"' data-status='"+data.status+"' class='child-market market-color "+data.sellShortName+data.buyShortName+"'>";
                }
                market += "<div class=\"container market-item\">";
                market +=
                    "<span class=\"coin-name\"><img class='coin-logo' src='" + data.image + "' />" + data.sellname
                    + "</span>";
                market +=
                    "<span class=\"coin-price\">0.00000000</span>";
                market +=
                    "<span class=\"coin-buy\">"
                    + "</span>";
                market +=
                    "<span class=\"coin-sell\">"
                    + "</span>";
                market +=
                    "<span class=\"coin-vol\" unit="+(data.sellShortName).toUpperCase()+">0.00</span>";
                market += "<span class=\"coin-chg\" style='color:"+color+"'>" + data.rose + "%</span>";
                market += "<span class=\"coin-trend\" ><div class=\"coin-trend-render\" style='width:200px;height:44px'></div></span>";
                market += "<a href='/trademarket.html?symbol="+data.treadId+"&type="+data.type+"&sb="+data.sellShortName+"_"+data.buyShortName+"'>"+util.getLan("index.go.trade")+"</a>";
                market += "</div>";
                market += "</article>";
                marketObject[data.type] =
                    (typeof marketObject[data.type] === "undefined" ? "" : marketObject[data.type]) + market;
                if(data.status == 3) {
                    subscribeList.push(data.sellShortName + data.buyShortName);
                }else{
                    platformSubscribe.push({"symbol":data.treadId,"id":data.sellShortName + data.buyShortName});
                }

            });
            $(".child-market", ".market").remove();
            for (var type in marketObject) {
                $("#marketType" + type).append(marketObject[type]);
            }

            var _data = [];
            var _category = [];
            for (var i = 0; i < 10; i++) {
                _data.push(1);
                _category.push(i);
            }

            var readers = document.getElementsByClassName('coin-trend-render');
            for(var i=0;i<readers.length;i++){
                var myChart = echarts.init(readers[i]);

                var option = {
                    tooltip: {},
                    legend: {
                        data:['']
                    },
                    grid:{
                        left:'5%',
                        top:'50%',
                        right:'5%',
                        bottom:'5%',
                    },
                    xAxis: {
                        show:false,
                        type:'category',
                        data: _category,
                    },
                    yAxis: {
                        show:false,
                    },
                    series: [{
                        type: 'line',
                        data: _data,
                        smooth:true,
                        symbol: 'none',
                        markLine:{
                            silent:true,
                        }
                    }],
                    animation:false,
                };

                // 使用刚指定的配置项和数据显示图表。
                myChart.setOption(option);
            }

            if(!isConnection){
                doConnection();
            }
        };
        util.network({url: url, param: {}, success: callback});
    },
    switchMarket: function () {
        $(".trade-tab").on("click", function () {
            var $that = $(this);
            var dataClass = $that.data().market;
            $(".trade-tab").removeClass("active");
            $that.addClass("active");
            $(".market-con").hide();
            $("#" + dataClass).show();
        })
    }
};

var ws = new WebSocket("wss://api.huobipro.com/ws");

var subscribeList = [];

var platformSubscribe = [];

var isConnection = false;

//需要计算btc 等于多少usdt
var BTC_USDT = 0;

var ETH_USDT = 0;

function doConnection(){
    ws.onopen = function () {
        isConnection = true;

        //订阅这两个是为了计算CNY值
        //ws.send(JSON.stringify({"sub":"market.btcusdt.detail","id":"btcusdt"}));
        //ws.send(JSON.stringify({"sub":"market.ethusdt.detail","id":"ethusdt"}));
        if (subscribeList.length == 0){
            login.aotoMarket2();
            return;
        }

        for(var i =0;i<subscribeList.length;i++){
            var subscribe = subscribeList[i];
            var fromTime = parseInt(new Date().getTime()/1000 - 24*60*60);
            var toTime = parseInt(new Date().getTime()/1000);
            ws.send(JSON.stringify({"sub":"market."+subscribe+".detail","id":subscribe}));
            ws.send(JSON.stringify({"req":"market."+subscribe+".kline.60min","id":subscribe,"from":fromTime,"to":toTime}));
        }

        for(var i =0;i<platformSubscribe.length;i++){
            fetchPlatformKLine(platformSubscribe[i].symbol,platformSubscribe[i].id);
        }
    };
}

function setMessageEvent(){
    ws.onmessage = function(event){
        //console.log("receive msg");
        var blob = event.data;
        var reader = new FileReader();
        reader.onload = function(e){
            var ploydata = new Uint8Array(e.target.result);
            var msg = pako.inflate(ploydata,{to:'string'});
            //console.log(msg);
            handleData(msg);
        };
        reader.readAsArrayBuffer(blob,"utf-8");
    };
    ws.onclose = function(){
        //console.log('connection closed');
    };
}

function handleData(msg){
    var data = JSON.parse(msg);
    //console.log(data);
    if (data.ping) {
        sendHeartMessage(data.ping);
        return;
    }

    if (data.rep && data.status == 'ok') {
        handleKlineData(data);
        return;
    }

    if (data.tick) {
        handleTickData(data);
        return
    }
}

//K线
function handleKlineData(data){
    var _data = [];
    var _category = [];
    for (var i = 0; i < data.data.length; i++) {
        var dataCell = data.data[i];
        _data.push(dataCell.close);
        _category.push(i);
    }

    var myChart = echarts.init(document.getElementsByClassName(data.id)[0].getElementsByClassName('coin-trend-render')[0]);
    var option = {
        tooltip: {},
        legend: {
            data:['']
        },
        grid:{
            left:'5%',
            top:'5%',
            right:'5%',
            bottom:'5%',
        },
        xAxis: {
            show:false,
            type:'category',
            data: _category,
        },
        yAxis: {
            show:false,
        },
        series: [{
            type: 'line',
            data: _data,
            smooth:true,
            symbol: 'none',
            markLine:{
                silent:true,
            }
        }],
        animation:false,
    };

    myChart.setOption(option);
}

function handleTickData(data){
    var level=Math.floor((data.tick.close-data.tick.open)/data.tick.close*10000);
    var cArr = data['ch'].match(/\.([a-z]+?)(btc|eth|usdt|gset)\./i);
    var dataCoin = cArr[1] + cArr[2];

    if (dataCoin == 'btcusdt') {
        BTC_USDT = data.tick.close;
    }
    if (dataCoin == 'ethusdt') {
        ETH_USDT = data.tick.close;
    }

    level /= 100;
    if(isNaN(level) || !level){
        level = 0.00;
    }
    level = parseFloat(level).toFixed(2);

    $("."+dataCoin+">.coin-chg").removeAttr("style");
    $("."+dataCoin+" .coin-price").removeClass('coin-price-up');
    $("."+dataCoin+" .coin-price").removeClass('coin-price-down');
    if(level>0){
        $("."+dataCoin+" .coin-chg").attr("style","color:#589065");
        $("."+dataCoin+" .coin-chg").html("+"+level+"%");
        $("."+dataCoin+" .coin-price").addClass('coin-price-up');
    }else{
        $("."+dataCoin+" .coin-chg").html(level+"%");
        $("."+dataCoin+" .coin-chg").attr("style","color:#AE4E54");
        $("."+dataCoin+" .coin-price").addClass('coin-price-down');
    }
    var preData = parseFloat($("."+dataCoin).attr('pre-data'));
    $("."+dataCoin).attr('pre-data',data.tick.close);
    $("."+dataCoin+" .coin-price").html(data.tick.close+'<i style="font-style: normal;">/￥'+calcCNY(cArr[2],data.tick.close).toFixed(2)+'</i>'); //成交额 即 sum(每一笔成交价 * 该笔的成交量)
    $("."+dataCoin+" .coin-buy").html(data.tick.high); //最高价
    $("."+dataCoin+" .coin-sell").html(data.tick.low); //最低价
    var unit = $("."+dataCoin+" .coin-vol").attr('unit');
    $("."+dataCoin+" .coin-vol").html(data.tick.amount.toFixed(2)+' '+unit); //成交量
}

function calcCNY(symbol,value){
    var result = 0;
    if(symbol.toLowerCase() == 'usdt'){
        result = USDT_CNY_RATE * value;
    }else if(symbol.toLowerCase() == 'gset'){
        result = value;
    }else{
        result = exchangeRate[symbol+'_gset'] * value;
    }
    if (isNaN(result) || !result){
        return 0;
    }
    return result;
}

function sendHeartMessage(ping){
    ws.send(JSON.stringify({"pong":ping}));
}

var exchangeRate = {};

/**
 * 获取平台k线
 */
function fetchPlatformKLine(symbol,_id) {
    var url = "/kline/fullperiod.html";
    var param = {
        symbol:symbol,
        step:900
    };
    var callback = function (data) {
        var _data = compatHuoBiKLine(data);
        handleKlineData({id:_id,data:_data});
    };
    util.network({url: url, param: param, success: callback});
}

function compatHuoBiKLine(data) {
    return (data != null && data.length >0)?data.map(function(item){
            return {high:item[1],open:item[2],low:item[3],close:item[4]};
    }):[];
}

/**
 * 平台
 * 左侧的实时价格
 */
function fetchRealTimePrice() {
    var symbols = [];
    $(".child-market").each(function(index,item){
        if($(item).data().status == 1){
            symbols.push($(item).data().symbol);
        }
    });
    var url = "/real/markets.html";
    var symbol = symbols.join(',');
    var param = {
        symbol:symbol
    };
    var callback = function (data) {
        if (data.code == 200) {
            for(var i=0;i<data.data.length;i++){
                var dataCoin = data.data[i].sellSymbol.toLowerCase()+''+data.data[i].buySymbol.toLowerCase();
                var tickData = {ch:"market."+dataCoin+".detail",tick:{
                    amount:data.data[i].total,
                    count:data.data[i].total,
                        open:data.data[i].p_open,
                        close:data.data[i].p_new,
                        high:data.data[i].sell,
                        low:data.data[i].buy,
                        vol:0,
                    }};

                handleTickData(tickData);

                //保存GSET对其他币种的价格
                exchangeRate[data.data[i].sellSymbol.toLowerCase()+'_'+data.data[i].buySymbol.toLowerCase()] = data.data[i].p_new;

            }
        }
    };
    util.network({url: url, param: param, success: callback});

    window.setTimeout(function () {
        fetchRealTimePrice();
    }, 1500);
}

$(function () {
    $("#indexLoginPwd").on("focus", function () {
        util.callbackEnter(login.login);
    });
    $("#loginbtn").on("click", function () {
        login.login(this);
    });
    $(".news-items").mouseover(function (ele) {
        login.newsHover(this);
    });

    login.switchMarket();
    login.aotoMarket2();

    doConnection();
    setMessageEvent();

    fetchRealTimePrice();


});
