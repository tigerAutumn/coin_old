package com.qkwl.service.admin.bc.impl;

import com.qkwl.service.admin.bc.dao.FPoolMapper;
import com.qkwl.service.admin.bc.dao.UserCoinWalletMapper;
import com.qkwl.common.dto.coin.FPool;

import com.qkwl.common.dto.wallet.UserCoinWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.UsesSunHttpServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("adminVirtualCoinServiceTx")
public class AdminVirtualCoinServiceTx {

    @Autowired
    private FPoolMapper poolMapper;
    @Autowired
    private UserCoinWalletMapper userCoinWalletMapper;

    /**
     * 生成地址
     */
    public boolean insertPoolInfo(Integer coinId, String address) throws Exception {
        FPool poolInfo = new FPool();
        poolInfo.setFaddress(address);
        poolInfo.setFcoinid(coinId);
        poolInfo.setFstatus(0);
        poolInfo.setVersion(1);
        if (poolMapper.insert(poolInfo) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 分配钱包
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertCoinWallet(Integer coinId, Integer uid) throws Exception{
        UserCoinWallet virtualWallet = userCoinWalletMapper.selectByUidAndCoin(uid,coinId);
        if(virtualWallet==null){
            virtualWallet = new UserCoinWallet();
            virtualWallet.setUid(uid);
            virtualWallet.setCoinId(coinId);
            virtualWallet.setTotal(BigDecimal.ZERO);
            virtualWallet.setFrozen(BigDecimal.ZERO);
            virtualWallet.setBorrow(BigDecimal.ZERO);
            virtualWallet.setIco(BigDecimal.ZERO);
            virtualWallet.setGmtCreate(new Date());
            virtualWallet.setGmtModified(new Date());
            userCoinWalletMapper.insert(virtualWallet);
        }
        return true;
    }
    
    /**
     * 钱包切换
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean creatCoinWallet(Integer oldCoinId, Integer newCoinId) throws Exception{
    	List<UserCoinWallet> virtualWallet = userCoinWalletMapper.selectExistCoinWallet(oldCoinId);
    	Date date = new Date();
    	for (UserCoinWallet userCoinWallet : virtualWallet) {
    		 if(userCoinWallet != null){
    			 userCoinWallet.setCoinId(newCoinId);
    			 userCoinWallet.setGmtCreate(date);
    			 userCoinWallet.setGmtModified(date);
    			 userCoinWallet.setId(null);
    	         userCoinWalletMapper.insert(userCoinWallet);
    	        }
		}
        return true;
    }
    
}
