package com.qkwl.common.coin;

import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.util.ReturnResult;

/**
 * 虚拟币驱动
 * @author jany
 */
public interface CoinDriver {
	
	/**
	 * 获取分类
	 * @return
	 */
	Integer getCoinSort();

	/**
	 * 获取高度
	 * @return
	 */
	Integer getBestHeight();
	
	/**
	 * 获取余额
	 * @return
	 */
	BigDecimal getBalance();
	
	/**
	 * 获取地址余额
	 * @return
	 */
	BigDecimal getBalance(String address);
	
	/**
	 * 生成地址
	 * @param uId
	 * @return
	 */
	String getNewAddress(String time);
	
	/**
	 * 钱包加锁
	 */
	boolean walletLock();
	
	/**
	 * 钱包解锁
	 * @param times
	 */
	boolean walletPassPhrase(int times);
	
	/**
	 * 设置手续费
	 * @param fee
	 * @return
	 */
	boolean setTxFee(BigDecimal fee);
	
	/**
	 * 获取交易列表
	 * @param count
	 * @param from
	 * @return
	 */
	List<TxInfo> listTransactions(int count, int from);
	
	/**
	 * 获取交易详情 
	 * @param txId
	 * @return
	 */
	TxInfo getTransaction(String txId);
	
	/**
	 * 发送
	 * @param address
	 * @param account
	 * @param comment
	 * @param fee
	 * @return
	 */
	String sendToAddress(String to, BigDecimal amount, String comment, BigDecimal fee);
	
	/**
	 * 发送
	 * @param from
	 * @param to
	 * @param amount
	 * @param comment
	 * @param fee
	 * @return
	 */
	String sendToAddress(String from ,String to, BigDecimal amount, String comment, BigDecimal fee);
	
	
	/**
	 * 发送
	 * @param from
	 * @param to
	 * @param amount
	 * @param comment
	 * @param fee
	 * @param propertyid
	 * @return
	 */
	JSONObject sendToAddress(String from ,String to, BigDecimal amount, String comment, BigDecimal fee,Integer propertyid);

	/**
	 * 公信宝发送
	 * @param address
	 * @param amount
	 * @param comment
	 * @param fee
	 * @param memo
	 * @return
	 */
	String sendToAddress(String address, BigDecimal amount, String comment, BigDecimal fee, String memo);
	
	/**
	 * ETC或ETH发送
	 * @param from
	 * @param to
	 * @param amount
	 * @param nonce
	 * @return
	 */
	String sendToAddress(String to, String amount, String nonce);
	
	/**
	 * ETH发送
	 * @param from
	 * @param to
	 * @param amount
	 * @param nonce
	 * @return
	 * @throws BCException 
	 */
	ReturnResult sendToAddress(String to, String amount, String nonce,String gasPrice,String gas);
	
	
	/**
	 * ETH加速
	 * @param from
	 * @param to
	 * @param amount
	 * @param nonce
	 * @return
	 */
	String sendToAddressAccelerate(String to, String amount, String nonce,String gasPrice);
	
	
	
	
	/**
	 * ETH获取nonce
	 * @return
	 */
	Integer getTransactionCount();
	/**
	 * ETCSHA3签名
	 * @param str
	 * @return
	 */
	String getETCSHA3(String str);

	/**
	 * 获取eth或etc交易
	 * @param transactionHash
	 * @return
	 */
	String getTransactionByHash(String transactionHash);
	
	/**
	 * 获取所有钱包
	 * @return
	 */
	JSONArray listaddress();
	
	/**
	 * 注册地址
	 * @param addr
	 * @param fee 手续费
	 * @return
	 */
	String registaddress(String addr,BigDecimal fee);
	
	
	/**
	 * 获取最合适的手续费
	 * @param time 预计多少小时到账
	 * @return
	 */
	BigDecimal estimatesmartfee(Integer time);
	
}
