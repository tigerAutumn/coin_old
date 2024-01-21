package com.qkwl.web.proxy;


import com.qkwl.common.util.ReturnResult;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 如果前端调用的接口需要判断是否需要第三方支持 就需要实现这个接口
 */
public interface IFetchData {

    /**
     * 买卖盘，最新成交
     */
    ReturnResult MarketJson(Integer symbol, Integer buysellcount, Integer successcount);

    /**
     * 获取首页行情
     *
     * @return
     */
    ReturnResult IndexMarketJson(Integer locale);


}
