var upColor = '#00da3c';
var downColor = '#ec0000';

var myChart = echarts.init(document.getElementById('chart-box'));

var myChart2 = null;

//var WS_URL  = 'wss://api.huobi.pro/ws';
var WS_URL = 'wss://api.huobipro.com/ws';

var PERIOD_1MIN = "1min";

var PERIOD_5MIN = "5min";

var PERIOD_15MIN = "15min";

var PERIOD_30MIN = "30min";

var PERIOD_60MIN = "60min";

var PERIOD_1DAY = "1day";

var PERIOD_1WEEK = "1week";

var PERIOD_1MON = "1mon";

//开始时间
var _from = 0;

//目标时间，默认是现在
var _to = formatDate(new Date);

//周期，默认15分钟
var _period = PERIOD_15MIN;

var periodValue = 60 * 15;

var chartAnimation = true;

//usdt_cny
var USDT_CNY = 6.5;

//需要计算btc 等于多少usdt
var BTC_USDT = 0;

//true平台，false火币
var isPlatformTrade = $("#isPlatformStatus").val() == 'true';

//符号 bchusdt、btcusdt
var _symbol = document.getElementById('hide-symbol').getAttribute('value').replace("_","");

var sellBuy = document.getElementById('hide-symbol').getAttribute('value').split("_");

document.getElementById('inner-price').innerHTML = '价格('+sellBuy[1].toUpperCase()+')';
document.getElementById('inner-amount').innerHTML = '数量('+sellBuy[0].toUpperCase()+')';
document.getElementById('inner-sum').innerHTML = '总价('+sellBuy[1].toUpperCase()+')';

//交易对
var liArray = document.getElementsByClassName('chart-panel')[0].getElementsByTagName('li');

var ws = new WebSocket(WS_URL);
var isConnection = false;

for (var i = 0; i < liArray.length; i++) {
    var liObj = liArray[i];
    liObj.index = i;
    liObj.addEventListener('click',function(e){
        for (var j = 0; j < liArray.length; j++) {
            liArray[j].removeAttribute('class');
        }

        liArray[this.index].setAttribute('class','time-selected');
        //console.log(liArray[this.index].innerHTML);
        _period = liArray[this.index].innerHTML;
        chartAnimation = true;
        requestData();
        subscribeData();
    });
}

initTab();

function initTab(){
    var tabItems = document.getElementsByClassName('coin-tab')[0].getElementsByTagName('span');
    var contentItem = document.getElementsByClassName('coin-list-item');
    var urlItems = document.getElementsByClassName('coin-list')[0].getElementsByTagName('li');
    for (var i = 0; i < tabItems.length; i++) {
        var tab = tabItems[i];
        tab.index = i;
        tab.addEventListener('click',function(){
            for (var j = 0; j < tabItems.length; j++) {
                tabItems[j].removeAttribute('class');
                contentItem[j].setAttribute('style','display:none');
            }
            this.setAttribute('class','tab-active');
            contentItem[this.index].removeAttribute('style');
        });
    }

    //url链接
    for (var i = 0; i < urlItems.length; i++) {
        urlItems[i].addEventListener('click',function(){
            window.location.href="/trademarket.html?sb="+this.getAttribute('data-symbol')+"&type="+this.getAttribute("type")+"&symbol="+this.getAttribute("symbol");
        });
    }

    // //usdt交易区
    // if (sellBuy[1] == 'usdt') {
    //     tabItems[0].setAttribute('class','tab-active');
    //     contentItem[0].removeAttribute('style');
    // }else if (sellBuy[1] == 'btc') {
    //     tabItems[1].setAttribute('class','tab-active');
    //     contentItem[1].removeAttribute('style');
    // }
}

/**
 *date 可以用setTime(毫秒) 修改时间
 */
function formatDate(date,pattern){
    var month = date.getMonth() + 1;
    if (month<10) {
        month = "0" + month;
    }

    var day = date.getDate();
    if (day<10) {
        day = "0" + day;
    }

    var hour = date.getHours();
    if (hour<10) {
        hour = "0" + hour;
    }

    var minute = date.getMinutes();
    if (minute<10) {
        minute = "0" + minute;
    }

    var seconds = date.getSeconds();
    if (seconds<10) {
        seconds = "0" + seconds;
    }

    var year = date.getFullYear();

    if (pattern) {
        return hour+":"+minute+":"+seconds;
    }
    return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+seconds;
}

