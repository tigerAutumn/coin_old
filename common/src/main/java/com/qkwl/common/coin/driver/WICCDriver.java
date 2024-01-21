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
public class WICCDriver implements CoinDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(WICCDriver.class);

    private CoinUtils coinUtils = null;
    private String passWord = null;
    private Integer coinSort = null;
    private String sendAccount = null;
    
    //转账手续费
    private BigDecimal transactionfee = new BigDecimal("0.0001"); 

    public WICCDriver(String accessKey, String secretKey, String ip, String port, String pass, Integer coinSort,String sendAccount) {
        coinUtils = new CoinUtils(accessKey, secretKey, ip, port);
        this.passWord = pass;
        this.coinSort = coinSort;
        this.sendAccount = sendAccount;
    }

    @Override
    public BigDecimal getBalance() {
        JSONObject resultJson = coinUtils.goWICC("getbalance", "[]");
        if(resultJson == null || !resultJson.containsKey("result") ) {
        	return null;
        }
        if(!resultJson.containsKey("result")) {
        	logger.error("getBalance 错误"+resultJson.toString());
        	return null;
        }
        JSONObject object = (JSONObject) resultJson.get("result");
        String result = object.toString();
        if (result.equals("null")) {
            return null;
        }
        if(object.containsKey("balance")) {
        	result = object.get("balance").toString();
        }
        return new BigDecimal(result);
    }

    @Override
    public String getNewAddress(String time) {
        JSONObject resultJson = coinUtils.goWICC("getnewaddress", "[]");
        if(resultJson == null || !resultJson.containsKey("result") ) {
        	return null;
        }
        JSONObject object = (JSONObject) resultJson.get("result");
        if(!object.containsKey("addr")) {
        	return null;
        }
        String result = object.get("addr").toString();
        if (result == null || "null".equals(result)) {
            return null;
        }
        return result;
    }

    @Override
    public boolean walletLock() {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        coinUtils.goWICC("walletlock", "[]");
        return true;
    }

    @Override
    public boolean walletPassPhrase(int times) {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        coinUtils.goWICC("walletpassphrase", "[\"" + passWord + "\"," + times + "]");
        return true;
    }

    @Override
    public boolean setTxFee(BigDecimal fee) {
    	//fee = fee.multiply(new BigDecimal(100000000));
        JSONObject resultJson = coinUtils.goWICC("settxfee", "[" + MathUtils.decimalFormat(fee) + "]");
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public List<TxInfo> listTransactions(int count, int from) {
        JSONObject resultJson = coinUtils.goWICC("listtransactions", "[\"*\"," + count + "," + from + "]");
        String result = resultJson.get("result").toString();
        if ("null".equals(result)) {
            return null;
        }
        List<TxInfo> txInfos = new ArrayList<TxInfo>();
        JSONArray jsonArray = JSONArray.parseArray(result);
        for (Object object : jsonArray) {
            JSONObject txObject = JSON.parseObject(object.toString());
            if (!"receive".equals(txObject.get("category").toString())) {
                continue;
            }
            if(sendAccount.equals(txObject.get("address").toString())) {
            	continue;
            }
            TxInfo txInfo = new TxInfo();
            //txInfo.setAccount(txObject.get("account").toString());
            txInfo.setAddress(txObject.get("address").toString());
            txInfo.setAmount(new BigDecimal(txObject.get("amount").toString()));
            txInfo.setCategory(txObject.get("category").toString());
            if (txObject.get("confirmations") != null && txObject.get("confirmations").toString().trim().length() > 0) {
                txInfo.setConfirmations(Integer.parseInt(txObject.get("confirmations").toString()));
            } else {
                txInfo.setConfirmations(0);
            }
            //txInfo.setVout(txObject.getInteger("vout"));
            long time = Long.parseLong(txObject.get("blocktime").toString() + "000");
            txInfo.setTime(new Date(time));
            txInfo.setTxid(txObject.get("txid").toString());
            txInfos.add(txInfo);
        }
        Collections.reverse(txInfos);
        return txInfos;
    }

    @Override
    public TxInfo getTransaction(String txId) {
        JSONObject json = coinUtils.goWICC("gettransaction", "[\"" + txId + "\"]");
        String result = json.get("result").toString();
        if ("null".equals(result)) {
            return null;
        }
        JSONObject resultJson = json.getJSONObject("result");
        JSONArray detailsArray = JSONArray.parseArray(resultJson.get("details").toString());
        TxInfo txInfo = new TxInfo();
        for (Object object : detailsArray) {
            JSONObject detailsObject = JSON.parseObject(object.toString());
            if (!detailsObject.get("category").toString().equals("receive")) {
                continue;
            }
            //txInfo.setAccount(detailsObject.get("account").toString());
            txInfo.setAddress(detailsObject.get("address").toString());
            txInfo.setAmount(new BigDecimal(detailsObject.get("amount").toString()));
            break;
        }
        txInfo.setCategory("receive");
        if (resultJson.get("confirmations") != null && resultJson.get("confirmations").toString().trim().length() > 0) {
            txInfo.setConfirmations(Integer.parseInt(resultJson.get("confirmations").toString()));
        } else {
            txInfo.setConfirmations(0);
        }
        long time = Long.parseLong(resultJson.get("blocktime").toString());
        txInfo.setTime(new Date(time));
        txInfo.setTxid(resultJson.get("txid").toString());
        return txInfo;
    }


   /* @Override
    public List<TxInfo> listTransactions(int count, int from) {
        JSONObject resultJson = coinUtils.goWICC("getalltxinfo", "["+from+"]");
        String result = null; 
        JSONObject UnConfirmTx = (JSONObject) resultJson.get("result");
        if(UnConfirmTx.containsKey("Confirmed")) {
        	result = UnConfirmTx.get("Confirmed").toString();
        }else {
        	return null;
        }
        if (result.equals("null") || result.equals("[]")) {
            return null;
        }
        List<TxInfo> txInfos = new ArrayList<TxInfo>();
        JSONArray jsonArray = JSONArray.parseArray(result);
        for (Object object : jsonArray) {
        	JSONObject txdetail = coinUtils.goWICC("gettxdetail", "[\""+object.toString()+"\"]");
        	JSONObject txObject = (JSONObject) txdetail.get("result");
        	if (txObject.equals("null")) {
        		continue;
            }
            JSONObject txObject = JSON.parseObject(object.toString());
        	if (!txObject.get("category").toString().equals("receive")) {
                continue;
            }
            TxInfo txInfo = new TxInfo();
            //txInfo.setAccount(txObject.get("addr").toString());
            txInfo.setAddress(txObject.get("addr").toString());
            txInfo.setAmount(new BigDecimal(txObject.get("money").toString()).divide(new BigDecimal(100000000)));
            //txInfo.setCategory(txObject.get("category").toString());
            if (txObject.get("confirmations") != null && txObject.get("confirmations").toString().trim().length() > 0) {
                txInfo.setConfirmations(Integer.parseInt(txObject.get("confirmations").toString()));
            } else {
                txInfo.setConfirmations(0);
            }
            //txInfo.setVout(txObject.getInteger("vout"));
            long time = Long.parseLong(txObject.get("confirmedtime").toString());
            txInfo.setTime(new Date(time));
            txInfo.setTxid(txObject.get("hash").toString());
            txInfos.add(txInfo);
        }
        Collections.reverse(txInfos);
        return txInfos;
    }*/

    /*@Override
    public TxInfo getTransaction(String txId) {
        JSONObject json = coinUtils.goWICC("gettxdetail", "[\"" + txId + "\"]");
        String result = json.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        JSONObject resultJson = json.getJSONObject("result");
        JSONArray detailsArray = JSONArray.parseArray(resultJson.get("details").toString());
        TxInfo txInfo = new TxInfo();
        for (Object object : detailsArray) {
            JSONObject detailsObject = JSON.parseObject(object.toString());
            if (!detailsObject.get("category").toString().equals("receive")) {
                continue;
            }
            txInfo.setAccount(detailsObject.get("account").toString());
            txInfo.setAddress(detailsObject.get("address").toString());
            txInfo.setAmount(new BigDecimal(detailsObject.get("amount").toString()));
            break;
        }
        txInfo.setCategory("receive");
        if (resultJson.get("confirmations") != null && resultJson.get("confirmations").toString().trim().length() > 0) {
            txInfo.setConfirmations(Integer.parseInt(resultJson.get("confirmations").toString()));
        } else {
            txInfo.setConfirmations(0);
        }
        long time = Long.parseLong(resultJson.get("time").toString());
        txInfo.setTime(new Date(time));
        txInfo.setTxid(resultJson.get("txid").toString());
        return txInfo;
    }*/

    @Override
    public String sendToAddress(String to, BigDecimal amount, String comment, BigDecimal fee) {
        if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(fee)) {
            return null;
        }
        walletPassPhrase(30);
        //扣除转账手续费
        //amount = amount.subtract(transactionfee);
        fee = fee.multiply(new BigDecimal(100000000));
        BigDecimal multiply = amount.multiply(new BigDecimal(100000000));
        multiply.setScale(0,BigDecimal.ROUND_HALF_UP);
        JSONObject resultJson = null;
        if(BigDecimal.ZERO.compareTo(fee) == 0) {
        	resultJson = coinUtils.goWICC("sendtoaddresswithfee", "[\""+sendAccount+"\",\"" + to + "\"," + multiply.longValue() + ","+ fee.longValue() +"]");
        }else {
        	resultJson = coinUtils.goWICC("sendtoaddress", "[\""+sendAccount+"\",\"" + to + "\"," + multiply.longValue() + "]");
        }
        logger.info("WICC.sendToAddress resultJson:"+resultJson.toString());
        walletLock();
        if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(new BigDecimal(0.0001))) {
            // 设置失败也无所谓
        }
        if(resultJson.containsKey("result")) {
        	JSONObject resultobject = (JSONObject) resultJson.get("result");
            if (resultobject == null || !resultobject.containsKey("hash")) {
            	System.out.println("WICC sendToAddress error --->"+resultJson.toString());
                return null;
            }else{
            	return resultobject.getString("hash");
            }
        }else {
        	System.out.println("WICC sendToAddress error --->"+resultJson.toString());
            return null;
        }

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
	public String sendToAddress(String from, String to, BigDecimal amount, String comment, BigDecimal fee) {
		
		if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(fee)) {
	        return null;
	    }
		JSONObject resultJson = null;
		try {
			walletPassPhrase(30);
	        amount = amount.multiply(new BigDecimal(100000000));
	        amount.setScale(0,BigDecimal.ROUND_HALF_UP);
	        fee = fee.multiply(new BigDecimal(100000000));
	        fee.setScale(0,BigDecimal.ROUND_HALF_UP);
	        resultJson = coinUtils.goWICC("sendtoaddress", "[\""+from+"\",\"" + to + "\"," + amount.longValue() + "]");
	        walletLock();

	        if (fee.compareTo(BigDecimal.ZERO) == 1 && !setTxFee(new BigDecimal(0.0001))) {
	            // 设置失败也无所谓
	        }

	        String result = resultJson.getString("result");
	        if (result == null || "null".equals(result) ) {
	        	logger.error("WICCDriver.sendToAddress 错误{from:"+from+",to:"+to+",amount:"+amount+"},retturn:"+resultJson.toString());
	            return null;
	        }
	        return result;
		} catch (Exception e) {
			 logger.error("WICCDriver.sendToAddress 异常{from:"+from+",to:"+to+",amount:"+amount+"}",e);
		}
		return null;
	       
	}

	@Override
	public JSONArray listaddress() {
		JSONObject resultJson = coinUtils.goWICC("listaddr", "[]");
		JSONArray result = (JSONArray) resultJson.get("result");
		return result;
	}

	@Override
	public String registaddress(String addr,BigDecimal fee) {
		BigDecimal multiply = fee.multiply(new BigDecimal(100000000));
		multiply.setScale(0,BigDecimal.ROUND_HALF_UP);
		JSONObject resultJson = coinUtils.goWICC("registaccounttx", "[\""+addr+"\","+multiply.longValue()+"]");
		
		JSONObject result = (JSONObject) resultJson.get("result");
		String string = null;
		if(result.containsValue("hash")) {
			string = result.get("hash").toString();
		}
		return string;
	}
	
	
	public static void main(String[] args) {
		BigDecimal bigDecimal = new BigDecimal("1000.00");
		System.out.println(bigDecimal);
	}

	@Override
	public JSONObject sendToAddress(String from, String to, BigDecimal amount, String comment, BigDecimal fee,
			Integer propertyid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getBalance(String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal estimatesmartfee(Integer time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnResult sendToAddress(String to, String amount, String nonce, String gasPrice, String gas){
		// TODO Auto-generated method stub
		return null;
	}
	
}
