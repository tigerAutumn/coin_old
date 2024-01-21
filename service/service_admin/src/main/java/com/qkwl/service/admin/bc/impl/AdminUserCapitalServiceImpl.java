package com.qkwl.service.admin.bc.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.dto.Enum.CapitalOperationInOutTypeEnum;
import com.qkwl.common.dto.Enum.CapitalOperationInStatus;
import com.qkwl.common.dto.Enum.CapitalOperationOutStatus;
import com.qkwl.common.dto.Enum.CapitalOperationTypeEnum;
import com.qkwl.common.dto.Enum.LogAdminActionEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.OperationlogEnum;
import com.qkwl.common.dto.Enum.ReferrerRecordStateEnum;
import com.qkwl.common.dto.Enum.SystemCoinSortEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationInStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationOutStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationTypeEnum;
import com.qkwl.common.dto.activity.FActivityRecord;
import com.qkwl.common.dto.admin.FAdmin;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.capital.AssetImbalance;
import com.qkwl.common.dto.capital.FUserPushDTO;
import com.qkwl.common.dto.capital.FUserVirtualAddressDTO;
import com.qkwl.common.dto.capital.FVirtualCapitalOperationDTO;
import com.qkwl.common.dto.capital.FWalletCapitalOperationDTO;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.entrust.FEntrust;
import com.qkwl.common.dto.log.FLogConsoleVirtualRecharge;
import com.qkwl.common.dto.log.FLogModifyCapitalOperation;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.admin.IAdminUserCapitalService;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.service.admin.bc.dao.AssetImbalanceMapper;
import com.qkwl.service.admin.bc.dao.FActivityRecordMapper;
import com.qkwl.service.admin.bc.dao.FLogConsoleVirtualRechargeMapper;
import com.qkwl.service.admin.bc.dao.FLogModifyCapitalOperationMapper;
import com.qkwl.service.admin.bc.dao.FUserMapper;
import com.qkwl.service.admin.bc.dao.FUserPushMapper;
import com.qkwl.service.admin.bc.dao.FUserScoreMapper;
import com.qkwl.service.admin.bc.dao.FUserVirtualAddressMapper;
import com.qkwl.service.admin.bc.dao.FVirtualCapitalOperationMapper;
import com.qkwl.service.admin.bc.dao.FWalletCapitalOperationMapper;
import com.qkwl.service.admin.bc.dao.UserCoinWalletMapper;
import com.qkwl.service.admin.bc.utils.MQSend;


/**
 * 用户资金操作
 * @author ZKF
 */
@Service("adminUserCapitalService")
public class AdminUserCapitalServiceImpl implements IAdminUserCapitalService {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserCapitalServiceImpl.class);
	
	@Autowired
	private FWalletCapitalOperationMapper walletCapitalOperationMapper;
	@Autowired
	private FVirtualCapitalOperationMapper virtualCapitalOperationMapper;
	@Autowired
	private FLogModifyCapitalOperationMapper logModifyCapitalOperationMapper;
	@Autowired
	private UserCoinWalletMapper userCoinWalletMapper;
	@Autowired
	private FUserVirtualAddressMapper userVirtualAddressMapper;
	@Autowired
	private FLogConsoleVirtualRechargeMapper logConsoleVirtualRechargeMapper;
	@Autowired
	private RedisHelper redisHelper;
	@Autowired
	private MQSend mqSend;
	@Autowired
	private FUserPushMapper userPushMapper;
	@Autowired
	private FActivityRecordMapper activityRecordMapper;
	@Autowired
	private FUserScoreMapper userScoreMapper;
	@Autowired
	private FUserMapper userMapper;
	@Autowired
	private AssetImbalanceMapper assetImbalanceMapper;

	
	
	/**
	 * 分页查询钱包操作记录
	 * @param pageParam 分页参数
	 * @param record 实体参数
	 * @param status 状态列表
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectWalletCapitalOperationList(com.qkwl.common.dto.common.Pagination,
	 * com.qkwl.common.dto.capital.FWalletCapitalOperationDTO, java.util.List,java.lang.Boolean,java.lang.Boolean)
	 */
	@Override
	public Pagination<FWalletCapitalOperationDTO> selectWalletCapitalOperationList(
			Pagination<FWalletCapitalOperationDTO> pageParam, FWalletCapitalOperationDTO record, List<Integer> status,
			Boolean limit, Boolean isvip6) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("finouttype", record.getFinouttype());
		map.put("fstatus", status.size() > 0 ? status : null);
		map.put("fid", record.getFid());
		map.put("finouttype", record.getFinouttype());
		map.put("isvip6", isvip6);
		map.put("start", pageParam.getBegindate());
		map.put("end", pageParam.getEnddate());
		map.put("amountLimit",limit);
		map.put("serialno",record.getFserialno());
		// 查询总数
		int count = walletCapitalOperationMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<FWalletCapitalOperationDTO> list = walletCapitalOperationMapper.getAdminPageList(map);
