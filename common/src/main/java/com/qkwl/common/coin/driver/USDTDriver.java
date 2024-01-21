package com.qkwl.common.coin.driver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.coin.CoinUtils;
import com.qkwl.common.coin.TxInfo;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.util.ReturnResult;

/**
 * WICCDriver
 *
 * @author hwj
 */
public class USDTDriver implements CoinDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(USDTDriver.class);

    private CoinUtils coinUtils = null;
    private String passWord = null;
    private Integer coinSort = null;
    private String sendAccount = null;
    

    public USDTDriver(String accessKey, String secretKey, String ip, String port, String pass, Integer coinSort,String sendAccount) {
        coinUtils = new CoinUtils(accessKey, secretKey, ip, port);
        this.passWord = pass;
        this.coinSort = coinSort;
        this.sendAccount = sendAccount;
    }

    @Override
    public BigDecimal getBalance() {
        JSONObject resultJson = coinUtils.goUSDT("omni_getbalance", "[\""+ sendAccount +"\",31]");
        if(resultJson == null || !resultJson.containsKey("result") ) {
        	return null;
        }
        if(!resultJson.containsKey("result")) {
        	logger.error("USDTDriver.getBalance 错误"+resultJson.toString());
        	return null;
        }
        JSONObject object = (JSONObject) resultJson.get("result");
        if(object == null || !object.containsKey("balance")) {
        	logger.error("USDTDriver.getBalance 错误"+resultJson.toString());
        	return null;
        }
        String result = object.get("balance").toString();
        return new BigDecimal(result);
    }
    
    
    @Override
	public BigDecimal getBalance(String address) {
    	JSONObject resultJson = coinUtils.goUSDT("omni_getbalance", "[\""+ address +"\",31]");
        if(resultJson == null || !resultJson.containsKey("result") ) {
        	return null;
        }
        if(!resultJson.containsKey("result")) {
        	logger.error("USDTDriver.getBalance 错误"+resultJson.toString());
        	return null;
        }
        JSONObject object = (JSONObject) resultJson.get("result");
        if(object == null || !object.containsKey("balance")) {
        	logger.error("USDTDriver.getBalance 错误"+resultJson.toString());
        	return null;
        }
        String result = object.get("balance").toString();
        return new BigDecimal(result);
	}


    @Override
    public String getNewAddress(String time) {
        JSONObject resultJson = coinUtils.goUSDT("getnewaddress", "[\"" + time + "\"]");
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        return result;
    }

    @Override
    public boolean walletLock() {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        coinUtils.goUSDT("walletlock", "[]");
        return true;
    }

    @Override
    public boolean walletPassPhrase(int times) {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        coinUtils.goUSDT("walletpassphrase", "[\"" + passWord + "\"," + times + "]");
        return true;
    }

    @Override
    public boolean setTxFee(BigDecimal fee) {
        JSONObject resultJson = coinUtils.goUSDT("settxfee", "[" + MathUtils.decimalFormat(fee) + "]");
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public List<TxInfo> listTransactions(int count, int from) {
    	int i = count + from;
        JSONObject resultJson = coinUtils.goUSDT("omni_listtransactions", "[\"*\"," + i + "]");
        String result = resultJson.get("result").toString();
        if ("null".equals(result)) {
            return null;
        }
        List<TxInfo> txInfos = new ArrayList<TxInfo>();
        JSONArray jsonArray = JSONArray.parseArray(result);
        
        for(int j = from ; j < jsonArray.size() ; j++) {
        	
      /*  }
        for (Object object : jsonArray) {*/
        	
            JSONObject txObject = JSON.parseObject(jsonArray.getString(j));
            
            if(txObject.getInteger("propertyid") != 31) {
            	continue;
            }
            String referenceaddress = txObject.getString("referenceaddress");
            if(referenceaddress.equals(sendAccount) || txObject.getString("sendingaddress").equals(sendAccount)) {
            	continue;
            }
            TxInfo txInfo = new TxInfo();
            txInfo.setTxid(txObject.get("txid").toString());
            txInfo.setAddress(referenceaddress);
            txInfo.setAmount(new BigDecimal(txObject.get("amount").toString()));
            txInfo.setCategory("receive");
            if(txObject.containsKey("blocktime")) {
                long time = Long.parseLong(txObject.get("blocktime").toString() + "000");
                txInfo.setTime(new Date(time));
            }
            if (txObject.get("confirmations") != null && txObject.get("confirmations").toString().trim().length() > 0) {
                txInfo.setConfirmations(Integer.parseInt(txObject.get("confirmations").toString()));
            } else {
                txInfo.setConfirmations(0);
            }
            txInfos.add(txInfo);
        }
        Collections.reverse(txInfos);
        return txInfos;
    }

    
	/**
	 * 获取交易详情 
	 * @param txId
	 * @return
	 */
    @Override
    public TxInfo getTransaction(String txId) {
        JSONObject json = coinUtils.goUSDT("omni_gettransaction", "[\"" + txId + "\"]");
        String result = json.get("result").toString();
        if ("null".equals(result)) {
            return null;
        }
        JSONObject resultJson = json.getJSONObject("result");
        TxInfo txInfo = new TxInfo();
        txInfo.setAddress(resultJson.get("referenceaddress").toString());
        txInfo.setAmount(new BigDecimal(resultJson.get("amount").toString()));
        if (resultJson.get("confirmations") != null && resultJson.get("confirmations").toString().trim().length() > 0) {
            txInfo.setConfirmations(Integer.parseInt(resultJson.get("confirmations").toString()));
        } else {
            txInfo.setConfirmations(0);
        }
        
        if(resultJson.containsKey("blocktime")) {
            long time = Long.parseLong(resultJson.get("blocktime").toString() + "000");
            txInfo.setTime(new Date(time));
        }
        txInfo.setTxid(resultJson.get("txid").toString());
        return txInfo;
    }

    @Override
    public String sendToAddress(String to, BigDecimal amount, String comment, BigDecimal fee) {
        JSONObject resultJson = null;
        resultJson = coinUtils.goUSDT("omni_send", "[\""+sendAccount+"\",\"" + to + "\",31,\"" + amount.longValue() + "\"]");
        logger.info("USDT.sendToAddress resultJson:"+resultJson.toString());
        if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(new BigDecimal(0.0001))) {
            // 设置失败也无所谓
        }
        try {
	        if(resultJson.containsKey("result")) {
	        	String resultobject = resultJson.get("result").toString();
	        	if("null".equals(resultobject)) {
	        		logger.error("USDT.sendToAddress错误{to："+to+",amount:"+ amount +",fee:"+fee+"},返回："+resultJson.toString());
	                return null;
	        	}
	        	return resultobject;
	        }else {
	        	logger.error("USDT.sendToAddress错误{to："+to+",amount:"+ amount +",fee:"+fee+"},返回："+resultJson.toString());
	            return null;
	        }
        } catch (Exception e) {
			logger.error("USDT.sendToAddress异常{to："+to+",amount:"+ amount +",fee:"+fee+"},返回："+resultJson.toString(),e);
			return null;
		}

    }
    
    @Override
	public String sendToAddress(String from, String to, BigDecimal amount, String comment, BigDecimal fee) {
		return null;
	}
    
	@Override
	public JSONObject sendToAddress(String from, String to, BigDecimal amount, String comment, BigDecimal fee,
			Integer propertyid) {
/*		if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(fee)) {
	        return null;
	    }*/
		JSONObject resultJson = null;
		try {
			//walletPassPhrase(30);
	        resultJson = coinUtils.goUSDT("omni_send", "[\""+from+"\",\"" + to + "\",31,\"" + amount + "\"]");
	        //walletLock();
	        logger.info("usdt.omni_send resultJson:"+resultJson.toString());

	        if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(new BigDecimal(0.0001))) {
	            // 设置失败也无所谓
	        }
	        return resultJson;
		} catch (Exception e) {
			logger.error("USDT.sendToAddress异常{to："+to+",amount:"+ amount +",fee:"+fee+"},返回："+resultJson,e);
		}
		return resultJson;
	}
    
	@Override
	public BigDecimal estimatesmartfee(Integer time) {
		JSONObject json = coinUtils.goUSDT("estimatesmartfee", "[" + time + "]");
		if(!json.containsKey("result")) {
			logger.error("USDT.estimatesmartfee错误"+json.toJSONString());
			return null;
		}
		JSONObject jsonObject = json.getJSONObject("result");
		if(jsonObject == null || "null".equals(jsonObject.toString())) {
			logger.error("USDT.estimatesmartfee错误"+json.toJSONString());
			return null;
		}
		if(jsonObject.containsKey("feerate")) {
			return jsonObject.getBigDecimal("feerate");
		}
		return null;
	}

    @Override
    public String sendToAddress(String address, BigDecimal amount, String comment, BigDecimal fee, String memo) {
        return null;
    }

    @Override
    public String sendToAddress(String to, String amount, String nonce) {
        return null;
    }

    @Override
    public String getETCSHA3(String str) {
        return null;
    }

    @Override
    public Integer getCoinSort() {
        return this.coinSort;
    }

    @Override
    public Integer getBestHeight() { return null; }

    @Override
    public Integer getTransactionCount() {
        return null;
    }

	@Override
	public String sendToAddressAccelerate(String to, String amount, String nonce,String gasPrice) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTransactionByHash(String transactionHash) {
		return null;
	}

	

	@Override
	public JSONArray listaddress() {
		return null;
	}

	@Override
	public String registaddress(String addr,BigDecimal fee) {
		return null;
	}
	
	
	public static void main(String[] args) {
		BigDecimal bigDecimal = new BigDecimal("1000.00");
		System.out.println(bigDecimal);
	}

	@Override
	public ReturnResult sendToAddress(String to, String amount, String nonce, String gasPrice, String gas){
		// TODO Auto-generated method stub
		return null;
	}



	

	
}