function calculateMA(dayCount, data) {
    var result = [];
    for (var i = 0, len = data.length; i < len; i++) {
        if (i < dayCount) {
            result.push('-');
            continue;
        }
        var sum = 0;
        for (var j = 0; j < dayCount; j++) {
            sum += data[i - j][1];
        }
        result.push(sum / dayCount);
    }
    return result;
}

function setupData(_rawData){
    rawData = _rawData;
    var tempTime = _from * 1000;
    var data = rawData.map(function (item) {
        return [item.open,item.close,item.low,item.high];
    });

    var date = new Date();
    var dates = data.map(function (item) {
        date.setTime(tempTime);
        tempTime += periodValue*1000;
        return formatDate(date);
    });

    var volumes = rawData.map(function(item,index){
        return [index,item.vol,item.close>item.open?1:-1];
    });

    var option = {
        backgroundColor: '#181B2A',
        legend: {
            data: ['日K', 'MA5', 'MA10', 'MA20', 'MA30'],
            inactiveColor: '#777',
            textStyle: {
                color: '#fff'
            }
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                animation: false,
                type: 'cross',
                lineStyle: {
                    color: '#376df4',
                    width: 2,
                    opacity: 1
                }
            }
        },
        visualMap: {
            show: false,
            seriesIndex: 5,
            dimension: 2,
            pieces: [{
                value: 1,
                color: downColor
            }, {
                value: -1,
                color: upColor
            }]
        },
        grid:[
            {
                left:0,
                right:80,
                height:'75%'
            },
            {
                left:0,
                right:80,
                top:'87%',
                height:'10%',
            }
        ],
        xAxis: [
            {	type: 'category',
                scale: true,
                boundaryGap : false,
                offset:0,
                type: 'category',
                data: dates,
                splitNumber: 20,
                splitLine: {show: true,lineStyle:{color:"#8392A5",width:0.2,opacity:0.5}},
                axisTick: {show:false},
                axisLabel: {show:false},
                axisLine: {show:true,onZero:false,lineStyle: { color: '#8392A5' } },
                min: 'dataMin',
                max: 'dataMax',
                axisPointer: {
                    z: 100
                }
            },

            {	type: 'category',
                gridIndex: 1,
                scale: true,
                boundaryGap : false,
                offset:0,
                type: 'category',
                data: dates,
                axisLine: {show:true,onZero:false,lineStyle: { color: '#8392A5',width:0.5} },
                min: 'dataMin',
                max: 'dataMax',
                axisPointer: {
                    z: 100
                }
            }
        ],
        yAxis: [
            {
                position:'right',
                scale: true,
                axisLine: { lineStyle: { color: '#8392A5' } },
                splitLine: { show: true,lineStyle:{color:'#8392A5',width:0.2}}
            },
            {
                position:'right',
                scale: true,
                gridIndex: 1,
                splitNumber: 2,
                axisLabel: {show: true},
                axisLine: {lineStyle: { color: '#8392A5'} },
                axisTick: {show: false},
                splitLine:{show:false},
                min: 'dataMin',
                max: 'dataMax',
            }
        ],
        dataZoom: [
            {
                type: 'inside',
                xAxisIndex: [0, 1],
                start: 5,
                end: 100
            },
            {
                show: true,
                xAxisIndex: [0, 1],
                type: 'inside',
                top: '85%',
                start: 5,
                end: 100
            }
        ],
        animation: chartAnimation,
        series: [
            {
                type: 'candlestick',
                name: '日K',
                data: data,
                itemStyle: {
                    normal: {
                        color: '#0CF49B',
                        color0: '#FD1050',
                        borderColor: '#0CF49B',
                        borderColor0: '#FD1050'
                    }
                }
            },
            {
                name: 'MA5',
                type: 'line',
                data: calculateMA(5, data),
                smooth: true,
                showSymbol: false,
                lineStyle: {
                    normal: {
                        width: 1
                    }
                }
            },
            {
                name: 'MA10',
                type: 'line',
                data: calculateMA(10, data),
                smooth: true,
                showSymbol: false,
                lineStyle: {
                    normal: {
                        width: 1
                    }
                }
            },
            {
                name: 'MA20',
                type: 'line',
                data: calculateMA(20, data),
                smooth: true,
                showSymbol: false,
                lineStyle: {
                    normal: {
                        width: 1
                    }
                }
            },
            {
                name: 'MA30',
                type: 'line',
                data: calculateMA(30, data),
                smooth: true,
                showSymbol: false,
                lineStyle: {
                    normal: {
                        width: 1
                    }
                }
            },
            {
                name: 'Volume',
                type: 'bar',
                xAxisIndex: 1,
                yAxisIndex: 1,
                data: volumes
            }
        ]
    };
    myChart.setOption(option);
}

