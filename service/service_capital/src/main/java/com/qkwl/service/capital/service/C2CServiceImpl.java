package com.qkwl.service.capital.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.Enum.UserBankInfoDefaultEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustStatusEnum;
import com.qkwl.common.dto.Enum.c2c.UserC2CEntrustTypeEnum;
import com.qkwl.common.dto.c2c.C2CBusiness;
import com.qkwl.common.dto.c2c.SystemC2CSetting;
import com.qkwl.common.dto.c2c.UserC2CEntrust;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.pre.PreValidationHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.c2c.C2CService;
import com.qkwl.common.util.SnowFlakeId;
import com.qkwl.common.util.Utils;
import com.qkwl.service.capital.dao.FUserBankinfoMapper;
import com.qkwl.service.capital.dao.UserCoinWalletMapper;
import com.qkwl.service.capital.dao.c2c.C2CBusinessMapper;
import com.qkwl.service.capital.dao.c2c.SystemC2CSettingMapper;
import com.qkwl.service.capital.dao.c2c.UserC2CEntrustMapper;
import com.qkwl.service.capital.impl.UserFinancesServiceImpl;
import com.qkwl.service.capital.model.FUserBankinfoDO;
import com.qkwl.service.capital.model.UserC2COrder;
import com.qkwl.service.common.mapper.UserCommonMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service("c2cService")
public class C2CServiceImpl implements C2CService{
	private static final Logger logger = LoggerFactory.getLogger(UserFinancesServiceImpl.class);
	
	@Autowired
	private C2CBusinessMapper c2cBusinessMapper;
	
	@Autowired
	private UserC2CEntrustMapper userC2CEntrustMapper;
	
	@Autowired
	private SystemC2CSettingMapper systemC2CSettingMapper;
	
	@Autowired
    private UserCommonMapper userCommonMapper;
	
	@Autowired
    private UserCoinWalletMapper userCoinWalletMapper;
	
	@Autowired
    private PreValidationHelper preValidationHelper;
	
	@Autowired
	private FUserBankinfoMapper fUserBankinfoMapper;
 	
	@Autowired
    private RedisHelper redisHelper;
	//延时队列
	private DelayQueue<UserC2COrder> delayQueue = new DelayQueue<UserC2COrder>();
	
	@PostConstruct
    public void init() {
        start();
    }
	
