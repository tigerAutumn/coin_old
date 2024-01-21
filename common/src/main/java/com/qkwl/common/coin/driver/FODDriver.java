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
import com.qkwl.common.util.CoinCommentUtils;
import com.qkwl.common.util.ReturnResult;

public class FODDriver implements CoinDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(FODDriver.class);

    private CoinUtils coinUtils = null;
    private String passWord = null;
    private Integer coinSort = null;
    private String sendAccount = null;
    private String contractAccount = null;
    private int contractWei = 0;
    

    public FODDriver(String ip, String port, String pass, Integer coinSort, String sendAccount, String contractAccount, int contractWei) {
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
        JSONObject resultJson = coinUtils.goFOD("eth_blockNumber", "[]");
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
            JSONObject resultJson = coinUtils.goFOD("eth_call", methodStrJson(jsonObject.toString(), "latest"));
            String result = resultJson.get("result").toString();
            if (result.equals("null")) {
                return null;
            }
            String balance = CoinUtils.ETHBalanceHexToStr(result, contractWei);
            return new BigDecimal(balance);
        } else {
            JSONObject resultJson = coinUtils.goFOD("eth_getBalance", methodStr(sendAccount, "latest"));
            System.out.println(resultJson.toString());
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
        JSONObject resultJson = coinUtils.goFOD("personal_newAccount", methodStr(passWord));
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        return result;
    }

    @Override
    public String sendToAddress(String to, String amount, String nonce) {
        return null;
    }
    
    
    @Override
    public String sendToAddressAccelerate(String to, String amount, String nonce,String gasPrice) {
    	return null;
    }
    
    @Override
    public String getTransactionByHash(String transactionHash) {
        /*JSONObject resultJson = coinUtils.goFOD("eth_getTransactionByHash", methodStrJson(transactionHash));
        String result = null;
        if(resultJson.containsKey("result")) {
        	result = resultJson.getString("result");
            if (result.equals("null") || result == null) {
            	logger.error("ETHDriver.getTransactionByHash执行异常,transactionHash:["+transactionHash+"],resultJson:"+resultJson.toString());
            }
        }else if(resultJson.containsKey("error")) {
        	logger.error("ETHDriver.getTransactionByHash执行异常,transactionHash:["+transactionHash+"],resultJson:"+resultJson.toString());
        }
        return result;*/
    	return null;
    }
    
    public static void main(String[] args) {
    	for (int i = 0; i < 61; i++) {
			System.out.print(i+",");
		}
/*    	String aa = "b4b53febd1791000d1936c2a2416f7b374de493339e9d29715195d3e6a1de7d3_DBgvjBRqvEQq4DpqxmXWUHqmrtBGVbB3GP" ;
    	System.out.println(aa.replaceAll("_.*", ""));
    	
    	String address = "0xacd22f0e733af9f7ac9bd5ba602d51c927cea4ab";
    	System.out.println(CoinCommentUtils.FODEncode(address));
    	
    	String a= "5162B45D626D2B8DC73AB5D8FD5E96A32E10CEF9";
    	
    	System.out.println(StringUtils.isAllUpperCase(a));*/
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
    	txId = CoinCommentUtils.FODDecode(txId);
        JSONObject json = coinUtils.goFOD("eth_getTransactionByHash", methodStr(txId));
        String result = json.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        JSONObject resultJson = json.getJSONObject("result");
        String blockNumberStr = CoinUtils.BigHexToString(resultJson.get("blockNumber").toString());
        String amount = CoinUtils.ETHBalanceHexToStr(resultJson.get("value").toString());
        TxInfo txinfo = new TxInfo();
        txinfo.setFrom(resultJson.get("from").toString());
        String to = resultJson.getString("to");
        to = CoinCommentUtils.FODEncode(to);
        txinfo.setTo(to);
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
        JSONObject resultJson = coinUtils.goFOD("web3_sha3", methodStr(str));
        String result = resultJson.get("result").toString();
        if (result.equals("null")) {
            return null;
        }
        return result;
    }

    @Override
    public Integer getTransactionCount() {
        JSONObject resultJson = coinUtils.goFOD("eth_getTransactionCount", methodStr(sendAccount, "latest"));
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
		JSONObject jsonObject = new JSONObject();
		String result = null;
		to = CoinCommentUtils.FODDecode(to);
		try {
	        if (contractAccount != null && contractAccount.length() > 0) {
	            jsonObject.put("from", sendAccount);
	            jsonObject.put("to", contractAccount);
	            jsonObject.put("value", "0x0");
	            jsonObject.put("gas", "0x1D4C0");
	            jsonObject.put("gasPrice", gasPrice);
	            jsonObject.put("nonce", nonce);
	            String data = "0xa9059cbb000000000000000000000000" + StringUtils.difference("0x", to) + CoinUtils.CntractToETHBalanceHex(amount, contractWei);
	            jsonObject.put("data", data);
	        } else {
	            jsonObject.put("from", sendAccount);
	            jsonObject.put("to", to);
	            jsonObject.put("value", CoinUtils.ToETHBalanceHex(amount));
	            jsonObject.put("gas", "0x1D4C0");
	            jsonObject.put("gasPrice", gasPrice);
	            jsonObject.put("nonce", nonce);
	        }
	        JSONObject resultJson = coinUtils.goFOD("personal_signAndSendTransaction", methodStrJson(jsonObject.toString(), this.passWord));
	        logger.info("FOD.sendToAddress nonce:"+nonce+",gas:"+gas+",gasPrice:"+gasPrice+",resultJson:"+resultJson.toString());
        	if(resultJson.containsKey("result")) {
        		result = CoinCommentUtils.FODEncode(resultJson.getString("result"));
        		
        	}else if(resultJson.containsKey("error")) {
        		JSONObject Object = resultJson.getJSONObject("error");
        		String message = Object.getString("message");
        		//{"id":1,"jsonrpc":"2.0","error":{"code":-32000,"message":"known transaction: a5307010de7aa330a7eecbc88b456e2397c531bd0b99643cf59a77a14d5eb541"}}
        		if(message.indexOf("known transaction:") != -1) {
        			result = "0x" + message.replace("known transaction: ", "");
        		//{"id":1,"jsonrpc":"2.0","error":{"code":-32000,"message":"could not decrypt key with given passphrase"}}
        		}else if(message.indexOf("could not decrypt key with given passphrase") != -1 ) {
        			return ReturnResult.FAILUER(403,"密码错误");
        		}
        		//{"id":1,"jsonrpc":"2.0","error":{"code":-32000,"message":"replacement transaction underpriced"}}    由于还有转账还没打包，获取链上nonce在提交会出现重复的情况
        	}
        	if(StringUtils.isEmpty(result) || "null".equals(result)) {
            	return null;
            }
		} catch (Exception e) {
			if(e instanceof BCException) {
				throw e;
			}
			logger.error("FOD.sendToAddress error",e);
			return null;
		}
        return ReturnResult.SUCCESS(200 ,result);
	}


    
}
