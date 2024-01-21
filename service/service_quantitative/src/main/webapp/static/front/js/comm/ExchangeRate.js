/**
 * 汇率
 */

//美元对人名币的汇率
var USDT_CNY_RATE = 0;

function getExchangeRate() {
    var url = "/market/rate.html";
    var callback = function(response){
        if(response.code == 200){
            USDT_CNY_RATE = response.data.CNY;
            console.log("CNY ="+USDT_CNY_RATE);
        }
    };
    util.network({
        url : url,
        success : callback
    });

    setTimeout(function(){
        getExchangeRate();
    },5000);
}

$(function(){

    getExchangeRate();
});