//请求或则订阅的数据
function getSendData() {
    _from = parseInt(calculateFromDateLong());
    _to = parseInt(new Date().getTime() / 1000);
    var req = {"req": "market." + _symbol + ".kline." + _period, "id": _symbol, "from": _from, "to": _to};
    return JSON.stringify(req);
}

/**
 * 最多300条
 * @returns {number}
 */
function calculateFromDateLong(){
    var date = new Date();
    //取5小时以前
    if (_period == PERIOD_1MIN) {
        periodValue = 60;
        return date.getTime()/1000 - 5*60*60;
    }

    //取24小时以前
    if (_period == PERIOD_5MIN) {
        periodValue = 60*5;
        return date.getTime()/1000 -5*300*60;
    }

    //80小时以前
    if (_period == PERIOD_15MIN) {
        periodValue = 60*15;
        return date.getTime()/1000 - 15*300*60;
    }

    //5天的数据
    if (_period == PERIOD_30MIN) {
        periodValue = 60*30;
        return date.getTime()/1000 - 30*300*60;
    }

    //7天的数据
    if (_period == PERIOD_60MIN) {
        periodValue = 60*60;
        return date.getTime()/1000 - 60*300*60;
    }

    //2个月之前的数据
    if (_period == PERIOD_1DAY) {
        periodValue = 60*60*24;
        return date.getTime()/1000 - 24*60*300*60;
    }

    //2个办月之前的数据
    if (_period == PERIOD_1WEEK) {
        periodValue = 60*60*24*7;
        return date.getTime()/1000 -  7*24*60*300*60;
    }

    //三个月的数据
    if (_period == PERIOD_1MON) {
        periodValue = 60*60*24*7*4;
        return date.getTime()/1000 - 4*24*60*300*60;
    }
}

function requestData(){
    if (isPlatformTrade){
        //获取平台数据
        _from = parseInt(calculateFromDateLong());
        _to = parseInt(new Date().getTime() / 1000);
        var url = "/kline/fullperiod.html";
        var param = {
            symbol:$("#symbol").val(),
            step:parseInt(_period) * 60
        };
        var callback = function (data) {
            handleResponseData(data);
        };
        util.network({url: url, param: param, success: callback});
        return;
    }
    var data = getSendData();
    ws.send(data);
}

