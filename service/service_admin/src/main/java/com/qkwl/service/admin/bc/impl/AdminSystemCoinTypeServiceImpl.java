package com.qkwl.service.admin.bc.impl;

import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.coin.CoinDriverFactory;
import com.qkwl.common.dto.Enum.SystemCoinStatusEnum;
import com.qkwl.common.dto.coin.SystemCoinInfo;
import com.qkwl.common.dto.coin.SystemCoinSetting;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.finances.FVirtualFinances;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.Utils;
import com.qkwl.service.admin.bc.comm.SystemRedisInit;
import com.qkwl.service.admin.bc.dao.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("adminSystemCoinTypeService")
public class AdminSystemCoinTypeServiceImpl implements IAdminSystemCoinTypeService{
	
	private static final Logger logger = LoggerFactory.getLogger(AdminSystemCoinTypeServiceImpl.class);

	@Autowired
	private SystemCoinTypeMapper systemCoinTypeMapper;

	@Autowired
	private FPoolMapper poolMapper;
	@Autowired
	private SystemRedisInit systemRedisInit;
	@Autowired
	private FVirtualFinancesMapper virtualFinancesMapper;
	@Autowired
	private AdminVirtualCoinServiceTx adminVirtualCoinServiceTx;
	@Autowired
	private SystemCoinSettingMapper systemCoinSettingMapper;
	@Autowired
	private FUserMapper userMapper;
	@Autowired
	private SystemCoinInfoMapper systemCoinInfoMapper;
    @Autowired
    private UserCoinWalletMapper userCoinWalletMapper;

	/**
	 * 获取虚拟币列表
	 * @param page 分页实体对象
	 * @param type 虚拟币实体对象
	 * @return 分页实体对象
	 */
	@Override
	public Pagination<SystemCoinType> selectVirtualCoinList(Pagination<SystemCoinType> page, SystemCoinType type) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", page.getOffset());
		map.put("limit", page.getPageSize());
		map.put("keyword", page.getKeyword());
		map.put("orderfield", page.getOrderField());
		map.put("orderdirection", page.getOrderDirection());

		int count = systemCoinTypeMapper.getSystemCoinTypeCount(map);
		if(count > 0) {
			List<SystemCoinType> articleList = systemCoinTypeMapper.getSystemCoinTypeList(map);
			page.setData(articleList);
		}
		page.setTotalRows(count);
		
