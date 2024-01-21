package com.qkwl.service.admin.job;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qkwl.common.dto.Enum.CapitalOperationInOutTypeEnum;
import com.qkwl.common.dto.Enum.CapitalOperationInStatus;
import com.qkwl.common.dto.Enum.CapitalOperationOutStatus;
import com.qkwl.common.dto.Enum.EntrustStateEnum;
import com.qkwl.common.dto.Enum.EntrustTypeEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.OperationlogEnum;
import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.Enum.UserFinancesStateEnum;
import com.qkwl.common.dto.Enum.UserPushStateEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationInStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationOutStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationTypeEnum;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.capital.AssetImbalance;
import com.qkwl.common.dto.capital.FUserPushDTO;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.entrust.FEntrustHistory;
import com.qkwl.common.dto.finances.FUserFinancesDTO;
import com.qkwl.common.dto.log.FLogUserAction;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.rpc.admin.IAdminC2CService;
import com.qkwl.common.rpc.admin.IAdminCommissionService;
import com.qkwl.common.rpc.admin.IAdminEntrustServer;
import com.qkwl.common.rpc.admin.IAdminLogService;
import com.qkwl.common.rpc.admin.IAdminRewardCodeService;
import com.qkwl.common.rpc.admin.IAdminUserCapitalService;
import com.qkwl.common.rpc.admin.IAdminUserFinances;
import com.qkwl.common.rpc.admin.IAdminUserService;
import com.qkwl.common.util.DateUtils;
import com.qkwl.service.admin.bc.dao.AssetImbalanceMapper;

@Component
public class AssetImbalanceJob {

	@Autowired
    private IAdminUserService adminUserService;
	@Autowired
    private RedisHelper redisHelper;
	@Autowired
    private IAdminC2CService adminC2CService;
	@Autowired
    private IAdminUserCapitalService adminUserCapitalService;
	@Autowired
    private IAdminEntrustServer adminEntrustServer;
    @Autowired
    private IAdminRewardCodeService adminRewardCodeService;
    @Autowired
    private IAdminLogService adminLogService;
    @Autowired
    private IAdminUserFinances adminUserFinances;
    @Autowired
	private IAdminCommissionService adminCommissionService;
	@Autowired
	private AssetImbalanceMapper assetImbalanceMapper;
    
	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(AssetImbalanceJob.class);
	
	@Scheduled(cron="0 0 23 1/1 * ? ")
	//@PostConstruct
	public void work() {
		logger.info("======================资产不平衡统计定时开始======================");
		//查询所有用户的id
		List<FUser> list = adminUserService.selectAll();
        
		//币种
        List<SystemCoinType> coinList = redisHelper.getCoinTypeListSystem();
        
        for (int i = 0; i < list.size(); i++) {
        	FUser fUser = list.get(i);
        	logger.info("当前第 "+i+" 个，uid："+fUser.getFid());
        	start(fUser, coinList);
		}
		logger.info("======================资产不平衡统计定时结束======================");
	}
	