function subscribeData(){
    if (!isPlatformTrade) {
        ws.send(JSON.stringify({"unsub": "market." + _symbol + ".depth.step0", "id": _symbol}));
        ws.send(JSON.stringify({"unsub": "market." + _symbol + ".detail", "id": _symbol}));

        ws.send(JSON.stringify({"sub": "market." + _symbol + ".depth.step0", "id": _symbol}));
        ws.send(JSON.stringify({"sub": "market." + _symbol + ".detail", "id": _symbol}));

        //请求实时成交
        ws.send(JSON.stringify({"req": "market." + _symbol + ".trade.detail", "id": _symbol}));
        //订阅实时成交
        ws.send(JSON.stringify({"sub": "market." + _symbol + ".trade.detail", "id": _symbol}));
    }

    //订阅详情
    ws.send(JSON.stringify({"sub":"market.btcusdt.detail","id":"btcusdt"}));
    ws.send(JSON.stringify({"sub":"market.bchusdt.detail","id":"bchusdt"}));
    ws.send(JSON.stringify({"sub":"market.ltcusdt.detail","id":"ltcusdt"}));
    ws.send(JSON.stringify({"sub":"market.ethusdt.detail","id":"ethusdt"}));
    ws.send(JSON.stringify({"sub":"market.etcusdt.detail","id":"etcusdt"}));
    ws.send(JSON.stringify({"sub":"market.btmusdt.detail","id":"btmusdt"}));

    ws.send(JSON.stringify({"sub":"market.bchbtc.detail","id":"bchbtc"}));
    ws.send(JSON.stringify({"sub":"market.ethbtc.detail","id":"ethbtc"}));
    ws.send(JSON.stringify({"sub":"market.etcbtc.detail","id":"etcbtc"}));
    ws.send(JSON.stringify({"sub":"market.btmbtc.detail","id":"btmbtc"}));
    ws.send(JSON.stringify({"sub":"market.ltcbtc.detail","id":"ltcbtc"}));

    ws.send(JSON.stringify({"sub":"market.btmeth.detail","id":"btmeth"}));

}

ws.onopen = function () {
    isConnection = true;

    requestData();

    subscribeData();
};

ws.onmessage = function(event){
    var blob = event.data;
    var reader = new FileReader();
    reader.onload = function(e){
        var ploydata = new Uint8Array(e.target.result);
        var msg = pako.inflate(ploydata,{to:'string'});
        handleData(msg);
    };
    reader.readAsArrayBuffer(blob,"utf-8");
};

ws.onclose = function(){
    isConnection = false;
};

function showData(xData,buyData,sellData){
    if (myChart2 == null) {
        myChart2 = echarts.init(document.getElementById('depth-chart'));
    }
    // 指定图表的配置项和数据
    var option = {
        title: {
            text: ''
        },
        tooltip : {
            show:true,
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor:'rgb(19,21,32)'
                }
            }
        },
        backgroundColor: '#262A42',
        legend: {
            data:['','','','','']
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                boundaryGap : false,
                data : xData,
                axisLine:{
                    show:true,
                    lineStyle:{
                        color:"#fff"
                    }
                },
                splitLine:{
                    show:false,
                },
                splitNumber: 20,

            }
        ],
        yAxis : [
            {
                type : 'value',
                axisLine:{
                    show:true,
                    lineStyle:{
                        color:"#fff"
                    }
                },
                splitLine:{
                    show:false,
                },
            }
        ],
        series : [
            {
                name:'',
                type:'line',
                stack: '总量',
                symbol:"none",
                lineStyle: {color:"#00000000",opacity:1},
                areaStyle: {color:"rgb(27,39,21)",opacity:1},
                data:buyData,
                markPoint:{
                    symbol:'rect'
                },
                symbolSize:"50px"
            },
            {
                name:'',
                type:'line',
                stack: '总量',
                symbol:"none",
                lineStyle: {color:"#00000000",opacity:1},
                areaStyle: {color:"rgb(43,25,38)",opacity:1},
                data:sellData
            }
        ]
    };
    // 使用刚指定的配置项和数据显示图表。
    myChart2.setOption(option);
}

function handleData(msg){
    var data = JSON.parse(msg);
    //console.log(data);
    if (data.ping) {
        sendHeartMessage(data.ping);
        return;
    }

    //请求k线
    if (data.status == 'ok' && data.rep && data.rep.indexOf('kline')>0 ) {
        handleResponseData(data);
        return
    }

    //请求实时交易
    if (data.status == 'ok' && data.rep && data.rep.indexOf("trade")>0) {
        handleTradeDetail(data.data);
        return
    }

    //订阅实时交易
    if (data.ch && data.ch.indexOf('trade')>0 && data.tick.data) {
        handleTradeDetail(data.tick.data,true);
        return;
    }

    //订阅详情
    if (data.ch && data.ch.indexOf("detail")>0 && data.tick) {
        handleDetail(data);
        return;
    }

    //订阅最新价
    if (data.tick) {
        handleTickData(data.tick);
        handleDepthTick(data.tick);
        return;
    }
}

/**
 * 深度图
 * @param tick
 */
