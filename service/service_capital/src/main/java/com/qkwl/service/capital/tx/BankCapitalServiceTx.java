package com.qkwl.service.capital.tx;

import com.qkwl.common.dto.Enum.CapitalOperationOutStatus;
import com.qkwl.common.dto.Enum.CapitalOperationTypeEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.util.Utils;
import com.qkwl.service.capital.dao.FUserBankinfoMapper;
import com.qkwl.service.capital.dao.FWalletCapitalOperationMapper;
import com.qkwl.service.capital.base.UserWalletBase;
import com.qkwl.service.capital.model.FWalletCapitalOperationDO;

import com.qkwl.service.capital.service.BankCapitalService;
import com.qkwl.service.capital.util.MQSendUtils;
import com.qkwl.service.common.mapper.UserCommonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 银行相关操作
 */
@Service("bankCapitalServiceTx")
public class BankCapitalServiceTx extends UserWalletBase {
    private static final Logger logger = LoggerFactory.getLogger(BankCapitalService.class);

    /**
     * 拆单单笔最高额度
     */
    private final static BigDecimal BatchAmount = BigDecimal.valueOf(50000);

    @Autowired
    private FUserBankinfoMapper userBankinfoMapper;
    @Autowired
    private FWalletCapitalOperationMapper walletCapitalOperationMapper;
    @Autowired
    private UserCommonMapper userCommonMapper;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private ValidateHelper validateHelper;
    @Autowired
    private MQSendUtils mqSendUtils;
    /**
     * 创建充值订单
     */
    public Boolean createRechargeOrder(FWalletCapitalOperationDO operation) {
        return walletCapitalOperationMapper.insert(operation) > 0;
    }

    /**
     * 创建提现订单
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean createWithdrawOrder(FWalletCapitalOperationDO operation, BigDecimal withdrawFee) throws Exception {

        //总提现金额
        BigDecimal opAmount = operation.getFamount();
        //单笔总额度
        BigDecimal onceAmount = MathUtils.toScaleNum(MathUtils.add(BatchAmount,MathUtils.mul(BatchAmount, withdrawFee)), MathUtils.ENTER_CNY_SCALE);
        //拆单后笔数
        BigDecimal[] results = opAmount.divideAndRemainder(onceAmount);

        BigDecimal withdrawAmount = BigDecimal.ZERO;

        //先分正常的单数
        for(int index = 0;index < results[0].intValue(); index++){
            operation.setFid(null);
            operation.setFfees(MathUtils.mul(onceAmount, withdrawFee));
            operation.setFamount(MathUtils.toScaleNum(MathUtils.sub(onceAmount, operation.getFfees()), MathUtils.ENTER_CNY_SCALE));
            if (walletCapitalOperationMapper.insert(operation) <= 0) {
                throw new Exception("添加提现记录失败");
            }
            withdrawAmount = MathUtils.add(withdrawAmount,MathUtils.add(operation.getFamount(),operation.getFfees()));
        }
        //还有余下的部分
        if(results[1].compareTo(BigDecimal.ZERO) > 0){
            operation.setFid(null);
            operation.setFfees(MathUtils.mul(results[1],withdrawFee));
            operation.setFamount(MathUtils.toScaleNum(MathUtils.sub(results[1],operation.getFfees()),MathUtils.ENTER_CNY_SCALE));
            if (walletCapitalOperationMapper.insert(operation) <= 0) {
                throw new Exception("添加提现记录失败");
            }
            withdrawAmount = MathUtils.add(withdrawAmount,MathUtils.add(operation.getFamount(),operation.getFfees()));
        }
        boolean resultStatus = super.updateUserCoinWallet(operation.getFuid(), operation.getFcoinid(), MathUtils.positive2Negative(withdrawAmount), withdrawAmount);
        if (!resultStatus) {
            throw new Exception();
        }
        return true;
    }

    /**
     * 取消充值订单
     */
    public Boolean cancleRechargeOrder(FWalletCapitalOperationDO operation) throws Exception {
        return walletCapitalOperationMapper.updateByPrimaryKey(operation) > 0;
    }

    /**
     * 取消提现订单
     */
    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean cancleWithdrawOrder(FWalletCapitalOperationDO operation) throws Exception {
        if (walletCapitalOperationMapper.updateByPrimaryKey(operation) <= 0) {
            return false;
        }
        BigDecimal opAmount = MathUtils.add(operation.getFamount(), operation.getFfees());
        boolean resultStatus = super.updateUserCoinWallet(operation.getFuid(), operation.getFcoinid(), opAmount, MathUtils.positive2Negative(opAmount));
        if (!resultStatus) {
            throw new Exception();
        }
        return true;
    }


    @Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Result updateWithdrawOrderInfo(String orderId, Boolean status) throws Exception {
        //查询提现订单
        FWalletCapitalOperationDO walletCapitalOperation = walletCapitalOperationMapper.selectBySerialNumber(orderId);
        if(walletCapitalOperation == null){
            logger.error("无提现记录,orderId:{}",orderId);
            return Result.failure("无提现记录");
        }
        //状态判断
        if(!walletCapitalOperation.getFstatus().equals(CapitalOperationOutStatus.OnLineLock)){
            logger.error("状态错误，订单号为：{},状态为：{}",walletCapitalOperation.getFstatus(),orderId);
            return Result.failure("状态错误");
        }

        walletCapitalOperation.setFupdatetime(Utils.getTimestamp());
        if(status) {
            walletCapitalOperation.setFstatus(CapitalOperationOutStatus.OperationSuccess);
            walletCapitalOperation.setFremark("SUCCESS");
        } else {
            walletCapitalOperation.setFstatus(CapitalOperationOutStatus.OperationLock);
            walletCapitalOperation.setFremark("提现回调状态为失败，请联系技术确认");
        }
        //更新记录信息
        if(walletCapitalOperationMapper.updateStatusByPrimaryKey(walletCapitalOperation) <= 0){
            return Result.failure("更新记录失败");
        }
        if(status) {
            BigDecimal amount = walletCapitalOperation.getFamount();
            BigDecimal frees = walletCapitalOperation.getFfees();
            BigDecimal totalAmt = MathUtils.add(amount, frees);

            boolean resultStatus = super.updateUserCoinWalletFrozen(walletCapitalOperation.getFuid(),walletCapitalOperation.getFcoinid(), MathUtils.positive2Negative(totalAmt));
            if (!resultStatus) {
                throw new Exception("更新钱包失败");
            }
            mqSendUtils.SendUserAction(walletCapitalOperation.getFuid(), LogUserActionEnum.RMB_WITHDRAW_ONLINE, CapitalOperationTypeEnum.RMB_OUT ,amount, frees);
        }
        return Result.success("提现成功！");
    }

}
