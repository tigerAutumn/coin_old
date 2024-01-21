package com.qkwl.service.coin.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.coin.CoinDriverFactory;
import com.qkwl.common.coin.TxInfo;
import com.qkwl.common.dto.coin.USDTCollect;
import com.qkwl.common.dto.Enum.CapitalOperationInOutTypeEnum;
import com.qkwl.common.dto.Enum.CapitalOperationInStatus;
import com.qkwl.common.dto.Enum.DataSourceEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.ScoreTypeEnum;
import com.qkwl.common.dto.Enum.SystemCoinSortEnum;
import com.qkwl.common.dto.Enum.USDTCollectStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationInStatusEnum;
import com.qkwl.common.dto.Enum.VirtualCapitalOperationTypeEnum;
import com.qkwl.common.dto.capital.FUserVirtualAddressDTO;
import com.qkwl.common.dto.capital.FVirtualCapitalOperationDTO;
import com.qkwl.common.dto.coin.FPool;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.mq.ScoreHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.common.util.CoinCommentUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.service.coin.mapper.FPoolMapper;
import com.qkwl.service.coin.mapper.FUserMapper;
import com.qkwl.service.coin.mapper.FUserVirtualAddressMapper;
import com.qkwl.service.coin.mapper.FVirtualCapitalOperationMapper;
import com.qkwl.service.coin.mapper.FWalletCapitalOperationMapper;
import com.qkwl.service.coin.mapper.USDTCollectMapper;
import com.qkwl.service.coin.mapper.UserCoinWalletMapper;
import com.qkwl.service.coin.util.JobUtils;
import com.qkwl.service.coin.util.MQSend;

@Service("coinService")
@Scope("prototype")
public class CoinService {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(CoinService.class);

	@Autowired
	private FVirtualCapitalOperationMapper fVirtualCapitalOperationMapper;
	@Autowired
	private FWalletCapitalOperationMapper fWalletCapitalOperationMapper;
	@Autowired
	private FUserVirtualAddressMapper fUserVirtualAddressMapper;
	@Autowired
	private FUserMapper userMapper;
	@Autowired
	private FPoolMapper poolMapper;
	@Autowired
	private UserCoinWalletMapper userCoinWalletMapper;

	@Autowired
	private USDTCollectMapper usdtCollectMapper;

	@Autowired
	private JobUtils jobUtils;
	@Autowired
	private ScoreHelper scoreHelper;
	@Autowired
	private ValidateHelper validateHelper;
	@Autowired
	private MQSend mqSend;

