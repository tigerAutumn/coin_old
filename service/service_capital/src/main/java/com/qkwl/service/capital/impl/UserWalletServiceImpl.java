package com.qkwl.service.capital.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.service.capital.dao.UserCoinWalletMapper;

/**
 * 用户钱包接口
 */
@Service("userWalletService")
public class UserWalletServiceImpl implements IUserWalletService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserWalletServiceImpl.class);
	
    @Autowired
    private UserCoinWalletMapper coinWalletMapper;

    @Override
    public UserCoinWallet getUserCoinWallet(Integer userId, Integer coinId) {
        return coinWalletMapper.selectByUidAndCoin(userId, coinId);
    }

    @Override
    public List<UserCoinWallet> listUserCoinWallet(Integer userId) {
    	try {
    		return coinWalletMapper.selectByUid(userId);
		} catch (Exception e) {
			logger.error("查询用户钱包异常",e);
			return null;
		}
        
    }
}