function handleDepthTick(tick){
    var bids = tick.bids;
    var asks = tick.asks;

    var xData   = [];
    var buyData = [];
    var sellData = [];
    //买入
    for (var i = 0; i < bids.length; i++) {
        var bid = bids[i];
        xData.push(bid[0].toFixed(2));
        var total = 0;
        for (var j = 0; j <=i; j++) {
            total += bids[j][1];
        }
        buyData.push(total);
    }

    //卖出
    for (var i = 0; i < asks.length; i++) {
        var ask = asks[i];
        xData.push(ask[0].toFixed(2));
        var total = 0;
        for (var j = 0; j <=i; j++) {
            total += asks[j][1];
        }
        sellData.push(total);
    }

    //买
    buyData.sort(function(x,y){
        return y - x;
    });

    sellData.sort(function(x,y){
        return x - y;
    });

    //把后面部分的数据拼接成0
    buyData =  buyData.concat(new Array(asks.length));

    //把签名部分的数据拼接成0
    sellData = new Array(bids.length).concat(sellData);

    xData.sort(function(x,y){
        return x - y;
    });

    showData(xData,buyData,sellData);
}

function handleTradeDetail(data,isInsert){
    var timeBox = document.getElementsByClassName('market-trades-time')[0];
    var typeBox = document.getElementsByClassName('market-trades-type')[0];
    var priceBox = document.getElementsByClassName('market-trades-price')[0];
    var amountBox = document.getElementsByClassName('market-trades-amount')[0];
    for (var i = 0; i < data.length; i++) {
        var dataCell = data[i];
        //时间
        var date = new Date();
        date.setTime(dataCell.ts);
        var timeDDTag = document.createElement('dd');
        timeDDTag.appendChild(document.createTextNode(formatDate(date,true)));
        if (isInsert) {
            var _dd = timeBox.getElementsByTagName('dd')[0];
            timeBox.insertBefore(timeDDTag,_dd);
        }else{
            timeBox.appendChild(timeDDTag);
        }

        //类型 卖、买
        var typeDDTag = document.createElement('dd');
        typeDDTag.appendChild(document.createTextNode(dataCell.direction == 'sell'?'卖出':'买入'));
        typeDDTag.className = (dataCell.direction == 'sell'?'sell-color':'buy-color');
        if (isInsert) {
            var _dd = typeBox.getElementsByTagName('dd')[0];
            typeBox.insertBefore(typeDDTag,_dd);
        }else{
            typeBox.appendChild(typeDDTag);
        }

        //价格
        var priceDDTag = document.createElement('dd');
        priceDDTag.appendChild(document.createTextNode(dataCell.price));
        if (isInsert) {
            var _dd = priceBox.getElementsByTagName('dd')[0];
            priceBox.insertBefore(priceDDTag,_dd);
        }else{
            priceBox.appendChild(priceDDTag);
        }

        //数量
        var amountDDTag = document.createElement('dd');
        amountDDTag.appendChild(document.createTextNode(parseFloat(dataCell.amount).toFixed(4)));
        if (isInsert) {
            var _dd = amountBox.getElementsByTagName('dd')[0];
            amountBox.insertBefore(amountDDTag,_dd);
        }else{
            amountBox.appendChild(amountDDTag);
        }
    }
}

/**
 * 心跳
 * @param ping
 */
function sendHeartMessage(ping){
    ws.send(JSON.stringify({"pong":ping}));
}

/**
 * k线数据
 * @param data
 */
function handleResponseData(data){
    setupData(compatHuoBiKLine(data));
    setTimeout(function () {
        chartAnimation = false;
        requestData();
    },2000);
}

//open high low close amount
// 交易两个平台的数据
function compatHuoBiKLine(data) {
    if(isPlatformTrade){
        return (data != null && data.length >0)?data.map(function(item){
            return {open:item[1],high:item[2],low:item[3],close:item[4],vol:item[5]};
        }):[];
    }
    return data.data;
}

