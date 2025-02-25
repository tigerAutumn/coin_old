package com.qkwl.admin.layui.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qkwl.admin.layui.base.WebBaseController;
import com.qkwl.admin.layui.utils.MQSend;
import com.qkwl.common.dto.Enum.*;
import com.qkwl.common.dto.admin.FAdmin;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.capital.AssetImbalance;
import com.qkwl.common.dto.capital.FUserPushDTO;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.dto.finances.FUserFinancesDTO;
import com.qkwl.common.dto.log.FLogUserAction;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.rpc.admin.*;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.DateUtils;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ZKF on 2017/4/8.
 */
@Controller
public class AdvancedQueryController extends WebBaseController {
	private static final Logger logger = LoggerFactory.getLogger(AdvancedQueryController.class);

    @Autowired
    private IAdminUserService adminUserService;
    @Autowired
    private IAdminUserCapitalService adminUserCapitalService;
    @Autowired
    private IAdminEntrustServer adminEntrustServer;
    @Autowired
    private IAdminStatisticsService adminStatisticsService;
    @Autowired
    private IAdminRewardCodeService adminRewardCodeService;
    @Autowired
    private IAdminLogService adminLogService;
    @Autowired
    private IAdminUserFinances adminUserFinances;
    @Autowired
    private IAdminC2CService adminC2CService;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private MQSend mqSend;
    @Autowired
	private IAdminCommissionService adminCommissionService;