	public void start(FUser user, List<SystemCoinType> coinList) {
		
		Integer fuid = user.getFid();
		try {
			
			//用户钱包
            Map<Integer, UserCoinWallet> walletMap = adminUserCapitalService.selectUserVirtualWallet(fuid);
            
            Date nowDate = new Date();
			
			
			//c2c查询
	        Map<Integer, UserC2CEntrust> statisticsRechargeWithdrawTotal = adminC2CService.statisticsRechargeWithdrawTotal(fuid,nowDate);
	        //该币为gset，btc，eth，usdt时，在该交易区，该币去购买别的币所减少的数量
	        Map<Integer, FEntrustHistory> mainCoinBuyCost = adminEntrustServer.selectTotalAmountByType(fuid, true, EntrustTypeEnum.BUY.getCode(), null, null ,nowDate);
	        //该币为gset，btc，eth，usdt时，在该交易区，该币去卖出别的币所增加的数量
	        Map<Integer, FEntrustHistory> mainCoinSellIncome = adminEntrustServer.selectTotalAmountByType(fuid, true, EntrustTypeEnum.SELL.getCode(), null, null ,nowDate);
	        //该币为交易币种，购买-增加的数量
	        Map<Integer, FEntrustHistory> tradeCoinBuyIncome = adminEntrustServer.selectTotalAmountByType(fuid, false, EntrustTypeEnum.BUY.getCode(), null, null ,nowDate);
	        //该币为交易币种，卖出-减少的数量
	        Map<Integer, FEntrustHistory> tradeCoinSellCost = adminEntrustServer.selectTotalAmountByType(fuid, false, EntrustTypeEnum.SELL.getCode(), null, null ,nowDate);
	        
	        //该币为gset，btc，eth，usdt时，在该交易区，该币去购买别的币所减少的数量
            Map<Integer, FEntrust> mainCoinBuyPartDealCost = adminEntrustServer.selectCurrentTotalAmountByType(fuid, true,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null,nowDate);
            Map<Integer, FEntrust> mainCoinBuyGoingCost = adminEntrustServer.selectCurrentTotalAmountByType(fuid, true,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), null, null,nowDate);
	        
            //该币为gset，btc，eth，usdt时，在该交易区，该币卖出的收入
            Map<Integer, FEntrust> mainCoinSellPartDealIncome = adminEntrustServer.selectCurrentTotalAmountByType(fuid, true, EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null,nowDate);
            
            //该币为交易币种，买入该币的收入
            Map<Integer, FEntrust> tradeCoinBuyPartDealIncome = adminEntrustServer.selectCurrentTotalAmountByType(fuid,false,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null,nowDate);
            
            //该币为交易币种，卖出该币的支出
            Map<Integer, FEntrust> tradeCoinSellPartDealCost = adminEntrustServer.selectCurrentTotalAmountByType(fuid,false,EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null,nowDate);
            Map<Integer, FEntrust> tradeCoinSellGoingCost = adminEntrustServer.selectCurrentTotalAmountByType(fuid, false,EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.Going.getCode(), null, null,nowDate);
            
            //虚拟币充提
            Map<Integer, BigDecimal> VirtualWalletRecharge = adminUserCapitalService.selectVirtualWalletTotalAmountMap(fuid,VirtualCapitalOperationTypeEnum.COIN_IN.getCode(),VirtualCapitalOperationInStatusEnum.SUCCESS, null, null,nowDate);
            Map<Integer, BigDecimal> VirtualWalletWithdraw = adminUserCapitalService.selectVirtualWalletTotalAmountMap(fuid,VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.OperationSuccess, null, null,nowDate);
            
            //虚拟币提现冻结 
            Map<Integer, BigDecimal> WaitForOperationFrozen = adminUserCapitalService.selectVirtualWalletTotalAmountMap(fuid, VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.WaitForOperation, null, null,nowDate);
            Map<Integer, BigDecimal> OperationLockFrozen = adminUserCapitalService.selectVirtualWalletTotalAmountMap(fuid, VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.OperationLock, null, null,nowDate);
            Map<Integer, BigDecimal> LockOrderFrozen = adminUserCapitalService.selectVirtualWalletTotalAmountMap(fuid, VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.LockOrder, null, null,nowDate);
            
            //手工充值
            Map<Integer, BigDecimal> rechargeWorkMap = adminUserCapitalService.selectAdminRechargeVirtualWalletTotalAmount(fuid, OperationlogEnum.AUDIT, null, nowDate);
            
            //虚拟币兑换
            Map<Integer, BigDecimal> rewardCodeMap = adminRewardCodeService.selectWalletTotalAmount(fuid, null, null);
            
            
            
            //循环币种
	        for (SystemCoinType coin : coinList) {
	        	//查询虚拟钱包
	            //UserCoinWallet vwallet = adminUserCapitalService.selectUserVirtualWallet(fuid, coin.getId());
	        	UserCoinWallet vwallet = walletMap.get(coin.getId());
	            if(vwallet == null){
	                continue;
	            }
	            
	            // 充提统计
	            BigDecimal recharge = BigDecimal.ZERO;
	            BigDecimal withdraw = BigDecimal.ZERO;
	            BigDecimal withdrawFrozen = BigDecimal.ZERO;

	            // PUSH资产
	            BigDecimal userPushCoinIn = BigDecimal.ZERO;
	            BigDecimal userPushCoinOut = BigDecimal.ZERO;
	            BigDecimal userPushCoinFrozen = BigDecimal.ZERO;

	            // 买卖交易
	            BigDecimal buyamount = BigDecimal.ZERO;
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
	            	//buy = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), null, null);
	                buy = getEntrustHistory(mainCoinBuyCost.get(coin.getId()));
	            	//sell = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.SELL.getCode(), null, null);
	                sell = getEntrustHistory(mainCoinSellIncome.get(coin.getId()));
	                //currentbuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	                currentbuy = getEntrust(mainCoinBuyPartDealCost.get(coin.getId()));
	                //currentsell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	                currentsell = getEntrust(mainCoinSellPartDealIncome.get(coin.getId()));
	                //FEntrust frozenEntrust = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), null, null);
	                FEntrust frozenEntrust = getEntrust(mainCoinBuyGoingCost.get(coin.getId()));
	                
	                frozenamount = MathUtils.sub(currentbuy.getFamount(), currentbuy.getFsuccessamount());
	                
	                
	                
	                
	                //交易冻结金额
	                frozenamount = MathUtils.add(frozenamount, frozenEntrust.getFamount());

	                //人民币充提统计
	                recharge = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.IN.getCode(),
	                        CapitalOperationInStatus.Come, null, null);
	                withdraw = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
	                        CapitalOperationOutStatus.OperationSuccess, null, null);