	@Override
	public List<C2CBusiness> selectBusinessByType(int type,int coinId) {
		logger.info("start selectBusinessByType");
		try {
			List<C2CBusiness> list =  c2cBusinessMapper.selectBusinessByType(type,coinId);
			logger.info("list size is---"+list.size());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PageInfo<UserC2CEntrust> selectList(UserC2CEntrust record
			,int pageNo,int pageSize) {
		try {
			PageHelper.startPage(pageNo, pageSize);
			if(!record.getStatus().equals(0)) {
				List<Integer> statusList = new ArrayList<>();
				statusList.add(record.getStatus());
				record.setStatusList(statusList);
			}
			List<UserC2CEntrust> list = userC2CEntrustMapper.selectList(record);
			PageInfo<UserC2CEntrust> pageInfo = new PageInfo<UserC2CEntrust>(list);
			return pageInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public SystemC2CSetting selectById(int id) {
		return systemC2CSettingMapper.selectByPrimaryKey(id);
	}

	@Override
	public UserC2CEntrust selectOrderById(int id) {
		return userC2CEntrustMapper.selectByPrimaryKey(id);
	}

	@Transactional(value="flexibleTransMgr" ,isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public Result createEntrust(UserC2CEntrust record) {
		if(record.getUserId() == null || record.getUserId() <= 0) {
			return Result.param("userId is null");
		}
		
		if(record.getBusinessId() == null || record.getBusinessId() <= 0) {
			return Result.param("businessId is null");
		}
		
		if(record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			return Result.param("amount is null");
		}
		
		if(record.getType() == null || record.getType() <= 0) {
			return Result.param("type is null");
		}
		
		FUser user = userCommonMapper.selectOneById(record.getUserId());
		if(user == null) {
			return Result.param("user is not found");
		}
		
		//判断最小买卖数量
		Map<String, String> param = getParam();
		String minBuyAmountStr = param.get("minBuyAmount");
		String minSellAmountStr = param.get("minSellAmount");
		String buyPriceStr = param.get("buyPrice");
		String sellPriceStr = param.get("sellPrice");
		String digitStr = param.get("digit");
		
		BigDecimal minBuyAmount = new BigDecimal(minBuyAmountStr);
		BigDecimal minSellAmount = new BigDecimal(minSellAmountStr);
		BigDecimal buyPrice = new BigDecimal(buyPriceStr);
		BigDecimal sellPrice = new BigDecimal(sellPriceStr);
		int digit = Integer.parseInt(digitStr);
		
		//获取usdt最新价格
		String usdtSellPriceStr = param.get("usdtSellPrice");
		String usdtBuyPriceStr = param.get("usdtBuyPrice");
		
		//如果是usdt交易
		if(record.getCoinId().equals(52)) {
			BigDecimal usdtPrice = getCNYValue();
			BigDecimal addPrice = new BigDecimal(usdtBuyPriceStr);
			BigDecimal subPrice = new BigDecimal(usdtSellPriceStr);
			BigDecimal usdtSellPrice = MathUtils.sub(usdtPrice, subPrice);
			BigDecimal usdtBuyPrice = MathUtils.add(usdtPrice, addPrice);
			
			buyPrice = usdtBuyPrice;
			sellPrice = usdtSellPrice;
		}
		
		//查询对应的商户，如果不存在则报错
		C2CBusiness c2cBusiness = selectBusiness(record.getBusinessId());
		if(c2cBusiness == null) {
			return Result.param("c2cBusiness is not found");
		}
		
		if (preValidationHelper.validateUserStatus(user)) {
            return Result.failure(10011, "账户出现安全隐患被冻结，请联系客服");
        }
		
		//如果用户没有绑定银行卡或者没有默认银行卡则报错
		FUserBankinfoDO fBankinfo = new FUserBankinfoDO();
		fBankinfo.setFuid(user.getFid());
		fBankinfo.setIsDefault(UserBankInfoDefaultEnum.TRUE.getCode());
		List<FUserBankinfoDO> bankList = fUserBankinfoMapper.getBankInfoListByBankInfo(fBankinfo);
		if(bankList==null || bankList.size() ==0) {
			return Result.failure(1001, "用户没有绑定银行卡");
		}
		
		String random = Utils.randomInteger(6);
		String id = new SnowFlakeId(0, 0).nextId()+"";
		String orderId = id.substring(0, 12);
		if(record.getType().equals(UserC2CEntrustTypeEnum.recharge.getCode())) {
			
			//如果当前用户已经下过一单了则报错
			UserC2CEntrust c2cParam = new UserC2CEntrust();
			c2cParam.setUserId(user.getFid());
			List<Integer> statusList = new ArrayList<>();
			statusList.add(UserC2CEntrustStatusEnum.processing.getCode());
			statusList.add(UserC2CEntrustStatusEnum.wait.getCode());
			c2cParam.setStatusList(statusList);
			c2cParam.setType(UserC2CEntrustTypeEnum.recharge.getCode());
			c2cParam.setCoinId(record.getCoinId());
			List<UserC2CEntrust> list = userC2CEntrustMapper.selectList(c2cParam);
			if(list != null && list.size() >= 1) {
				return Result.failure(1002, "已存在未完成的订单，请耐心等待完成后方可下单！");
			}
			
			//如果数量小于最小买入数量则报错
			if(record.getAmount().compareTo(minBuyAmount) < 0) {
				return Result.failure(1000, "数量不能少于"+minBuyAmount);
			}
			
			BigDecimal money = MathUtils.mul(buyPrice, record.getAmount());
			BigDecimal newMoney = MathUtils.toScaleNum(money, digit);
			record.setMoney(newMoney);
			
			//订单编号
			String orderNum = "CZ"+orderId;
			record.setOrderNumber(orderNum);
			
			record.setStatus(UserC2CEntrustStatusEnum.wait.getCode());
			record.setPrice(buyPrice);
		}else if(record.getType().equals(UserC2CEntrustTypeEnum.withdraw.getCode())) {
			
			if(record.getAmount().compareTo(minSellAmount) < 0) {
				return Result.failure(1000, "数量不能少于"+minSellAmount);
			}
			
			//卖出订单限制10单
			//如果当前用户已经下过一单了则报错
			UserC2CEntrust c2cParam = new UserC2CEntrust();
			c2cParam.setUserId(user.getFid());
			c2cParam.setType(UserC2CEntrustTypeEnum.withdraw.getCode());
			c2cParam.setCoinId(record.getCoinId());
			List<Integer> statusList = new ArrayList<>();
			statusList.add(UserC2CEntrustStatusEnum.processing.getCode());
			c2cParam.setStatusList(statusList);
			List<UserC2CEntrust> list = userC2CEntrustMapper.selectList(c2cParam);
			if(list != null && list.size() >= 10) {
				return Result.failure(1004, "已存在未完成的订单，请耐心等待完成后方可下单！");
			}
			
			//如果是卖出，则需要冻结用户钱包GSET资产 
			//首先比较用户钱包GSET资产是否有足够的余额
			UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(record.getUserId(),record.getCoinId());
	        if (wallet == null) {
	            if (logger.isErrorEnabled()) {
	                logger.error("wallt is null  userId:{}", record.getUserId());
	            }
	            return Result.failure(10118, "余额不足");
	        }
	        if (wallet.getTotal().compareTo(record.getAmount()) < 0) {
	            return Result.failure(10118, "余额不足");
	        }
	        
	        wallet.setTotal(MathUtils.sub(wallet.getTotal(),record.getAmount()));
            wallet.setFrozen(MathUtils.add(wallet.getFrozen(),record.getAmount()));
            wallet.setGmtModified(new Date());
            userCoinWalletMapper.updateByPrimaryKey(wallet);
            
            BigDecimal money = MathUtils.mul(sellPrice, record.getAmount());
            if(money.compareTo(BigDecimal.valueOf(50000))>0) {
            	return Result.failure(1005, "单笔卖出订单金额不能超过5万");
            }
            BigDecimal newMoney = MathUtils.toScaleNum(money, digit);
			record.setMoney(newMoney);
			
			String orderNum = "TX"+orderId;
			record.setOrderNumber(orderNum);
			record.setStatus(UserC2CEntrustStatusEnum.wait.getCode());
			record.setPrice(sellPrice);
		}
		
		int bankId = bankList.get(0).getFid();
		record.setRemark(random);
		record.setBusinessName(c2cBusiness.getBusinessName());
		record.setBankAccount(c2cBusiness.getBankAccountName());
		record.setBankCode(c2cBusiness.getBankNumber());
		record.setBank(c2cBusiness.getBankName());
		record.setBankAddress(c2cBusiness.getBankAddress());
		record.setBankId(bankId);
		record.setPhone(c2cBusiness.getPhone());
		record.setVersion(0);
		//record.setCoinId(9);
		try {
			userC2CEntrustMapper.insert(record);
			
			//买入订单添加到延时队列
			if(record.getType().equals(UserC2CEntrustTypeEnum.recharge.getCode())) {
				add(record.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result.success("成功",record);
	}
	
	@Override
	public Map<String, String> getParam() {
		Map<String, String> map = new HashMap<>();
		try {
			List<SystemC2CSetting> list = systemC2CSettingMapper.selectAll();
			for(SystemC2CSetting value : list) {
				map.put(value.getKey(), value.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public C2CBusiness selectBusiness(int id) {
		return c2cBusinessMapper.selectByPrimaryKey(id);
	}
	
	
	public void start(){
		logger.info("------start processing queue-------");
		//先从数据库查询需要添加到延时队列的数据
		UserC2CEntrust record = new UserC2CEntrust();
		record.setStatus(UserC2CEntrustStatusEnum.wait.getCode());
		record.setType(UserC2CEntrustTypeEnum.recharge.getCode());
		List<UserC2CEntrust> list = userC2CEntrustMapper.selectList(record);
		for(UserC2CEntrust obj : list) {
			add(obj.getId());
		}
		
		logger.info("------queue size is-------"+delayQueue.size()+"-----");
		new Thread(new Runnable(){
			public void run(){
				try{
					while(true){
						//延时队列接收到了对象
						UserC2COrder order = delayQueue.take();
						if(order != null) {
							logger.info("------失效的订单id是"+order.getOrderId()+"-------");
							//更新数据库
							//先查询此订单是否为审核通过状态
							UserC2CEntrust c2cEntrust = selectOrderById(order.getOrderId());
							if(c2cEntrust.getStatus().equals(UserC2CEntrustStatusEnum.wait.getCode())
									&& c2cEntrust.getType().equals(UserC2CEntrustTypeEnum.recharge.getCode())) {
								c2cEntrust.setStatus(UserC2CEntrustStatusEnum.close.getCode());
								userC2CEntrustMapper.updateByPrimaryKeySelective(c2cEntrust);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();;
	}

	@Override
	public void add(int orderId){
		//当前加入
		logger.info("--------------当前加入队列的id是--"+orderId+"----");
		//1800000
		Map<String, String> param = getParam();
		String buyExpireTimeStr = param.get("buyExpireTime");
		int buyExpireTime = Integer.parseInt(buyExpireTimeStr)*60;
		delayQueue.put(new UserC2COrder(orderId, buyExpireTime));
	}
	
	@Override
	public void remove(int orderId){
		UserC2COrder[] array = delayQueue.toArray(new UserC2COrder[]{});
		if(array == null || array.length <= 0){
			return;
		}
		UserC2COrder target = null;
		for(UserC2COrder order : array){
			if(order.getOrderId() == orderId){
				target = order;
				break;
			}
		}
		if(target != null){
			delayQueue.remove(target);
		}
	}

	@Override
	public boolean updateEntrust(int orderId) {
		UserC2CEntrust c2cEntrust = userC2CEntrustMapper.selectByPrimaryKey(orderId);
		
		if(c2cEntrust == null) {
			return false;
		}
		
		if(c2cEntrust.getStatus().equals(UserC2CEntrustStatusEnum.wait.getCode())
				&& c2cEntrust.getType().equals(UserC2CEntrustTypeEnum.recharge.getCode())) {
			c2cEntrust.setStatus(UserC2CEntrustStatusEnum.processing.getCode());
			userC2CEntrustMapper.updateByPrimaryKeySelective(c2cEntrust);
		}
		//从队列里面去掉
		remove(orderId);
		return true;
	}
	
	

	@Override
	public int getEntrustCount(UserC2CEntrust record) {
		List<UserC2CEntrust> list = userC2CEntrustMapper.selectList(record);
		return list.size();
	}
	
	 public BigDecimal getCNYValue() {
	    	try {
	    		String cny_value = redisHelper.getRedisData("CNY_VALUE");
		        if (!TextUtils.isEmpty(cny_value)) {
		            return new BigDecimal(cny_value);
		        }
		      //通过这个接口获取美元对人名币的汇率
		        String url = "http://data.fixer.io/api/latest?access_key=bf18da33f4011c6dc52c839a4688bd3b";
		        OkHttpClient client = new OkHttpClient.Builder()
		                .connectTimeout(10, TimeUnit.SECONDS)
		                .writeTimeout(10, TimeUnit.SECONDS)
		                .readTimeout(10, TimeUnit.SECONDS)
		                .build();
		        
		        Response response = client.newCall(new Request.Builder()
					        .url(url)
					        .build())
					        .execute();
				
		        JSONObject jsonObject = JSONObject.parseObject(response.body().string());
		        JSONObject rates = jsonObject.getJSONObject("rates");
		        BigDecimal bigDecimal = rates.getBigDecimal("CNY");
		        BigDecimal bigDecimal2 = rates.getBigDecimal("USD");
		        BigDecimal bg = bigDecimal.divide(bigDecimal2,bigDecimal.ROUND_HALF_UP);
		        setCNYValue(bg.toString());
	        	return bg;
	    	} catch (IOException e) {
				return null;
	    	}
	    }
	 
	public void setCNYValue(String cny) {
	    if (TextUtils.isEmpty(cny)) {
            return;
        }
        redisHelper.setRedisData("CNY_VALUE", cny, 60 * 60);
    }

	@Transactional(value="flexibleTransMgr" ,isolation = Isolation.REPEATABLE_READ, propagation=Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public boolean cancelEntrust(int orderId) {
		UserC2CEntrust c2cEntrust = userC2CEntrustMapper.selectByPrimaryKey(orderId);
		
		if(c2cEntrust == null) {
			return false;
		}
		
		if(c2cEntrust.getStatus().equals(UserC2CEntrustStatusEnum.wait.getCode())) {
			//如果是卖出订单 还需要将冻结金额解冻
			if(c2cEntrust.getType().equals(UserC2CEntrustTypeEnum.withdraw.getCode())) {
				UserCoinWallet wallet = userCoinWalletMapper.selectByUidAndCoinLock(c2cEntrust.getUserId(), c2cEntrust.getCoinId());
				if(MathUtils.compareTo(wallet.getFrozen(), c2cEntrust.getAmount()) < 0) {
					return false;
				}
				wallet.setFrozen(MathUtils.sub(wallet.getFrozen(), c2cEntrust.getAmount()));
				wallet.setTotal(MathUtils.add(wallet.getTotal(), c2cEntrust.getAmount()));
				wallet.setGmtModified(new Date());;
				if(userCoinWalletMapper.updateByPrimaryKey(wallet) <= 0 ) {
					logger.error("c2c用户撤单,更新钱包失败，id：" + wallet.getId() );
					return false;
				}
			}
			c2cEntrust.setStatus(UserC2CEntrustStatusEnum.cancel.getCode());
			userC2CEntrustMapper.updateByPrimaryKeySelective(c2cEntrust);
			//从队列里面去掉
			remove(orderId);
			return true;
		}
		return false;
	}
}