	/**
	 * 定时创建充值订单
	 * 
	 * @param coinType
	 * @throws Exception
	 */
	public void updateRecharge(SystemCoinType coinType) {
		int coinid = coinType.getId();
		
/*		String systemArgs = jobUtils.getSystemArgs(ArgsConstant.USDTMinimumRechargeNumber);
		BigDecimal USDTMinimumRechargeNumber = null ;
		if(StringUtils.isEmpty(systemArgs)) {
			USDTMinimumRechargeNumber = new BigDecimal(100);
		}else {
			USDTMinimumRechargeNumber = new BigDecimal(systemArgs);
		}*/
		
		//最小充值数
		BigDecimal rechargeMinLimit = coinType.getRechargeMinLimit();

		int begin = 0;
		int step = 100;
		boolean is_continue = true;

		String accesskey = coinType.getAccessKey();
		String secretkey = coinType.getSecrtKey();
		String ip = coinType.getIp();
		String port = coinType.getPort();

		// get CoinDriver
		CoinDriver coinDriver = new CoinDriverFactory.Builder(coinType.getCoinType(), ip, port).accessKey(accesskey)
				.secretKey(secretkey).assetId(coinType.getAssetId()).sendAccount(coinType.getEthAccount()).builder()
				.getDriver();

		if (coinDriver == null) {
			return;
		}
		List<TxInfo> txInfos;
		while (is_continue) {
			try {
				txInfos = coinDriver.listTransactions(step, begin);
				begin += step;
				if (txInfos == null || txInfos.size() == 0) {
					is_continue = false;
					continue;
				}
			} catch (Exception e) {
				logger.error("updateRecharge listTransactions error", e);
				is_continue = false;
				continue;
			}
			for (TxInfo txInfo : txInfos) {
				String txid = txInfo.getTxid().trim();

				Date date = Utils.getCurTimeString("2017-08-06 17:40:00");
				if (txInfo.getTime() != null && txInfo.getTime().before(date)) {
					continue;
				}

				// BTC类 特殊处理
				if (coinType.getCoinType().equals(SystemCoinSortEnum.BTC.getCode())) {
					if ("BTC".equals(coinType.getShortName()) || "LTC".equals(coinType.getShortName())
							|| "BCC".equals(coinType.getShortName())) {
						txid = txid + "_" + txInfo.getVout();
					} else {
						txid = txid + "_" + txInfo.getAddress().trim();
					}
				} else if (coinType.getCoinType().equals(SystemCoinSortEnum.ICS.getCode())
						|| coinType.getCoinType().equals(SystemCoinSortEnum.MIC.getCode())) {
					txid = txid + "_" + txInfo.getAddress().trim() + "_" + coinType.getAssetId();
				} else if (coinType.getCoinType().equals(SystemCoinSortEnum.GXS.getCode())) {
					txid = txid + "_" + txInfo.getAddress() + "_" + txInfo.getVout();
				}

				// 判断操作记录
				List<FVirtualCapitalOperationDTO> fvirtualcaptualoperations = this.fVirtualCapitalOperationMapper
						.selectByTxid(txid);
				if (fvirtualcaptualoperations.size() > 0) {
					continue;
				}

				// logger.info("----> btc 正在循环读取每一条记录 : " + txid);

				FVirtualCapitalOperationDTO fvirtualcaptualoperation = new FVirtualCapitalOperationDTO();

				boolean hasOwner = true;
				String address = txInfo.getAddress().trim();
				Integer baseCoinId = coinid;
				
				if (coinType.getCoinType().equals(SystemCoinSortEnum.ICS.getCode())) {
					SystemCoinType icsCoinType = jobUtils.getCoinTypeShortName("ICS");
					if (icsCoinType == null) {
						logger.error("ICS coinType is null");
						continue;
					}
					baseCoinId = icsCoinType.getId();
				}else if (coinType.getCoinType().equals(SystemCoinSortEnum.MIC.getCode())) {
					SystemCoinType icsCoinType = jobUtils.getCoinTypeShortName("MIC");
					if (icsCoinType == null) {
						logger.error("MIC coinType is null");
						continue;
					}
					baseCoinId = icsCoinType.getId();
				}

				// 判断用户是否存在
				if(rechargeMinLimit.compareTo(txInfo.getAmount()) > 0) {
					txInfo.setUid(null);
				}else if (coinType.getCoinType().equals(SystemCoinSortEnum.GXS.getCode())) {
					FUser fUser = userMapper.selectByPrimaryKey(txInfo.getUid());
					if (fUser == null) {
						txInfo.setUid(null);
					}
				}else if (coinType.getCoinType().equals(SystemCoinSortEnum.USDT.getCode())) {
					FUserVirtualAddressDTO fvirtualaddresse = this.fUserVirtualAddressMapper
							.selectByCoinAndAddress(baseCoinId, address);
					if(fvirtualaddresse == null) {
						txInfo.setUid(null);
					} else {
						txInfo.setUid(fvirtualaddresse.getFuid());
					}
				}else if (coinType.getCoinType().equals(SystemCoinSortEnum.EOS.getCode())) {
					if(StringUtils.isNumeric(txInfo.getMemo())) {
						try {
							Integer uid = Integer .valueOf(txInfo.getMemo());
							FUser selectByPrimaryKey = userMapper.selectByPrimaryKey(uid);
							if(selectByPrimaryKey != null) {
								txInfo.setUid(uid);
							}
						} catch (Exception e) {
							logger.error("eos.mome is no number,memeo:"+address);
						}
					}
				}else {
					FUserVirtualAddressDTO fvirtualaddresse = this.fUserVirtualAddressMapper
							.selectByCoinAndAddress(baseCoinId, address);
					if (fvirtualaddresse == null) {
						txInfo.setUid(null);
					} else {
						txInfo.setUid(fvirtualaddresse.getFuid());
					}
				}

				if (txInfo.getUid() != null) {
					fvirtualcaptualoperation.setFuid(txInfo.getUid());
				} else {
					hasOwner = false;// 没有这个地址，充错进来了？没收！
				}

				fvirtualcaptualoperation.setFamount(txInfo.getAmount());
				fvirtualcaptualoperation.setFfees(BigDecimal.ZERO);
				fvirtualcaptualoperation.setFcoinid(coinid);
				fvirtualcaptualoperation.setFtype(VirtualCapitalOperationTypeEnum.COIN_IN.getCode());
				fvirtualcaptualoperation.setFstatus(VirtualCapitalOperationInStatusEnum.WAIT_0);
				fvirtualcaptualoperation.setFhasowner(hasOwner);
				fvirtualcaptualoperation.setFbtcfees(BigDecimal.ZERO);
				fvirtualcaptualoperation.setFblocknumber(0);
				fvirtualcaptualoperation.setFconfirmations(0);
				fvirtualcaptualoperation.setFrechargeaddress(txInfo.getAddress().trim());
				fvirtualcaptualoperation.setFcreatetime(Utils.getTimestamp());
				fvirtualcaptualoperation.setFupdatetime(Utils.getTimestamp());
				fvirtualcaptualoperation.setVersion(0);
				fvirtualcaptualoperation.setFsource(DataSourceEnum.WEB.getCode());
				fvirtualcaptualoperation.setFuniquenumber(txid);
				if(!StringUtils.isEmpty(txInfo.getMemo())) {
					fvirtualcaptualoperation.setMemo(txInfo.getMemo());
				}
				if (txInfo.getBlockNumber() != null) {
					fvirtualcaptualoperation.setFblocknumber(txInfo.getBlockNumber());
				}
				if (txInfo.getTime() != null) {
					fvirtualcaptualoperation.setTxTime(txInfo.getTime());
				}
				int result = this.fVirtualCapitalOperationMapper.insert(fvirtualcaptualoperation);
				if (result <= 0) {
					logger.error("Coin updateRecharge insert failed");
				}

				// 如果是usdt就记录下哪个地址有钱
				if (coinType.getShortName().equals("USDT")) {
					USDTCollect u = new USDTCollect();
					u.setAddress(address);
					u.setCreatetime(Utils.getTimestamp());
					u.setUpdatetime(Utils.getTimestamp());
					usdtCollectMapper.insert(u);
				}

			}
		}
	}

