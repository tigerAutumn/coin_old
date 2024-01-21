package com.qkwl.web.filter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qkwl.common.dto.api.FApiAuth;
import com.qkwl.common.redis.RedisConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.qkwl.common.util.RequstLimit;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.common.framework.redis.RedisHelper;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 系统拦截器
 *
 * @author ZKF
 */
public class PrivilegeInteceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(PrivilegeInteceptor.class);
	
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 版本号
     */
    private final static int VERSION = 2;


    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        if (VERSION == 2) {
            return preHandle20(request, response, arg2);
        }
        return true;
    }

    private boolean preHandle20(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        String uri = request.getRequestURI();
        //对外api
        if (UrlController.isApiUrls(uri)) {
            if (!checkSign(request,false)) {
                ReturnResult returnResult = ReturnResult.FAILUER("签名错误");
                PrintWriter writer = response.getWriter();
                writer.write(JSON.toJSONString(returnResult));
                return false;
            }
            return true;
        }

        // 不接受任何jsp请求
        if (uri.endsWith(".jsp")) {
            return false;
        }
        // 只拦截.html结尾的请求
        if (!uri.endsWith(".html")) {
            return false;
        }
        
        if(!reqLimit(arg2, request)) {
        	ReturnResult returnResult = ReturnResult.FAILUER("访问过于频繁，请稍后再试！");
        	PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(returnResult));
            return false;
        }
        return true;
    }

    /**
     * api的签名验证
     *
     * @param httpServletRequest
     * @return
     */
    private boolean checkSign(HttpServletRequest httpServletRequest,boolean isApp) {
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
        Map<String, String> realParams = new HashMap<>();
        //这四个参数是必须的
        if (!parameterMap.containsKey("AccessKeyId") || !parameterMap.containsKey("SignatureVersion")
                || !parameterMap.containsKey("SignatureMethod")
                || !parameterMap.containsKey("Timestamp")
                || !parameterMap.containsKey("Signature")) {
            return false;
        }

        //签名方法不正确
        if (!httpServletRequest.getParameter("SignatureMethod").equals("HmacSHA256")) {
            return false;
        }

        String Signature = httpServletRequest.getParameter("Signature");
        String AccessKeyId = httpServletRequest.getParameter("AccessKeyId");

        //根据 AccessKeyId 获取对应的appSecret
    	String appSecret = findWebApiSecret(AccessKeyId);

        if (null == appSecret || "".equals(appSecret)) {
            return false;
        }

        //获取请求的参数
        while (parameterNames.hasMoreElements()) {
            String s = parameterNames.nextElement();
            //Signature 和 token 不需要参与签名的计算
            if (!"Signature".equals(s) && !"token".equals(s)) {
                realParams.put(s, httpServletRequest.getParameter(s));
            }
        }
        //根据亚马逊的签名规范
        String method = httpServletRequest.getMethod();
        String requestURI = httpServletRequest.getRequestURI();
        boolean bool = check(method, requestURI, realParams, appSecret, Signature, "api.hotcoin.top");
        if(bool) {
        	return true;
        }
        bool = check(method, requestURI, realParams, appSecret, Signature, "hkapi.hotcoin.top");
        if(bool) {
        	return true;
        }
        return false;
    }
    
    
    private boolean check(String method,String requestURI,Map<String, String> realParams,String appSecret,String Signature,String host) {
        //String host = "hotcoin.top";
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n') // GET
                .append(host.toLowerCase()).append('\n') // Host
                .append(requestURI).append('\n'); // /path
        SortedMap<String, String> map = new TreeMap<>(realParams);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }

        sb.deleteCharAt(sb.length() - 1);
        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey =
                    new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (InvalidKeyException e) {
            return false;
        }

        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        //需要对签名进行base64的编码
        String actualSign = Base64.getEncoder().encodeToString(hash);
        Signature = Signature.replace("\n","");
        actualSign = actualSign.replace("\n","");
        if (!Signature.equals(actualSign)) {
            return false;
        }
        return true;
    }

    private String findWebApiSecret(String appKey) {
        FApiAuth apiAuth = redisHelper.getApiAuthByKey(RedisConstant.IS_AUTH_API_KEY + appKey);
        if (apiAuth == null || !apiAuth.isValid()) {
            return null;
        }
        return apiAuth.getFsecretkey();
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }
    
    /**
     * 接口的访问频次限制
     *@param request
     * @return 
     *@return
     */
	private boolean reqLimit(Object handler ,HttpServletRequest request) {
		try {
			if (handler instanceof HandlerMethod) {
		        HandlerMethod hm = (HandlerMethod) handler;
		        // 使用注解
		        RequstLimit requstLimit = hm.getMethodAnnotation(RequstLimit.class);
		        if(requstLimit == null){
		            return true;
		        }
		        
		        String ip = Utils.getIpAddr(request);
		        String url = request.getRequestURL().toString();
		        String key = "req_limit_".concat(url).concat(ip);
		        // 最大数
		        int maxCount = requstLimit.count();
		        key += "_";
		        long count = redisHelper.getIncrByKey(key);
		        if(count == 1) {
		        	redisHelper.set(key, "1" , 60);
		        }
		        
		        if (count > maxCount) {
		            logger.info("用户IP[" + ip + "]访问地址[" + url + "]超过了限定的次数["
		                    + maxCount + "]");
		            // 超过次数，权限拒绝访问，访问太频繁！
		            return false;
		        }
		    }
		    return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