function handleTickData(tick){
    var dl = document.getElementsByClassName('cell');
    //卖
    var asks = tick.asks;
    //买
    var bids = tick.bids;

    var total = 7;
    var bids_data = [];
    //console.log(dl.length);
    //console.log(asks);
    for (var i = 0; i < dl.length; i++) {
        var d = dl[i];
        var spans = d.getElementsByTagName('span');
        if (i<total) {
            if(sellBuy[1].toUpperCase() == 'USDT'){
                spans[1].innerHTML = (parseFloat(asks[total-1-i][0]).toFixed(2));
            }else{
                if (sellBuy[0].toUpperCase() == 'BTM'){
                    spans[1].innerHTML = (parseFloat(asks[total-1-i][0]).toFixed(8));
                }else{
                    spans[1].innerHTML = (parseFloat(asks[total-1-i][0]).toFixed(6));
                }
            }
            spans[2].innerHTML = (parseFloat( asks[total-1-i][1])).toFixed(4);
            spans[3].innerHTML = (asks[total-1-i][0] * asks[total-1-i][1]).toFixed(4);
        }else{
            bids_data.push(bids[i-total]);
        }
    }

    //bids_data.reverse();

    for (var i = total; i < dl.length; i++) {
        var d = dl[i];
        var spans = d.getElementsByTagName('span');
        if(sellBuy[1].toUpperCase() == 'USDT') {
            spans[1].innerHTML = (parseFloat(bids_data[i-total][0]).toFixed(2));
        }else{
            if (sellBuy[0].toUpperCase() == 'BTM'){
                spans[1].innerHTML = (parseFloat(bids_data[i-total][0]).toFixed(8));
            }else{
                spans[1].innerHTML = (parseFloat(bids_data[i-total][0]).toFixed(6));
            }
        }

        spans[2].innerHTML = (parseFloat( bids_data[i-total][1])).toFixed(4);
        spans[3].innerHTML = (bids_data[i-total][0] * bids_data[i-total][1]).toFixed(4);
    }
}

function handleDetail(data){
    var cArr = data['ch'].match(/\.([a-z]+?)(gset|btc|eth|usdt)\./i);
    var dataCoin = cArr[1] +'_'+ cArr[2];//交易对
    var level= Math.floor((data.tick.close-data.tick.open)/data.tick.close*10000);
    if(document.getElementsByClassName(dataCoin).length == 0){
        return;
    }

    var price = document.getElementsByClassName(dataCoin)[0].getElementsByClassName('trade-price')[0];
    var rate  = document.getElementsByClassName(dataCoin)[0].getElementsByClassName('trade-rate')[0];
    level = level/100;
    if(isNaN(level) || !level){
        level = 0.00;
    }else{
        level = parseFloat(level).toFixed(2);
    }

    price.innerHTML = data.tick.close;
    if (level>0) {
        rate.setAttribute("style","color:#72c02c");
        rate.innerHTML = "+"+level+"%";
    }else{
        rate.setAttribute("style","color:#e74c3c");
        rate.innerHTML = level+"%";
    }

    //获取btc对应的usdt价格
    if ('BTC_USDT' == dataCoin.toUpperCase()) {
        BTC_USDT = data.tick.close;
    }

    if((cArr[1]+cArr[2]) != _symbol){
        return;
    }
    var sell = document.getElementById('hide-symbol').getAttribute('value').split("_")[0];
    var span = document.getElementById('zf').getElementsByTagName('span')[0];
    span.removeAttribute('style');

    $('#high').html('高 '+data.tick.high);
    $('#low').html('低 '+data.tick.low);
    $('#vol').html('24H量 '+data.tick.amount.toFixed(4)+' '+sell.toUpperCase());
    $('#tip-price').html(data.tick.close);
    $('#tip-cny').html('≈ '+calculateCNY(data.tick.close,cArr[2]).toFixed(2)+' CNY');

    if (level>0) {
        span.setAttribute("style","color:#72c02c");
        span.innerHTML = "+"+level+"%";
    }else{
        span.setAttribute("style","color:#e74c3c");
        span.innerHTML = level+"%";
    }
    var headerText = document.getElementsByClassName('mark-depth')[0].getElementsByClassName('header-text')[0];
    if (cArr[2] == sellBuy[1] && cArr[1] == sellBuy[0]) {
        $('.mark-depth .header-text').html('最新价 '+data.tick.close+sellBuy[1]+' <span></span>');
        $('.mark-depth .header-text span').html('≈ '+calculateCNY(data.tick.close,cArr[2]).toFixed(2)+' CNY');
    }
}

