package com.qkwl.service.capital.tx;

import com.qkwl.common.match.MathUtils;
import com.qkwl.service.capital.base.UserWalletBase;
import com.qkwl.service.capital.dao.FPoolMapper;
import com.qkwl.service.capital.dao.FUserVirtualAddressMapper;
import com.qkwl.service.capital.dao.FVirtualCapitalOperationMapper;
import com.qkwl.service.capital.model.FPoolDO;
import com.qkwl.service.capital.model.FUserVirtualAddressDO;
import com.qkwl.service.capital.model.FVirtualCapitalOperationDO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 币种相关操作
 */
@Service("coinCapitalServiceTx")
public class CoinCapitalServiceTx extends UserWalletBase {
    @Autowired
    private FVirtualCapitalOperationMapper virtualCapitalOperationMapper;
    @Autowired
    private FUserVirtualAddressMapper userVirtualAddressMapper;
    @Autowired
    private FPoolMapper poolMapper;

    /**
     * 创建充值订单
     */
    public Boolean createRechargeOrder(FVirtualCapitalOperationDO operation) {
        return virtualCapitalOperationMapper.insert(operation) > 0;
    }

    /**
     * 创建提现订单
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean createWithdrawOrder(FVirtualCapitalOperationDO operation) throws Exception {
        if (virtualCapitalOperationMapper.insert(operation) <= 0) {
            return false;
        }
        BigDecimal opAmount = MathUtils.add(MathUtils.add(operation.getFamount(), operation.getFfees()), operation.getFbtcfees());
        boolean resultStatus = super.updateUserCoinWallet(operation.getFuid(), operation.getFcoinid(), MathUtils.positive2Negative(opAmount), opAmount);
        if (!resultStatus) {
            throw new Exception();
        }
        return true;
    }

    /**
     * 取消提现订单
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean cancleWithdrawOrder(FVirtualCapitalOperationDO operation) throws Exception {
        if (virtualCapitalOperationMapper.updateByPrimaryKey(operation) <= 0) {
            return false;
        }
        BigDecimal opAmount = MathUtils.add(MathUtils.add(operation.getFamount(), operation.getFfees()), operation.getFbtcfees());
        boolean resultStatus = super.updateUserCoinWallet(operation.getFuid(), operation.getFcoinid(), opAmount, MathUtils.positive2Negative(opAmount));
        if (!resultStatus) {
            throw new Exception();
        }
        return true;
    }

    /**
     * 充值地址
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean createCoinAddress(FPoolDO fpool, FUserVirtualAddressDO virtualAddress) throws Exception {
        // 修改充值地址的使用状态标志位为已使用
        if (poolMapper.updatePoolStatus(fpool) <= 0) {
            throw new Exception();
        }
        if (userVirtualAddressMapper.insert(virtualAddress) <= 0) {
            throw new Exception();
        }
        return true;
    }
}
