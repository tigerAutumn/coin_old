package com.qkwl.service.admin.bc.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.Enum.LogAdminActionEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.c2c.C2CBusinessStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustTypeEnum;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.SystemC2CSetting;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.capital.FUserBankinfoDTO;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.rpc.admin.IAdminC2CService;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.service.admin.bc.dao.C2CBusinessMapper;
import com.qkwl.service.admin.bc.dao.FSystemBankinfoWithdrawMapper;
import com.qkwl.service.admin.bc.dao.FUserBankinfoMapper;
import com.qkwl.service.admin.bc.dao.SystemC2CSettingMapper;
import com.qkwl.service.admin.bc.dao.UserC2CEntrustMapper;
import com.qkwl.service.admin.bc.dao.UserCoinWalletMapper;
import com.qkwl.service.admin.bc.utils.MQSend;

@Service("adminC2CService")
public class AdminC2CServiceImpl implements IAdminC2CService {

	private static final Logger logger = LoggerFactory.getLogger(AdminC2CServiceImpl.class);
	
	@Autowired
	private C2CBusinessMapper c2CBusinessMapper;
	
	@Autowired
	private UserC2CEntrustMapper userC2CEntrustMapper;
	
	@Autowired
	private FUserBankinfoMapper fUserBankInfoMapper;
	
	@Autowired
	private UserCoinWalletMapper userCoinWalletMapper;
	
	@Autowired
	private SystemC2CSettingMapper systemC2CSettingMapper;
	
	@Autowired
	private FSystemBankinfoWithdrawMapper systemBankinfoWithdrawMapper;
	
	@Autowired
	private MQSend mqSend;

	@Override
	public List<C2CBusiness> selectC2CBusinessList() {
		try {
			return c2CBusinessMapper.selectAll();
		} catch (Exception e) {
			logger.error("selectC2CBusinessList 执行异常",e);
		}
		return null;
		
	}

	@Override
	public C2CBusiness selectC2CBusinessById(Integer id) {
		try {
			return c2CBusinessMapper.selectByPrimaryKey(id);
		} catch (Exception e) {
			logger.error("selectC2CBusinessList 执行异常",e);
		}
		return null;
	}

	@Override
	public int updateC2CBusinessById(C2CBusiness c2cBusiness,Integer adminId) {
		try {
			int updateByPrimaryKey = c2CBusinessMapper.updateByPrimaryKey(c2cBusiness);
			if(updateByPrimaryKey == 0 ) {
				return 0;
			}
			// MQ
			/*mqSend.SendAdminAction(record.getFagentid(), record.getFadminid(), record.getFuid(),
					LogAdminActionEnum.SYSTEM_COIN_WITHDRAW, record.getFcoinid(), record.getFamount());*/
			return updateByPrimaryKey;
		} catch (Exception e) {
			logger.error("selectC2CBusinessList 执行异常",e);
		}
		return 0;
		
	}

	@Override
	public int saveC2CBusiness(C2CBusiness c2cBusiness,Integer adminId) {
		try {
			c2cBusiness.setStatus(C2CBusinessStatusEnum.Normal.getCode());
			return c2CBusinessMapper.insert(c2cBusiness);
		} catch (Exception e) {
			logger.error("saveC2CBusiness 执行异常",e);
		}
		return 0;
	}

	@Override
	public PageInfo<UserC2CEntrust> selectUserC2CEntrustList(UserC2CEntrust u,Integer pageNum,Integer pageSize) {
		try {
			PageHelper.startPage(pageNum, pageSize);
			List<UserC2CEntrust> list = userC2CEntrustMapper.selectByParams(u);
			PageInfo<UserC2CEntrust> pageInfo = new PageInfo<UserC2CEntrust>(list);
			return pageInfo;
		} catch (Exception e) {
			logger.error("selectUserC2CEntrustList 异常",e);
			return null;
		}
		
	}