function calculateCNY(value,symbol) {
    var result = 0;
    if(symbol.toLowerCase() == 'usdt'){
        result = USDT_CNY_RATE * value;
    }else if(symbol.toLowerCase() == 'gset'){
        result = value;
    }else{
        result = exchangeRate[symbol+'_gset'] * value;
    }
    if (isNaN(result) || !result){
        return 0.00;
    }
    return result;
}

//保存
var exchangeRate = {};

/**
 * 平台
 * 左侧的实时价格
 */
function fetchRealTimePrice() {
    var symbols = [];
    $(".coin-list-item li").each(function(index,item){
        if($(item).find('div').data().status == true){
            symbols.push($(item).attr('symbol'));
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
                var dataCoin = data.data[i].sellSymbol.toLowerCase()+'_'+data.data[i].buySymbol.toLowerCase();
                var price = document.getElementsByClassName(dataCoin)[0].getElementsByClassName('trade-price')[0];
                var rate  = document.getElementsByClassName(dataCoin)[0].getElementsByClassName('trade-rate')[0];
                var level= Math.floor((data.data[i].p_new-data.data[i].p_open)/data.data[i].p_new*10000);
                price.innerHTML = data.data[i].p_new;
                level = level / 100;
                if(isNaN(level) || !level){
                    level = 0.00;
                }
                if (level>0) {
                    rate.setAttribute("style","color:#72c02c");
                    rate.innerHTML = "+"+level+"%";
                }else{
                    rate.setAttribute("style","color:#e74c3c");
                    rate.innerHTML = level+"%";
                }

                //保存GSET对其他币种的价格
                exchangeRate[data.data[i].sellSymbol.toLowerCase()+'_'+data.data[i].buySymbol.toLowerCase()] = data.data[i].p_new;


                handleDetail({"ch":"market."+data.data[i].sellSymbol.toLowerCase()+data.data[i].buySymbol.toLowerCase()+".detail",
                "tick":{
                    "amount":data.data[i].total,"close":data.data[i].p_new,"open":data.data[i].p_open,"low":data.data[i].buy,"high":data.data[i].sell,"version":"1","vol":data.data[i].total
                }});

                //console.log(exchangeRate);
            }
        }
    };
    util.network({url: url, param: param, success: callback});

    window.setTimeout(function () {
        fetchRealTimePrice();
    }, 2000);
}

/**
 * 平台
 * 获取实时成交
 */
function fetchRealTimeTrade(){
    var url = "/real/market.html";
    var symbol = $("#symbol").val();
    var param = {
        symbol:symbol,
        buysellcount:100,
        successcount:100
    };

    var callback = function (data) {
        if (data.code == 200) {
            var trades = data.data.trades;

            var timeBox = document.getElementsByClassName('market-trades-time')[0];
            var typeBox = document.getElementsByClassName('market-trades-type')[0];
            var priceBox = document.getElementsByClassName('market-trades-price')[0];
            var amountBox = document.getElementsByClassName('market-trades-amount')[0];

            var priceSymbol = priceBox.getElementsByTagName('dt')[0].innerHTML;
            var amountSymbol = amountBox.getElementsByTagName('dt')[0].innerHTML;

            var timeBoxHtml = '<dt>时间</dt>';
            var typeBoxHtml = '<dt>方向</dt>';
            var priceBoxHtml = '<dt>'+priceSymbol+'</dt>';
            var amountBoxHtml = '<dt>'+amountSymbol+'</dt>';

            for(var i=0;i<trades.length;i++){
                timeBoxHtml += '<dd>'+trades[i].time+'</dd>';
                typeBoxHtml += '<dd>'+trades[i].type+'</dd>';
                priceBoxHtml += '<dd>'+trades[i].price+'</dd>';
                amountBoxHtml += '<dd>'+trades[i].amount+'</dd>';
            }

            timeBox.innerHTML = timeBoxHtml;
            typeBox.innerHTML = typeBoxHtml;
            priceBox.innerHTML = priceBoxHtml;
            amountBox.innerHTML = amountBoxHtml;
        }
    };

    util.network({url: url, param: param, success: callback});
    window.setTimeout(function () {
        fetchRealTimeTrade();
    }, 2000);
}

