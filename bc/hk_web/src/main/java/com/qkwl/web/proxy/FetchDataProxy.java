package com.qkwl.web.proxy;

import com.qkwl.common.okhttp.ApiException;
import com.qkwl.common.okhttp.HBApiImpl;
import com.qkwl.common.util.ReturnResult;

public class FetchDataProxy implements IFetchData {

    @Override
    public ReturnResult MarketJson(Integer symbol, Integer buysellcount, Integer successcount) {
        try {
            HBApiImpl.getInstance();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return ReturnResult.FAILUER("");
    }

    @Override
    public ReturnResult IndexMarketJson(Integer local) {
        return null;
    }


}