	@Override
	public UserC2CEntrust getEntrustDetailsById(Integer entrustId) {
		try {
			UserC2CEntrust selectByPrimaryKey = userC2CEntrustMapper.selectByPrimaryKey(entrustId);
			if(selectByPrimaryKey == null) {
				return null;
			}
			if(selectByPrimaryKey.getType() == UserC2CEntrustTypeEnum.withdraw.getCode()) {
				FUserBankinfoDTO selectByPrimaryKey2 = fUserBankInfoMapper.selectByPrimaryKey(selectByPrimaryKey.getBankId());
				if(selectByPrimaryKey2 == null) {
					return null;
				}
				selectByPrimaryKey.setBank(selectByPrimaryKey2.getFname());
				selectByPrimaryKey.setBankAccount(selectByPrimaryKey2.getFrealname());
				selectByPrimaryKey.setBankCode(selectByPrimaryKey2.getFbanknumber());
				selectByPrimaryKey.setBankAddress(selectByPrimaryKey2.getFprov()+" "+selectByPrimaryKey2.getFcity()+" "+selectByPrimaryKey2.getFdist()+" "+selectByPrimaryKey2.getFaddress());
			}else {
				C2CBusiness selectByPrimaryKey2 = c2CBusinessMapper.selectByPrimaryKey(selectByPrimaryKey.getBusinessId());
				if(selectByPrimaryKey2 == null) {
					return null;
				}
				selectByPrimaryKey.setBank(selectByPrimaryKey2.getBankName());
				selectByPrimaryKey.setBankAccount(selectByPrimaryKey2.getBankAccountName());
				selectByPrimaryKey.setBankCode(selectByPrimaryKey2.getBankNumber());
				selectByPrimaryKey.setBankAddress(selectByPrimaryKey2.getBankAddress());
			}
			return selectByPrimaryKey;
		} catch (Exception e) {
			logger.error("getEntrustDetailsById 异常 ，entrustId："+entrustId ,e);
			return null;
		}
	}
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public ReturnResult auditPassThrough(Integer id,Integer adminId,Integer type) {
		try {
			UserC2CEntrust entrust = userC2CEntrustMapper.selectByPrimaryKeyForLock(id);
			if(entrust == null) {
				return ReturnResult.FAILUER("审核失败，委单不存在");
			}
			//用于区分权限
			if(entrust.getType() != type ) {
				return ReturnResult.FAILUER("审核失败，类型错误");
			}

			UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(entrust.getUserId(), entrust.getCoinId());
			Date date = new Date();
			if(entrust.getType() != null && entrust.getType() == UserC2CEntrustTypeEnum.recharge.getCode()) {
				if(entrust.getStatus() != UserC2CEntrustStatusEnum.processing.getCode() && entrust.getStatus() != UserC2CEntrustStatusEnum.close.getCode() && entrust.getStatus() != UserC2CEntrustStatusEnum.wait.getCode()) {
					return ReturnResult.FAILUER("当前委单状态为"+ UserC2CEntrustStatusEnum.getValueByCode(entrust.getStatus()) + ",审核失败");
				}
				wallet.setTotal(MathUtils.add(wallet.getTotal(), entrust.getAmount()));
				wallet.setGmtModified(date);
			}else if(entrust.getType() != null && entrust.getType() == UserC2CEntrustTypeEnum.withdraw.getCode()) {
				if(entrust.getStatus() != UserC2CEntrustStatusEnum.processing.getCode()) {
					return ReturnResult.FAILUER("当前委单状态为"+ UserC2CEntrustStatusEnum.getValueByCode(entrust.getStatus()) + ",审核失败");
				}
				if(MathUtils.compareTo(wallet.getFrozen(), entrust.getAmount()) < 0) {
					return ReturnResult.FAILUER("用户钱包冻结金额不足");
				}
				wallet.setFrozen(MathUtils.sub(wallet.getFrozen(), entrust.getAmount()));
				wallet.setGmtModified(date);
			}else {
				logger.error("委单状态不存在，id："+id);
				return ReturnResult.FAILUER("委单类型不存在");
			}
			entrust.setAdminId(adminId);
			entrust.setStatus(UserC2CEntrustStatusEnum.success.getCode());
			entrust.setUpdateTime(date);

			if(userC2CEntrustMapper.updateByPrimaryKey(entrust) <= 0 ) {
				logger.error("c2c审核,更新委单失败，id：" + id );
				throw new BCException("审核失败，更新数据库失败");
			}
			if(userCoinWalletMapper.updateByPrimaryKey(wallet) <= 0 ) {
				logger.error("c2c审核,更新钱包失败，id：" + id );
				throw new BCException("审核失败，更新数据库失败");
			}
			c2CBusinessMapper.updateIncrByPrimaryKey(entrust.getAmount(), entrust.getBusinessId());
			// MQ
			if(entrust.getType() != null && entrust.getType() == UserC2CEntrustTypeEnum.recharge.getCode()) {
				mqSend.SendUserAction(0, entrust.getUserId(), LogUserActionEnum.C2C_RECHARGE_PASS,
						0, entrust.getAmount(), null , null);
				mqSend.SendAdminAction(0, adminId, entrust.getUserId(),
						LogAdminActionEnum.C2C_RECHARGE_PASS, 0, entrust.getAmount());
			}else {
				mqSend.SendUserAction(0, entrust.getUserId(), LogUserActionEnum.C2C_WITHDRAW_PASS,
						0, entrust.getAmount(), null , null);
				mqSend.SendAdminAction(0, adminId, entrust.getUserId(),
						LogAdminActionEnum.C2C_WITHDRAW_PASS, 0, entrust.getAmount());
			}
			
			mqSend.sendC2CEntrustStatus(entrust.getId());
			return ReturnResult.SUCCESS("审核成功");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			if(e instanceof BCException) {
				return ReturnResult.FAILUER(e.getMessage());
			}else {
				logger.error("c2c委单审核失败，id："+id,e);
				return ReturnResult.FAILUER("审核失败");
			}
		}
	}

	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public ReturnResult auditReject(Integer id,Integer adminId,Integer type) {
		try {
			UserC2CEntrust entrust = userC2CEntrustMapper.selectByPrimaryKeyForLock(id);
			if(entrust == null) {
				return ReturnResult.FAILUER("驳回失败，委单不存在");
			}
			//用于区分权限
			if(entrust.getType() != type ) {
				return ReturnResult.FAILUER("驳回失败，类型错误");
			}
			if(entrust.getStatus() != UserC2CEntrustStatusEnum.processing.getCode() && entrust.getStatus() != UserC2CEntrustStatusEnum.wait.getCode()) {
				return ReturnResult.FAILUER("当前委单状态为"+ UserC2CEntrustStatusEnum.getValueByCode(entrust.getStatus()) + ",驳回失败");
			}
			Date date = new Date();
			if(entrust.getType() != null && entrust.getType() == UserC2CEntrustTypeEnum.withdraw.getCode()) {
				UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(entrust.getUserId(), entrust.getCoinId());
				if(MathUtils.compareTo(wallet.getFrozen(), entrust.getAmount()) < 0) {
					return ReturnResult.FAILUER("用户钱包冻结金额不足");
				}
				wallet.setFrozen(MathUtils.sub(wallet.getFrozen(), entrust.getAmount()));
				wallet.setTotal(MathUtils.add(wallet.getTotal(), entrust.getAmount()));
				wallet.setGmtModified(date);;
				if(userCoinWalletMapper.updateByPrimaryKey(wallet) <= 0 ) {
					logger.error("c2c驳回,更新钱包失败，id：" + id );
					throw new BCException("驳回失败，更新钱包失败");
				}
			}else if(entrust.getType() != null && entrust.getType() != UserC2CEntrustTypeEnum.recharge.getCode()) {
				logger.error("委单状态不存在，id："+id);
				return ReturnResult.FAILUER("委单类型不存在");
			}
			entrust.setAdminId(adminId);
			entrust.setStatus(UserC2CEntrustStatusEnum.cancel.getCode());
			entrust.setUpdateTime(date);
			if(userC2CEntrustMapper.updateByPrimaryKey(entrust) <= 0 ) {
				logger.error("c2c驳回,更新委单失败，id：" + id );
				throw new BCException("审核失败");
			}
			if(entrust.getType() != null && entrust.getType() == UserC2CEntrustTypeEnum.recharge.getCode()) {
				mqSend.SendUserAction(0, entrust.getUserId(), LogUserActionEnum.C2C_RECHARGE_REFUSE,
						0, entrust.getAmount(), null , null);
				mqSend.SendAdminAction(0, adminId, entrust.getUserId(),
						LogAdminActionEnum.C2C_RECHARGE_REFUSE, 0, entrust.getAmount());
			}else {
				mqSend.SendUserAction(0, entrust.getUserId(), LogUserActionEnum.C2C_WITHDRAW_REFUSE,
						0, entrust.getAmount(), null , null);
				mqSend.SendAdminAction(0, adminId, entrust.getUserId(),
						LogAdminActionEnum.C2C_WITHDRAW_REFUSE, 0, entrust.getAmount());
			}
			mqSend.sendC2CEntrustStatus(entrust.getId());
			return ReturnResult.SUCCESS("驳回成功");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			if(e instanceof BCException) {
				return ReturnResult.FAILUER(e.getMessage());
			}else {
				logger.error("c2c委单驳回失败，id："+id,e);
				return ReturnResult.FAILUER("审核失败");
			}
		}
	}
	
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public ReturnResult lock(Integer id, Integer adminId) {
		try {
			UserC2CEntrust entrust = userC2CEntrustMapper.selectByPrimaryKeyForLock(id);
			if(entrust == null) {
				return ReturnResult.FAILUER("执行失败，委单不存在");
			}
			if(entrust.getType() != UserC2CEntrustTypeEnum.withdraw.getCode()) {
				return ReturnResult.FAILUER("充值单不存在锁定");
			}
			if( entrust.getStatus() != UserC2CEntrustStatusEnum.wait.getCode()) {
				return ReturnResult.FAILUER("当前委单状态为"+ UserC2CEntrustStatusEnum.getValueByCode(entrust.getStatus()) + ",锁定失败");
			}
			entrust.setAdminId(adminId);
			entrust.setStatus(UserC2CEntrustStatusEnum.processing.getCode());
			entrust.setUpdateTime(new Date());
			if(userC2CEntrustMapper.updateByPrimaryKey(entrust) <= 0 ) {
				logger.error("c2c提现锁定,更新委单失败，id：" + id );
				return ReturnResult.FAILUER("锁定失败");
			}
			return ReturnResult.SUCCESS("锁定成功");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			if(e instanceof BCException) {
				return ReturnResult.FAILUER(e.getMessage());
			}else {
				logger.error("c2c委单驳回失败，id："+id,e);
				return ReturnResult.FAILUER("审核失败");
			}
		}
	}
	
