package com.qkwl.service.mqtt.job;



import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.dto.Enum.SystemTradeStatusEnum;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.redis.MemCache;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.service.mqtt.config.MqttProperties;

@Component
public class MqttJob {

	@Autowired MqttClient mqttClient;
	@Autowired
	MqttProperties mqttProperties;
	private static final Logger logger = LoggerFactory.getLogger(MqttJob.class);
	
	private final int[] TIME_KIND = {1, 5, 15, 30,60, // 分钟
	            24 * 60, 7 * 24 * 60,30 * 24 * 60 // 天
	};
	
	@Autowired
	protected MemCache memCache;
	
	@Scheduled(cron="*/2 * * * * ?")  
	public void work() throws MqttException {
		String coins = memCache.get(RedisConstant.TRADE_LIST_KEY + "_" + 0);
		sendMKTInfo(coins);
		sendDetail(coins);
	}
	
	@Scheduled(cron="*/2 * * * * ?")  
	public void work1() throws MqttException {
		String coins = memCache.get(RedisConstant.TRADE_LIST_KEY + "_" + 0);
		//行情
		sendWebMKTInfo(coins);
		//币币交易详情
		sendWebDetail(coins);
		//实时交易
	}
	
	//K线
//	@Scheduled(cron="*/3 * * * * ?")  
//	public void kline() throws MqttException {
//		String coins = memCache.get(RedisConstant.TRADE_LIST_KEY + "_" + 0);
//		sendKline(coins);
//	}
	
	
	public void sendKline(String coins) throws MqttException {
		
		MqttMessage message = new MqttMessage();
		message.setQos(0);
		message.setRetained(true);
		
		JSONObject obj = JSON.parseObject(coins);
		JSONArray tradeTypeList = obj.getJSONArray("extObject");
		
		for (Object val : tradeTypeList) {
			
			JSONObject tradeType = JSONObject.parseObject(val.toString());
			Integer status = tradeType.getIntValue("status");
			Integer id = tradeType.getInteger("id");
			
			//GSET 交易区
			if(!status.equals(SystemTradeStatusEnum.ABNORMAL.getCode()) ) {
				//发送K线
				for (int i : TIME_KIND) {
		                // Kline
		           String klineStr = memCache.get(RedisConstant.LASTKLINE_KEY + id + "_" + i);
		           JSONObject kline = JSON.parseObject(klineStr);
		           JSONArray klines = kline.getJSONArray("extObject");
		           message.setPayload(klines.toJSONString().getBytes());
		           mqttClient.publish(mqttProperties.getWebKlineTopic() + "/"+id+"_"+i, message);  
		        }
			}
		}
	}
	
