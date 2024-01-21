package com.qkwl.common.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 工具类
 * @author ZKF
 */
public class HttpUtil {

	/**
	 * @Title: getNameValuePairs
	 * @Description: TODO(Map转NameValuePair数组)
	 * @param params POST传入参数
	 * @return NameValuePair[] 返回类型
	 */
	public static List<NameValuePair> getNameValuePairs(Map<String, String> params) {
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			nameValuePair.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return nameValuePair;
	}

	/**
	 * @Title: getLinkString
	 * @Description: TODO(把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串)
	 * @param params 	需要排序并参与字符拼接的参数组
	 * @param sort 		true排序，false不排序
	 * @param marks 	true加双引号，false不加双引号
	 * @return String  	拼接后字符串
	 */
	public static String getLinkString(Map<String, String> params, boolean sort, boolean marks) {
		String prestr = "";
		List<String> keys = new ArrayList<String>(params.keySet());
		if(!sort){
			Collections.sort(keys);
		}
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if(marks){
				key 	= "\"" + key 	+ "\"";
				value 	= "\"" + value 	+ "\"";
			}
			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr;
	}
	
	/**
	 * @Title: loadKeyStore
	 * @Description: TODO(加载keyStory，例：tomcat.keystore)
	 * @param keyStoreFilePath
	 * @param password
	 * @param type KeyStore.getDefaultType()
	 * @throws IOException KeyStore  返回类型
	 */
	public static KeyStore loadKeyStore(String keyStoreFilePath, String password, String type){
		FileInputStream is 	= null;
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(type);
			is 	= new FileInputStream(new File(keyStoreFilePath));
			trustStore.load(is, password.toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return trustStore;
	}
	
	public static String sendGet(String url){
        try {
        	  //1.获得一个httpclient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            //2.生成一个get请求
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = null;
            //3.执行get请求并返回结果
            
			response = httpclient.execute(httpget);
			String result = null;
	        //4.处理结果，这里将结果返回为字符串
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            result = EntityUtils.toString(entity);
	        }
	        if(httpclient!=null){
	            httpclient.close();
	        }
	        if(response!=null){
	            response.close();
	        }
	        return result;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
	
	
	 /**
     * 向指定 URL 发送POST方法的请求
     * @param url
     *            发送请求的 URL
     * @param param 请求参数，map
     * @return 所代表远程资源的响应结果
	 * @throws IOException 
	 * @throws ClientProtocolException 
     */
    public static String sendPost(String url, Map<String, String> param){
    	if(param == null){
            return null;
        }
    	try {
    		String resultStr = null;
            //1.获得一个httpclient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                //给参数赋值
                formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            //System.out.println(EntityUtils.toString(entity));
            //2.生成一个post请求
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(entity);
            //3.执行post请求并返回结果
            CloseableHttpResponse response = httpclient.execute(httppost);
            //4.处理结果，这里将结果返回为字符串
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
                resultStr = EntityUtils.toString(responseEntity);
            }
            if(httpclient!=null){
                httpclient.close();
            }
            if(response!=null){
                response.close();
            }
            return resultStr;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public static void main(String[] args) {
//    	Map<String, String> param = new HashMap<>();
//    	param.put("api_key", "AhFQlNqzKwCcvSrMsjlW2FmOo5eQg1K1");
//    	param.put("api_secret", "fE4LOmd3I2XWMb_W24Ltlt3o-F1vumxz");
//    	param.put("return_url", "https://www.hotcoin.top");
//    	param.put("notify_url", "https://www.hotcoin.top");
//    	param.put("biz_no", "12345");
//    	param.put("comparison_type", "1");
//    	param.put("idcard_mode", "3");
//		System.out.println(sendPost("https://api.megvii.com/faceid/lite/get_token", param));
		
		
		Map<String, String> param = new HashMap<>();
    	param.put("api_key", "AhFQlNqzKwCcvSrMsjlW2FmOo5eQg1K1");
    	param.put("api_secret", "fE4LOmd3I2XWMb_W24Ltlt3o-F1vumxz");
    	param.put("biz_id", "1538053170,8f444dc5-a423-467b-ac03-ceb8f97eadd5");
		System.out.println(sendGet("https://api.megvii.com/faceid/lite/get_result?api_key=AhFQlNqzKwCcvSrMsjlW2FmOo5eQg1K1"
				+ "&api_secret=fE4LOmd3I2XWMb_W24Ltlt3o-F1vumxz&biz_id=1538053170,8f444dc5-a423-467b-ac03-ceb8f97eadd5"));
		
//		Map<String, String> param = new HashMap<>();
//    	param.put("api_key", "AhFQlNqzKwCcvSrMsjlW2FmOo5eQg1K1");
//    	param.put("api_secret", "fE4LOmd3I2XWMb_W24Ltlt3o-F1vumxz");
//    	param.put("return_url", "https://www.hotcoin.top");
//    	param.put("notify_url", "https://www.hotcoin.top");
//    	param.put("biz_no", "12345");
//    	param.put("comparison_type", "1");
//    	param.put("idcard_mode", "2");
//		System.out.println(sendPost("https://api.megvii.com/faceid/lite/get_token", param));
	}
}
