package com.qkwl.common.okhttp;

import com.qkwl.common.huobi.request.CreateOrderRequest;
import com.qkwl.common.huobi.request.QueryKLineRequest;
import com.qkwl.common.huobi.request.QueryOrderRequest;
import com.qkwl.common.huobi.response.Account;
import com.qkwl.common.huobi.response.KLine;
import com.qkwl.common.huobi.response.Order;

import java.util.List;

public class HBApiImpl implements IHBApi {

    static final String API_KEY = "11411a60-7260ebc2-03b476da-1fe28";

    static final String API_SECRET = "f69562dd-b9f457c1-7b338f63-bdd34";

    private static volatile HBApiImpl sInstance;

    private ApiClient apiClient;

    private HBApiImpl(){
        apiClient = new ApiClient(API_KEY,API_SECRET);
    }

    public static HBApiImpl getInstance() {
        if (sInstance == null){
            synchronized (IHBApi.class){
                if (sInstance == null){
                    sInstance = new HBApiImpl();
                }
            }
        }

        return sInstance;
    }

    @Override
    public List<Account> getAccountList() {
        return apiClient.getAccounts();
    }

    @Override
    public long createOrder(CreateOrderRequest request) {
        return apiClient.createOrder(request);
    }

    @Override
    public List<Order> queryOrderList(QueryOrderRequest request) {
        return apiClient.queryOrderList(request);
    }

    @Override
    public List<KLine> queryKLineList(QueryKLineRequest request) {
        return apiClient.queryKLineList(request);
    }
}
