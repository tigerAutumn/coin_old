package com.qkwl.service.user.impl;

import com.qkwl.common.match.MathUtils;

import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.service.user.dao.UserCoinWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包操作
 * Created by wangchen on 2017-04-19.
 */
public class UserWalletBaseImpl {

    @Autowired
    private UserCoinWalletMapper coinWalletMapper;


    /**
     * 更新虚拟币钱包可用操作
     * @param uid   用户
     * @param coinId 币种ID
     * @param total 可用虚拟币金额（正数是加，负数是减）
     * @return true-成功，false-失败
     */
    protected boolean updateUserCoinWalletTotal(Integer uid, Integer coinId,BigDecimal total){
        return updateUserCoinWallet(uid, coinId, total, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    /**
     * 更新虚拟币钱包冻结操作
     * @param uid   用户
     * @param coinId 币种ID
     * @param freezeTotal 冻结虚拟币金额（正数是加，负数是减）
     * @return true-成功，false-失败
     */
    protected boolean updateUserCoinWalletFreezeTotal(Integer uid, Integer coinId,BigDecimal freezeTotal){
        return updateUserCoinWallet(uid, coinId, BigDecimal.ZERO, freezeTotal, BigDecimal.ZERO);
    }
    /**
     * 更新虚拟币钱包借贷操作
     * @param uid   用户
     * @param coinId 币种ID
     * @param borrowTotal 借贷虚拟币金额（正数是加，负数是减）
     * @return true-成功，false-失败
     */
    protected boolean updateUserCoinWalletBorrow(Integer uid, Integer coinId,BigDecimal borrowTotal){
        return updateUserCoinWallet(uid, coinId, BigDecimal.ZERO, BigDecimal.ZERO, borrowTotal);
    }

    /**
     * 更新虚拟币钱包操作（不含借贷）
     * @param uid   用户
     * @param coinId 币种ID
     * @param total 可用虚拟币金额（正数是加，负数是减）
     * @param freezeTotal 冻结虚拟币金额（正数是加，负数是减）
     * @return true-成功，false-失败
     */
    protected boolean updateUserCoinWallet(Integer uid, Integer coinId,BigDecimal total,BigDecimal freezeTotal){
        return updateUserCoinWallet(uid, coinId, total, freezeTotal, BigDecimal.ZERO);
    }

    /**
     * 更新虚拟币钱包操作
     * @param uid 用户
     * @param coinId 币种ID
     * @param total 可用虚拟币金额（正数是加，负数是减）
     * @param freezeTotal 冻结虚拟币金额（正数是加，负数是减）
     * @param borrowTotal 借贷金额（正数是加，负数是减）
     * @return true-成功，false-失败
     */
    protected boolean updateUserCoinWallet(Integer uid, Integer coinId, BigDecimal total, BigDecimal freezeTotal, BigDecimal borrowTotal){
        UserCoinWallet coinWallet = coinWalletMapper.selectByUidAndCoinLock(uid,coinId);
        if (coinWallet == null) {
            return false;
        }
        coinWallet.setTotal(MathUtils.add(coinWallet.getTotal(), total));
        coinWallet.setFrozen(MathUtils.add(coinWallet.getFrozen(), freezeTotal));
        coinWallet.setBorrow(MathUtils.add(coinWallet.getBorrow(), borrowTotal));
        coinWallet.setGmtModified(new Date());
        // 可用金额判断
        if(coinWallet.getTotal().compareTo(BigDecimal.ZERO) == -1){
            return false;
        }
        // 冻结金额判断
        if(coinWallet.getFrozen().compareTo(BigDecimal.ZERO) == -1){
            return false;
        }
        // 借贷金额
        if(coinWallet.getBorrow().compareTo(BigDecimal.ZERO) == -1){
            return false;
        }
        //更新钱包操作
        if (coinWalletMapper.updateByPrimaryKey(coinWallet) <= 0) {
            return false;
        }
        return true;
    }
}
