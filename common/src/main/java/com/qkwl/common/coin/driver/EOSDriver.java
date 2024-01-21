package com.qkwl.common.coin.driver;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.coin.CoinDriver;
import com.qkwl.common.coin.CoinUtils;
import com.qkwl.common.coin.TxInfo;
import com.qkwl.common.util.ByteUtil;
import com.qkwl.common.util.CoinCommentUtils;
import com.qkwl.common.util.ReturnResult;

/**
 * BTCDriver
 *
 * @author hwj
 */
public class EOSDriver implements CoinDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(EOSDriver.class);

    private CoinUtils coinUtils = null;
    private String passWord = null;
    private Integer coinSort = null;
    private String sendAccount = null;
    private String chainPort = null;
    private String walletPort = null;

    public EOSDriver(String accessKey, String secretKey, String ip, String port, String pass, Integer coinSort, String sendAccount) {
        coinUtils = new CoinUtils(accessKey, secretKey, ip, port);
        this.passWord = pass;
        this.coinSort = coinSort;
        this.sendAccount = sendAccount;
        if(port.indexOf(",") != 0) {
        	String[] split = port.split(",");
        	this.chainPort = split[0];
        	this.walletPort = split[1];
        }else {
        	this.chainPort = port;
        	this.walletPort = port;
        }
        
    }

    @Override
    public BigDecimal getBalance() {
        String result = coinUtils.goEOS(chainPort,"/v1/chain/get_currency_balance", "{\"account\":\"" + sendAccount + "\",\"code\":\"eosio.token\",\"symbol\":\"EOS\"}");
        if(result != null) {
        	List<String> parse = JSONArray.parseArray(result, String.class);
        	result = parse.get(0).replace(" EOS", "");
        }
        return new BigDecimal(result);
    }

    @Override
    public String getNewAddress(String time) {
    	return null;
    }

    @Override
    public boolean walletLock() {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        logger.info("eos请求lock");
        String goEOS = coinUtils.goEOS(walletPort,"/v1/wallet/lock", "\"default\"");
        logger.info("eos请求lock，返回："+goEOS);
        return true;
    }

    @Override
    public boolean walletPassPhrase(int times) {
        if (passWord == null || passWord.length() <= 0) {
            return false;
        }
        String data = "[\"default\",\"" + passWord + "\"]";
        logger.info("eos请求unlock");
        String goEOS = coinUtils.goEOS(walletPort,"/v1/wallet/unlock", data);
        logger.info("eos请求unlock，返回："+goEOS);
        return true;
    }

    @Override
    public boolean setTxFee(BigDecimal fee) {
        return true;
    }

    @Override
    public List<TxInfo> listTransactions(int count, int from) {
    	String result = coinUtils.goEOS(chainPort,"/v1/history/get_actions", "{\"pos\":" + from + ",\"offset\":"+count+",\"account_name\":\"" + sendAccount + "\"}");
    	if(StringUtils.isEmpty(result)) {
    		return null;
    	}
    	JSONObject parseObject = JSONObject.parseObject(result);
    	if(!parseObject.containsKey("actions")) {
    	   return null;
    	}
    	List<TxInfo> txInfos = new ArrayList<TxInfo>();
    	JSONArray jsonArray = parseObject.getJSONArray("actions");
    	Integer lastBlock = parseObject.getInteger("last_irreversible_block");
    	for (Object object : jsonArray) {
    		JSONObject txObject = null;
    		try {
    			txObject = JSON.parseObject(object.toString());
    			TxInfo txInfo = new TxInfo();
    			JSONObject actionTrace = txObject.getJSONObject("action_trace");
				JSONObject data = actionTrace.getJSONObject("act").getJSONObject("data");
				String fromAccount = data.getString("from");
				if(sendAccount.equals(fromAccount)) {
					continue;
				}
				String string = data.getString("memo");
				if(StringUtils.isEmpty(string) || !StringUtils.isNumeric(string)) {
					continue;
				}
				String quantity = data.getString("quantity").replace(" EOS", "");
				Integer integer = txObject.getInteger("block_num");
				txInfo.setBlockNumber(integer);
				txInfo.setTxid(actionTrace.getString("trx_id"));
				txInfo.setAddress(sendAccount);
				txInfo.setMemo(string);
				txInfo.setBlockNumber(integer);
				txInfo.setConfirmations(lastBlock - integer);
				txInfo.setAmount(new BigDecimal(quantity));
				txInfos.add(txInfo);
			} catch (Exception e) {
				logger.error("EOS.listTransactions 异常"+ object.toString(), e);
			}
        }
    	return txInfos;
    }

    @Override
    public TxInfo getTransaction(String txId) {
        String json = coinUtils.goEOS(chainPort,"/v1/history/get_transaction", "{\"id\":\"" +txId+ "\"}");
        if(StringUtils.isEmpty(json)) {
        	return null;
        }
        JSONObject parseObject = JSON.parseObject(json);
        if(!parseObject.containsKey("trx")) {
        	return null;
        }
        try {
        	//JSONObject data = parseObject.getJSONObject("trx").getJSONObject("trx").getJSONObject("actions").get(0).getJSONObject("data");
        	TxInfo txInfo = new TxInfo();
        	Integer blockNum = parseObject.getInteger("block_num");
        	Integer lastBlock = parseObject.getInteger("last_irreversible_block");
        	//String quantity = data.getString("quantity").replace(" EOS", "");
        	//txInfo.setAddress(data.getString("to"));
        	txInfo.setBlockNumber(blockNum);
			txInfo.setConfirmations(lastBlock - blockNum);
			//txInfo.setAmount(new BigDecimal(quantity));
            return txInfo;
		} catch (Exception e) {
			logger.error("EOS.getTransaction 异常,params:{txId:"+txId+"}",e);
		}
        return null;
        
    }

    @Override
    public String sendToAddress(String to, BigDecimal amount, String comment, BigDecimal fee) {
    	return null;
    }

    @Override
    public String sendToAddress(String address, BigDecimal amount, String comment, BigDecimal fee, String memo) {
    	try {
    	amount = amount.setScale(4, BigDecimal.ROUND_HALF_UP);
    	String data = "{\"code\": \"eosio.token\", \"action\": \"transfer\", \"args\": {\"from\": \"" + sendAccount + "\", \"to\": \"" + address + "\", \"quantity\": \""+amount.toString() + " EOS\", \"memo\": \""+memo+"\"}}";
    	logger.info("eos请求abi_json_to_bin,参数："+data);
    	String abi_json_to_bin = coinUtils.goEOS(chainPort,"/v1/chain/abi_json_to_bin", data);
    	logger.info("eos请求abi_json_to_bin,返回："+abi_json_to_bin);
    	JSONObject abiJsonToBin = JSON.parseObject(abi_json_to_bin);
    	
    	logger.info("eos请求get_info");
    	String get_info = coinUtils.goEOS(chainPort,"/v1/chain/get_info", "");
    	logger.info("eos请求get_info,返回："+get_info);
        JSONObject getInfo = JSON.parseObject(get_info);
        
        
        String data1 = "{\"block_num_or_id\": "+getInfo.getInteger("head_block_num")+"}";
        logger.info("eos请求get_block,参数："+data1);
        String get_block = coinUtils.goEOS(chainPort,"/v1/chain/get_block", "{\"block_num_or_id\": "+getInfo.get("head_block_num")+"}");
        logger.info("eos请求get_block,返回："+get_block);
        JSONObject bliockInfo = JSON.parseObject(get_block);
        Date parseDate;
		
		parseDate = DateUtils.parseDate(bliockInfo.getString("timestamp"), new String[] {"yyyy-MM-dd'T'HH:mm:ss.S"});
		
        parseDate = DateUtils.addHours(parseDate, 1);
        
        Integer i = bliockInfo.getInteger("block_num");
        Integer integer = i & 0x0000ffff;
        String tx = "{\"expiration\":\""+ DateFormatUtils.format(parseDate, "yyyy-MM-dd'T'HH:mm:ss") + "\","
        			+"\"ref_block_num\": "+ integer + ","
        			+"\"ref_block_prefix\": "+ bliockInfo.get("ref_block_prefix") + ","
        			+"\"max_net_usage_words\":  0,"
        			+"\"max_kcpu_usage\":  0,"
        			+"\"delay_sec\":  0,"
        			+ "\"signatures\":  [],"
        			+ "\"context_free_actions\":  [],"
        			+ "\"context_free_data\":  [],"
        			+ "\"actions\":  [{"
        							+ "\"account\": \"eosio.token\","
        							+ "\"name\": \"transfer\","
        							+ "\"authorization\":"
        										+ " [{"
        											+ "\"actor\": \""+ sendAccount +"\","
        											+ "\"permission\": \"active\""
        										+ "}],"
        							+ "\"data\": \""+ abiJsonToBin.getString("binargs") +"\"}]" 
        							
        			+ "}";
        
        walletPassPhrase(30);
        logger.info("eos请求get_public_keys");
        String public_key = coinUtils.goEOS(walletPort,"/v1/wallet/get_public_keys", ""); //["EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV"]
        logger.info("eos请求get_public_keys,返回"+public_key);
        
        String available_key = "{\"transaction\":"+tx 
        						+ ",\"available_keys\":"+public_key 
        						+"}";
        
        logger.info("eos请求get_required_keys，参数："+available_key );
        String get_required_keys = coinUtils.goEOS(chainPort,"/v1/chain/get_required_keys", available_key);
        logger.info("eos请求get_required_keys，返回："+get_required_keys );
        
        String sign_key = JSON.parseObject(get_required_keys).getString("required_keys"); //["EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV"]
        
        //String  need_sign = "[" + tx + "," + sign_key +",\""+0000000000000000000000000000000000000000000000000000000000000000+"\"]";
        String  need_sign = "[" + tx + "," + sign_key +",\"\"]";
        if(getInfo.containsKey("chain_id") && getInfo.getString("chain_id") != null) {
        	 need_sign = "[" + tx + "," + sign_key +",\""+getInfo.getString("chain_id")+"\"]";
        }
        
        logger.info("eos请求sign_transaction，参数："+need_sign );
        String sign_transaction = coinUtils.goEOS(walletPort,"/v1/wallet/sign_transaction", need_sign);
        logger.info("eos请求sign_transaction，返回："+sign_transaction );
        
        walletLock();
        JSONObject parseObject = JSON.parseObject(sign_transaction);
        String  packed_trx ="{" 
							+ "\"compression\": \"none\", "
							+ "\"packed_context_free_data\": \"\","  
							+ "\"packed_trx\" : \""+ pack(parseObject) + "\"," 
							+ "\"signatures\" : "+ parseObject.getString("signatures") 
							+"}";
        
        
        logger.info("eos请求push_transaction，参数："+packed_trx );
        String push_transaction = coinUtils.goEOS(chainPort,"/v1/chain/push_transaction", packed_trx);
        logger.info("eos请求push_transaction，返回："+push_transaction );
        
        JSONObject parseObject2 = JSON.parseObject(push_transaction);
        
        if(parseObject2.containsKey("transaction_id")) {
        	return parseObject2.getString("transaction_id");
        }
        
        
    	} catch (ParseException e) {
    		logger.error("eos.sendToAddress异常,参数：{address："+address+",amount:"+amount +",memo:"+memo +"}",e);
		}
        return null;
    }
    
    private static String pack(JSONObject signTransaction) {

		try {
			StringBuilder sb = new StringBuilder();
	    	//String packed_trx ;
			Date parseDate;
			parseDate = CoinCommentUtils.fromISODate(signTransaction.getString("expiration"));
			Integer time = (int) (parseDate.getTime()/1000);
			sb.append(ByteUtil.packInt(time));
			sb.append(ByteUtil.packShort(signTransaction.getShort("ref_block_num")));
			sb.append(ByteUtil.packInt(signTransaction.getInteger("ref_block_prefix")));
			sb.append(ByteUtil.packShortVariable(signTransaction.getShort("max_net_usage_words")));
			sb.append(ByteUtil.packShortVariable(signTransaction.getShort("max_cpu_usage_ms")));
			sb.append(ByteUtil.packShortVariable(signTransaction.getShort("delay_sec")));
			sb.append(ByteUtil.packShortVariable((short) 0));
			JSONArray actions = signTransaction.getJSONArray("actions");
			sb.append(ByteUtil.packShortVariable((short) actions.size()));
			for (int i = 0; i < actions.size(); i++) {
				JSONObject jsonobject = actions.getJSONObject(i);
				sb.append(ByteUtil.packLong(ByteUtil.stringToName(jsonobject.getString("account"))));
				sb.append(ByteUtil.packLong(ByteUtil.stringToName(jsonobject.getString("name"))));
				JSONArray authorizations = jsonobject.getJSONArray("authorization");
				sb.append(ByteUtil.packShortVariable((short) authorizations.size()));
				for (int j = 0; j < authorizations.size(); j++) {
					JSONObject authorization = authorizations.getJSONObject(j);
					sb.append(ByteUtil.packLong(ByteUtil.stringToName(authorization.getString("actor"))));
					sb.append(ByteUtil.packLong(ByteUtil.stringToName(authorization.getString("permission"))));
				}
				String data = jsonobject.getString("data");
				if(data.length() % 2 == 1) {
					sb.append(ByteUtil.packShortVariable((short) (data.length() / 2 + 1)));
				}else {
					sb.append(ByteUtil.packShortVariable((short) (data.length() / 2)));
				}
				sb.append(data);
				if(i == actions.size() - 1) {
					sb.append("00");
				}
			}
			return sb.toString();
		} catch (Exception e) {
			logger.error("eos.pack异常 signTransaction:"+signTransaction,e);
		}
		return null;
	}

    public static void main(String[] args) {
		
		try {
			String a = "{\"expiration\":\"2018-09-03T04:11:47\",\"ref_block_num\":27475,\"ref_block_prefix\":3773960086,\"max_net_usage_words\":0,\"max_cpu_usage_ms\":0,\"delay_sec\":0,\"context_free_actions\":[],\"actions\":[{\"account\":\"eosio.token\",\"name\":\"transfer\",\"authorization\":[{\"actor\":\"hotcoineosio\",\"permission\":\"active\"}],\"data\":\"401da66a3a8a326db055c2545dddf235010000000000000004454f5300000000dc01e4bc9fe69db0e68891e698afe8b081e68891e59ca8e593aae9878ce68891e59ca8e5b9b2e4bb80e4b988e591a2e4bc9fe69db0e5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5958ae5a5b332\"}],\"transaction_extensions\":[],\"signatures\":[\"SIG_K1_Kh6bMaytwyjhU4QEoKrLZ3RrYgYJAir3UVua8998wHwkoxgsesTGxcavQVfeGTmyJieJ4NZK944D84NgDhXWnFKzNamEau\"],\"context_free_data\":[]}"; 
			JSONObject parseObject = JSON.parseObject(a);
			String pack = pack(parseObject);
			System.out.println(pack);
		} catch (Exception e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public ReturnResult sendToAddress(String to, String amount, String nonce, String gasPrice, String gas) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	
	
}
