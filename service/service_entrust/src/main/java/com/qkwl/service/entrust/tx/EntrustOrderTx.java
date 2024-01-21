package com.qkwl.service.entrust.tx;

import com.qkwl.common.dto.Enum.EntrustChangeEnum;
import com.qkwl.common.dto.Enum.EntrustTypeEnum;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.dto.coin.SystemTradeTypeVO;
import com.qkwl.common.dto.mq.MQEntrustState;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.mq.MQSendHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.huobi.request.CreateOrderRequest;
import com.qkwl.common.huobi.response.Account;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.mq.MQConstant;
import com.qkwl.common.mq.MQTopic;
import com.qkwl.common.okhttp.ApiException;
import com.qkwl.common.okhttp.HBApiImpl;
import com.qkwl.service.entrust.dao.FEntrustMapper;
import com.qkwl.service.entrust.dao.UserCoinWalletMapper;
import com.qkwl.service.entrust.model.EntrustDO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 委单接口实现
 *
 * @author LY
 */
@Service("entrustServerTx")
public class EntrustOrderTx {

    @Autowired
    private FEntrustMapper entrustMapper;
    @Autowired
    private UserCoinWalletMapper userCoinWalletMapper;
    @Autowired
    private MQSendHelper MQSendHelper;

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 创建委单
     *
     * @param entrust 委单数据
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BigInteger createEntrust(EntrustDO entrust) throws Exception {
        //火币下单接口
        /**
        long HBOrderID = 0;
        try {
            List<Account> accountList = HBApiImpl.getInstance().getAccountList();
            if (accountList != null && accountList.size() > 0){
                List<SystemTradeType> tradeTypeList = redisHelper.getTradeTypeList(0);
                String symbol = "";
                //获取交易对
                for (SystemTradeType type:tradeTypeList) {
                    if (type.getId() == entrust.getFtradeid()) {

                        symbol = type.getBuyShortName().toLowerCase() + type.getSellShortName().toLowerCase();
                    }
                }
                CreateOrderRequest  createOrderRequest = new CreateOrderRequest();
                createOrderRequest.symbol = symbol;
                createOrderRequest.accountId = String.valueOf(accountList.get(0).id);
                createOrderRequest.amount = entrust.getFcount().toString();
                createOrderRequest.price = entrust.getFprize().toString();

                if (entrust.getFtype() == EntrustTypeEnum.BUY.getCode()) {
                    createOrderRequest.type = CreateOrderRequest.OrderType.BUY_MARKET;
                }else {
                    createOrderRequest.type = CreateOrderRequest.OrderType.SELL_MARKET;
                }
                HBOrderID = HBApiImpl.getInstance().createOrder(createOrderRequest);
            }
        }catch (ApiException e){
            e.printStackTrace();
            throw new Exception("create order api failure");
        }
        //火币的订单ID
        entrust.setFhuobientrustid(new BigInteger(String.valueOf(HBOrderID)));
         **/

        if (entrustMapper.insert(entrust) <= 0) {
            throw new Exception("entrust insert failure");
        }
        UserCoinWallet wallet = null;
        if(entrust.getFtype().equals(EntrustTypeEnum.BUY.getCode())){
            wallet = userCoinWalletMapper.selectLock(entrust.getFuid(),entrust.getFbuycoinid());
            if(wallet == null){
                throw new Exception("wallet is null");
            }
            if(wallet.getTotal().compareTo(entrust.getFamount())<0){
                throw new Exception("wallet total balance");
            }
            wallet.setTotal(MathUtils.sub(wallet.getTotal(),entrust.getFamount()));
            wallet.setFrozen(MathUtils.add(wallet.getFrozen(),entrust.getFamount()));
            wallet.setGmtModified(new Date());
        } else if(entrust.getFtype().equals(EntrustTypeEnum.SELL.getCode())){
            wallet = userCoinWalletMapper.selectLock(entrust.getFuid(),entrust.getFsellcoinid());
            if(wallet == null){
                throw new Exception("wallet is null");
            }
            if(wallet.getTotal().compareTo(entrust.getFcount())<0){
                throw new Exception("wallet total balance");
            }
            wallet.setTotal(MathUtils.sub(wallet.getTotal(),entrust.getFcount()));
            wallet.setFrozen(MathUtils.add(wallet.getFrozen(),entrust.getFcount()));
            wallet.setGmtModified(new Date());
        }
        if(wallet == null){
            throw new Exception("wallet is null");
        }
        if (userCoinWalletMapper.update(wallet)<=0) {
            throw new Exception("wallet update failure");
        }
        // send MQ
        MQEntrustState mqBody = new MQEntrustState();
        mqBody.setTradeId(entrust.getFtradeid());
        mqBody.setBuyID(entrust.getFuid());
        mqBody.setBuyOrderId(entrust.getFid());
        mqBody.setBuyPrize(entrust.getFprize());
        mqBody.setCount(entrust.getFcount());
        String key;
        if (entrust.getFtype().equals(EntrustTypeEnum.BUY.getCode())) {
            mqBody.setType(EntrustChangeEnum.BUY);
            key = "TRADE_BUY_" + entrust.getFid();
        } else if (entrust.getFtype().equals(EntrustTypeEnum.SELL.getCode())) {
            mqBody.setType(EntrustChangeEnum.SELL);
            key = "TRADE_SELL_" + entrust.getFid();
        } else {
            throw new Exception("entrust type error");
        }
        if (!MQSendHelper.send(MQTopic.ENTRUST_STATE, MQConstant.TAG_ENTRUST_STATE, key, mqBody)) {
            throw new Exception("mq send failure");
        }
        return entrust.getFid();
    }

    /**
     * 撤单
     */
    public Boolean cancleEntrust(EntrustDO entrust) throws Exception {
        return entrustMapper.updateByfId(entrust) > 0;
    }
}
