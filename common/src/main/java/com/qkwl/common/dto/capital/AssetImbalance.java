package com.qkwl.common.dto.capital;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class AssetImbalance implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer id;

    private Integer userId;

    private Integer coinId;
    
    private String coinName;

    private BigDecimal recharge;

    private BigDecimal rechargeWork;

    private BigDecimal rewardCoin;

    private BigDecimal withdraw;

    private BigDecimal buy;

    private BigDecimal sell;

    private BigDecimal fees;

    private BigDecimal coinTradeBuy;

    private BigDecimal coinTradeSell;

    private BigDecimal coinTradeFee;

    private BigDecimal vip6;

    private BigDecimal pushIn;

    private BigDecimal pushOut;

    private BigDecimal financesCountSend;

    private BigDecimal withdrawFrozen;

    private BigDecimal tradeFrozen;

    private BigDecimal tradeFrozenCoin;

    private BigDecimal pushFrozen;

    private BigDecimal frozenFinances;

    private BigDecimal c2cRecharge;

    private BigDecimal c2cWithdraw;

    private BigDecimal c2cWithdrawFrozen;

    private BigDecimal commission;

    private BigDecimal freePlan;

    private BigDecimal free;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCoinId() {
        return coinId;
    }

    public void setCoinId(Integer coinId) {
        this.coinId = coinId;
    }

    public BigDecimal getRecharge() {
        return recharge;
    }

    public void setRecharge(BigDecimal recharge) {
        this.recharge = recharge;
    }

    public BigDecimal getRechargeWork() {
        return rechargeWork;
    }

    public void setRechargeWork(BigDecimal rechargeWork) {
        this.rechargeWork = rechargeWork;
    }

    public BigDecimal getRewardCoin() {
        return rewardCoin;
    }

    public void setRewardCoin(BigDecimal rewardCoin) {
        this.rewardCoin = rewardCoin;
    }

    public BigDecimal getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(BigDecimal withdraw) {
        this.withdraw = withdraw;
    }

    public BigDecimal getBuy() {
        return buy;
    }

    public void setBuy(BigDecimal buy) {
        this.buy = buy;
    }

    public BigDecimal getSell() {
        return sell;
    }

    public void setSell(BigDecimal sell) {
        this.sell = sell;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getCoinTradeBuy() {
        return coinTradeBuy;
    }

    public void setCoinTradeBuy(BigDecimal coinTradeBuy) {
        this.coinTradeBuy = coinTradeBuy;
    }

    public BigDecimal getCoinTradeSell() {
        return coinTradeSell;
    }

    public void setCoinTradeSell(BigDecimal coinTradeSell) {
        this.coinTradeSell = coinTradeSell;
    }

    public BigDecimal getCoinTradeFee() {
        return coinTradeFee;
    }

    public void setCoinTradeFee(BigDecimal coinTradeFee) {
        this.coinTradeFee = coinTradeFee;
    }

    public BigDecimal getVip6() {
        return vip6;
    }

    public void setVip6(BigDecimal vip6) {
        this.vip6 = vip6;
    }

    public BigDecimal getPushIn() {
        return pushIn;
    }

    public void setPushIn(BigDecimal pushIn) {
        this.pushIn = pushIn;
    }

    public BigDecimal getPushOut() {
        return pushOut;
    }

    public void setPushOut(BigDecimal pushOut) {
        this.pushOut = pushOut;
    }

    public BigDecimal getFinancesCountSend() {
        return financesCountSend;
    }

    public void setFinancesCountSend(BigDecimal financesCountSend) {
        this.financesCountSend = financesCountSend;
    }

    public BigDecimal getWithdrawFrozen() {
        return withdrawFrozen;
    }

    public void setWithdrawFrozen(BigDecimal withdrawFrozen) {
        this.withdrawFrozen = withdrawFrozen;
    }

    public BigDecimal getTradeFrozen() {
        return tradeFrozen;
    }

    public void setTradeFrozen(BigDecimal tradeFrozen) {
        this.tradeFrozen = tradeFrozen;
    }

    public BigDecimal getTradeFrozenCoin() {
        return tradeFrozenCoin;
    }

    public void setTradeFrozenCoin(BigDecimal tradeFrozenCoin) {
        this.tradeFrozenCoin = tradeFrozenCoin;
    }

    public BigDecimal getPushFrozen() {
        return pushFrozen;
    }

    public void setPushFrozen(BigDecimal pushFrozen) {
        this.pushFrozen = pushFrozen;
    }

    public BigDecimal getFrozenFinances() {
        return frozenFinances;
    }

    public void setFrozenFinances(BigDecimal frozenFinances) {
        this.frozenFinances = frozenFinances;
    }

    public BigDecimal getC2cRecharge() {
        return c2cRecharge;
    }

    public void setC2cRecharge(BigDecimal c2cRecharge) {
        this.c2cRecharge = c2cRecharge;
    }

    public BigDecimal getC2cWithdraw() {
        return c2cWithdraw;
    }

    public void setC2cWithdraw(BigDecimal c2cWithdraw) {
        this.c2cWithdraw = c2cWithdraw;
    }

    public BigDecimal getC2cWithdrawFrozen() {
        return c2cWithdrawFrozen;
    }

    public void setC2cWithdrawFrozen(BigDecimal c2cWithdrawFrozen) {
        this.c2cWithdrawFrozen = c2cWithdrawFrozen;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getFreePlan() {
        return freePlan;
    }

    public void setFreePlan(BigDecimal freePlan) {
        this.freePlan = freePlan;
    }

    public BigDecimal getFree() {
        return free;
    }

    public void setFree(BigDecimal free) {
        this.free = free;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}
    
}