    @RequestMapping("admin/usercapital")
    public ModelAndView usercapital(
            @RequestParam(value = "keyword", required=false, defaultValue = "0") Integer fuid,
            @RequestParam(value = "rwbegindate",required=false) Date rwbegindate,
            @RequestParam(value = "rwenddate",required=false) Date rwenddate,
            @RequestParam(value = "showRW",required=false, defaultValue = "true") Boolean showRW,
            @RequestParam(value = "showTrade",required=false, defaultValue = "true") Boolean showTrade,
            @RequestParam(value = "showBalance",required=false, defaultValue = "true") Boolean showBalance){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("query/usercapital");
        //判断用户UID
        if(fuid == 0){
            modelAndView.addObject("showRW", showRW);
            modelAndView.addObject("showTrade", showTrade);
            modelAndView.addObject("showBalance", showBalance);
            return modelAndView;
        }

        FUser user = adminUserService.selectById(fuid);
        //判断用户是否存在
        if(user == null){
            modelAndView.addObject("showRW", showRW);
            modelAndView.addObject("showTrade", showTrade);
            modelAndView.addObject("showBalance", showBalance);
            return modelAndView;
        }

        //返回数据集
        JSONArray  array = new JSONArray();
        JSONArray  trade = new JSONArray();
        JSONArray  balance = new JSONArray();
        JSONArray  forzenBalance = new JSONArray();
        JSONObject balanceObj = new JSONObject();
        JSONArray c2clist = new JSONArray();

        //虚拟币钱包列表
        List<UserCoinWallet> vwalletList = new ArrayList<UserCoinWallet>();

        //币种
        List<SystemCoinType> coinList = redisHelper.getCoinTypeListSystem();

        //SystemCoinType cny = redisHelper.getCoinTypeShortNameSystem("CNY");
        
        //c2c查询
        Map<Integer, UserC2CEntrust> statisticsRechargeWithdrawTotal = adminC2CService.statisticsRechargeWithdrawTotal(fuid,null);
        

        //循环币种
        for (SystemCoinType coin : coinList) {
            //查询虚拟钱包
            UserCoinWallet vwallet = adminUserCapitalService.selectUserVirtualWallet(fuid, coin.getId());
            if(vwallet == null){
                continue;
            }
            vwallet.setCoinName(coin.getName());
            vwalletList.add(vwallet);

            // 充提统计
            BigDecimal recharge = BigDecimal.ZERO;
            //BigDecimal zsjfRecharge = BigDecimal.ZERO;
            BigDecimal withdraw = BigDecimal.ZERO;
            BigDecimal withdrawFrozen = BigDecimal.ZERO;

            // PUSH资产
            BigDecimal userPushCoinIn = BigDecimal.ZERO;
            BigDecimal userPushCoinOut = BigDecimal.ZERO;
            BigDecimal userPushCoinFrozen = BigDecimal.ZERO;

            // 买卖交易
            BigDecimal buycount = BigDecimal.ZERO;
            BigDecimal buyamount = BigDecimal.ZERO;
            BigDecimal sellcount = BigDecimal.ZERO;
            BigDecimal sellamount = BigDecimal.ZERO;
            BigDecimal frozenamount = BigDecimal.ZERO;
            BigDecimal frozencount = BigDecimal.ZERO;
            BigDecimal frozencountCoin = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;

            BigDecimal vip6RMB = BigDecimal.ZERO;
            
            //佣金
            BigDecimal commission = BigDecimal.ZERO;

            //币币交易
            BigDecimal coinTradeBuy = BigDecimal.ZERO;
            BigDecimal coinTradeSell = BigDecimal.ZERO;
            BigDecimal coinTradeFee = BigDecimal.ZERO;
            FEntrustHistory buy = null;
            FEntrustHistory sell = null;
            FEntrustHistory coinBuy = null;
            FEntrustHistory coinSell = null;
            FEntrustHistory coinSelfBuy = null;
            FEntrustHistory coinSelfSell = null;

            FEntrust currentbuy = null;
            FEntrust currentsell = null;
            FEntrust currentcoinBuy = null;
            FEntrust currentcoinSell = null;
            FEntrust currentSelfCoinBuy = null;
            FEntrust currentSelfCoinSell = null;
            
            //C2C 
            BigDecimal c2cRecharge = BigDecimal.ZERO;
            BigDecimal c2cWithdraw = BigDecimal.ZERO;
            BigDecimal c2cWithdrawFrozen = BigDecimal.ZERO;
            
            //理财
            BigDecimal financesCountSend = BigDecimal.ZERO;
            BigDecimal frozenFinances =BigDecimal.ZERO;

            if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
                buy = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), rwbegindate, rwenddate);
                sell = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.SELL.getCode(), rwbegindate, rwenddate);
                currentbuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);
                currentsell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);

                frozenamount = MathUtils.sub(currentbuy.getFamount(), currentbuy.getFsuccessamount());
                FEntrust frozenEntrust = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), rwbegindate, rwenddate);
                //交易冻结金额
                frozenamount = MathUtils.add(frozenamount, frozenEntrust.getFamount());

                //人民币充提统计
                recharge = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.IN.getCode(),
                        CapitalOperationInStatus.Come, rwbegindate, rwenddate);
                withdraw = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
                        CapitalOperationOutStatus.OperationSuccess, rwbegindate, rwenddate);


                // 冻结
                BigDecimal wf = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
                        CapitalOperationOutStatus.WaitForOperation, rwbegindate, rwenddate);
                withdrawFrozen = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
                        CapitalOperationOutStatus.OperationLock, rwbegindate, rwenddate);
                //充提冻结
                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);

                // PUSH资产
                FUserPushDTO userPushIn = adminUserCapitalService.selectUserPushBalance(null, user.getFshowid(),
                        null, UserPushStateEnum.PAYSUCCEED.getCode(), rwbegindate, rwenddate);
                FUserPushDTO userPushOut = adminUserCapitalService.selectUserPushBalance(user.getFshowid(), null,
                        null, UserPushStateEnum.PAYSUCCEED.getCode(), rwbegindate, rwenddate);
                userPushCoinIn = userPushOut == null ? BigDecimal.ZERO : userPushOut.getFamount();
                userPushCoinOut = userPushIn == null ? BigDecimal.ZERO : userPushIn.getFamount();

                //用法币购买别的币所花费的
                buyamount = MathUtils.add(buy.getFsuccessamount(), currentbuy.getFsuccessamount());
                //卖别的币所获得的法币
                sellamount = MathUtils.add(sell.getFsuccessamount(), currentsell.getFsuccessamount());
                //总共交易卖的手续费
                fee = MathUtils.add(sell.getFfees(), currentsell.getFfees());

                //vip6购买
                FLogUserAction userAction = adminLogService.selectVip6ByUser(fuid, LogUserActionEnum.BUY_VIP6.getCode());
                if(userAction != null){
                    vip6RMB = new BigDecimal(3888D);
                }
                
                //返佣
                commission = adminCommissionService.selectIssuedAmountByIntroId(fuid);
                if (commission == null) {
					commission = BigDecimal.ZERO;
				}
            } else {

                // 币币交易-主币种-买-减少
                coinSelfBuy = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), rwbegindate, rwenddate);
                // 币币交易-主币种-卖-增加
                coinSelfSell = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.SELL.getCode(), rwbegindate, rwenddate);
                // 币币交易-交易币种-买-增加
                coinBuy = adminEntrustServer.selectTotalAmountByType(fuid, null, coin.getId(),
                        EntrustTypeEnum.BUY.getCode(), rwbegindate, rwenddate);
                // 币币交易-交易币种-卖-减少
                coinSell = adminEntrustServer.selectTotalAmountByType(fuid, null, coin.getId(),
                        EntrustTypeEnum.SELL.getCode(), rwbegindate, rwenddate);

                // 币币交易-主币种-买-减少
                currentSelfCoinBuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);
                FEntrust currentSelfCoinBuyGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), rwbegindate, rwenddate);
                // 币币交易-主币种-卖-增加
                currentSelfCoinSell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,
                        EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);
                //FEntrust currentSelfCoinSellGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), rwbegindate, rwenddate);
                // 币币交易-交易币种-买-增加
                currentcoinBuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),
                        EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);
                // 币币交易-交易币种-卖-减少
                currentcoinSell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),
                        EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), rwbegindate, rwenddate);
                FEntrust currentcoinSellGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),
                        EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.Going.getCode(), rwbegindate, rwenddate);

                frozencountCoin = MathUtils.add(
                        MathUtils.sub(currentSelfCoinBuy.getFamount(), currentSelfCoinBuy.getFsuccessamount()),
                        MathUtils.sub(currentSelfCoinBuyGoing.getFamount(), currentSelfCoinBuyGoing.getFsuccessamount())
                );
                //交易冻结数量
                frozencountCoin = MathUtils.add(frozencountCoin,
                        MathUtils.add(currentcoinSell.getFleftcount(), currentcoinSellGoing.getFleftcount()));

                //虚拟币充提
                recharge = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),
                        VirtualCapitalOperationTypeEnum.COIN_IN.getCode(),
                        VirtualCapitalOperationInStatusEnum.SUCCESS, rwbegindate, rwenddate);
                withdraw = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),
                        VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(),
                        VirtualCapitalOperationOutStatusEnum.OperationSuccess, rwbegindate, rwenddate);

                // 冻结
                BigDecimal wf = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),
                        VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(),
                        VirtualCapitalOperationOutStatusEnum.WaitForOperation, rwbegindate, rwenddate);
                withdrawFrozen = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),
                        VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(),
                        VirtualCapitalOperationOutStatusEnum.OperationLock, rwbegindate, rwenddate);
                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);
                wf = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),
                        VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(),
                        VirtualCapitalOperationOutStatusEnum.LockOrder, rwbegindate, rwenddate);
                //充提冻结
                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);

                // PUSH资产
                /*FUserPushDTO userPushIn = adminUserCapitalService.selectUserPushBalance(null, user.getFshowid(),
                        coin.getId(), UserPushStateEnum.PAYSUCCEED.getCode(), rwbegindate, rwenddate);
                FUserPushDTO userPushOut = adminUserCapitalService.selectUserPushBalance(user.getFshowid(), null,
                        coin.getId(), UserPushStateEnum.PAYSUCCEED.getCode(), rwbegindate, rwenddate);
                userPushCoinIn = userPushIn == null ? BigDecimal.ZERO : userPushIn.getFcount();
                userPushCoinOut = userPushOut == null ? BigDecimal.ZERO : userPushOut.getFcount();*/

                // 币币交易-花费
                coinTradeBuy = MathUtils.add(
                        MathUtils.add(coinSelfBuy.getFsuccessamount(),
                                MathUtils.add(currentSelfCoinBuy.getFsuccessamount(), currentSelfCoinBuyGoing.getFsuccessamount())
                        ),
                        MathUtils.add(coinSell.getFcount(), currentcoinSell.getFcount())
                );
                // 币币交易-收入
                coinTradeSell = MathUtils.add(
                        MathUtils.add(coinSelfSell.getFsuccessamount(), currentSelfCoinSell.getFsuccessamount()),
                        MathUtils.add(coinBuy.getFcount(), currentcoinBuy.getFcount())
                );

                coinTradeFee = MathUtils.add(
                        MathUtils.add(coinSelfSell.getFfees(),
                                MathUtils.add(currentSelfCoinSell.getFfees(), currentSelfCoinBuyGoing.getFfees())),
                        MathUtils.add(coinBuy.getFfees(), currentcoinBuy.getFfees())
                );

                FUserPushDTO userFrozenPush = adminUserCapitalService.selectUserPushBalance(user.getFshowid(),
                        null, coin.getId(), UserPushStateEnum.PAYWAIT.getCode(), rwbegindate, rwenddate);
                userPushCoinFrozen = userFrozenPush == null ? BigDecimal.ZERO : userFrozenPush.getFcount();
            }

            //手工充值
            BigDecimal rechargeWork = adminUserCapitalService.selectAdminRechargeVirtualWalletTotalAmount(fuid, coin.getId(), OperationlogEnum.AUDIT, rwbegindate, rwenddate);
            BigDecimal frozenWork = adminUserCapitalService.selectAdminRechargeVirtualWalletTotalAmount(fuid, coin.getId(), OperationlogEnum.FFROZEN, rwbegindate, rwenddate);

            //虚拟币兑换
            BigDecimal rewardCoin = adminRewardCodeService.selectWalletTotalAmount(fuid, coin.getId(), rwbegindate, rwenddate);

            // 理财	当前理财功能未开启，且数据库现有的理财状态均为取消状态
