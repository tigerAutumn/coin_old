package com.qkwl.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by hwj on 20180825
 */
public class CoinCommentUtils {

    private static final Logger logger = LoggerFactory.getLogger(CoinCommentUtils.class);

    /**
      * fod地址加密过程
      */
    public static String FODEncode(String address){
    	if(StringUtils.isEmpty(address) ) {
    		return null;
    	}
    	address = address.trim();
    	if(!address.startsWith("0x")) {
    		return null;
    	}
    	address = address.substring(2, address.length());
    	StringBuffer sb = new StringBuffer(address);
    	address = sb.reverse().toString().toUpperCase();
		return address;
    }
    /**
     * fod地址解密过程
     */
    public static String FODDecode(String address){
    	if(StringUtils.isEmpty(address)) {
    		return null;
    	}
    	address = address.trim();
    	StringBuffer sb = new StringBuffer(address);
    	address ="0x" + sb.reverse().toString().toLowerCase();
        return address;
    }
    
    public static Date formatDate(String time){
        SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
    * 把字符串转换时间
    *
    * @param dateStr 2018-08-29T08:45:18
    * @return 时间
    */
    public static Date fromISODate(String time){
        if(!time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")){
            return null;
        }
        time=time.replaceFirst("T", " ");
        Date date=formatDate(time);
         // 1、取得本地时间：
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, (zoneOffset + dstOffset));
        return cal.getTime();
    }

   
}
