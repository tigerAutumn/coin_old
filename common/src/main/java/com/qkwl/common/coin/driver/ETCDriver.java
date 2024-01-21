package com.qkwl.common.coin.driver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.coin.CoinUtils;
import com.qkwl.common.coin.TxInfo;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.util.ReturnResult;

public class ETCDriver implements CoinDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(ETCDriver.class);

    private CoinUtils coinUtils = null;
    private String passWord = null;
    private Integer coinSort = null;
    private String sendAccount = null;
    private String contractAccount = null;
    private int contractWei = 0;

    public ETCDriver(String ip, String port, String pass, Integer coinSort, String sendAccount, String contractAccount, int contractWei) {
        coinUtils = new CoinUtils(ip, port);
        this.passWord = pass;
        this.coinSort = coinSort;
        this.sendAccount = sendAccount;
        this.contractAccount = contractAccount;
        this.contractWei = contractWei;
    }

    @Override
    public Integer getCoinSort() {
        return this.coinSort;
    }

    @Override
    public Integer getBestHeight() {
        JSONObject resultJson = coinUtils.goETC("eth_blockNumber", "[]");
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        String blockNumberStr = CoinUtils.BigHexToString(resultJson.get("result").toString());
        return Integer.parseInt(blockNumberStr);
    }

    @Override
    public BigDecimal getBalance() {
        if (contractAccount != null && contractAccount.length() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("from", sendAccount);
            jsonObject.put("to", contractAccount);
            jsonObject.put("value", "0x0");
            jsonObject.put("gas", "0xea60");
            jsonObject.put("gasPrice", "0x5e93d9401");
            String data = "0x70a08231000000000000000000000000" + StringUtils.difference("0x", sendAccount);
            jsonObject.put("data", data);
            JSONObject resultJson = coinUtils.goETC("eth_call", methodStrJson(jsonObject.toString(), "latest"));
            String result = resultJson.get("result").toString();
            if (result.equals("null")) {
                return null;
            }
            String balance = CoinUtils.ETHBalanceHexToStr(result, contractWei);
            return new BigDecimal(balance);
        } else {
            JSONObject resultJson = coinUtils.goETC("eth_getBalance", methodStr(sendAccount, "latest"));
            String result = resultJson.get("result").toString();
            if (result.equals("null")) {
                return null;
            }
            String balance = CoinUtils.ETHBalanceHexToStr(result);
            return new BigDecimal(balance);
        }
    }

    @Override
    public String getNewAddress(String uId) {
        JSONObject resultJson = coinUtils.goETC("personal_newAccount", methodStr(passWord));
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        return result;
    }

    @Override
    public String sendToAddress(String to, String amount, String nonce) {
        JSONObject jsonObject = new JSONObject();
        if (contractAccount != null && contractAccount.length() > 0) {
            jsonObject.put("from", sendAccount);
            jsonObject.put("to", contractAccount);
            jsonObject.put("value", "0x0");
            jsonObject.put("gas", "0xea60");
            jsonObject.put("gasPrice", "0x5e93d9401");
            String data = "0xa9059cbb000000000000000000000000" + StringUtils.difference("0x", to) + CoinUtils.CntractToETHBalanceHex(amount, contractWei);
            jsonObject.put("data", data);
        } else {
            jsonObject.put("from", sendAccount);
            jsonObject.put("to", to);
            jsonObject.put("value", CoinUtils.ToETHBalanceHex(amount));
            jsonObject.put("gas", "0x15f90");
            jsonObject.put("gasPrice", "0x4a817c800");
            jsonObject.put("nonce", nonce);
        }
        JSONObject resultJson = coinUtils.goETC("personal_sendTransaction", methodStrJson(jsonObject.toString(), this.passWord));
        logger.info("ETC.sendToAddress resultJson:"+resultJson.toString());
        String result = null;
        try {
        	result = resultJson.getString("result");
            if (result == null || result.equals("null")) {
                System.out.println("ETC sendToAddress error --->"+resultJson.toString());
                return null;
            }
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ETC sendToAddress error --->"+resultJson.toString());
            return null;
		}
        
        return result;
    }
    
	@Override
	public String sendToAddressAccelerate(String to, String amount, String nonce,String gasPrice) {
		 JSONObject jsonObject = new JSONObject();
	        if (contractAccount != null && contractAccount.length() > 0) {
	            jsonObject.put("from", sendAccount);
	            jsonObject.put("to", contractAccount);
	            jsonObject.put("value", "0x0");
	            jsonObject.put("gas", "0xea60");
	            jsonObject.put("gasPrice", "0x5e93d9401");
	            String data = "0xa9059cbb000000000000000000000000" + StringUtils.difference("0x", to) + CoinUtils.CntractToETHBalanceHex(amount, contractWei);
	            jsonObject.put("data", data);
	        } else {
	            jsonObject.put("from", sendAccount);
	            jsonObject.put("to", to);
	            jsonObject.put("value", CoinUtils.ToETHBalanceHex(amount));
	            jsonObject.put("gas", "0x15f90");
	            //jsonObject.put("gasPrice", "0x4a817c800");
	            //jsonObject.put("gasPrice", "0x54310A200");
	            jsonObject.put("gasPrice", gasPrice);
	            jsonObject.put("nonce", nonce);
	        }
	        JSONObject resultJson = coinUtils.goETC("personal_sendTransaction", methodStrJson(jsonObject.toString(), this.passWord));
	        String result = null;
	        try {
	        	result = resultJson.getString("result");
	            if (result.equals("null") || result == null) {
	                System.out.println("ETC sendToAddress error --->"+resultJson.toString());
	                return null;
	            }
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ETC sendToAddress error --->"+resultJson.toString());
	            return null;
			}
	        
	        return result;
	}
	
    
    @Override
    public boolean walletLock() {
        return true;
    }

    @Override
    public boolean walletPassPhrase(int times) {
        return true;
    }

    @Override
    public boolean setTxFee(BigDecimal fee) {
        return true;
    }

    @Override
    public List<TxInfo> listTransactions(int count, int from) {
        return null;
    }

    @Override
    public TxInfo getTransaction(String txId) {
        JSONObject json = coinUtils.goETC("eth_getTransactionByHash", methodStr(txId));
        String result = json.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        JSONObject resultJson = json.getJSONObject("result");
        String blockNumberStr = CoinUtils.BigHexToString(resultJson.get("blockNumber").toString());
        String amount = CoinUtils.ETHBalanceHexToStr(resultJson.get("value").toString());
        TxInfo txinfo = new TxInfo();
        txinfo.setFrom(resultJson.get("from").toString());
        txinfo.setTo(resultJson.get("to").toString());
        txinfo.setBlockNumber(Integer.parseInt(blockNumberStr));
        txinfo.setAmount(new BigDecimal(amount));
        return txinfo;
    }

    @Override
    public String sendToAddress(String address, BigDecimal account, String comment, BigDecimal fee) {
        return null;
    }

    @Override
    public String sendToAddress(String address, BigDecimal amount, String comment, BigDecimal fee, String memo) {
        return null;
    }

    @Override
    public String getETCSHA3(String str) {
        JSONObject resultJson = coinUtils.goETC("web3_sha3", methodStr(str));
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        return result;
    }

    @Override
    public Integer getTransactionCount() {
        JSONObject resultJson = coinUtils.goETC("eth_getTransactionCount", methodStr(sendAccount, "latest"));
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        BigInteger hexBalanceTmp = new BigInteger(result.substring(2), 16);
        return hexBalanceTmp.intValue();
    }

    private static String methodStr(String... params) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            if (i == 0) {
                strBuffer.append("[\"" + params[i] + "\"");
            } else {
                strBuffer.append(",\"" + params[i] + "\"");
            }
            if (i == params.length - 1) {
                strBuffer.append("]");
            }
        }
        return strBuffer.toString();
    }

    private static String methodStrJson(String... params) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            if (i == 0) {
                strBuffer.append("[" + params[i] + "");
            } else {
                strBuffer.append(",\"" + params[i] + "\"");
            }
            if (i == params.length - 1) {
                strBuffer.append("]");
            }
        }
        return strBuffer.toString();
    }

	@Override
	public String getTransactionByHash(String transactionHash) {
		JSONObject resultJson = coinUtils.goETC("eth_getTransactionByHash", methodStrJson(transactionHash));
        String result = null;
        if(resultJson.containsKey("result")) {
        	result = resultJson.getString("result");
            if (result.equals("null") || result == null) {
            	logger.error("ETCDriver.getTransactionByHash执行异常,transactionHash:["+transactionHash+"],resultJson:"+resultJson.toString());
            }
        }else if(resultJson.containsKey("error")) {
        	logger.error("ETCDriver.getTransactionByHash执行异常,transactionHash:["+transactionHash+"],resultJson:"+resultJson.toString());
        }
        return result;
	}

	@Override
	public String sendToAddress(String from, String to, BigDecimal amount, String comment, BigDecimal fee) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray listaddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String registaddress(String addr,BigDecimal fee) {
		// TODO Auto-generated method stub
		return null;
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