//			LinkedList<FWalletCapitalOperationDTO> datalist = new LinkedList<>();
//			for(FWalletCapitalOperationDTO dto : list){
//				FUserScore score = userScoreMapper.selectByUid(dto.getFuid());
//				if(score != null && score.getFlevel().equals(6)){
//					dto.setLevel(6);
//					datalist.addFirst(dto);
//				}else{
//					datalist.add(dto);
//				}
//			}
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}

	/**
	 * 根据id查询钱包操作记录
	 * @param fid 操作id
	 * @return 钱包操作实体
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectById(int)
	 */
	@Override
	public FWalletCapitalOperationDTO selectById(int fid) {
		return walletCapitalOperationMapper.selectByPrimaryKey(fid);
	}



	/**
	 * 获取用户钱包
	 * @param fuid 用户id
	 * @return 用户钱包实体
	 */
	@Override
	public UserCoinWallet selectUserWallet(int fuid, int fcoinid) {
		return userCoinWalletMapper.selectByUidAndCoin(fuid, fcoinid);
	}

	/**
	 * 更新钱包操作
	 * @param admin 管理员
 	 * @param capital 钱包操作记录
	 * @param amount 总量
	 * @param isRecharge 是否充值
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateWalletCapital(FAdmin admin, FWalletCapitalOperationDTO capital,
	    BigDecimal amount, boolean isRecharge)  throws Exception {
		UserCoinWallet userWallet = userCoinWalletMapper.selectByUidAndCoinLock(capital.getFuid(), capital.getFcoinid());
		if (isRecharge) {
			// 审核充值
			userWallet.setTotal(MathUtils.add(userWallet.getTotal(), amount));
		} else {
			if(MathUtils.sub(userWallet.getFrozen(),amount).compareTo(BigDecimal.ZERO)<0){
				throw new Exception("钱包冻结异常");
			}
			// 审核提现
			if (capital.getFstatus() == CapitalOperationOutStatus.OperationSuccess) {
				userWallet.setFrozen(MathUtils.sub(userWallet.getFrozen(), amount));
			}
			// 取消提现
			if (capital.getFstatus() == CapitalOperationOutStatus.Cancel) {
				userWallet.setFrozen(MathUtils.sub(userWallet.getFrozen(), amount));
				userWallet.setTotal(MathUtils.add(userWallet.getTotal(), amount));
			}
		}
		//充值提成推荐人提成
		if(isRecharge && capital.getFstatus().equals(CapitalOperationInStatus.Come)){
			try {
				BigDecimal percent = new BigDecimal(redisHelper.getSystemArgs(ArgsConstant.REFERRER_RECORD_PERCENT));
				if(percent.compareTo(BigDecimal.ZERO) > 0){
					FUser user = userMapper.selectByPrimaryKey(capital.getFuid());
					if (user.getFintrouid() != null && user.getFhasrealvalidate() && user.getFintrouid() != null && user.getFintrouid() > 0) {
						FUser introUser = userMapper.selectByPrimaryKey(user.getFintrouid());
						if (introUser != null && introUser.getFhasrealvalidate()) {
							FActivityRecord referrerRecord = new FActivityRecord();
							referrerRecord.setFstate(ReferrerRecordStateEnum.Non_Release.getCode());
							referrerRecord.setFtype(1);//充值赠送
							referrerRecord.setFamount(MathUtils.toScaleNum(MathUtils.mul(capital.getFamount(), percent),
									MathUtils.ENTER_COIN_SCALE));
							referrerRecord.setFrecharge(capital.getFamount());
							referrerRecord.setFuid(introUser.getFid());
							referrerRecord.setFintrouid(capital.getFuid());
							referrerRecord.setFremark("推荐人" + user.getFid() + capital.getFbank() +"，充值金额:"
									+ MathUtils.toScaleNum(capital.getFamount(), MathUtils.ENTER_CNY_SCALE));
							referrerRecord.setVersion(0);
							referrerRecord.setFcoinid(capital.getFcoinid());
							referrerRecord.setFcreatetime(new Date());
							if (activityRecordMapper.insert(referrerRecord) <= 0) {
								throw new Exception("update referrerRecord err");
							}
						}
					}
				}
			} catch (Exception e) {
				throw new Exception("update activity err");
			}
		}

		userWallet.setGmtModified(Utils.getTimestamp());
		if (walletCapitalOperationMapper.updateByPrimaryKey(capital) <= 0) {
			throw new Exception("update Capital err");
		}
		if(userCoinWalletMapper.updateByPrimaryKey(userWallet) <= 0){
			throw new Exception("update userWallet err");
		}
		// MQ
		if (isRecharge) {
			// RMB充值
			mqSend.SendUserAction(capital.getFagentid(), capital.getFuid(), LogUserActionEnum.RMB_RECHARGE, capital.getFtype(), amount);
			mqSend.SendAdminAction(capital.getFagentid(), admin.getFid(), capital.getFuid(), LogAdminActionEnum.SYSTEM_RMB_RECHARGE, capital.getFtype(), amount);
		} else {
			// RMB提现
			if (capital.getFstatus() == CapitalOperationOutStatus.OperationSuccess) {
				mqSend.SendUserAction(capital.getFagentid(), capital.getFuid(), LogUserActionEnum.RMB_WITHDRAW, CapitalOperationTypeEnum.RMB_OUT ,amount, capital.getFfees());
				mqSend.SendAdminAction(capital.getFagentid(), admin.getFid(), capital.getFuid(), LogAdminActionEnum.SYSTEM_COIN_WITHDRAW, capital.getFtype(), amount);
			}
			// RMB取消提现
			if (capital.getFstatus() == CapitalOperationOutStatus.Cancel) {
				mqSend.SendUserAction(capital.getFagentid(), capital.getFuid(), LogUserActionEnum.RMB_WITHDRAW_CANCEL, CapitalOperationTypeEnum.RMB_OUT, amount, "admin_" + admin.getFid());
				mqSend.SendAdminAction(capital.getFagentid(), admin.getFid(), capital.getFuid(), LogAdminActionEnum.SYSTEM_RMB_WITHDRAW, capital.getFtype(), amount);
			}
		}
		return true;
	}


	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result updateOnlineWithdrawStatus(Integer adminId,String orderId, Boolean status) throws Exception {
		FWalletCapitalOperationDTO capital = walletCapitalOperationMapper.selectBySerialNumber(orderId);
		if(capital == null){
			logger.error("无提现记录,orderId:{}",orderId);
			return Result.failure("无提现记录");
		}
		//状态判断
		if(!capital.getFstatus().equals(CapitalOperationOutStatus.OnLineLock)){
			logger.error("状态错误，订单号为：{},状态为：{}",capital.getFstatus(),orderId);
			return Result.failure("状态错误");
		}

		capital.setFupdatetime(Utils.getTimestamp());
		if(status) {
			capital.setFstatus(CapitalOperationOutStatus.OperationSuccess);
		} else {
			capital.setFstatus(CapitalOperationOutStatus.OperationLock);
			capital.setFremark("提现回调状态为失败，请联系技术确认");
		}
		//更新记录信息
		if(walletCapitalOperationMapper.updateByPrimaryKey(capital) <= 0){
			return Result.failure("更新记录失败");
		}
		if(status) {
			BigDecimal amount = capital.getFamount();
			BigDecimal frees = capital.getFfees();
			BigDecimal totalAmt = MathUtils.add(amount, frees);

			UserCoinWallet userWallet = userCoinWalletMapper.selectByUidAndCoinLock(capital.getFuid(), capital.getFcoinid());
			// 审核提现
			userWallet.setFrozen(MathUtils.sub(userWallet.getFrozen(), totalAmt));
			userWallet.setGmtModified(Utils.getTimestamp());
			if(userCoinWalletMapper.updateByPrimaryKey(userWallet) <= 0){
				throw new Exception("update userWallet err");
			}

			mqSend.SendUserAction(capital.getFagentid(), capital.getFuid(), LogUserActionEnum.RMB_WITHDRAW_ONLINE, CapitalOperationTypeEnum.RMB_OUT ,amount, capital.getFfees());

		}
		return Result.success("提现成功！");
	}


	/**
	 * 更新钱包操作记录
	 * @param capital 操作记录
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#updateWalletCapital(FWalletCapitalOperationDTO)
	 */
	@Override
	public boolean updateWalletCapital(FWalletCapitalOperationDTO capital) throws BCException {
		int result = walletCapitalOperationMapper.updateByPrimaryKey(capital);
		if (result <= 0) {
			return false;
		}
		mqSend.SendAdminAction(capital.getFagentid(), capital.getFadminid(), capital.getFuid(), LogAdminActionEnum.CANCEL_RMB_RECHARGE, capital.getFtype(), capital.getFamount());
		return true;
	}

	/**
	 * 是否第一次充值
	 * @param fuid 用户id
	 * @return 是否第一次充值
	 */
	@Override
	public boolean selectIsFirstCharge(int fuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("finouttype", CapitalOperationInOutTypeEnum.IN.getCode());
		map.put("fstatus", CapitalOperationInStatus.Come);
		int countCny = walletCapitalOperationMapper.countWalletCapitalOperation(map);
		if(countCny > 0){
			return false;
		}

		map.clear();
		map.put("fuid", fuid);
		map.put("ftype", VirtualCapitalOperationTypeEnum.COIN_IN.getCode());
		map.put("fstatus", VirtualCapitalOperationInStatusEnum.SUCCESS);
		int countCoin = virtualCapitalOperationMapper.countVirtualCapitalOperation(map);

		return (countCny + countCoin) <= 0;
	}

	/**
	 * 更新充值记录
	 * @param capital 钱包操作记录
	 * @param capitalLog 修改日志
	 * @return 是否修改成功
	 * @throws BCException 更新失败
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#updateModifyCapital(FWalletCapitalOperationDTO, com.qkwl.common.dto.log.FLogModifyCapitalOperation)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateModifyCapital(FWalletCapitalOperationDTO capital, FLogModifyCapitalOperation capitalLog) throws BCException {
		int result = walletCapitalOperationMapper.updateByPrimaryKey(capital);
		if (result <= 0) {
			return false;
		}
		result = logModifyCapitalOperationMapper.insert(capitalLog);
		if (result <= 0) {
			throw new BCException();
		}
		
		mqSend.SendAdminAction(0, capitalLog.getFadminid(), capital.getFuid(), LogAdminActionEnum.SYSTEM_MODIFY_RMB_RECHARGE, capital.getFtype(), capitalLog.getFamount());
		return true;
	}

	/**
	 * 分页查询虚拟币操作记录 
	 * @param pageParam 分页参数
	 * @param record 实体参数
	 * @param status 状态列表
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectVirtualCapitalOperationList(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.capital.FVirtualCapitalOperationDTO, java.util.List,
	 * java.lang.Boolean)
	 */
	@Override
	public Pagination<FVirtualCapitalOperationDTO> selectVirtualCapitalOperationList(
			Pagination<FVirtualCapitalOperationDTO> pageParam, FVirtualCapitalOperationDTO record,
			List<Integer> status, Boolean isvip6) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("ftype", record.getFtype());
		map.put("fcoinid", record.getFcoinid());
		map.put("fstatus", status.size() > 0 ? status : null);
		map.put("isvip6", isvip6);
		map.put("start", pageParam.getBegindate());
		map.put("end", pageParam.getEnddate());
		// 查询总数
		int count = virtualCapitalOperationMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<FVirtualCapitalOperationDTO> list = virtualCapitalOperationMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}

	/**
	 * 根据id查询虚拟币操作记录
	 * @param fid 操作id
	 * @return 操作记录实体
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectVirtualById(int)
	 */
	@Override
	public FVirtualCapitalOperationDTO selectVirtualById(int fid) {
		return virtualCapitalOperationMapper.selectAllById(fid);
	}

	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result resetAuditVirtualCapitalOperation(Integer id, Integer adminId) throws Exception {
		FVirtualCapitalOperationDTO operation = virtualCapitalOperationMapper.selectAllById(id);
		if(operation == null){
			return Result.failure("记录未找到");
		}
		if(!operation.getFstatus().equals(VirtualCapitalOperationOutStatusEnum.OperationSuccess)
				|| !operation.getFtype().equals(VirtualCapitalOperationTypeEnum.COIN_OUT.getCode())){
			return Result.failure("只能重置提现成功数据");
		}
		operation.setFstatus(VirtualCapitalOperationOutStatusEnum.OperationLock);
		operation.setFupdatetime(new Date());
		operation.setFadminid(adminId);
		if(virtualCapitalOperationMapper.updateByPrimaryKey(operation)<=0){
			return Result.failure("重置失败");
		}
		UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(operation.getFuid(),operation.getFcoinid());
		if(wallet == null){
			throw new Exception("wallet is null,uid:"+operation.getFuid()+",coinId： "+operation.getFcoinid());
		}
		wallet.setFrozen(MathUtils.add(wallet.getFrozen(),operation.getFamount()));
		wallet.setFrozen(MathUtils.add(wallet.getFrozen(),operation.getFfees()));
		wallet.setFrozen(MathUtils.add(wallet.getFrozen(),operation.getFbtcfees()));
		wallet.setGmtModified(new Date());
		if(userCoinWalletMapper.updateByPrimaryKey(wallet)<=0){
			throw new Exception("wallet is err,uid:"+operation.getFuid()+",coinId： "+operation.getFcoinid());
		}
		mqSend.SendAdminAction(operation.getFagentid(), operation.getFadminid(), operation.getFuid(), LogAdminActionEnum.RESET_COIN_WITHDRAW, operation.getFcoinid(), operation.getFamount(),operation.getFuniquenumber());
		return Result.success("重置成功");
	}

	/**
	 * 获取用户虚拟币钱包
	 * @param fuid 用户id
	 * @param fcoinid 币种id
	 * @return 虚拟币钱包实体
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectUserVirtualWallet(int, int)
	 */
	@Override
	public UserCoinWallet selectUserVirtualWallet(int fuid, int fcoinid) {
		return userCoinWalletMapper.selectByUidAndCoin(fuid, fcoinid);
	}
	
	/**
	 * 获取用户虚拟币钱包
	 * @param fuid 用户id
	 * @return map<币种id，钱包>
	 */
	@Override
	public Map<Integer,UserCoinWallet> selectUserVirtualWallet(int fuid) {
		try {
			List<UserCoinWallet> selectByUid = userCoinWalletMapper.selectByUid(fuid);
			return selectByUid.stream().collect(Collectors.toMap(UserCoinWallet::getCoinId, f ->f));
		} catch (Exception e) {
			logger.error("查询用户钱包异常uid："+fuid ,e);
			return null;
		}
	}

	/**
	 * 获取地址数量
	 * @param address 地址
	 * @return 数量
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectAddressNum(java.lang.String)
	 */
	@Override
	public int selectAddressNum(String address) {
		return userVirtualAddressMapper.getAddressNum(address);
	}

	/**
	 * 更新用户虚拟币操作
	 * @param recordId 虚拟币操作
	 * @param amount 总量
	 * @param addressNum 地址数量
 	 * @param coinDriver 钱包工具
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateVirtualCapital(Integer recordId, Integer adminId, BigDecimal amount, int addressNum,
										CoinDriver coinDriver) throws BCException {
		FVirtualCapitalOperationDTO record =null ;
		try {
			record = virtualCapitalOperationMapper.selectAllById(recordId);
		} catch (Exception e) {
			logger.error("查提现申请异常",e);
			return false;
		}
		if(!record.getFstatus().equals(VirtualCapitalOperationOutStatusEnum.LockOrder)){
			throw new BCException("订单锁定中，不允许重复操作！");
		}
		int fuid = record.getFuid();
		int fcoinid = record.getFcoinid();
		UserCoinWallet userVirtualWallet = userCoinWalletMapper.selectByUidAndCoinLock(fuid, fcoinid);
		if (userVirtualWallet == null) {
			throw new BCException("虚拟币钱包为空");
		}
		if (MathUtils.sub(userVirtualWallet.getFrozen(), amount).compareTo(BigDecimal.ZERO) == -1) {
			throw new BCException("虚拟币钱包冻结余额不足");
		}
		// 提现人钱包
		userVirtualWallet.setFrozen(MathUtils.sub(userVirtualWallet.getFrozen(), amount));
		userVirtualWallet.setGmtModified(Utils.getTimestamp());

		record.setFadminid(adminId);
		record.setFstatus(VirtualCapitalOperationOutStatusEnum.OperationSuccess);
		// 提现地址
		String address = record.getFwithdrawaddress();
		// 平台互转
		if (addressNum > 0) {
			UserCoinWallet virtualwalletTo = null ;
			FVirtualCapitalOperationDTO tovirtualcaptualoperation = new FVirtualCapitalOperationDTO();
			if(!coinDriver.getCoinSort().equals(SystemCoinSortEnum.EOS.getCode())){
				List<FUserVirtualAddressDTO> userVirtualAddresses = userVirtualAddressMapper.getUserByAddress(address);
				if (userVirtualAddresses == null || userVirtualAddresses.size() != 1) {
					throw new BCException("平台互转转入地址为空");
				}
				// 转入地址信息
				FUserVirtualAddressDTO userVirtualAddress = userVirtualAddresses.get(0);
				// 转入钱包信息
				virtualwalletTo = userCoinWalletMapper.selectByUidAndCoinLock(userVirtualAddress.getFuid(), fcoinid);
				if (virtualwalletTo == null) {
					throw new BCException("平台互转转入虚拟币钱包为空");
				}
				tovirtualcaptualoperation.setFuid(userVirtualAddress.getFuid());
			}else {
				// 转入钱包信息
				virtualwalletTo = userCoinWalletMapper.selectByUidAndCoinLock(Integer.valueOf(record.getMemo()), fcoinid);
				if (virtualwalletTo == null) {
					throw new BCException("平台互转转入虚拟币钱包为空");
				}
				tovirtualcaptualoperation.setFuid(Integer.valueOf(record.getMemo()));
			}
			// 写记录
			tovirtualcaptualoperation.setFcoinid(fcoinid);
			tovirtualcaptualoperation.setFamount(record.getFamount());
			tovirtualcaptualoperation.setFfees(BigDecimal.ZERO);
			tovirtualcaptualoperation.setFbtcfees(BigDecimal.ZERO);
			tovirtualcaptualoperation.setFhasowner(true);
			tovirtualcaptualoperation.setFstatus(VirtualCapitalOperationInStatusEnum.SUCCESS);// 收款成功
			tovirtualcaptualoperation.setFtype(VirtualCapitalOperationTypeEnum.COIN_IN.getCode());// 收款
			tovirtualcaptualoperation.setFuniquenumber("[平台互转]" + Utils.UUID());
			tovirtualcaptualoperation.setFrechargeaddress(record.getFwithdrawaddress());
			tovirtualcaptualoperation.setFupdatetime(Utils.getTimestamp());
			tovirtualcaptualoperation.setFcreatetime(Utils.getTimestamp());
			tovirtualcaptualoperation.setVersion(0);
			tovirtualcaptualoperation.setFplatform(record.getFplatform());
			if (virtualCapitalOperationMapper.insert(tovirtualcaptualoperation) <= 0) {
				throw new BCException("平台互转记录写入出错");
			}
			// 更新钱包信息
			virtualwalletTo.setTotal(MathUtils.add(virtualwalletTo.getTotal(), record.getFamount()));
			virtualwalletTo.setGmtModified(Utils.getTimestamp());
			if (userCoinWalletMapper.updateByPrimaryKey(virtualwalletTo) <= 0) {
				throw new BCException("平台互转对方钱包被锁定");
			}
			// 更新提现信息
			record.setFuniquenumber("[平台互转]" + Utils.UUID());
		} else {
			// 返回TXID
			String resultTX = null;
			Integer nonceTmp = null;
			try {
				if (coinDriver.getCoinSort().equals(SystemCoinSortEnum.ETH.getCode()) ) {
					logger.info("开始转账ETH,recordId:"+recordId);
					resultTX = sendETH(coinDriver, record);
				}else if(coinDriver.getCoinSort().equals(SystemCoinSortEnum.MOAC.getCode()) ) {
					logger.info("开始转账MOAC,recordId:"+recordId);
					resultTX = sendMOAC(coinDriver, record);
				}else if(coinDriver.getCoinSort().equals(SystemCoinSortEnum.FOD.getCode()) ) {
					logger.info("开始转账FOD,recordId:"+recordId);
					resultTX = sendFOD(coinDriver, record);
				}else if(coinDriver.getCoinSort().equals(SystemCoinSortEnum.ETC.getCode()) ) {
					logger.info("开始转账ETC,recordId:"+recordId);
					nonceTmp = coinDriver.getTransactionCount();
					String nonce = "0x" + Integer.toHexString(nonceTmp);
					resultTX = coinDriver.sendToAddress(address, record.getFamount().toString(), nonce);
				}else if(coinDriver.getCoinSort().equals(SystemCoinSortEnum.EOS.getCode()) ) {
					logger.info("开始转账EOS,recordId:"+recordId);
					resultTX = coinDriver.sendToAddress(address, record.getFamount(), null,null,record.getMemo());
				} else if (coinDriver.getCoinSort().equals(SystemCoinSortEnum.ETP.getCode())) {
					logger.info("开始转账ETP,recordId:"+recordId);
					resultTX = coinDriver.sendToAddress(address, record.getFamount().toString(), null);
				} else if (coinDriver.getCoinSort().equals(SystemCoinSortEnum.GXS.getCode())) {
					logger.info("开始转账GXS,recordId:"+recordId);
					resultTX = coinDriver.sendToAddress(address, record.getFamount(), record.getFid().toString(),
							record.getFbtcfees(), record.getMemo());
				} else {
					logger.info("开始转账other,recordId:"+recordId);
					resultTX = coinDriver.sendToAddress(address, record.getFamount(), record.getFid().toString(),
							record.getFbtcfees());
				}
			} catch (Exception e) {
				if(e instanceof BCException) {
					throw e;
				}
				logger.info("转账异常 ,recordId:"+recordId,e);
				throw new BCException("打币链接钱包出现错误");
			}
			if (StringUtils.isEmpty(resultTX)) {
				logger.error("钱包连接错误  : " + resultTX + "_" + nonceTmp);
				throw new BCException("钱包连接错误  : " + resultTX + "_" + nonceTmp);
			}
			// 打币记录返回TXID
			record.setFuniquenumber(resultTX);
			record.setFnonce(nonceTmp);
		}
		// 更新数据
		if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
			logger.error("更新订单失败,recordId:"+recordId);
			throw new BCException("更新订单失败");
		}
		if (userCoinWalletMapper.updateByPrimaryKey(userVirtualWallet) <= 0) {
			logger.error("更新用户钱包失败,recordId:"+recordId);
			throw new BCException("更新用户钱包失败");
		}
		logger.info("转账结束");
		// MQ
		mqSend.SendUserAction(record.getFagentid(), record.getFuid(), LogUserActionEnum.COIN_WITHDRAW,
				record.getFcoinid(), amount, record.getFfees(), record.getFbtcfees());

		// MQ
		mqSend.SendAdminAction(record.getFagentid(), record.getFadminid(), record.getFuid(),
				LogAdminActionEnum.SYSTEM_COIN_WITHDRAW, record.getFcoinid(), record.getFamount());
		return true;
	}
	
	private String sendETH(CoinDriver coinDriver,FVirtualCapitalOperationDTO record) throws BCException {
		//获取合适gwei
		BigDecimal estimatesmartfee = coinDriver.estimatesmartfee(0);
		String ratito = redisHelper.getSystemArgs(ArgsConstant.ETH_GASPRICE_RAITO);
		String gas = redisHelper.getSystemArgs(ArgsConstant.ETH_GAS);
		String maxGasPrice = redisHelper.getSystemArgs(ArgsConstant.ETH_MAX_GASPRICE);
		String gasPrice = null;
		
		//gasprice构造
		if(estimatesmartfee != null) {
			if(!StringUtils.isEmpty(ratito)) {
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal(ratito));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}else {
				//默认1.5倍
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal("1.5"));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}
		}else {
			//默认值：20gwei
			estimatesmartfee = new BigDecimal("20000000000");
			gasPrice = "0x4A817C800";
		}
		//gasprice风控
		if(!StringUtils.isEmpty(maxGasPrice)) {
			BigDecimal maxGasPriceLimit = new BigDecimal(maxGasPrice);
			if(maxGasPriceLimit.compareTo(BigDecimal.ZERO) > 0 && maxGasPriceLimit.compareTo(estimatesmartfee) < 0) {
				logger.error("转账gasprice超过风控阈值,gasprice:" + estimatesmartfee + ",maxGasPriceLimit:" + maxGasPriceLimit);
				throw new BCException("转账gasprice超过风控阈值，转账失败");
			}
		}
		
		//gas 
		if(!StringUtils.isEmpty(gas)) {
			BigDecimal gasBigDecimal = new BigDecimal(gas);
			gas = "0x" + Long.toHexString(gasBigDecimal.longValue());
		}else {
			//默认值： 40000
			gas = "0x9C40" ;
		}
		
		String nonce = null;
		Integer nonceTmp;
		Long incrByKey = redisHelper.getIncrByKey(RedisConstant.ETH_CURRENT_NONCE);
		if(incrByKey != null && incrByKey != 0 && incrByKey != 1) {
			nonceTmp = incrByKey.intValue();
		}else{
			nonceTmp = coinDriver.getTransactionCount();
			redisHelper.setNoExpire(RedisConstant.ETH_CURRENT_NONCE, nonceTmp.toString());
		}
		nonce = "0x" + Integer.toHexString(nonceTmp);
		ReturnResult sendToAddress = coinDriver.sendToAddress(record.getFwithdrawaddress(), record.getFamount().toString(), nonce,gasPrice,gas);
		if(sendToAddress.getCode() == 403) {
			//如果密码错误，回滚一下nonce,再将异常抛出，回滚事务
			redisHelper.getDecrByKey(RedisConstant.ETH_CURRENT_NONCE);
			throw new BCException(sendToAddress.getMsg());
		}else {
			return (String) sendToAddress.getData();
		}
	}
	
	private String sendMOAC(CoinDriver coinDriver,FVirtualCapitalOperationDTO record) throws BCException {
		//获取合适gwei
		BigDecimal estimatesmartfee = coinDriver.estimatesmartfee(0);
		String ratito = redisHelper.getSystemArgs(ArgsConstant.MOAC_GASPRICE_RAITO);
		String gasPrice = null;
		
		//gasprice构造
		if(estimatesmartfee != null) {
			if(!StringUtils.isEmpty(ratito)) {
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal(ratito));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}else {
				//默认1.5倍
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal("1"));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}
		}else {
			//默认值：20gwei
			estimatesmartfee = new BigDecimal("20000000000");
			gasPrice = "0x4A817C800";
		}
		String nonce = null;
		Integer nonceTmp;
		Long incrByKey = redisHelper.getIncrByKey(RedisConstant.MOAC_CURRENT_NONCE);
		if(incrByKey != null && incrByKey != 0 && incrByKey != 1) {
			nonceTmp = incrByKey.intValue();
		}else{
			nonceTmp = coinDriver.getTransactionCount();
			redisHelper.setNoExpire(RedisConstant.MOAC_CURRENT_NONCE, nonceTmp.toString());
		}
		nonce = "0x" + Integer.toHexString(nonceTmp);
		ReturnResult sendToAddress = coinDriver.sendToAddress(record.getFwithdrawaddress(), record.getFamount().toString(), nonce,gasPrice,null);
		if(sendToAddress.getCode() == 403) {
			//如果密码错误，回滚一下nonce,再将异常抛出，回滚事务
			redisHelper.getDecrByKey(RedisConstant.MOAC_CURRENT_NONCE);
			throw new BCException(sendToAddress.getMsg());
		}else {
			return (String) sendToAddress.getData();
		}
	}
	
	private String sendFOD(CoinDriver coinDriver,FVirtualCapitalOperationDTO record) throws BCException {
		//获取合适gwei
		BigDecimal estimatesmartfee = coinDriver.estimatesmartfee(0);
		String ratito = redisHelper.getSystemArgs(ArgsConstant.FOD_GASPRICE_RAITO);
		String gasPrice = null;
		
		//gasprice构造
		if(estimatesmartfee != null) {
			if(!StringUtils.isEmpty(ratito)) {
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal(ratito));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}else {
				//默认1.5倍
				estimatesmartfee = estimatesmartfee.multiply(new BigDecimal("1"));
				gasPrice = "0x" + Long.toHexString(estimatesmartfee.longValue());
			}
		}else {
			//默认值：20gwei
			estimatesmartfee = new BigDecimal("20000000000");
			gasPrice = "0x4A817C800";
		}
		String nonce = null;
		Integer nonceTmp;
		Long incrByKey = redisHelper.getIncrByKey(RedisConstant.FOD_CURRENT_NONCE);
		if(incrByKey != null && incrByKey != 0 && incrByKey != 1) {
			nonceTmp = incrByKey.intValue();
		}else{
			nonceTmp = coinDriver.getTransactionCount();
			redisHelper.setNoExpire(RedisConstant.FOD_CURRENT_NONCE, nonceTmp.toString());
		}
		nonce = "0x" + Integer.toHexString(nonceTmp);
		ReturnResult sendToAddress = coinDriver.sendToAddress(record.getFwithdrawaddress(), record.getFamount().toString(), nonce,gasPrice,null);
		if(sendToAddress.getCode() == 403) {
			//如果密码错误，回滚一下nonce,再将异常抛出，回滚事务
			redisHelper.getDecrByKey(RedisConstant.FOD_CURRENT_NONCE);
			throw new BCException(sendToAddress.getMsg());
		}else {
			return (String) sendToAddress.getData();
		}
	}
	
	
	/**
	 * 更新用户虚拟币操作_加速
	 * @param recordId 虚拟币操作
	 * @param amount 总量
	 * @param addressNum 地址数量
 	 * @param coinDriver 钱包工具
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
	public boolean updateVirtualCapitalAccelerate(Integer recordId, Integer adminId, BigDecimal amount, String gasPrice,
			boolean isUseNewNonce, CoinDriver coinDriver) throws BCException {
		FVirtualCapitalOperationDTO record = virtualCapitalOperationMapper.selectAllById(recordId);
		if(!record.getFstatus().equals(VirtualCapitalOperationOutStatusEnum.LockOrder)){
			throw new BCException("订单锁定中，不允许重复操作！");
		}
		record.setFadminid(adminId);
		record.setFstatus(VirtualCapitalOperationOutStatusEnum.OperationSuccess);
		// 提现地址
		String address = record.getFwithdrawaddress();
	
		// 返回TXID
		String resultTX = null;
		Integer nonceTmp = null;
		try {
			if (coinDriver.getCoinSort().equals(SystemCoinSortEnum.ETH.getCode()) ) {
				logger.info("开始加速ETH,recordId:"+recordId);
				
				//gas 
				String gas = redisHelper.getSystemArgs(ArgsConstant.ETH_GAS);
				if(!StringUtils.isEmpty(gas)) {
					BigDecimal gasBigDecimal = new BigDecimal(gas);
					gas = "0x" + Long.toHexString(gasBigDecimal.longValue());
				}else {
					//默认值： 40000
					gas = "0x9C40" ;
				}
				
				if(isUseNewNonce) {
					Long incrByKey = redisHelper.getIncrByKey(RedisConstant.ETH_CURRENT_NONCE, 5 * 60 );
					if(incrByKey != null && incrByKey != 0 && incrByKey != 1) {
						nonceTmp = incrByKey.intValue();
					}else{
						nonceTmp = coinDriver.getTransactionCount();
						redisHelper.set(RedisConstant.ETH_CURRENT_NONCE, nonceTmp.toString(), 5 * 60);
					}
				}else {
					nonceTmp = record.getFnonce();
				}
				String nonce = "0x" + Integer.toHexString(nonceTmp);
				
				ReturnResult sendToAddress = coinDriver.sendToAddress(address, record.getFamount().toString(), nonce,gasPrice,gas);
				if(sendToAddress.getCode() == 403) {
					//如果密码错误而且是新交易，回滚一下nonce,再将异常抛出
					if(isUseNewNonce){
						redisHelper.getDecrByKey(RedisConstant.ETH_CURRENT_NONCE);
					}
					throw new BCException(sendToAddress.getMsg());
				}else {
					resultTX = (String) sendToAddress.getData();
				}
			} else {
				throw new BCException("目前暂不支持eth以外的加速");
			}
		} catch (Exception e) {
			//e.printStackTrace();
			if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
				throw new BCException("钱包连接错误，更新订单失败");
			}
			throw new BCException("打币链接钱包出现错误");
		}
		if (resultTX == null || "".equals(resultTX) ||  "null".equals(resultTX)) {
			if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
				throw new BCException("钱包连接错误，更新订单失败");
			}
			throw new BCException("钱包连接错误  : " + resultTX + "_" + nonceTmp);
		}
		// 打币记录返回TXID
		record.setFuniquenumber(resultTX);
		record.setFnonce(nonceTmp);
		
		// 更新数据
		if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
			throw new BCException("更新订单失败");
		}
		logger.info("加速结束");
		// MQ
		mqSend.SendAdminAction(record.getFagentid(), record.getFadminid(), record.getFuid(),
				LogAdminActionEnum.SYSTEM_COIN_WITHDRAW_ACCELERATE, record.getFcoinid(), record.getFamount());
		return true;
	}

	/**
	 * 更新用户虚拟币操作——手动完成
	 * @param recordId 虚拟币操作
	 * @param amount 总量
	 * @param addressNum 地址数量
 	 * @param coinDriver 钱包工具
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateVirtualCapitalSuccessful(Integer recordId, Integer adminId, BigDecimal amount, String txhash) throws BCException {
		FVirtualCapitalOperationDTO record = virtualCapitalOperationMapper.selectAllById(recordId);
		if(!record.getFstatus().equals(VirtualCapitalOperationOutStatusEnum.LockOrder)){
			throw new BCException("订单锁定中，不允许重复操作！");
		}
		int fuid = record.getFuid();
		int fcoinid = record.getFcoinid();
		UserCoinWallet userVirtualWallet = userCoinWalletMapper.selectByUidAndCoinLock(fuid, fcoinid);
		if (userVirtualWallet == null) {
			throw new BCException("虚拟币钱包为空");
		}
		if (MathUtils.sub(userVirtualWallet.getFrozen(), amount).compareTo(BigDecimal.ZERO) == -1) {
			throw new BCException("虚拟币钱包冻结余额不足");
		}
		// 提现人钱包
		userVirtualWallet.setFrozen(MathUtils.sub(userVirtualWallet.getFrozen(), amount));
		userVirtualWallet.setGmtModified(Utils.getTimestamp());

		record.setFadminid(adminId);
		record.setFstatus(VirtualCapitalOperationOutStatusEnum.OperationSuccess);
		record.setFuniquenumber(txhash);
			
		// 更新数据
		if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
			logger.error("更新订单失败,recordId:"+recordId);
			throw new BCException("更新订单失败");
		}
		if (userCoinWalletMapper.updateByPrimaryKey(userVirtualWallet) <= 0) {
			logger.error("更新用户钱包失败,recordId:"+recordId);
			throw new BCException("更新用户钱包失败");
		}
		logger.info("转账结束");
		// MQ
		mqSend.SendUserAction(record.getFagentid(), record.getFuid(), LogUserActionEnum.COIN_WITHDRAW,
				record.getFcoinid(), amount, record.getFfees(), record.getFbtcfees());

		// MQ
		mqSend.SendAdminAction(record.getFagentid(), record.getFadminid(), record.getFuid(),
				LogAdminActionEnum.SYSTEM_COIN_WITHDRAW_MANUAL_COMPLETION, record.getFcoinid(), record.getFamount());
		return true;
	}
	
	
	
	
	
	/**
	 * 更新虚拟币操作记录
	 * @param fAdmin 管理员
	 * @param record 记录
	 * @return 是否执行成功
	 * @throws BCException 更新异常
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#updateVirtualCapital(com.qkwl.common.dto.admin.FAdmin, FVirtualCapitalOperationDTO)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateVirtualCapital(FAdmin fAdmin, FVirtualCapitalOperationDTO record) throws BCException {
		if (record.getFstatus() == VirtualCapitalOperationOutStatusEnum.Cancel) {
			UserCoinWallet userVirtualWallet = userCoinWalletMapper.selectByUidAndCoinLock(record.getFuid(), record.getFcoinid());
			BigDecimal amountfees = MathUtils.add(record.getFamount(), record.getFfees());
			BigDecimal amount = MathUtils.add(amountfees, record.getFbtcfees());
			BigDecimal frozenRmb = userVirtualWallet.getFrozen();
			if (MathUtils.sub(frozenRmb, amount).compareTo(BigDecimal.ZERO) < 0) {
				throw new BCException("虚拟币冻结金额异常");
			}
			userVirtualWallet.setTotal(MathUtils.add(userVirtualWallet.getTotal(), amount));
			userVirtualWallet.setFrozen(MathUtils.sub(userVirtualWallet.getFrozen(), amount));
			userVirtualWallet.setGmtModified(Utils.getTimestamp());
			if (userCoinWalletMapper.updateByPrimaryKey(userVirtualWallet) <= 0) {
				throw new BCException("更新虚拟钱包失败");
			}
		} 
		if (virtualCapitalOperationMapper.updateByPrimaryKey(record) <= 0) {
			throw new BCException("更新订单失败");
		}
		// MQ
		if (record.getFstatus() == VirtualCapitalOperationOutStatusEnum.Cancel) { 
			mqSend.SendUserAction(record.getFagentid(), record.getFuid(), LogUserActionEnum.COIN_WITHDRAW_CANCEL, record.getFcoinid(), record.getFamount(), "admin_" + fAdmin.getFid());
		
			mqSend.SendAdminAction(record.getFagentid(), record.getFadminid(), record.getFuid(), LogAdminActionEnum.CANCEL_COIN_WITHDRAW, record.getFcoinid(), record.getFamount());
		}
		logger.info("更新订单状态结束："+record.getFid());
		return true;
	}

	/**
	 * 新增虚拟币充值记录
	 * @param record 充值实体
	 * @return 是否新增成功
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#insertConsoleVirtualRecharge(com.qkwl.common.dto.log.FLogConsoleVirtualRecharge)
	 */
	@Override
	public boolean insertConsoleVirtualRecharge(FLogConsoleVirtualRecharge record) {
		int i = logConsoleVirtualRechargeMapper.insert(record);
		return i > 0;
	}

	/**
	 * 根据id查询虚拟币充值
	 * @param fid 充值id
	 * @return 虚拟币充值实体
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectConsoleVirtualRechargeById(int)
	 */
	@Override
	public FLogConsoleVirtualRecharge selectConsoleVirtualRechargeById(int fid) {
		return logConsoleVirtualRechargeMapper.selectByPrimaryKey(fid);
	}

	/**
	 * 删除虚拟币充值
	 * @param fid 充值id
	 * @return 是否删除成功
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#deleteConsoleVirtualRechargeById(int)
	 */
	@Override
	public boolean deleteConsoleVirtualRechargeById(int fid) {
		int i = logConsoleVirtualRechargeMapper.deleteByPrimaryKey(fid);
		return i > 0;
	}

	/**
	 * 更新虚拟币充值
	 * @param record 虚拟币充值
	 * @return 是否更新成功
	 * @throws BCException 更新异常
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#updateConsoleVirtualRecharge(com.qkwl.common.dto.log.FLogConsoleVirtualRecharge)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateConsoleVirtualRecharge(FLogConsoleVirtualRecharge record) throws BCException {
		if (record.getFstatus() == OperationlogEnum.FFROZEN) {
			int fuid = record.getFuid();
			int fcoinId = record.getFcoinid();
			BigDecimal qty = record.getFamount();
			UserCoinWallet userVirtualWallet = userCoinWalletMapper.selectByUidAndCoinLock(fuid, fcoinId);
			if (userVirtualWallet == null) {
				return false;
			}
			userVirtualWallet.setFrozen(MathUtils.add(userVirtualWallet.getFrozen(), qty));
			userVirtualWallet.setGmtModified(Utils.getTimestamp());
			userCoinWalletMapper.updateByPrimaryKey(userVirtualWallet);
		}
		if (record.getFstatus() == OperationlogEnum.AUDIT) {
			int fuid = record.getFuid();
			int fcoinId = record.getFcoinid();
			BigDecimal qty = record.getFamount();
			UserCoinWallet userVirtualWallet = userCoinWalletMapper.selectByUidAndCoinLock(fuid, fcoinId);
			if (userVirtualWallet == null) {
				return false;
			}
			userVirtualWallet.setFrozen(MathUtils.sub(userVirtualWallet.getFrozen(), qty));
			userVirtualWallet.setTotal(MathUtils.add(userVirtualWallet.getTotal(), qty));
			userVirtualWallet.setGmtModified(Utils.getTimestamp());
			userCoinWalletMapper.updateByPrimaryKey(userVirtualWallet);
		}
		int result = logConsoleVirtualRechargeMapper.updateByPrimaryKey(record);
		if (result <= 0) {
			throw new BCException();
		}
		mqSend.SendAdminAction(0, record.getFcreatorid(), record.getFuid(), LogAdminActionEnum.ADMIN_COIN_RECHARGE, record.getFcoinid(), record.getFamount());
		return true;
	}
	
	

	/**
	 * 分页查询虚拟币充值
	 * @param pageParam 分页参数
	 * @param record 实体参数
	 * @param status 状态列表
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectConsoleVirtualRechargeList(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.log.FLogConsoleVirtualRecharge, java.util.List)
	 */
	@Override
	public Pagination<FLogConsoleVirtualRecharge> selectConsoleVirtualRechargeList(Pagination<FLogConsoleVirtualRecharge> pageParam, FLogConsoleVirtualRecharge record, List<Integer> status) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("fstatus", status.size() > 0 ? status : null);
		map.put("ftype", record.getFtype());
		map.put("coinId", record.getFcoinid());
		map.put("start", pageParam.getBegindate());
		map.put("end", pageParam.getEnddate());
		// 查询总数
		int count = logConsoleVirtualRechargeMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<FLogConsoleVirtualRecharge> list = logConsoleVirtualRechargeMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}

	/**
	 * 分页查询用户钱包
	 * @param pageParam 分页参数
	 * @param fuids 用户ids
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectUserWalletList(com.qkwl.common.dto.common.Pagination, java.util.List)
	 */
	@Override
	public Pagination<UserCoinWallet> selectUserWalletList(Pagination<UserCoinWallet> pageParam, List<Integer> fuids) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("fuids", fuids);
		// 查询总数
		int count = userCoinWalletMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<UserCoinWallet> list = userCoinWalletMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	/**
	 * 分页查询用户钱包
	 * @param pageParam 分页参数
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectUserWalletList(com.qkwl.common.dto.common.Pagination)
	 */
	@Override
	public Pagination<UserCoinWallet> selectUserWalletList(Pagination<UserCoinWallet> pageParam) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		// 查询总数
		int count = userCoinWalletMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<UserCoinWallet> list = userCoinWalletMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	/**
	 * 分页查询虚拟币钱包
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @param fuids 用户ids
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectUserVirtualWalletList(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.wallet.UserCoinWallet, java.util.List)
	 */
	@Override
	public Pagination<UserCoinWallet> selectUserVirtualWalletList(Pagination<UserCoinWallet> pageParam, UserCoinWallet filterParam, List<Integer> fuids) {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("keyword", pageParam.getKeyword());
		map.put("coinId", filterParam.getCoinId());
		map.put("fuids", fuids);
		// 查询总数
		int count = userCoinWalletMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<UserCoinWallet> list = userCoinWalletMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
	}
	
	
	/**
	 * 分页查询虚拟币钱包
	 * @param pageParam 分页参数
	 * @param filterParam 实体参数
	 * @return 分页查询记录列表
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectUserVirtualWalletListByCoin(com.qkwl.common.dto.common.Pagination, com.qkwl.common.dto.wallet.UserCoinWallet)
	 */
	@Override
	public Pagination<UserCoinWallet> selectUserVirtualWalletListByCoin(Pagination<UserCoinWallet> pageParam, UserCoinWallet filterParam) {
		try {
		// 组装查询条件数据
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", pageParam.getOffset());
		map.put("limit", pageParam.getPageSize());
		map.put("orderField", pageParam.getOrderField());
		map.put("orderDirection", pageParam.getOrderDirection());
		map.put("coinId", filterParam.getCoinId());
		// 查询总数
		int count = userCoinWalletMapper.countAdminPage(map);
		if(count > 0) {
			// 查询数据
			List<UserCoinWallet> list = userCoinWalletMapper.getAdminPageList(map);
			// 设置返回数据
			pageParam.setData(list);
		}
		pageParam.setTotalRows(count);
		return pageParam;
		} catch (Exception e) {
			logger.error("查询钱包失败",e);
			return pageParam;
		}
	}
	
	/**
	 * 根据类型查询人民币操作总金额
	 * @param fuid   用户id
	 * @param type 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总金额
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectWalletTotalAmount(java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date)
	 */
	@Override
	public BigDecimal selectWalletTotalAmount(Integer fuid, Integer type, Integer status, Date start, Date end){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("type", type);
		map.put("status", status);
		map.put("start", start);
		map.put("end", end);

		BigDecimal total = walletCapitalOperationMapper.getTotalAmountByType(map);

		return total;
	}
	
	/**
	 * 根据类型查询虚拟币操作总金额
	 * @param fuid   用户id
	 * @param coinid 币种id
	 * @param type	 类型
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总金额
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectVirtualWalletTotalAmount(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date)
	 */
	@Override
	public BigDecimal selectVirtualWalletTotalAmount(Integer fuid, Integer coinid, Integer type, Integer status, Date start, Date end){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("coinid", coinid);
		map.put("type", type);
		map.put("status", status);
		map.put("start", start);
		map.put("end", end);
		BigDecimal total = virtualCapitalOperationMapper.getTotalAmountByType(map);
		
		return total;
	}
	
	@Override
	public Map<Integer,BigDecimal> selectVirtualWalletTotalAmountMap(Integer fuid, Integer type, Integer status, Date start, Date end,Date date){
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("fuid", fuid);
			map.put("type", type);
			map.put("status", status);
			map.put("start", start);
			map.put("end", end);
			map.put("date", date);
			List<FVirtualCapitalOperationDTO> totalAmountByTypeList = virtualCapitalOperationMapper.getTotalAmountByTypeList(map);
			Map<Integer,BigDecimal> collect = totalAmountByTypeList.stream().collect(Collectors.toMap(FVirtualCapitalOperationDTO::getFcoinid, FVirtualCapitalOperationDTO::getFamount));
			return collect;
		} catch (Exception e) {
			logger.info("查询虚拟币充提统计异常",e);
			return null;
		}
	}
	
	/**
	 * 根据类型查询手工充值虚拟币操作总金额
	 * @param fuid   用户id
	 * @param coinid 币种id
	 * @param status 状态
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 总金额
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectAdminRechargeVirtualWalletTotalAmount(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date)
	 */
	@Override
	public BigDecimal selectAdminRechargeVirtualWalletTotalAmount(Integer fuid, Integer coinid, Integer status, Date start, Date end){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("coinid", coinid);
		map.put("status", status);
		map.put("start", start);
		map.put("end", end);
		
		BigDecimal total = logConsoleVirtualRechargeMapper.getTotalAmountByStatus(map);
		
		return total;
	}
	
	
	/**
	 * 根据类型查询手工充值虚拟币操作总金额
	 * @param fuid   用户id
	 * @param coinid 币种id
	 * @param status 状态
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return map<币种id,总金额>
	 * @see com.qkwl.common.rpc.admin.IAdminUserCapitalService#selectAdminRechargeVirtualWalletTotalAmount(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date)
	 */
	@Override
	public Map<Integer,BigDecimal> selectAdminRechargeVirtualWalletTotalAmount(Integer fuid, Integer status, Date start, Date end){
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("fuid", fuid);
			map.put("status", status);
			map.put("start", start);
			map.put("end", end);
			 List<FLogConsoleVirtualRecharge> totalAmountByStatusList = logConsoleVirtualRechargeMapper.getTotalAmountByStatusList(map);
			 return totalAmountByStatusList.stream().collect(Collectors.toMap(FLogConsoleVirtualRecharge::getFcoinid, FLogConsoleVirtualRecharge::getFamount));
		} catch (Exception e) {
			logger.error("查询手工充值列表异常,fuid:"+fuid,e);
			return null;
		}
	}
	
	

	@Override
	public FUserPushDTO selectUserPushBalance(Integer uid, Integer pushid, Integer coinid, Integer state, Date start, Date end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", uid);
		map.put("fpushuid", pushid);
		map.put("fcoinid", coinid);
		map.put("state", state);
		map.put("start", start);
		map.put("end", end);
		return userPushMapper.selectUserPushBalance(map);
	}


	/**
	 * 修改资金信息
	 */
	@Override
	public boolean updateUserWallet(Integer uid, Integer coinId, BigDecimal amount){
		UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(uid,coinId);
		if(wallet == null){
			return false;
		}
		wallet.setTotal(MathUtils.add(wallet.getTotal(),amount));
		wallet.setFrozen(MathUtils.add(wallet.getFrozen(),amount.negate()));
		wallet.setGmtModified(new Date());
		return userCoinWalletMapper.updateByPrimaryKey(wallet) > 0;
	}

	@Override
	public boolean updateHistoryActivity(FLogConsoleVirtualRecharge record){
		int i = logConsoleVirtualRechargeMapper.updateByHistoryActivity(record);
		return i > 0;
	}

	@Override
	public Result insertRecharge(FVirtualCapitalOperationDTO operation){

		int count = virtualCapitalOperationMapper.selectByTx(operation.getFuniquenumber());
		if(count > 0){
			return Result.failure("已存在此交易！");
		}

		if(virtualCapitalOperationMapper.insertRecharge(operation) > 0){
			return Result.success("新增成功！");
		}
		return Result.failure("新增失败！");
	}

	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result recheckVirtualRecharge(FVirtualCapitalOperationDTO operation) throws Exception{

		FVirtualCapitalOperationDTO virtualOperation = virtualCapitalOperationMapper.selectAllById(operation.getFid());
		if(virtualOperation == null){
			return Result.failure("未找到此记录！");
		}

		if(virtualOperation.getFstatus().equals(VirtualCapitalOperationInStatusEnum.SUCCESS)){
			return Result.failure("此记录已到帐，不能再次审核！");
		}

		virtualOperation.setFconfirmations(operation.getFconfirmations());
		virtualOperation.setFstatus(VirtualCapitalOperationInStatusEnum.SUCCESS);
		virtualOperation.setFupdatetime(new Date());

		UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(virtualOperation.getFuid(),
				virtualOperation.getFcoinid());
		wallet.setTotal(MathUtils.add(wallet.getTotal(), virtualOperation.getFamount()));
		wallet.setGmtModified(new Date());
		if(userCoinWalletMapper.updateByPrimaryKey(wallet) <= 0){
			throw new Exception("更新钱包失败！");
		}

		if(virtualCapitalOperationMapper.updateByPrimaryKey(virtualOperation) <= 0){
			throw new Exception("更新充值记录失败！");
		}

		return Result.success("审核成功！");
	}

	@Override
	public Map<Integer,UserCoinWallet> selectUserWallet(int fuid) {
		try {
			List<UserCoinWallet> selectByUid = userCoinWalletMapper.selectByUid(fuid);
			Map<Integer, UserCoinWallet> collect = selectByUid.stream().collect(Collectors.toMap(UserCoinWallet::getCoinId, u -> u));
			return collect;
		} catch (Exception e) {
			logger.error("selectUserWallet 异常,fuid:"+fuid,e);
			return null;
		}

	}

	@Override
	public PageInfo<AssetImbalance> selectAssetImbalancePage(AssetImbalance params, Integer page, Integer pageSize) {
		try {
			PageHelper.startPage(page, pageSize);
			List<AssetImbalance> list = assetImbalanceMapper.selectByParams(params);
			PageInfo<AssetImbalance> pageInfo = new PageInfo<AssetImbalance>(list);
			return pageInfo;
		} catch (Exception e) {
			logger.error("查询资产不平衡列表异常",e);
		}
		return null;
	}

}