	                // 冻结
	                BigDecimal wf = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
	                        CapitalOperationOutStatus.WaitForOperation, null, null);
	                withdrawFrozen = adminUserCapitalService.selectWalletTotalAmount(fuid, CapitalOperationInOutTypeEnum.OUT.getCode(),
	                        CapitalOperationOutStatus.OperationLock, null, null);
	                //充提冻结
	                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);

	                // PUSH资产
	                /*FUserPushDTO userPushIn = adminUserCapitalService.selectUserPushBalance(null, user.getFshowid(),
	                        null, UserPushStateEnum.PAYSUCCEED.getCode(), null, null);
	                FUserPushDTO userPushOut = adminUserCapitalService.selectUserPushBalance(user.getFshowid(), null,
	                        null, UserPushStateEnum.PAYSUCCEED.getCode(), null, null);
	                userPushCoinIn = userPushOut == null ? BigDecimal.ZERO : userPushOut.getFamount();
	                userPushCoinOut = userPushIn == null ? BigDecimal.ZERO : userPushIn.getFamount();
*/
	                //用法币购买别的币所花费的
	                buyamount = MathUtils.add(buy.getFsuccessamount(), currentbuy.getFsuccessamount());
	                //卖别的币所获得的法币
	                sellamount = MathUtils.add(sell.getFsuccessamount(), currentsell.getFsuccessamount());
	                //总共交易卖的手续费
	                fee = MathUtils.add(sell.getFfees(), currentsell.getFfees());

	                //vip6购买
	              /*  FLogUserAction userAction = adminLogService.selectVip6ByUser(fuid, LogUserActionEnum.BUY_VIP6.getCode());
	                if(userAction != null){
	                    vip6RMB = new BigDecimal(3888D);
	                }*/
	                
	                //返佣
	                commission = adminCommissionService.selectIssuedAmountByIntroId(fuid);
	                if (commission == null) {
						commission = BigDecimal.ZERO;
					}
	            } else {

	                // 币币交易-主币种-买-减少
	                //coinSelfBuy = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), null, null);
	            	coinSelfBuy = getEntrustHistory(mainCoinBuyCost.get(coin.getId()));
	                // 币币交易-主币种-卖-增加
	                // coinSelfSell = adminEntrustServer.selectTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.SELL.getCode(), null, null);
	            	coinSelfSell = getEntrustHistory(mainCoinSellIncome.get(coin.getId()));
	            	// 币币交易-交易币种-买-增加
	                //coinBuy = adminEntrustServer.selectTotalAmountByType(fuid, null, coin.getId(),EntrustTypeEnum.BUY.getCode(), null, null);
	            	coinBuy = getEntrustHistory(tradeCoinBuyIncome.get(coin.getId()));
	            	// 币币交易-交易币种-卖-减少
	                //coinSell = adminEntrustServer.selectTotalAmountByType(fuid, null, coin.getId(),EntrustTypeEnum.SELL.getCode(), null, null);
	            	coinSell = getEntrustHistory(tradeCoinSellCost.get(coin.getId()));
	                
	            	
	            	// 币币交易-主币种-买-减少
	                //currentSelfCoinBuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	            	currentSelfCoinBuy = getEntrust(mainCoinBuyPartDealCost.get(coin.getId()));
	                //FEntrust currentSelfCoinBuyGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(),null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), null, null);
	            	FEntrust currentSelfCoinBuyGoing = getEntrust(mainCoinBuyGoingCost.get(coin.getId()));
	            	// 币币交易-主币种-卖-增加
	                //currentSelfCoinSell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	            	currentSelfCoinSell = getEntrust(mainCoinSellPartDealIncome.get(coin.getId()));
	            	//FEntrust currentSelfCoinSellGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, coin.getId(), null,EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.Going.getCode(), null, null);
	                // 币币交易-交易币种-买-增加
	                //currentcoinBuy = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),EntrustTypeEnum.BUY.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	            	currentcoinBuy = getEntrust(tradeCoinBuyPartDealIncome.get(coin.getId()));
	            	// 币币交易-交易币种-卖-减少
	                //currentcoinSell = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.PartDeal.getCode(), null, null);
	            	currentcoinSell = getEntrust(tradeCoinSellPartDealCost.get(coin.getId()));
	            	//FEntrust currentcoinSellGoing = adminEntrustServer.selectCurrentTotalAmountByType(fuid, null, coin.getId(),EntrustTypeEnum.SELL.getCode(), EntrustStateEnum.Going.getCode(), null, null);
	            	FEntrust currentcoinSellGoing  = getEntrust(tradeCoinSellGoingCost.get(coin.getId()));
	            	
	                frozencountCoin = MathUtils.add(
	                        MathUtils.sub(currentSelfCoinBuy.getFamount(), currentSelfCoinBuy.getFsuccessamount()),
	                        MathUtils.sub(currentSelfCoinBuyGoing.getFamount(), currentSelfCoinBuyGoing.getFsuccessamount())
	                );
	                //交易冻结数量
	                frozencountCoin = MathUtils.add(frozencountCoin,
	                        MathUtils.add(currentcoinSell.getFleftcount(), currentcoinSellGoing.getFleftcount()));

	                //虚拟币充提
	                //recharge = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(), VirtualCapitalOperationTypeEnum.COIN_IN.getCode(), VirtualCapitalOperationInStatusEnum.SUCCESS, null, null);
	                recharge = VirtualWalletRecharge.get(coin.getId()) == null ? BigDecimal.ZERO : VirtualWalletRecharge.get(coin.getId());
	                //withdraw = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(),VirtualCapitalOperationOutStatusEnum.OperationSuccess, null, null);
	                withdraw = VirtualWalletWithdraw.get(coin.getId()) == null ? BigDecimal.ZERO : VirtualWalletWithdraw.get(coin.getId());
	                
	                
	                // 冻结
	                //BigDecimal wf = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(), VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.WaitForOperation, null, null);
	                BigDecimal wf = WaitForOperationFrozen.get(coin.getId()) == null ? BigDecimal.ZERO : WaitForOperationFrozen.get(coin.getId());
	                //withdrawFrozen = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(),  VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.OperationLock, null, null);
	                withdrawFrozen = OperationLockFrozen.get(coin.getId()) == null ? BigDecimal.ZERO : OperationLockFrozen.get(coin.getId());
	                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);
	                //wf = adminUserCapitalService.selectVirtualWalletTotalAmount(fuid, coin.getId(), VirtualCapitalOperationTypeEnum.COIN_OUT.getCode(), VirtualCapitalOperationOutStatusEnum.LockOrder, null, null);
	                wf = LockOrderFrozen.get(coin.getId()) == null ? BigDecimal.ZERO : LockOrderFrozen.get(coin.getId());
	                //充提冻结
	                withdrawFrozen = MathUtils.add(withdrawFrozen, wf);

	                // PUSH资产
	                /*FUserPushDTO userPushIn = adminUserCapitalService.selectUserPushBalance(null, user.getFshowid(),
	                        coin.getId(), UserPushStateEnum.PAYSUCCEED.getCode(), null, null);
	                FUserPushDTO userPushOut = adminUserCapitalService.selectUserPushBalance(user.getFshowid(), null,
	                        coin.getId(), UserPushStateEnum.PAYSUCCEED.getCode(), null, null);
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
	                                //MathUtils.add(currentSelfCoinSell.getFfees(), currentSelfCoinSellGoing.getFfees())),
	                        		MathUtils.add(currentSelfCoinSell.getFfees(), currentSelfCoinBuyGoing.getFfees())),
	                        MathUtils.add(coinBuy.getFfees(), currentcoinBuy.getFfees())
	                );

	                /*FUserPushDTO userFrozenPush = adminUserCapitalService.selectUserPushBalance(user.getFshowid(),
	                        null, coin.getId(), UserPushStateEnum.PAYWAIT.getCode(), null, null);
	                userPushCoinFrozen = userFrozenPush == null ? BigDecimal.ZERO : userFrozenPush.getFcount();*/
	            }
	            
	            //手工充值
	            //BigDecimal rechargeWork = adminUserCapitalService.selectAdminRechargeVirtualWalletTotalAmount(fuid, coin.getId(), OperationlogEnum.AUDIT, null, null);
	            BigDecimal rechargeWork = rechargeWorkMap.get(coin.getId()) == null ? BigDecimal.ZERO : rechargeWorkMap.get(coin.getId());
	            
	            //虚拟币兑换
	            //BigDecimal rewardCoin = adminRewardCodeService.selectWalletTotalAmount(fuid, coin.getId(), null, null);
	            BigDecimal rewardCoin = rewardCodeMap.get(coin.getId()) == null ? BigDecimal.ZERO : rewardCodeMap.get(coin.getId());
	            
	            // 理财	当前理财功能未开启，且现有的理财数据均为取消状态
	            /*FUserFinancesDTO userFinancesSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.SEND.getCode());
	            FUserFinancesDTO userFinancesRedeemSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.REDEEM.getCode());
	            FUserFinancesDTO userFinancesFrozenSend = adminUserFinances.selectUserFinancesBalance(fuid, coin.getId(), UserFinancesStateEnum.FROZEN.getCode());
	            // 处理理财数量
	            financesCountSend = serFinancesSend == null ? BigDecimal.ZERO : userFinancesSend.getFplanamount();
	            BigDecimal financesCountRedeemSend = userFinancesRedeemSend == null ? BigDecimal.ZERO : userFinancesRedeemSend.getFplanamount();
	            frozenFinances = userFinancesFrozenSend == null ? BigDecimal.ZERO:userFinancesFrozenSend.getFamount();
	            financesCountSend = MathUtils.add(financesCountSend, financesCountRedeemSend);
	            */
	            
	            
	            //c2c
	            if(statisticsRechargeWithdrawTotal != null ) {
	            	 UserC2CEntrust statisticsEntrust = statisticsRechargeWithdrawTotal.get(coin.getId());
	                 if(statisticsEntrust != null) {
	                 	c2cRecharge = statisticsEntrust.getRecharge();
	                    c2cWithdraw = statisticsEntrust.getWithdraw();
	                    c2cWithdrawFrozen = statisticsEntrust.getWithdrawFrozen();
	                 }
	            }
	            
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

	            BigDecimal frozenTrade = BigDecimal.ZERO;  
	            if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
	                frozenTrade = frozenamount;  //法币交易冻结的金额
	                freeplan = MathUtils.sub(freeplan, buyamount); //用法币购买别的币所花费的
	                freeplan = MathUtils.add(freeplan, sellamount);//卖别的币所获得的法币
	                freeplan = MathUtils.sub(freeplan, fee);  //法币卖的手续费
	            }else{
	                frozenTrade = frozencount;  // 0
	                freeplan = MathUtils.sub(freeplan, coinTradeBuy);  //虚拟币交易的花费
	                freeplan = MathUtils.add(freeplan, coinTradeSell);  //虚拟币交易的收入
	                freeplan = MathUtils.sub(freeplan, coinTradeFee);  //虚拟币交易的手续费
	                freeplan = MathUtils.sub(freeplan, frozencountCoin);//虚拟币交易导致冻结的数量
	            }
	            freeplan = MathUtils.sub(freeplan, frozenTrade);
	            boolean isFreeBalance = freeplan.compareTo(vwallet.getTotal()) == 0;
	            
	            //如果资产不平衡则入库
	            if (!isFreeBalance) {
	            	AssetImbalance assetImbalance = assetImbalanceMapper.selectByUidAndCoinId(fuid, coin.getId());
	            	if(assetImbalance == null) {
	            		assetImbalance = new AssetImbalance();
	            	}
	            	assetImbalance.setUserId(fuid);
	            	assetImbalance.setCoinId(coin.getId());
	            	assetImbalance.setRecharge(rechargeCoin);
	            	assetImbalance.setWithdraw(withdrawCoin);
	            	assetImbalance.setRechargeWork(rechargeWork);
	            	assetImbalance.setRewardCoin(rewardCoin);
	            	if(coin.getCoinType().equals(SystemCoinTypeEnum.CNY.getCode())){
		            	assetImbalance.setBuy(buyamount);
		            	assetImbalance.setSell(sellamount);
	            	}
	            	assetImbalance.setFees(fee);
	            	assetImbalance.setVip6(vip6RMB);
	            	assetImbalance.setPushIn(userPushCoinIn);
	            	assetImbalance.setPushOut(userPushCoinOut);
	            	assetImbalance.setFinancesCountSend(financesCountSend);
	            	assetImbalance.setWithdrawFrozen(frozenCoin);
	            	assetImbalance.setTradeFrozen(frozenTrade);
	            	assetImbalance.setPushFrozen(userPushCoinFrozen);
	            	assetImbalance.setFrozenFinances(frozenFinances);
	            	assetImbalance.setCoinTradeBuy(coinTradeBuy);
	            	assetImbalance.setCoinTradeSell(coinTradeSell);
	            	assetImbalance.setCoinTradeFee(coinTradeFee);
	            	assetImbalance.setTradeFrozenCoin(frozencountCoin);
	            	assetImbalance.setC2cRecharge(c2cRecharge);
	            	assetImbalance.setC2cWithdraw(c2cWithdraw);
	            	assetImbalance.setC2cWithdrawFrozen(c2cWithdrawFrozen);
	            	assetImbalance.setCommission(commission);
	            	assetImbalance.setFreePlan(freeplan);
	            	assetImbalance.setFree(vwallet.getTotal());
	            	assetImbalance.setCreateTime(new Date());
	            	if(assetImbalance.getId() != null) {
	            		assetImbalanceMapper.updateByPrimaryKey(assetImbalance);
	            	}else {
	            		assetImbalanceMapper.insert(assetImbalance);
	            	}
				}else {
					assetImbalanceMapper.deleteByUidAndCoinId(fuid, coin.getId());
				}
	        }
		} catch (Exception e) {
			logger.info("=======================资产不平衡统计报错，uid为："+fuid+"======================");
			e.printStackTrace();
		}
		
	}
	
	
	
	public FEntrustHistory getEntrustHistory(FEntrustHistory f) {
    	if(f != null) {
    		return f;
    	}else {
    		FEntrustHistory eh = new FEntrustHistory();
    		eh.setFamount(BigDecimal.ZERO);
    		eh.setFcount(BigDecimal.ZERO);
    		eh.setFfees(BigDecimal.ZERO);
    		eh.setFlast(BigDecimal.ZERO);
    		eh.setFleftcount(BigDecimal.ZERO);
    		eh.setFleftfees(BigDecimal.ZERO);
    		eh.setFprize(BigDecimal.ZERO);
    		eh.setFsuccessamount(BigDecimal.ZERO);
    		return eh;
    	}
    }
	
	public FEntrust getEntrust(FEntrust f) {
    	if(f != null) {
    		return f;
    	}else {
    		FEntrust eh = new FEntrust();
    		eh.setFamount(BigDecimal.ZERO);
    		eh.setFcount(BigDecimal.ZERO);
    		eh.setFfees(BigDecimal.ZERO);
    		eh.setFlast(BigDecimal.ZERO);
    		eh.setFleftcount(BigDecimal.ZERO);
    		eh.setFleftfees(BigDecimal.ZERO);
    		eh.setFprize(BigDecimal.ZERO);
    		eh.setFsuccessamount(BigDecimal.ZERO);
    		return eh;
    	}
    }
	
}