	/**
	 * 定时刷新确认数
	 * 
	 * @param coinType
	 * @throws Exception
	 */
	@Transactional(value="flexibleTransMgr", isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updateCoinCome(SystemCoinType coinType) throws Exception {
		int coinid = coinType.getId();

		String accesskey = coinType.getAccessKey();
		String secretkey = coinType.getSecrtKey();
		String ip = coinType.getIp();
		String port = coinType.getPort();

		// get CoinDriver
		CoinDriver coinDriver = new CoinDriverFactory.Builder(coinType.getCoinType(), ip, port).accessKey(accesskey)
				.secretKey(secretkey).assetId(coinType.getAssetId()).sendAccount(coinType.getEthAccount()).builder()
				.getDriver();

		if (coinDriver == null) {
			return;
		}
		List<FVirtualCapitalOperationDTO> fVirtualCapitalOperations = fVirtualCapitalOperationMapper.seletcGoing(coinid,
				coinType.getConfirmations());

		// 遍历
		for (FVirtualCapitalOperationDTO fvirtualcaptualoperation : fVirtualCapitalOperations) {
			if (fvirtualcaptualoperation == null) {
				continue;
			}
			// 确认数
			int Confirmations = 0;
			// 充值数量
			// BigDecimal Amount = BigDecimal.ZERO;
			// txid
			String[] txids = fvirtualcaptualoperation.getFuniquenumber().split("_");
			String txid = txids[0];
			if (coinType.getCoinType().equals(SystemCoinSortEnum.ETH.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.ETC.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.MOAC.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.FOD.getCode())) {
				// 获取区块高度并更新
				if (fvirtualcaptualoperation.getFblocknumber() <= 0) {
					TxInfo etcInfo = coinDriver.getTransaction(txid);
					if (etcInfo != null) {
						fvirtualcaptualoperation.setFblocknumber(etcInfo.getBlockNumber());
						if (this.fVirtualCapitalOperationMapper.updateByPrimaryKey(fvirtualcaptualoperation) <= 0) {
							logger.error("updatate key failed : " + etcInfo.getBlockNumber());
							throw new Exception();
						}
					}
					continue;
				}
				// 获取确认数,数量
				int blockNumberCreate = fvirtualcaptualoperation.getFblocknumber();
				Confirmations = coinDriver.getBestHeight() - blockNumberCreate;
				// Amount = fvirtualcaptualoperation.getFamount();
			} else if (coinType.getCoinType().equals(SystemCoinSortEnum.ETP.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.GXS.getCode())) {
				int height = coinDriver.getBestHeight();
				Confirmations = height - fvirtualcaptualoperation.getFblocknumber();
				// Amount = fvirtualcaptualoperation.getFamount();
			} else if (coinType.getCoinType().equals(SystemCoinSortEnum.BTC.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.WICC.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.USDT.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.EOS.getCode())
					) {
				// BTC处理
				TxInfo btcInfo = coinDriver.getTransaction(txid);
				if (btcInfo == null) {
					continue;
				}
				Confirmations = btcInfo.getConfirmations();
				// Amount = btcInfo.getAmount();
			} else if (coinType.getCoinType().equals(SystemCoinSortEnum.ICS.getCode())
					|| coinType.getCoinType().equals(SystemCoinSortEnum.MIC.getCode())) {
				TxInfo btcInfo = coinDriver.getTransaction(txid);
				if (btcInfo == null) {
					continue;
				}
				// 资产类型，3小企股转账，5其它资产转账
				if (!btcInfo.getType().equals(3) && !btcInfo.getType().equals(5)) {
					continue;
				}
				Confirmations = btcInfo.getConfirmations();
				// Amount = btcInfo.getAmount();
			}

			if (Confirmations > 0 && Confirmations > fvirtualcaptualoperation.getFconfirmations()) {
				// logger.info("----> 确认数 : " + Confirmations);
				// fvirtualcaptualoperation.setFamount(Amount);
				fvirtualcaptualoperation.setFconfirmations(Confirmations);
				fvirtualcaptualoperation.setFupdatetime(Utils.getTimestamp());
				// 确认状态
				if (fvirtualcaptualoperation.getFstatus() != VirtualCapitalOperationInStatusEnum.SUCCESS) {
					if (Confirmations >= coinType.getConfirmations()) {
						// 更新钱包
						UserCoinWallet userCoinWallet = userCoinWalletMapper
								.selectLock(fvirtualcaptualoperation.getFuid(), fvirtualcaptualoperation.getFcoinid());
						if (userCoinWallet == null) {
							continue;
						}
						userCoinWallet.setTotal(
								MathUtils.add(userCoinWallet.getTotal(), fvirtualcaptualoperation.getFamount()));
						if (this.userCoinWalletMapper.update(userCoinWallet) <= 0) {
							throw new Exception();
						}
						boolean isFirstRecharge = isFirstCharge(fvirtualcaptualoperation.getFuid());
						// 更新订单
						fvirtualcaptualoperation.setFstatus(VirtualCapitalOperationInStatusEnum.SUCCESS);
						if (this.fVirtualCapitalOperationMapper.updateByPrimaryKey(fvirtualcaptualoperation) <= 0) {
							throw new Exception();
						}
						BigDecimal last = jobUtils.getLastPrice(fvirtualcaptualoperation.getFcoinid());
						BigDecimal amount = MathUtils.mul(fvirtualcaptualoperation.getFamount(), last);

						scoreHelper.SendUserScore(fvirtualcaptualoperation.getFuid(), amount,
								ScoreTypeEnum.RECHARGE.getCode(),
								"充值" + coinType.getShortName() + ":" + fvirtualcaptualoperation.getFamount());
						// 首次充值奖励
						if (isFirstRecharge) {
							scoreHelper.SendUserScore(fvirtualcaptualoperation.getFuid(), BigDecimal.ZERO,
									ScoreTypeEnum.FIRSTCHARGE.getCode(),
									ScoreTypeEnum.FIRSTCHARGE.getValue().toString());
						}
						mqSend.SendUserAction(fvirtualcaptualoperation.getFagentid(),
								fvirtualcaptualoperation.getFuid(), LogUserActionEnum.COIN_RECHARGE,
								fvirtualcaptualoperation.getFcoinid(), 0, fvirtualcaptualoperation.getFamount());
						// 风控短信
						if (fvirtualcaptualoperation.getFamount().compareTo(coinType.getRiskNum()) >= 0) {
							String riskphone = jobUtils.getSystemArgs(ArgsConstant.RISKPHONE);
							String[] riskphones = riskphone.split("#");
							if (riskphones.length > 0) {
								FUser fuser = userMapper.selectByPrimaryKey(fvirtualcaptualoperation.getFuid());
								for (String string : riskphones) {
									try {
										validateHelper.smsRiskManage(fuser.getFloginname(), string,
												PlatformEnum.BC.getCode(), BusinessTypeEnum.SMS_RISKMANAGE.getCode(),
												"充值", fvirtualcaptualoperation.getFamount(), coinType.getName());
									} catch (Exception e) {
										logger.error("updateCoinCome riskphones err");
										e.printStackTrace();
									}
								}
							}
						}
					} else {
						if (this.fVirtualCapitalOperationMapper.updateByPrimaryKey(fvirtualcaptualoperation) <= 0) {
							throw new Exception();
						}
					}
				} else {
					if (this.fVirtualCapitalOperationMapper.updateByPrimaryKey(fvirtualcaptualoperation) <= 0) {
						throw new Exception();
					}
				}
			}
		}
	}

	/**
	 * 定时将维基币收集到主账号
	 * @param coinType 
	 */
	public void updateWICCCoinCollect(SystemCoinType coinType) {
		// 查询所有转账
		/*
		 * 轮询 1如果钱包没有钱就返回 2判断钱包是否已经注册 3.没注册进行注册
		 * 4.转账（转账到本地钱包的账户用sendtoaddress不需要手续费，但无法将金额完全转出，用sendtoaddresswithfee可以完全转出，
		 * 但要扣手续费，故使用sendtoaddress进行转账）
		 */
		String accesskey = coinType.getAccessKey();
		String secretkey = coinType.getSecrtKey();
		String ip = coinType.getIp();
		String port = coinType.getPort();
		String ethAccount = coinType.getEthAccount();

		// 注册手续费
		BigDecimal registfee = new BigDecimal("0.01");
		// 转账手续费
		BigDecimal transactionfee = new BigDecimal("0.0001");

		// get CoinDriver
		CoinDriver coinDriver = new CoinDriverFactory.Builder(coinType.getCoinType(), ip, port).accessKey(accesskey)
				.secretKey(secretkey).assetId(coinType.getAssetId()).sendAccount(ethAccount).builder().getDriver();

		if (coinDriver == null) {
			return;
		}
		try {
			JSONArray listaddress = coinDriver.listaddress();
			Iterator<Object> iterator = listaddress.iterator();
			while (iterator.hasNext()) {
				JSONObject next = (JSONObject) iterator.next();
				BigDecimal balance = (BigDecimal) next.get("balance");
				if (balance.compareTo(transactionfee) <= 0) {
					continue;
				}
				String addr = (String) next.get("addr");
				if (coinType.getEthAccount().equals(addr)) {
					continue;
				}
				String regid = (String) next.get("regid");
				if (StringUtils.isEmpty(regid.trim())) {
					String hash = coinDriver.registaddress(addr, registfee);
					if (StringUtils.isEmpty(hash)) {
						logger.error("地址 " + addr + " 激活失败");
						continue;
					}
					logger.info("地址 " + addr + " 激活,hash:" + hash);
					BigDecimal subtract = balance.subtract(registfee);
					if (subtract.compareTo(transactionfee) <= 0) {
						continue;
					}
					subtract = subtract.subtract(transactionfee);
					coinDriver.sendToAddress(addr, ethAccount, subtract, "", transactionfee);
				} else {
					if (balance.compareTo(transactionfee) <= 0) {
						continue;
					}
					BigDecimal subtract = balance.subtract(transactionfee);
					coinDriver.sendToAddress(addr, ethAccount, subtract, "", transactionfee);
				}
			}
		} catch (Exception e) {
			logger.error("coinService.updateWICCCoinCollect 异常", e);
		}
	}
	
	/**
	 * 定时将usdt收集到主账号
	 * @param coinType
	 */
	public void updateUSDTCoinCollect(SystemCoinType coinType,SystemCoinType coinTypebtc) {
		
		logger.info("开始收集usdt  ======>");

		// 查询还没进行收集的地址（由于交易提交后，钱包还未能查询到到账的钱，大概不到半个钟后，交易被确认了，就能查询到钱，故查询一小时前的转账地址）
		// 查询地址余额

		/*
		 * //收集 
		 * 如果成功，更新状态，结束 
		 * 如果失败，充值btc，更新表状态，等待下次收集，结束
		 */
		String accesskey = coinType.getAccessKey();
		String secretkey = coinType.getSecrtKey();
		String ip = coinType.getIp();
		String port = coinType.getPort();
		String ethAccount = coinType.getEthAccount();
		// get CoinDriver
		CoinDriver coinDriver = new CoinDriverFactory.Builder(coinType.getCoinType(), ip, port)
				.accessKey(accesskey)
				.secretKey(secretkey)
				.assetId(coinType.getAssetId())
				.sendAccount(ethAccount)
				.builder().getDriver();
		boolean issend = true;
		BigDecimal transferNeedBtc = null;
		BigDecimal riskManagement = null;
		BigDecimal minimumCollection = null;
		try {
			// 转账多少btc进行收集
			transferNeedBtc = coinDriver.estimatesmartfee(3);
			
			//风控阈值
			String systemArgs = jobUtils.getSystemArgs(ArgsConstant.USDTRiskManagement);
			if(!StringUtils.isEmpty(systemArgs)) {
				riskManagement = new BigDecimal(systemArgs);
			}else {
				riskManagement = new BigDecimal(0.0002);
			}
			//usdt最小收集数
			String systemArgs2 = jobUtils.getSystemArgs(ArgsConstant.USDTMinimumCollection);
			if(!StringUtils.isEmpty(systemArgs2)) {
				minimumCollection = new BigDecimal(systemArgs2);
			}else {
				minimumCollection = new BigDecimal(5);
			}
			if(transferNeedBtc == null) {
				return;
			}
			if(transferNeedBtc.compareTo(riskManagement) > 0) {
				issend = false;
			}
		} catch (Exception e) {
			logger.error("updateUSDTCoinCollect 异常",e);
		}
		
		
		String accesskeybtc = coinTypebtc.getAccessKey();
		String secretkeybtc = coinTypebtc.getSecrtKey();
		String ipbtc = coinTypebtc.getIp();
		String portbtc = coinTypebtc.getPort();
		String ethAccountbtc = coinTypebtc.getEthAccount();
		// get CoinDriver
		CoinDriver coinDriverbtc = new CoinDriverFactory.Builder(coinTypebtc.getCoinType(), ipbtc, portbtc)
				.accessKey(accesskeybtc)
				.secretKey(secretkeybtc)
				.assetId(coinTypebtc.getAssetId())
				.sendAccount(ethAccountbtc)
				.builder().getDriver();
		if (coinDriver == null) {
			return;
		}
		int begin = 0;
		int step = 100;
		boolean is_continue = true;

		while (is_continue) {
			try {
				//查询15分钟前未进行收集的usdt地址
				List<USDTCollect> selectUnCollectlist = usdtCollectMapper.selectUnCollectList(begin, step);
				if (selectUnCollectlist.size() <= step) {
					is_continue = false;
				}
				
				for (USDTCollect usdtCollect : selectUnCollectlist) {
					logger.info("开始收集地址："+usdtCollect.getAddress());
					String address = usdtCollect.getAddress();
					BigDecimal balance = coinDriver.getBalance(address);
					if(balance == null) {
						continue;
					}
					if(balance.compareTo(minimumCollection) <= 0) {
						usdtCollect.setUpdatetime(Utils.getTimestamp());
						usdtCollect.setStatus(USDTCollectStatusEnum.FINISHED.getCode());
						if(usdtCollectMapper.updateByPrimaryKey(usdtCollect) == 0) {
							logger.error("usdtcollect 更改状态失败，id："+ usdtCollect.getId());
						}
						continue;
					}
					logger.info("开始转账，address："+ address + "余额" + balance);
					JSONObject sendToAddress = coinDriver.sendToAddress(address, ethAccount, balance , "", BigDecimal.ZERO, 31);
					if(sendToAddress == null) {
						logger.info("usdtcollect 发送usdt失败，返回空，{id："+ usdtCollect.getId()+",address:"+address+",ethAccount"+ethAccount+",balance:"+balance+"}");
						continue;
					}
					String string = sendToAddress.getString("error");
					//发送成功
					if(StringUtils.isEmpty(string) || "null".equals(string)) {
						logger.info("address:"+address + ",转账成功");
						usdtCollect.setUpdatetime(Utils.getTimestamp());
						usdtCollect.setStatus(USDTCollectStatusEnum.FINISHED.getCode());
						if(usdtCollectMapper.updateByPrimaryKey(usdtCollect) == 0) {
							logger.error("usdtcollect 更改状态失败，id："+ usdtCollect.getId());
						}
						continue;
					}
					if(usdtCollect.getIsrechargebtc()) {
						logger.error("usdt地址已充值btc，但转账失败，id："+ usdtCollect.getId());
						continue;
					}
					if(!issend) {
						continue;
					}
					String sendToAddress2 = coinDriverbtc.sendToAddress(address, transferNeedBtc, "", new BigDecimal("0.00001"));
					if(!StringUtils.isEmpty(sendToAddress2)) {
						usdtCollect.setIsrechargebtc(true);
						usdtCollect.setRechargebtc(transferNeedBtc);
						usdtCollect.setUpdatetime(Utils.getTimestamp());
						if(usdtCollectMapper.updateByPrimaryKey(usdtCollect) == 0) {
							logger.error("usdtcollect 更改状态失败，id："+ usdtCollect.getId());
						}
					}
				}
			} catch (Exception e) {
				logger.error("updateUSDTCoinCollect异常",e);
			}
			
		}
		logger.info("usdt收集结束");
	}

	
	//修正fod地址
	//@PostConstruct
	public void updateFODAddress() {
		logger.info("开始修改用户fod地址");
		List<FUserVirtualAddressDTO> useraddressList = fUserVirtualAddressMapper.selectByCoinId(28);
		for(FUserVirtualAddressDTO f:useraddressList) {
			String address = f.getFadderess();
			if(!address.startsWith("0x")) {
				continue;
			}
			address = CoinCommentUtils.FODEncode(address);
			f.setFadderess(address);
			fUserVirtualAddressMapper.updateAdressByPrimaryKey(f);
		}
		logger.info("开始修改fod地址");
		List<FPool> poolList = poolMapper.selectByCoinId(28);
		for(FPool f:poolList) {
			String address = f.getFaddress();
			if(!address.startsWith("0x")) {
				continue;
			}
			address = CoinCommentUtils.FODEncode(address);
			f.setFaddress(address);
			poolMapper.updateAdressByPrimaryKey(f);
		}
		logger.info("修改fod地址结束");
	}
	
	
	
	/**
	 * 首次充值判断
	 * 
	 * @param fuid
	 * @return
	 */
	public boolean isFirstCharge(int fuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fuid", fuid);
		map.put("finouttype", CapitalOperationInOutTypeEnum.IN.getCode());
		map.put("fstatus", CapitalOperationInStatus.Come);
		int countCny = fWalletCapitalOperationMapper.countWalletCapitalOperation(map);
		if (countCny > 0) {
			return false;
		}

		map.clear();
		map.put("fuid", fuid);
		map.put("ftype", VirtualCapitalOperationTypeEnum.COIN_IN.getCode());
		map.put("fstatus", VirtualCapitalOperationInStatusEnum.SUCCESS);
		int countCoin = fVirtualCapitalOperationMapper.countVirtualCapitalOperation(map);

		return (countCny + countCoin) <= 0;
	}

}
