package com.qkwl.common.coin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;

public class CoinUtils {

	private static final Logger logger =  LoggerFactory.getLogger(CoinUtils.class);

	// 用户名
	private String accessKey = null;
	// 密码
	private String secretKey = null;
	// 钱包IP地址
	private String ip = null;
	// 端口
	private String port = null;
	// 加密
	private String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	// Wei
	private static final String ETHWEI = "1000000000000000000";
    public static final String ETHWEI_8 = "100000000";

	public CoinUtils(String accessKey,String secretKey, String ip, String port) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.ip = ip;
		this.port = port;
	}
	
	public CoinUtils(String ip, String port) {
		this.ip = ip;
		this.port = port;
	}

	public JSONObject go(String method, String condition) {
		try {
			String result = "";
			String tonce = "" + (System.currentTimeMillis() * 1000);
			authenticator();

			String params = "tonce=" + tonce.toString() + "&accesskey=" + accessKey + "&requestmethod=post&id=1&method=" + method + "&params=" + condition;

			String hash = getSignature(params, secretKey);

			String url = "http://" + accessKey + ":" + secretKey + "@" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			String userpass = accessKey + ":" + hash;
			String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
			con.setRequestProperty("Authorization", basicAuth);

			String postdata = "{\"method\":\"" + method + "\", \"params\":" + condition + ", \"id\": 1}";
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin go failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin go error {}，{} ", method, condition, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	
	public JSONObject goWICC(String method, String condition) {
		try {
			String result = "";
			String tonce = "" + (System.currentTimeMillis() * 1000);
			authenticator();

			String params = "tonce=" + tonce.toString() + "&accesskey=" + accessKey + "&requestmethod=post&id=1&method=" + method + "&params=" + condition;

			String hash = getSignature(params, secretKey);

			String url = "http://" + accessKey + ":" + secretKey + "@" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			String userpass = accessKey + ":" + hash;
			String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
			con.setRequestProperty("Authorization", basicAuth);

			String postdata = "{\"method\":\"" + method + "\", \"params\":" + condition + ", \"id\": 1}";
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin go failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin go error {}，{} ", method, condition, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	public JSONObject goETC(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.addRequestProperty("Content-Type","application/json");
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1" + ", \"jsonrpc\": \"2.0\"}";

			//System.out.println("postdata:"+postdata);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin goETC failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goETC error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	public JSONObject goETH(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("Content-Type","application/json");
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1}";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin goETH failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goETH error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	public JSONObject goMOAC(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("Content-Type","application/json");
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1}";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin goETH failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goETH error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	public JSONObject goFOD(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("Content-Type","application/json");
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1}";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin goETH failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goETH error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}

	public JSONObject goETP(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port + "/rpc";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + "}";

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
			in.close();
			result = response.toString();

			if (responseCode != 200) {
				logger.error("Coin goETP failed : {}_{}", responseCode,  postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}

            if (method.equals("getnewaddress") || method.equals("fetch-height")) {
                return JSON.parseObject("{\"result\":\"" + result + "\"}");
            }

			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goETP error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}

	
	public JSONObject goUSDT(String method, String params) {
		try {
			String result = "";
			String tonce = "" + (System.currentTimeMillis() * 1000);
			authenticator();
			//String params = "tonce=" + tonce.toString() + "&accesskey=" + accessKey + "&requestmethod=post&id=1&method=" + method + "&params=" + condition;
			String hash = getSignature(params, secretKey);
			String url = "http://" + accessKey + ":" + secretKey + "@" + ip + ":" + port;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// add reuqest header
			con.addRequestProperty("Content-Type","application/json");
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1" + ", \"jsonrpc\": \"2.0\"}";
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				logger.error("Coin goUSDT failed : {}_{}_{}", responseCode,  params, postdata);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goUSDT error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":\"exception\",\"id\":3}");
		}
	}

	public String goEOS(String methodport,String method, String params) {
		try {
			String result = "";
			authenticator();
			String url = "http://" + ip + ":" + methodport + method;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("Content-Type","application/json;charset=UTF-8");
			//con.setRequestProperty("Charsert", "UTF-8");
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//wr.writeBytes(params);
			wr.write(params.getBytes("UTF-8"));
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				logger.error("Coin goEOS failed : {}_{}", responseCode,  params);
				return null;
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			String inputLine;
			StringBuffer response = new StringBuffer();
			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();
			return result;
		} catch (Exception e) {
			logger.error("Coin goEOS error {}，{} ", method, params, e);
			return null;
		}
	}

	public JSONObject goGXS(String method, String params) {
		try {
			String result = "";
			String url = "http://" + ip + ":" + port + "/rpc";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			String postdata = "{\"method\":\"" + method + "\", \"params\":" + params + ", \"id\": 1}";

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			inputLine = in.readLine();
			response.append(inputLine);
			in.close();
			result = response.toString();

			if (responseCode != 200) {
				logger.error("Coin goGXS failed : {}_{}", responseCode,  result);
				return JSON.parseObject("{\"result\":\"null\",\"error\":" + responseCode + ",\"id\":2}");
			}

			if (method.equals("getnewaddress") || method.equals("fetch-height")) {
				return JSON.parseObject("{\"result\":\"" + result + "\"}");
			}

			return JSON.parseObject(result);
		} catch (Exception e) {
			logger.error("Coin goGXS error {}，{} ", method, params, e);
			return JSON.parseObject("{\"result\":\"null\",\"error\":null,\"id\":3}");
		}
	}
	
	private String getSignature(String data, String key) throws Exception {
		// get an hmac_sha1 key from the raw key bytes
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

		// get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);

		// compute the hmac on input data bytes
		byte[] rawHmac = mac.doFinal(data.getBytes());
		return bytArrayToHex(rawHmac);
	}

	private String bytArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

	// The easiest way to tell Java to use HTTP Basic authentication is to set a default Authenticator:
	private void authenticator() {
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(accessKey, secretKey.toCharArray());
			}
		});
	}

	/**
	 * 大数:16进制转string
	 * @return
	 */
	public static String BigHexToString(String bigHex) {
		BigInteger bigInteger = new BigInteger(bigHex.substring(2), 16);
		return bigInteger.toString();
	}
	
	/**
	 * ETH余额:16进制 换算为10进制2位小数
	 * @param hexBalance
	 * @return
	 */
	public static String ETHBalanceHexToStr(String hexBalance) {
		BigInteger hexBalanceTmp = new BigInteger(hexBalance.substring(2), 16);
		BigDecimal balance = new BigDecimal(hexBalanceTmp.toString());
		BigDecimal balanceWei = new BigDecimal(ETHWEI);
		BigDecimal result = balance.divide(balanceWei,4,RoundingMode.DOWN);
		return result.toString();
	}
	

	/**
	 * ETH余额:16进制 换算为10进制2位小数
	 * @param hexBalance
	 * @return
	 */
	public static String ETHBalanceHexToStr(String hexBalance, int wei) {
		BigInteger hexBalanceTmp = new BigInteger(hexBalance.substring(2), 16);
		BigDecimal balance = new BigDecimal(hexBalanceTmp.toString());
		BigDecimal balanceWei = new BigDecimal(Math.pow(10L, wei));
		BigDecimal result = balance.divide(balanceWei,4,RoundingMode.DOWN);
		return result.toString();
	}
	
	public static void main(String[] args) {
/*		String ethBalanceHexToStr = ETHBalanceHexToStr("0x0000000000000000000000000000000000000000000000000000001045d74a9c",8);
		System.out.println(ethBalanceHexToStr);*/
		String a= "sendtoaddress (\"Dacrsaddress\") \"receive address\" \"amount\"\n\nSend an amount to a given address. The amount is a real and is rounded to the nearest 0.00000001\n\nArguments:\n1. \"Dacrsaddress\"  (string, optional) The Coin address to send to.\n2. receive address   (string, required) The Coin address to receive\n3.\"amount\"   (string, required) \n\nResult:\n\"transactionid\"  (string) The transaction id.\n\nExamples:\n> Dacrsd sendtoaddress \"1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd\" 0.1\n> Dacrsd sendtoaddress \"1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd\" 0.1 \"donation\" \"seans outpost\"\n> curl --user myusername --data-binary '{\"jsonrpc\": \"1.0\", \"id\":\"curltest\", \"method\": \"sendtoaddress\", \"params\": [\"1M72Sfpbz1BPpXFHz9m3CdqATR44Jvaydd\", 0.1, \"donation\", \"seans outpost\"> Dacrsd sendtoaddress \"0-6\" 10 \n> Dacrsd sendtoaddress \"00000000000000000005\" 10 \n> Dacrsd sendtoaddress \"0-6\" \"0-5\" 10 \n> Dacrsd sendtoaddress \"00000000000000000005\" \"0-6\"10 \n] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/\n";
		System.out.println(a);
	}
	
	/**
	 * ETH余额:10进制2位小数换算位16进制
	 * @param Balance
	 * @return
	 */
	public static String ToETHBalanceHex(String Balance) {
		BigDecimal balance = new BigDecimal(Balance);
		BigDecimal balanceWei = new BigDecimal(ETHWEI);
		BigInteger hexBalanceTmp = balance.multiply(balanceWei).toBigInteger();
		String resultStr = "0x" + hexBalanceTmp.toString(16);
		return resultStr;
	}

	/**
	 * ETH余额:10进制2位小数换算位16进制
	 * @param Balance
	 * @return
	 */
	public static String CntractToETHBalanceHex(String Balance, int wei) {
		BigDecimal balance = new BigDecimal(Balance);
		BigInteger hexBalanceTmp;
		if (wei == 0) {
			hexBalanceTmp = balance.toBigInteger();
		} else {
			BigDecimal balanceWei = new BigDecimal(Math.pow(10L, wei));
			hexBalanceTmp = balance.multiply(balanceWei).toBigInteger();
		}
        String resultStr = StringUtils.leftPad(hexBalanceTmp.toString(16), 64, "0");
		return resultStr;
	}
}