/*            FUserFinancesDTO userFinancesSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.SEND.getCode());
            FUserFinancesDTO userFinancesRedeemSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.REDEEM.getCode());
            FUserFinancesDTO userFinancesFrozenSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.FROZEN.getCode());
            
            // 处理理财数量
            financesCountSend = userFinancesSend == null ? BigDecimal.ZERO : userFinancesSend.getFplanamount();
            BigDecimal financesCountRedeemSend = userFinancesRedeemSend == null ? BigDecimal.ZERO : userFinancesRedeemSend.getFplanamount();
            frozenFinances = userFinancesFrozenSend == null ? BigDecimal.ZERO:userFinancesFrozenSend.getFamount();
            financesCountSend = MathUtils.add(financesCountSend, financesCountRedeemSend);*/
            
            
            //c2c
            if(statisticsRechargeWithdrawTotal != null ) {
            	 UserC2CEntrust statisticsEntrust = statisticsRechargeWithdrawTotal.get(coin.getId());
                 if(statisticsEntrust != null) {
                 	c2cRecharge = statisticsEntrust.getRecharge();
                    c2cWithdraw = statisticsEntrust.getWithdraw();
                    c2cWithdrawFrozen = statisticsEntrust.getWithdrawFrozen();
                 }
            }
           
            
            
            JSONObject c2cObj = new JSONObject();
            c2cObj.put("type", coin.getName());
            c2cObj.put("recharge", c2cRecharge);
            c2cObj.put("withdraw", c2cWithdraw);
            c2clist.add(c2cObj);
            
            

            //判断是否显示充提
            if(showRW){
                //虚拟币充提统计
                JSONObject coinObj = new JSONObject();
                coinObj.put("type", coin.getName());
                coinObj.put("recharge", recharge);
                coinObj.put("withdraw", withdraw);
                array.add(coinObj);
            }

            //判断是否显示交易
            if(showTrade){
                JSONObject tradeObj = new JSONObject();
                tradeObj.put("type", coin.getName());
                tradeObj.put("buycount", buycount);
                tradeObj.put("buyamount", buyamount);
                tradeObj.put("sellcount", sellcount);
                tradeObj.put("sellamount", sellamount);

                trade.add(tradeObj);
            }

            //判断是否显示平衡
            if(showBalance){

                BigDecimal rechargeCoin = recharge;
                BigDecimal withdrawCoin = withdraw;
                BigDecimal frozenCoin = withdrawFrozen;

                

                //充值+手工充值+虚拟币兑换-体现
                BigDecimal freeplan = MathUtils.add(rechargeCoin, rechargeWork);
                freeplan = MathUtils.add(freeplan, rewardCoin);
                freeplan = MathUtils.sub(freeplan, withdrawCoin);

                freeplan = MathUtils.add(freeplan, userPushCoinIn);  
                freeplan = MathUtils.sub(freeplan, userPushCoinOut);
                freeplan = MathUtils.add(freeplan, financesCountSend); //理财
                freeplan = MathUtils.sub(freeplan, vip6RMB); 
                freeplan = MathUtils.add(freeplan, commission); //返佣
                freeplan = MathUtils.sub(freeplan, frozenCoin);   //法币充提冻结
                freeplan = MathUtils.sub(freeplan, userPushCoinFrozen);
                freeplan = MathUtils.sub(freeplan, frozenFinances);  // 理财
                
                freeplan = MathUtils.add(freeplan, c2cRecharge);
                freeplan = MathUtils.sub(freeplan, c2cWithdraw);
                freeplan = MathUtils.sub(freeplan, c2cWithdrawFrozen);

                BigDecimal frozenplan = MathUtils.add(frozenWork, frozenCoin); //用于统计法币交易冻结
                BigDecimal frozenTrade = BigDecimal.ZERO;  
                if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
                    frozenTrade = frozenamount;  //法币交易冻结的金额
                    frozenplan = MathUtils.add(frozenplan, frozenamount);
                    freeplan = MathUtils.sub(freeplan, buyamount); //用法币购买别的币所花费的
                    freeplan = MathUtils.add(freeplan, sellamount);//卖别的币所获得的法币
                    freeplan = MathUtils.sub(freeplan, fee);  //法币卖的手续费
                }else{
                    frozenTrade = frozencount;  // 0
                    frozenplan = MathUtils.add(frozenplan, frozencount);
                    freeplan = MathUtils.sub(freeplan, coinTradeBuy);  //虚拟币交易的花费
                    freeplan = MathUtils.add(freeplan, coinTradeSell);  //虚拟币交易的收入
                    freeplan = MathUtils.sub(freeplan, coinTradeFee);  //虚拟币交易的手续费
                    freeplan = MathUtils.sub(freeplan, frozencountCoin);//虚拟币交易导致冻结的数量
                }
                frozenplan = MathUtils.add(frozenplan, userPushCoinFrozen);
                frozenplan = MathUtils.add(frozenplan, frozenFinances);
                frozenplan = MathUtils.add(frozenplan, frozencountCoin);//虚拟币交易导致冻结的数量
                frozenplan = MathUtils.add(frozenplan, c2cWithdrawFrozen);
                freeplan = MathUtils.sub(freeplan, frozenTrade);

                boolean isFreeBalance = freeplan.compareTo(vwallet.getTotal()) == 0;
                JSONObject balanceCoinObj = new JSONObject();
                balanceCoinObj.put("type", coin.getName());
                balanceCoinObj.put("recharge", rechargeCoin);
                balanceCoinObj.put("withdraw", withdrawCoin);
                balanceCoinObj.put("rechargeWork", rechargeWork);
                balanceCoinObj.put("rewardCoin", rewardCoin);
                if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
                    balanceCoinObj.put("buy", buyamount);
                    balanceCoinObj.put("sell", sellamount);
                }else{
                    balanceCoinObj.put("buy", buycount);
                    balanceCoinObj.put("sell", sellcount);
                }
                balanceCoinObj.put("fees", fee);
                balanceCoinObj.put("vip6", vip6RMB);
                balanceCoinObj.put("pushin", userPushCoinIn);
                balanceCoinObj.put("pushout", userPushCoinOut);
                balanceCoinObj.put("financesCountSend", financesCountSend);
                balanceCoinObj.put("frozenCoin", frozenCoin);
                balanceCoinObj.put("frozenTrade", frozenTrade);
                balanceCoinObj.put("pushfrozen", userPushCoinFrozen);
                balanceCoinObj.put("frozenFinances", frozenFinances);
                balanceCoinObj.put("coinTradeBuy",coinTradeBuy);
                balanceCoinObj.put("coinTradeSell",coinTradeSell);
                balanceCoinObj.put("c2cRecharge",c2cRecharge);
                balanceCoinObj.put("c2cWithdraw",c2cWithdraw);
                balanceCoinObj.put("c2cWithdrawFrozen",c2cWithdrawFrozen);
                balanceCoinObj.put("coinTradeFee",coinTradeFee);
                balanceCoinObj.put("frozencountCoin",frozencountCoin);
                balanceCoinObj.put("commission", commission);

                // 统计
                balanceCoinObj.put("freeplan", freeplan);
                balanceCoinObj.put("free", vwallet.getTotal());
                balanceCoinObj.put("isFreeBalance", isFreeBalance);

                JSONObject balanceFrozenObj = new JSONObject();
                boolean isFrozenBalance = frozenplan.compareTo(MathUtils.add(vwallet.getFrozen(), vwallet.getBorrow())) == 0;
                // 冻结
                balanceFrozenObj.put("type", coin.getName());
                balanceFrozenObj.put("pushfrozen", userPushCoinFrozen);
                balanceFrozenObj.put("frozenCoin", frozenCoin);
                balanceFrozenObj.put("frozenWork", frozenWork);
                balanceFrozenObj.put("frozenTrade", frozenTrade);
                balanceFrozenObj.put("frozenFinances", frozenFinances);
                balanceFrozenObj.put("frozenTradeCoin", frozencountCoin);
                balanceFrozenObj.put("c2cWithdrawFrozen", c2cWithdrawFrozen);
                balanceFrozenObj.put("frozenplan", frozenplan);
                balanceFrozenObj.put("frozen", vwallet.getFrozen());
                balanceFrozenObj.put("isFrozenBalance", isFrozenBalance);

                balance.add(balanceCoinObj);
                forzenBalance.add(balanceFrozenObj);
                if(!isFreeBalance) {
                	logger.error("用户"+fuid+"存在不平衡:"+balanceCoinObj.toJSONString());
                }
            }

        }

        //数据返回
        modelAndView.addObject("showRW", showRW);
        modelAndView.addObject("showTrade", showTrade);
        modelAndView.addObject("showBalance", showBalance);
        modelAndView.addObject("keyword", fuid);
        modelAndView.addObject("vwalletList", vwalletList);
        modelAndView.addObject("rwList", array);
        modelAndView.addObject("tradeList", trade);
        modelAndView.addObject("balanceCoin", balance);
        modelAndView.addObject("balanceFrozen", forzenBalance);
        modelAndView.addObject("c2clist", c2clist);
        return modelAndView;
    }


    @RequestMapping("admin/monthDataList")
    public ModelAndView monthDataList(
            @RequestParam(value = "month", required = false) String month) throws ParseException{
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("query/monthDataList");

        Date date = new Date();

        if(!StringUtils.isEmpty(month)){
            date = DateUtils.parse(month, DateUtils.YYYY_MM_DD);
        }else{
            return modelAndView;
        }

        Map<String, Object> map = getBeginEndDate(date);

        JSONArray array = new JSONArray();

        List<SystemCoinType> coinList = redisHelper.getCoinTypeListSystem();
        for (SystemCoinType coin : coinList) {
            if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
                JSONObject rmb = new JSONObject();

                BigDecimal RMBrecharge = adminStatisticsService.sumRWrmb(CapitalOperationInOutTypeEnum.IN.getCode(), map);
                BigDecimal RMBOnLinerecharge = adminStatisticsService.sumOtherRmb(map);
                RMBrecharge = MathUtils.add(RMBrecharge,RMBOnLinerecharge);

                BigDecimal RMBwithdraw = adminStatisticsService.sumRWrmb(CapitalOperationInOutTypeEnum.OUT.getCode(), map);

                BigDecimal RMBbuy = adminStatisticsService.sumBSrmb(EntrustTypeEnum.BUY.getCode(), map);
                BigDecimal RMBsell = adminStatisticsService.sumBSrmb(EntrustTypeEnum.SELL.getCode(), map);

                rmb.put("name", coin.getName());
                rmb.put("recharge", RMBrecharge);
                rmb.put("withdraw", RMBwithdraw);
                rmb.put("buy", RMBbuy);
                rmb.put("sell", RMBsell);
                array.add(rmb);
            }else{
                JSONObject obj = new JSONObject();

                BigDecimal CoinRecharge = adminStatisticsService.sumRWcoin(VirtualCapitalOperationTypeEnum.COIN_IN.getCode(), map, coin.getId());
                BigDecimal CoinWithdraw = adminStatisticsService.sumRWcoin(VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), map, coin.getId());

                BigDecimal CoinBuy = adminStatisticsService.sumBScoin(EntrustTypeEnum.BUY.getCode(), map, coin.getId());
                BigDecimal CoinSell = adminStatisticsService.sumBScoin(EntrustTypeEnum.SELL.getCode(), map, coin.getId());

                obj.put("name", coin.getName());
                obj.put("recharge", CoinRecharge);
                obj.put("withdraw", CoinWithdraw);
                obj.put("buy", CoinBuy);
                obj.put("sell", CoinSell);
                array.add(obj);
            }
        }

        modelAndView.addObject("monthDataList", array);
        modelAndView.addObject("month", month);
        return modelAndView;
    }

    /**
     * 虚拟币钱包
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/capitalStorage")
    public ModelAndView capitalStorage(
            @RequestParam(value = "ftype",defaultValue="-1") Integer type,
            @RequestParam(value = "pageNum",defaultValue="1") Integer currentPage,
            @RequestParam(value = "orderField",defaultValue="total") String orderField,
            @RequestParam(value = "orderDirection",defaultValue="desc") String orderDirection) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("query/capitalStorage");
        // 搜索关键字
        Pagination<UserCoinWallet> pageParam = new Pagination<UserCoinWallet>(currentPage, Constant.adminPageSize);
        // 排序条件
        pageParam.setOrderField(orderField);
        pageParam.setOrderDirection(orderDirection);

        // 页面参数
        Map<Integer, String> typeMap = redisHelper.getCoinTypeNameMap();
        typeMap.put(-1, "全部");
        modelAndView.addObject("typeMap", typeMap);

        UserCoinWallet filterParam = new UserCoinWallet();
        // 虚拟币类型
        if(type < 0){
            return modelAndView;
        } else {
            filterParam.setCoinId(type);
            modelAndView.addObject("ftype", type);
        }

        // 查询
        Pagination<UserCoinWallet> pagination = adminUserCapitalService.selectUserVirtualWalletListByCoin(
                pageParam, filterParam);

        modelAndView.addObject("virtualwalletList", pagination);
        return modelAndView;
    }

    /**
     * 用户列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/userRegSort")
    public ModelAndView userRegSort(
            @RequestParam(value="pageNum",required=false,defaultValue="1") Integer currentPage,
            @RequestParam(value="orderField",required=false,defaultValue="fregistertime") String orderField,
            @RequestParam(value="orderDirection",required=false,defaultValue="desc") String orderDirection,
            @RequestParam(value="startDate",required=false) String startDate,
            @RequestParam(value="endDate",required=false) String endDate
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("query/userRegSort");
        // 定义查询条件
        Pagination<FUser> pageParam = new Pagination<FUser>(currentPage, Constant.adminPageSize);
        FUser fUser = new FUser();
        //参数判断
        if (!org.springframework.util.StringUtils.isEmpty(startDate)){
            pageParam.setBegindate(startDate);
            modelAndView.addObject("startDate", startDate);
        }
        if (!org.springframework.util.StringUtils.isEmpty(endDate)){
            pageParam.setEnddate(endDate);
            modelAndView.addObject("endDate", endDate);
        }
        pageParam.setOrderDirection(orderDirection);
        pageParam.setOrderField(orderField);

        Pagination<FUser> pagination = this.adminUserService.selectUserPageList(pageParam, fUser);

        modelAndView.addObject("userList", pagination);
        return modelAndView;
    }

    public Map<String, Object> getBeginEndDate(Date date){

        Map<String, Object> map = new HashMap<String, Object>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        String beginDate = sdf.format(cal.getTime());

        int i = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.add(Calendar.DAY_OF_MONTH, i);
        String endDate = sdf.format(cal.getTime());

        map.put("beginDate", beginDate);
        map.put("endDate", endDate);

        return map;
    }

    @RequestMapping("capital/goCapitalPage")
    public ModelAndView userEdit(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "url", required = false) String url) {
        ModelAndView modelAndView = new ModelAndView();
        if (uid > 0) {
            Map<Integer, String> coinMap = redisHelper.getCoinTypeNameMap();
            modelAndView.addObject("coinMap", coinMap);
            modelAndView.addObject("uid", uid);
        }
        modelAndView.setViewName(url);
        return modelAndView;
    }

    @RequestMapping("/admin/updateSubmitUserCapital")
    @ResponseBody
    public ReturnResult updateSubmitUserCapital(
            @RequestParam(value="uid",required=true) Integer fid,
            @RequestParam(value="fcoinid", required=true ) Integer fcoinid,
            @RequestParam(value="amount", required=true ) BigDecimal amount
    ) throws Exception {

        //查询钱包
        if(fid == null || fcoinid == null || amount == null){
            return ReturnResult.FAILUER("参数不合法");
        }
        //更新钱包
        if(adminUserCapitalService.updateUserWallet(fid,fcoinid,amount)){
            HttpServletRequest request = sessionContextUtils.getContextRequest();
            String ip = Utils.getIpAddr(request);
            FAdmin sessionAdmin = (FAdmin) request.getSession().getAttribute("login_admin");
            mqSend.SendAdminAction(sessionAdmin.getFagentid(), sessionAdmin.getFid(), fid, LogAdminActionEnum.MODIFY_CAPITAL_BALANCE,
                    ip, "修改资金不平衡,fcoinid:"+fcoinid+",amount:"+amount);

            return ReturnResult.SUCCESS("修改成功");
        }

        return ReturnResult.FAILUER("修改失败");
    }
    
    /**
     * 资产不平衡页面
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/assetInbalance")
    public ModelAndView getAssetInbalancePage(@RequestParam(value="pageNum",required=false,defaultValue="1") Integer pageNum,
            @RequestParam(value = "userId", required = false,defaultValue = "0") Integer userId,
            @RequestParam(value = "coinId", required = false,defaultValue = "0") Integer coinId
    		) {
    	try {
    		 ModelAndView modelAndView = new ModelAndView();
    	        modelAndView.setViewName("query/assetImbalance");
    	        AssetImbalance assetImbalance = new AssetImbalance();
    	        if (userId > 0) {
    	            modelAndView.addObject("userId", userId);
    	            assetImbalance.setUserId(userId);
    	        }
    	        if (coinId > 0) {
    	            modelAndView.addObject("coinId", coinId);
    	            assetImbalance.setCoinId(coinId);
    	        }
    	        List<SystemCoinType> coinTypeList = redisHelper.getCoinTypeListAll();
    			Map<Integer,String> coinTypeMap = new HashMap<>();
    			coinTypeMap.put(0, "全部");
    			for (SystemCoinType systemCoinType : coinTypeList) {
    				coinTypeMap.put(systemCoinType.getId(), systemCoinType.getName());
    			}
    			modelAndView.addObject("coinTypeMap", coinTypeMap);
    	        PageInfo<AssetImbalance> selectAssetImbalancePage = adminUserCapitalService.selectAssetImbalancePage(assetImbalance, pageNum, Constant.adminPageSize);
    	        modelAndView.addObject("AssetImbalanceList",selectAssetImbalancePage);
    	        return modelAndView;
		} catch (Exception e) {
			logger.error("访问capital/assetInbalance异常，userId："+userId+",coinId:"+coinId,e);
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("comm/404");
			return modelAndView;
		}
       
    }
    
}