	@Override
	public List<SystemC2CSetting>  getC2CSetting() {
		try {
			return systemC2CSettingMapper.selectAll();
		} catch (Exception e) {
			logger.error("getC2CSetting 异常",e);
			return null;
		}
	}

	@Override
	public SystemC2CSetting getC2CSettingById(Integer id) {
		try {
			return systemC2CSettingMapper.selectByPrimaryKey(id);
		} catch (Exception e) {
			logger.error("getC2CSettingById 异常",e);
			return null;
		}
	}

	@Override
	public int updateC2CSetting(SystemC2CSetting systemC2CSetting, Integer adminId) {
		try {
			 return systemC2CSettingMapper.updateByPrimaryKeySelective(systemC2CSetting);
		} catch (Exception e) {
			logger.error("updateC2CSetting 异常",e);
			return 0;
		}
	}

	@Override
	public Map<Integer,UserC2CEntrust> statisticsRechargeWithdrawTotal(Integer userId,Date date) {
		try {
			List<UserC2CEntrust> statisticsRechargeWithdrawTotal = userC2CEntrustMapper.statisticsRechargeWithdrawTotal(userId,date);
			Map<Integer,UserC2CEntrust> map = new HashMap<>();
			for (UserC2CEntrust userC2CEntrust : statisticsRechargeWithdrawTotal) {
				//充值已完成
				if(userC2CEntrust.getType() == UserC2CEntrustTypeEnum.recharge.getCode() && userC2CEntrust.getStatus() == UserC2CEntrustStatusEnum.success.getCode()) {
					UserC2CEntrust u = map.get(userC2CEntrust.getCoinId());
					if(u == null) {
						u = new UserC2CEntrust();
					}
					u.setRecharge(userC2CEntrust.getAmount());
					map.put(userC2CEntrust.getCoinId(), u);
				}
				//提现已完成
				if(userC2CEntrust.getType() == UserC2CEntrustTypeEnum.withdraw.getCode() && userC2CEntrust.getStatus() == UserC2CEntrustStatusEnum.success.getCode()) {
					UserC2CEntrust u = map.get(userC2CEntrust.getCoinId());
					if(u == null) {
						u = new UserC2CEntrust();
					}
					u.setWithdraw(userC2CEntrust.getAmount());
					map.put(userC2CEntrust.getCoinId(), u);
				}
				//提现锁定
				if(userC2CEntrust.getType() == UserC2CEntrustTypeEnum.withdraw.getCode() && userC2CEntrust.getStatus() == UserC2CEntrustStatusEnum.processing.getCode()) {
					UserC2CEntrust u = map.get(userC2CEntrust.getCoinId());
					if(u == null) {
						u = new UserC2CEntrust();
					}
					if(u.getWithdrawFrozen() == null) {
						u.setWithdrawFrozen(userC2CEntrust.getAmount());
					}else {
						u.setWithdrawFrozen(u.getWithdrawFrozen().add(userC2CEntrust.getAmount()));
					}
					map.put(userC2CEntrust.getCoinId(), u);
				}
				//提现等待
				if(userC2CEntrust.getType() == UserC2CEntrustTypeEnum.withdraw.getCode() && userC2CEntrust.getStatus() == UserC2CEntrustStatusEnum.wait.getCode()) {
					UserC2CEntrust u = map.get(userC2CEntrust.getCoinId());
					if(u == null) {
						u = new UserC2CEntrust();
					}
					if(u.getWithdrawFrozen() == null) {
						u.setWithdrawFrozen(userC2CEntrust.getAmount());
					}else {
						u.setWithdrawFrozen(u.getWithdrawFrozen().add(userC2CEntrust.getAmount()));
					}
					map.put(userC2CEntrust.getCoinId(), u);
				}
			}
			return map;
		} catch (Exception e) {
			logger.error("statisticsRechargeWithdrawTotal 异常,userId:"+userId,e);
			return null;
		}
		
	}

	@Override
	public List<FSystemBankinfoWithdraw> getBankInfoList() {
		try {
			List<FSystemBankinfoWithdraw> selectAll = systemBankinfoWithdrawMapper.selectAll();
			return selectAll;
		} catch (Exception e) {
			logger.error("查询银行类型异常",e);
			return null;
		}
	}

	@Override
	public int saveBankInfo(FSystemBankinfoWithdraw fSystemBankinfoWithdraw ,Integer adminId) {
		try {
			int insert = systemBankinfoWithdrawMapper.insert(fSystemBankinfoWithdraw);
			return insert;
		} catch (Exception e) {
			logger.error("新增银行类型异常",e);
			return 0;
		}
	}

	@Override
	public int deleteBankInfoById(Integer id ,Integer adminId) {
		try {
			int delete = systemBankinfoWithdrawMapper.deleteById(id);
			return delete;
		} catch (Exception e) {
			logger.error("删除银行类型异常",e);
			return 0;
		}
	}






	

}