		return page;
	}
	
	/**
	 * 查询虚拟币基本信息
	 * @param id 虚拟币ID
	 * @return 虚拟币实体对象
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#selectVirtualCoinById(int)
	 */
	@Override
	public SystemCoinType selectVirtualCoinById(int id) {
		return systemCoinTypeMapper.selectByPrimaryKey(id);
	}

	/**
	 * 新增虚拟币
	 * @param coin 虚拟币实体对象
	 * @return true：成功，false：失败
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#insert(com.qkwl.common.dto.coin.SystemCoinType)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean insert(SystemCoinType coin) {
		try {
			int result = systemCoinTypeMapper.insert(coin);
			if (result <= 0) {
				return false;
			}
			//更新虚拟币手续费
			for (Integer level : Constant.VIP_LEVEL) {
				SystemCoinSetting setting = new SystemCoinSetting();
				setting.setCoinId(coin.getId());
				setting.setLevelVip(level);
				setting.setWithdrawMax(BigDecimal.ZERO);
				setting.setWithdrawMin(BigDecimal.ZERO);
				setting.setWithdrawFee(BigDecimal.ZERO);
				setting.setIsPercentage(true);
				setting.setWithdrawTimes(0);
				setting.setWithdrawDayLimit(BigDecimal.ZERO);
				setting.setGmtCreate(new Date());
				setting.setGmtModified(new Date());
				setting.setVersion(0);
				systemCoinSettingMapper.insert(setting);
			}
			//跟新redis中的虚拟币列表
			systemRedisInit.initSystemCoinType();
			systemRedisInit.initCoinSetting();
			return true;
		} catch (Exception e) {
			logger.error("新增币种异常",e);
			return false;
		}
		
	}

	/**
	 * 修改虚拟币基本信息
	 * @param coin 虚拟币实体对象
	 * @return true：成功，false：失败
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#updateVirtualCoin(com.qkwl.common.dto.coin.SystemCoinType)
	 */
	@Override
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public boolean updateVirtualCoin(SystemCoinType coin) {
		try {
			int result = systemCoinTypeMapper.updateSystemCoinType(coin);
			if (result <= 0) {
				return false;
			}
			boolean updateFage=false;
			for (Integer level : Constant.VIP_LEVEL) {
				SystemCoinSetting feesList = systemCoinSettingMapper.selectSystemCoinSetting(coin.getId(), level);
				if (feesList == null) {
					SystemCoinSetting setting = new SystemCoinSetting();
					setting.setCoinId(coin.getId());
					setting.setLevelVip(level);
					setting.setWithdrawMax(BigDecimal.ZERO);
					setting.setWithdrawMin(BigDecimal.ZERO);
					setting.setWithdrawFee(BigDecimal.ZERO);
					setting.setWithdrawTimes(0);
					setting.setWithdrawDayLimit(BigDecimal.ZERO);
					setting.setGmtCreate(new Date());
					setting.setGmtModified(new Date());
					setting.setVersion(0);
					systemCoinSettingMapper.insert(setting);
					updateFage=true;
				}
			}
			//跟新redis中的虚拟币列表
			systemRedisInit.initSystemCoinType();
			if(updateFage){
				systemRedisInit.initCoinSetting();
			}
			return true;
		} catch (Exception e) {
			logger.error("修改币种异常",e);
			return false;
		}
		
	}

	/**
	 * 启用虚拟币钱包
	 * @param coin 虚拟币实体对象
	 * @return true：成功，false：失败
	 */
	public boolean updateVirtualCoinByEnabled(SystemCoinType coin) {
		int result = systemCoinTypeMapper.updateSystemCoinTypeStatus(coin);
		if (result <= 0) {
			return false;
		}
		if (!coin.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())) {
			boolean updateFage=false;
			for (Integer level : Constant.VIP_LEVEL) {
				SystemCoinSetting feesList = systemCoinSettingMapper.selectSystemCoinSetting(coin.getId(), level);
				if (feesList == null) {
					SystemCoinSetting setting = new SystemCoinSetting();
					setting.setCoinId(coin.getId());
					setting.setLevelVip(level);
					setting.setWithdrawMax(BigDecimal.ZERO);
					setting.setWithdrawMin(BigDecimal.ZERO);
					setting.setWithdrawFee(BigDecimal.ZERO);
					setting.setWithdrawTimes(0);
					setting.setWithdrawDayLimit(BigDecimal.ZERO);
					setting.setGmtCreate(new Date());
					setting.setGmtModified(new Date());
					setting.setVersion(0);
					systemCoinSettingMapper.insert(setting);
					updateFage = true;
				}
				if (updateFage) {
					systemRedisInit.initCoinSetting();
				}
			}
			//分配虚拟币钱
			Integer coinId = coin.getId();
			List<FUser> userList = userMapper.selectAll();
			for (FUser user : userList) {
				try {
					adminVirtualCoinServiceTx.insertCoinWallet(coinId, user.getFid());
				} catch (Exception e) {
				}
			}
		}
		//跟新redis中的虚拟币列表
		systemRedisInit.initSystemCoinType();
		return true;
	}
	
	/**
	 * 修改钱包链接
	 * @param coin 虚拟币实体对象
	 * @return true：成功，false：失败
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#updateVirtualCoinWalletLink(com.qkwl.common.dto.coin.SystemCoinType)
	 */
	@Override
	public boolean updateVirtualCoinWalletLink(SystemCoinType coin) {
		int result = systemCoinTypeMapper.updateSystemCoinTypeLink(coin);
		if (result <= 0) {
			return false;
		}
		boolean updateFage =false;
		for (Integer level : Constant.VIP_LEVEL) {
			SystemCoinSetting feesList = systemCoinSettingMapper.selectSystemCoinSetting(coin.getId(), level);
			if (feesList == null) {
				SystemCoinSetting setting = new SystemCoinSetting();
				setting.setCoinId(coin.getId());
				setting.setLevelVip(level);
				setting.setWithdrawMax(BigDecimal.ZERO);
				setting.setWithdrawMin(BigDecimal.ZERO);
				setting.setWithdrawFee(BigDecimal.ZERO);
				setting.setWithdrawTimes(0);
				setting.setWithdrawDayLimit(BigDecimal.ZERO);
				setting.setGmtCreate(new Date());
				setting.setGmtModified(new Date());
				setting.setVersion(0);
				systemCoinSettingMapper.insert(setting);
				updateFage=true;
			}
		}
		//跟新redis中的虚拟币列表
		systemRedisInit.initSystemCoinType();
		if(updateFage){
			systemRedisInit.initCoinSetting();
		}
		return true;
	}
	

	/***************虚拟币地址操作****************/
	
	/**
	 * 查询虚拟币地址数量列表
	 * @param page 分页实体对象
	 * @return 分页实体对象
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#selectVirtualCoinAddressNumList(com.qkwl.common.dto.common.Pagination)
	 */
	@Override
	public Pagination<Map<String, Object>> selectVirtualCoinAddressNumList(Pagination<Map<String, Object>> page) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("offset", page.getOffset());
		map.put("limit", page.getPageSize());
		map.put("keyword", page.getKeyword());
		int count = poolMapper.countVirtualCoinAddressNumList(map);
		if(count > 0) {
			List<Map<String, Object>> addressList = poolMapper.getVirtualCoinAddressNumList(map);
			page.setData(addressList);
		}
		page.setTotalRows(count);
		
		return page;
	}
	
	/**
	 * 生成虚拟币地址
	 * @param coinType 虚拟币实体对象
	 * @param count 生成数量
	 * @param password 钱包密码
	 * @return 200添加成功,302钱包连接失败，请检查配置信息，303取地址受限，304钱包连接失败，请检查配置信息，未知错误
	 * @see com.qkwl.common.rpc.admin.IAdminSystemCoinTypeService#createVirtualCoinAddress(com.qkwl.common.dto.coin.SystemCoinType, int, String)
	 */
	@Override
	public int createVirtualCoinAddress(SystemCoinType coinType,int count,String password){
		String accesskey = coinType.getAccessKey();
		String secretkey = coinType.getSecrtKey();
		String ip = coinType.getIp();
		String port = coinType.getPort();
		if (accesskey == null || secretkey == null || ip == null || port == null) {
			return 301;
		}

		// get CoinDriver
		CoinDriver coinDriver = new CoinDriverFactory.Builder(coinType.getCoinType(), ip, port)
				.accessKey(accesskey)
				.secretKey(secretkey)
				.pass(password)
				.assetId(coinType.getAssetId())
				.sendAccount(coinType.getEthAccount())
				.builder()
				.getDriver();

		String address = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
		try {
			for (int i = 0; i < count; i++) {
				address = coinDriver.getNewAddress(sdf.format(Utils.getTimestamp()));
				if (address == null || address.trim().length() == 0) {
					continue;
				}
				
				try{
					adminVirtualCoinServiceTx.insertPoolInfo(coinType.getId(), address);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			return 200;
		} catch (Exception e) {
			return 304;
		} finally {
			try {
				coinDriver.walletLock();
			} catch (Exception e) {
				return 304;
			}
		}
	}

	
	@Override
	public boolean insertVirtualFinances(FVirtualFinances record) {
		if(virtualFinancesMapper.insert(record)<=0){
			return false;
		}
		systemRedisInit.initVirtualFinances();
		return true;
	}
	

	@Override
	public FVirtualFinances selectVirtualFinances(Integer fid) {
		return virtualFinancesMapper.selectByPrimaryKey(fid);
	}
	

	@Override
	public List<FVirtualFinances> selectVirtualFinancesList(Integer fcoinid, Integer fstate) {
		return virtualFinancesMapper.selectByCoinId(fcoinid, fstate);
	}
	

	@Override
	public boolean updateVirtualFinances(FVirtualFinances record) {
		if(virtualFinancesMapper.updateByPrimaryKey(record)<=0){
			return false;
		}
		systemRedisInit.initVirtualFinances();
		return true;
	}
	

	@Override
	public boolean deleteVirtualFinances(Integer fid) {
		if(virtualFinancesMapper.deleteByPrimaryKey(fid)<=0){
			return false;
		}
		systemRedisInit.initVirtualFinances();
		return true;
	}

	@Override
	public List<SystemCoinSetting> selectSystemCoinSettingList(Integer coinId) {
		return systemCoinSettingMapper.selectListByCoinId(coinId);
	}

	@Override
	public boolean updateSystemCoinSetting(SystemCoinSetting record) {
		if(systemCoinSettingMapper.updateByPrimaryKey(record)>0){
			systemRedisInit.initCoinSetting();
			return true;
		}
		return false;
	}

	@Override
	public boolean insertSystemCoinInfo(SystemCoinInfo coinInfo) {
		int insert = systemCoinInfoMapper.insert(coinInfo);
		if (insert > 0){
			systemRedisInit.initCoinInfo();
			return true;
		}
		return false;
	}

	@Override
	public boolean updateSystemCoinInfo(SystemCoinInfo coinInfo) {
		int update = systemCoinInfoMapper.updateSystemCoinInfo(coinInfo);
		if (update > 0){
			systemRedisInit.initCoinInfo();
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteSystemCoinInfo(Integer id) {
		int delete = systemCoinInfoMapper.deleteByPrimaryKey(id);
		if (delete > 0){
			systemRedisInit.initCoinInfo();
			return true;
		}			
		return false;
	}

	@Override
	public SystemCoinInfo selectSystemCoinInfo(Integer primaryKey) {
		return systemCoinInfoMapper.selectByPrimaryKey(primaryKey);
	}

	@Override
	public Pagination<SystemCoinInfo> selectSystemCoinInfoList(Map<String, Object> params) {
		System.out.println("selectSystemCoinInfoList ------------ ");
		Set<Map.Entry<String, Object>> entries = params.entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			System.out.println("key = "+entry.getKey()+" value = "+entry.getValue());
		}
		Pagination<SystemCoinInfo> systemCoinInfoPagination = new Pagination<>();
		systemCoinInfoPagination.setData(systemCoinInfoMapper.getSystemCoinInfoList(params));
		return systemCoinInfoPagination;
	}

	@Override
	public String coinSwitch(Integer oldCoinId, Integer newCoinId) {
		try {
		    Integer selectCount = userCoinWalletMapper.selectCount(newCoinId);
		    if(selectCount > 0) {
		    	return "操作失败，新币种已存在客户钱包。";
		    }
		    boolean creatCoinWallet = adminVirtualCoinServiceTx.creatCoinWallet(oldCoinId, newCoinId);
		    if(creatCoinWallet) {
		    	return null;
		    }
		    return "执行失败";
		} catch (Exception e) {
			logger.error("AdminSystemCoinTypeServiceImpl.coinSwitch 异常,oldCoinId:"+oldCoinId +",newCoinId:"+newCoinId,e);
			return "系统异常";
		}
	}
}