	public void sendDetail(String coins){
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(0);
			message.setRetained(true);
			JSONObject obj = JSON.parseObject(coins);
			JSONArray tradeTypeList = obj.getJSONArray("extObject");
			
			for (Object val : tradeTypeList) {
				if (val != null) {
					JSONObject tradeType = JSONObject.parseObject(val.toString());
					Integer status = tradeType.getIntValue("status");
					Integer id = tradeType.getInteger("id");
					
					JSONObject newTradeType = new JSONObject();
					//GSET 交易区
					if(!status.equals(SystemTradeStatusEnum.ABNORMAL.getCode()) ) {
						//行情
						String tickerStr = memCache.get(RedisConstant.TICKERE_KEY + id);
						
						JSONObject depth = new JSONObject();
						if(StringUtils.isEmpty(tickerStr)) {
							newTradeType.put("p_new", 0);
							newTradeType.put("p_open", 0);
						}else {
							JSONObject tickerJson = JSONObject.parseObject(tickerStr).getJSONObject("extObject");
							newTradeType.put("p_new", tickerJson.getString("last"));
							newTradeType.put("p_open", tickerJson.getString("kai"));
							
							newTradeType.put("total", tickerJson.getBigDecimal("vol") == null ? 0d : tickerJson.getBigDecimal("vol"));
							newTradeType.put("buy", tickerJson.getBigDecimal("buy") == null ? 0d : tickerJson.getBigDecimal("low"));
							newTradeType.put("sell", tickerJson.getBigDecimal("sell") == null ? 0d : tickerJson.getBigDecimal("high"));

							newTradeType.put("sellSymbol", tradeType.getString("sellShortName"));
							newTradeType.put("buySymbol", tradeType.getString("buyShortName"));
						}
						
						newTradeType.put("sellAppLogo", tradeType.getString("sellAppLogo"));
						newTradeType.put("sellShortName", tradeType.getString("sellShortName"));
						newTradeType.put("buyShortName", tradeType.getString("buyShortName"));
						newTradeType.put("id", id);
						
						// 小数位处理(默认价格2位，数量4位)
				        newTradeType.put("digit", tradeType.getString("digit"));
						
						//发送深度
						String buyDepthStr = memCache.get(RedisConstant.BUYDEPTH_KEY + id);
						if(StringUtils.isNotEmpty(buyDepthStr)) {
							JSONObject buyDepthA = JSON.parseObject(buyDepthStr);
					        JSONArray buyDepth = buyDepthA.getJSONArray("extObject");
					        
					        // 数据过滤
					        JSONArray newBuyDepth = new JSONArray();
					        for (Object object : buyDepth) {
					            JSONArray array = JSON.parseArray(object.toString());
					            if (Double.valueOf(array.get(1).toString()) > 0d) {
					            	newBuyDepth.add(array);
					            }
					        }
					        depth.put("buyDepth", newBuyDepth);
						}else {
							depth.put("buyDepth", new JSONArray());
						}
						
						String sellDepthStr = memCache.get(RedisConstant.SELLDEPTH_KEY + id);
						if(StringUtils.isNotEmpty(sellDepthStr)) {
							JSONObject sellDepthA = JSON.parseObject(sellDepthStr);
					        JSONArray sellDepth = sellDepthA.getJSONArray("extObject");
					        // 数据过滤
					        JSONArray newSellDepth = new JSONArray();
					        for (Object object : sellDepth) {
					            JSONArray array = JSON.parseArray(object.toString());
					            if (Double.valueOf(array.get(1).toString()) > 0d) {
					            	newSellDepth.add(array);
					            }
					        }
					        depth.put("sellDepth", newSellDepth);
						}else {
							depth.put("sellDepth", new JSONArray());
						}
						newTradeType.put("depth", depth);
						message.setPayload(newTradeType.toJSONString().getBytes());
						mqttClient.publish(mqttProperties.getMktTopic() + "/"+id, message);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	// 多个二级topic
	public void sendMKTInfo(String coins) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(0);
			message.setRetained(true);
			
			JSONObject obj = JSON.parseObject(coins);
			JSONArray tradeTypeList = obj.getJSONArray("extObject");
			
			JSONArray gsetList = new JSONArray();
			JSONArray btcList = new JSONArray();
			JSONArray ethtList = new JSONArray();
			JSONArray usdtList = new JSONArray();
			for (Object val : tradeTypeList) {
				if (val != null) {
					JSONObject tradeType = JSONObject.parseObject(val.toString());
					Integer status = tradeType.getIntValue("status");
					Integer id = tradeType.getInteger("id");
					Integer type = tradeType.getInteger("type");
					
					JSONObject newTradeType = new JSONObject();
					//GSET 交易区
					if(!status.equals(SystemTradeStatusEnum.ABNORMAL.getCode()) ) {
						//行情
						String tickerStr = memCache.get(RedisConstant.TICKERE_KEY + id);
						
						if(StringUtils.isEmpty(tickerStr)) {
							newTradeType.put("p_new", 0);
							newTradeType.put("p_open", 0);
						}else {
							JSONObject tickerJson = JSONObject.parseObject(tickerStr).getJSONObject("extObject");
							newTradeType.put("p_new", tickerJson.getBigDecimal("last") == null ? 0d : tickerJson.getBigDecimal("last"));
							newTradeType.put("p_open", tickerJson.getBigDecimal("kai") == null ? 0d : tickerJson.getBigDecimal("kai"));
							
							newTradeType.put("total", tickerJson.getBigDecimal("vol") == null ? 0d : tickerJson.getBigDecimal("vol"));
							newTradeType.put("buy", tickerJson.getBigDecimal("buy") == null ? 0d : tickerJson.getBigDecimal("low"));
							newTradeType.put("sell", tickerJson.getBigDecimal("sell") == null ? 0d : tickerJson.getBigDecimal("high"));

							newTradeType.put("sellSymbol", tradeType.getString("sellShortName"));
							newTradeType.put("buySymbol", tradeType.getString("buyShortName"));
						}
						
						newTradeType.put("sellAppLogo", tradeType.getString("sellAppLogo"));
						newTradeType.put("sellShortName", tradeType.getString("sellShortName"));
						newTradeType.put("buyShortName", tradeType.getString("buyShortName"));
						newTradeType.put("id", id);
						
						// 小数位处理(默认价格2位，数量4位)
						// 小数位处理(默认价格2位，数量4位)
				        newTradeType.put("digit", tradeType.getString("digit"));
						
						if (type.equals(1)) {
							gsetList.add(newTradeType);
						}
						//BTC交易区
						else if (type.equals(2)) {
							BigDecimal cny = getCny(8, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							btcList.add(newTradeType);
						}
						//ETH交易区
						else if (type.equals(3)) {
							BigDecimal cny = getCny(11, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							ethtList.add(newTradeType);
						}
						else if(type.equals(4)) {
							BigDecimal cny = getCny(57, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							usdtList.add(newTradeType);
						}
					}
				}
			}
			
			message.setPayload(gsetList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getGsetTopic(), message);
			
			message.setPayload(btcList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getBtcTopic(), message);
			
			message.setPayload(ethtList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getEthTopic(), message);
			
			message.setPayload(usdtList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getUsdtTopic(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendWebDetail(String coins){
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(0);
			message.setRetained(true);
			JSONObject obj = JSON.parseObject(coins);
			JSONArray tradeTypeList = obj.getJSONArray("extObject");
			
			for (Object val : tradeTypeList) {
				if (val != null) {
					JSONObject tradeType = JSONObject.parseObject(val.toString());
					Integer status = tradeType.getIntValue("status");
					Integer id = tradeType.getInteger("id");
					
					JSONObject newTradeType = new JSONObject();
					//GSET 交易区
					if(!status.equals(SystemTradeStatusEnum.ABNORMAL.getCode()) ) {
						//行情
						String tickerStr = memCache.get(RedisConstant.TICKERE_KEY + id);
						
						// 小数位处理(默认价格2位，数量4位)
						String digit = StringUtils.isEmpty(tradeType.getString("digit")) ? "2#4" : tradeType.getString("digit");
				        String[] digits = digit.split("#");
				        int cnyDigit = Integer.valueOf(digits[0]);
				        int coinDigit = Integer.valueOf(digits[1]);
						
						JSONObject depth = new JSONObject();
						if(StringUtils.isEmpty(tickerStr)) {
							newTradeType.put("p_new", 0);
							newTradeType.put("p_open", 0);
						}else {
							JSONObject tickerJson = JSONObject.parseObject(tickerStr).getJSONObject("extObject");
							newTradeType.put("p_new", MathUtils.toScaleNum(tickerJson.getBigDecimal("last"),cnyDigit));
							newTradeType.put("p_open", MathUtils.toScaleNum(tickerJson.getBigDecimal("kai"),cnyDigit));
							
							newTradeType.put("total", tickerJson.getBigDecimal("vol") == null ? 0d : MathUtils.toScaleNum(tickerJson.getBigDecimal("vol"), coinDigit));
							newTradeType.put("buy", tickerJson.getBigDecimal("buy") == null ? 0d : MathUtils.toScaleNum(tickerJson.getBigDecimal("low"),cnyDigit));
							newTradeType.put("sell", tickerJson.getBigDecimal("sell") == null ? 0d : MathUtils.toScaleNum(tickerJson.getBigDecimal("high"),cnyDigit));

							newTradeType.put("sellSymbol", tradeType.getString("sellShortName"));
							newTradeType.put("buySymbol", tradeType.getString("buyShortName"));
						}
						
						newTradeType.put("sellAppLogo", tradeType.getString("sellAppLogo"));
						newTradeType.put("sellShortName", tradeType.getString("sellShortName"));
						newTradeType.put("buyShortName", tradeType.getString("buyShortName"));
						newTradeType.put("id", id);
						
						//发送深度
						String buyDepthStr = memCache.get(RedisConstant.BUYDEPTH_KEY + id);
						if(StringUtils.isNotEmpty(buyDepthStr)) {
							JSONObject buyDepthA = JSON.parseObject(buyDepthStr);
					        JSONArray buyDepth = buyDepthA.getJSONArray("extObject");
					        
					        // 数据过滤
					        JSONArray newBuyDepth = new JSONArray();
					        BigDecimal allCount = BigDecimal.ZERO;
					        for (Object object : buyDepth) {
					            JSONArray array = JSON.parseArray(object.toString());
					            
					            JSONArray newArray = new JSONArray();
					            if (Double.valueOf(array.get(1).toString()) > 0d) {
					            	//单价
					            	BigDecimal price = new BigDecimal(array.get(0).toString());
					            	//数量
					            	BigDecimal count = new BigDecimal(array.get(1).toString());
					            	
					            	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
					            	newArray.add(MathUtils.toScaleNum(count,coinDigit));
					            	allCount = MathUtils.add(allCount, count);
					            	newArray.add(MathUtils.toScaleNum(allCount,coinDigit));
					            	newBuyDepth.add(newArray);
					            }
					        }
					        depth.put("buyDepth", newBuyDepth);
						}else {
							depth.put("buyDepth", new JSONArray());
						}
						
						String sellDepthStr = memCache.get(RedisConstant.SELLDEPTH_KEY + id);
						if(StringUtils.isNotEmpty(sellDepthStr)) {
							JSONObject sellDepthA = JSON.parseObject(sellDepthStr);
					        String sellDepth = sellDepthA.getString("extObject");
					        List<Object> list = JSONArray.parseArray(sellDepth, Object.class);
					        // 数据过滤
					        List<Object> newSellDepth = new JSONArray();
					        BigDecimal allCount = BigDecimal.ZERO;
					        for (Object object : list) {
					            JSONArray array = JSON.parseArray(object.toString());
					            if (Double.valueOf(array.get(1).toString()) > 0d) {
					            	JSONArray newArray = new JSONArray();
						            if (Double.valueOf(array.get(1).toString()) > 0d) {
						            	//单价
						            	BigDecimal price = new BigDecimal(array.get(0).toString());
						            	//数量
						            	BigDecimal count = new BigDecimal(array.get(1).toString());
						            	
						            	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
						            	newArray.add(MathUtils.toScaleNum(count,coinDigit));
						            	allCount = MathUtils.add(allCount, count);
						            	newArray.add(MathUtils.toScaleNum(allCount,coinDigit));
						            	newSellDepth.add(newArray);
						            }
					            }
					        }
					        sort(newSellDepth);
					        depth.put("sellDepth", newSellDepth);
						}else {
							depth.put("sellDepth", new JSONArray());
						}
						newTradeType.put("depth", depth);
						
						message.setPayload(newTradeType.toJSONString().getBytes());
						mqttClient.publish(mqttProperties.getWebMktTopic() + "/"+id, message);
						
						//发送实时成交数据
						String successStr = memCache.get(RedisConstant.SUCCESSENTRUST_KEY + id);
						JSONObject success = JSON.parseObject(successStr);
				        JSONArray successArray = success.getJSONArray("extObject");
				        
				        //精度控制
				        JSONArray newSuccessArray = new JSONArray();
				        for(Object successObj :successArray) {
				        	JSONArray array = JSON.parseArray(successObj.toString());
				        	
				        	JSONArray newArray = new JSONArray();
				        	BigDecimal price = new BigDecimal(array.get(0).toString());
				        	BigDecimal count = new BigDecimal(array.get(1).toString());
				        	newArray.add(MathUtils.toScaleNum(price,cnyDigit));
				        	newArray.add(MathUtils.toScaleNum(count,coinDigit));
				        	newArray.add(array.get(2));
				        	newArray.add(array.get(3));
				        	newSuccessArray.add(newArray);
				        }
				        
				        message.setPayload(newSuccessArray.toJSONString().getBytes());
				        mqttClient.publish(mqttProperties.getWebRealTimeTrade() + "/"+id, message);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String sellDepth = "[[23.39000000,4.8700], [23.40000000,0.4900], [24.46000000,13.0800], [24.48000000,0.0500], [24.99000000,0.0400], [25.82000000,0.0300],"
				+ " [26.20000000,2.5500], [26.62000000,0.0300], [26.87000000,0.1800], [27.03000000,0.3900], [30.50000000,35.4000], [40.00000000,0.1000], "
				+ "[41.00000000,0.1000], [43.00000000,0.1000], [44.00000000,0.1000], [46.00000000,0.0900], [52.50000000,1.0000], [53.64000000,0.1000], [54.08000000,0.1000]]";
        List<Object> list = JSONArray.parseArray(sellDepth, Object.class);
       
        // 数据过滤
        List<Object> newSellDepth = new JSONArray();
        BigDecimal allCount = BigDecimal.ZERO;
        for (Object object : list) {
            JSONArray array = JSON.parseArray(object.toString());
            if (Double.valueOf(array.get(1).toString()) > 0d) {
            	JSONArray newArray = new JSONArray();
	            if (Double.valueOf(array.get(1).toString()) > 0d) {
	            	//单价
	            	BigDecimal price = new BigDecimal(array.get(0).toString());
	            	//数量
	            	BigDecimal count = new BigDecimal(array.get(1).toString());
	            	
	            	newArray.add(MathUtils.toScaleNum(price,2));
	            	newArray.add(MathUtils.toScaleNum(count,2));
	            	allCount = MathUtils.add(allCount, count);
	            	newArray.add(MathUtils.toScaleNum(allCount,2));
	            	newSellDepth.add(newArray);
	            }
            }
        }
        new MqttJob().sort(newSellDepth);
	    System.out.println(list);
	    System.out.println(newSellDepth);
	}
	
	// 多个二级topic
	public void sendWebMKTInfo(String coins) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(0);
			message.setRetained(true);
			
			JSONObject obj = JSON.parseObject(coins);
			JSONArray tradeTypeList = obj.getJSONArray("extObject");
			
			JSONArray gsetList = new JSONArray();
			JSONArray btcList = new JSONArray();
			JSONArray ethtList = new JSONArray();
			JSONArray usdtList = new JSONArray();
			for (Object val : tradeTypeList) {
				if (val != null) {
					JSONObject tradeType = JSONObject.parseObject(val.toString());
					Integer status = tradeType.getIntValue("status");
					Integer id = tradeType.getInteger("id");
					Integer type = tradeType.getInteger("type");
					
					JSONObject newTradeType = new JSONObject();
					//GSET 交易区
					if(!status.equals(SystemTradeStatusEnum.ABNORMAL.getCode()) ) {
						//行情
						String tickerStr = memCache.get(RedisConstant.TICKERE_KEY + id);
						
						JSONObject depth = new JSONObject();
						if(StringUtils.isEmpty(tickerStr)) {
							newTradeType.put("p_new", 0);
							newTradeType.put("p_open", 0);
							
							depth.put("p_new", 0);
							depth.put("p_open", 0);
						}else {
							JSONObject tickerJson = JSONObject.parseObject(tickerStr).getJSONObject("extObject");
							newTradeType.put("p_new", tickerJson.getString("last"));
							newTradeType.put("p_open", tickerJson.getString("kai"));
							
							depth.put("p_new", tickerJson.getString("last"));
							depth.put("p_open", tickerJson.getString("kai"));
							
							newTradeType.put("total", tickerJson.getBigDecimal("vol") == null ? 0d : tickerJson.getBigDecimal("vol"));
							newTradeType.put("buy", tickerJson.getBigDecimal("buy") == null ? 0d : tickerJson.getBigDecimal("low"));
							newTradeType.put("sell", tickerJson.getBigDecimal("sell") == null ? 0d : tickerJson.getBigDecimal("high"));

							newTradeType.put("sellSymbol", tradeType.getString("sellShortName"));
							newTradeType.put("buySymbol", tradeType.getString("buyShortName"));
						}
						
						newTradeType.put("sellAppLogo", tradeType.getString("sellAppLogo"));
						newTradeType.put("sellShortName", tradeType.getString("sellShortName"));
						newTradeType.put("buyShortName", tradeType.getString("buyShortName"));
						newTradeType.put("id", id);
						
						//GSET交易区
						if (type.equals(1)) {
							gsetList.add(newTradeType);
						}
						//BTC交易区
						else if (type.equals(2)) {
							BigDecimal cny = getCny(8, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							btcList.add(newTradeType);
						}
						//ETH交易区
						else if (type.equals(3)) {
							BigDecimal cny = getCny(11, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							ethtList.add(newTradeType);
						}
						else if(type.equals(4)) {
							BigDecimal cny = getCny(57, newTradeType.getString("p_new"));
							newTradeType.put("cny", cny);
							usdtList.add(newTradeType);
						}
					}
				}
			}
			
			message.setPayload(gsetList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getWebGsetTopic(), message);
			
			message.setPayload(btcList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getWebBtcTopic(), message);
			
			message.setPayload(ethtList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getWebEthTopic(), message);
			
			message.setPayload(usdtList.toJSONString().getBytes());
			mqttClient.publish(mqttProperties.getWebUsdtTopic(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BigDecimal getCny(int tradeId,String p_newStr) {
		//取BTC/GSET交易对价格计算
		String tradeTicker = memCache.get(RedisConstant.TICKERE_KEY + tradeId);
		JSONObject tradeTickerJson = JSONObject.parseObject(tradeTicker).getJSONObject("extObject");
		String  lastStr = tradeTickerJson.getString("last");
		//BTC/GSET交易对最新价格
		BigDecimal cny = new BigDecimal(lastStr);
		
		//当前交易对最新价格
		BigDecimal p_new = new BigDecimal(p_newStr);
		BigDecimal money = MathUtils.mul(p_new, cny);
		BigDecimal newMoney = MathUtils.toScaleNum(money, 2);
		return newMoney;
	}
	
	public void sort(List<Object> list) {
		Collections.sort(list, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				JSONArray o1Array = JSON.parseArray(o1.toString());
				JSONArray o2Array = JSON.parseArray(o2.toString());
            	//o1单价
            	BigDecimal o1Price = new BigDecimal(o1Array.get(0).toString());
            	//o2单价
            	BigDecimal o2Price = new BigDecimal(o2Array.get(0).toString());
		            	
            	if(o1Price.compareTo(o2Price)<0) {
            		return 1;
            	}else if(o1Price.compareTo(o2Price)==0) {
            		return 0;
            	}else {
            		return -1;
            	}
			}
		});
	}
	
//	//发送实时成交数据
//	public void sendWebRealTimeTrade(String coins) {
//		try {
//			MqttClient sampleClient = MqttClientSingleton.getInstance();
//			MqttMessage message = new MqttMessage();
//			message.setQos(0);
//			message.setRetained(true);
//			JSONObject obj = JSON.parseObject(coins);
//			JSONArray tradeTypeList = obj.getJSONArray("extObject");
//			
//			for (Object val : tradeTypeList) {
//				if (val != null) {
//					JSONObject tradeType = JSONObject.parseObject(val.toString());
//					Integer id = tradeType.getInteger("id");
//					String successStr = memCache.get(RedisConstant.SUCCESSENTRUST_KEY + id);
//					
//					
//					
//				}
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
}