/**
 * 平台
 * 深度
 */
function fetchRealTimeDepth() {
    var url = "kline/fulldepth.html";
    var symbol = $("#symbol").val();
    var param = {
        symbol:symbol
    };
    var callback = function (data) {
        if (data.code == 200) {
            var dl = document.getElementsByClassName('cell');
            //卖
            var asks = data.data.depth.asks;
            //买
            var bids = data.data.depth.bids;

            var ticker = {"asks":asks,"bids":bids};

            //显示深度图
            handleDepthTick(ticker);

            var total = 7;
            var bids_data = [];
            for (var i = total; i >=0 ; i--) {
                var d = dl[i];
                var spans = d.getElementsByTagName('span');
                spans[1].innerHTML = '';
                spans[2].innerHTML = '';
                spans[3].innerHTML = '';
                if(sellBuy[1].toUpperCase() == 'USDT'){
                    if(asks[total-i]) {
                        spans[1].innerHTML = (parseFloat(asks[total - i][0]).toFixed(2));
                    }
                }else{
                    if (sellBuy[0].toUpperCase() == 'BTM'){
                        if(asks[total-i]){
                            spans[1].innerHTML = (parseFloat(asks[total-i][0]).toFixed(8));
                        }
                    }else{
                        if (asks[total-i]){
                            spans[1].innerHTML = (parseFloat(asks[total-i][0]).toFixed(6));
                        }
                    }
                }
                if(asks[total-i]) {
                    spans[2].innerHTML = (parseFloat(asks[total-i][1])).toFixed(4);
                    spans[3].innerHTML = (asks[total-i][0] * asks[total-i][1]).toFixed(4);
                }
            }
            bids_data = bids;
            //bids_data.reverse();
            total += 1;
            for (var i = total; i < dl.length; i++) {
                var d = dl[i];
                var spans = d.getElementsByTagName('span');
                spans[1].innerHTML = '';
                spans[2].innerHTML = '';
                spans[3].innerHTML = '';
                if(sellBuy[1].toUpperCase() == 'USDT') {
                    if(bids_data[i-total]) {
                        spans[1].innerHTML = (parseFloat(bids_data[i - total][0]).toFixed(2));
                    }
                }else{
                    if (sellBuy[0].toUpperCase() == 'BTM'){
                        if (bids_data[i-total]) {
                            spans[1].innerHTML = (parseFloat(bids_data[i-total][0]).toFixed(8));
                        }

                    }else{
                        if (bids_data[i-total]){
                            spans[1].innerHTML = (parseFloat(bids_data[i-total][0]).toFixed(6));
                        }
                    }
                }
                if(bids_data[i-total]){
                    spans[2].innerHTML = (parseFloat(bids_data[i-total][1])).toFixed(4);
                    spans[3].innerHTML = (bids_data[i-total][0] * bids_data[i-total][1]).toFixed(4);
                }
            }
        }
    };
    util.network({url: url, param: param, success: callback});

    window.setTimeout(function () {
        fetchRealTimeDepth();
    }, 2000);
}

/**
 * 平台k线
 */
function fetchRealTimeKLine() {
    requestData();
}

function inputListener() {
    $("#tradebuyprice").on('input',function (ev) {
        var val = $("#tradebuyprice").val();
        var cny =  parseFloat(calculateCNY(val,sellBuy[1])).toFixed(2);
        $("#tradebuyprice-cny").html("≈ "+cny+" CNY");
    });

    $("#tradesellprice").on('input',function (ev) {
        var val = $("#tradesellprice").val();
        var cny = parseFloat(calculateCNY(val,sellBuy[1])).toFixed(2);
        $("#tradesellprice-cny").html("≈ "+cny+" CNY");
    });
}

$(function(){
    inputListener();
    fetchRealTimePrice();
    fetchRealTimeKLine();
    if (isPlatformTrade) {
        fetchRealTimeDepth();
        fetchRealTimeTrade();
    }
    return;
})