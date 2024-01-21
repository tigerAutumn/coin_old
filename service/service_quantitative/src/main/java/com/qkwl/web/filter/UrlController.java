package com.qkwl.web.filter;

public class UrlController {
    /**
     * 需要签名的api
     */
    private static String[] APIURLS = new String[]{
            "/v1/order/place",
            "/v1/order/cancel",
            "/v1/order/detail",
            "/v1/order/detailById",
            "/v1/order/entrust",
            "/v1/order/entrustList",
            "/v1/ticker",
            "/v1/depth",
            "/v1/balance",
            "/v1/trade"
    };


    public static boolean isApiUrls(String url) {
        boolean flag = false;
        for (String s : APIURLS) {
            if (url.startsWith(s)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
