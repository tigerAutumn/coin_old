package com.qkwl.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValiUtil {
	
	
    /**
     * 正则表达式校验邮箱
     * @param emaile 待匹配的邮箱
     * @return 匹配成功返回true 否则返回false;
     */
    public static boolean checkEmaile(String emaile){
        String RULE_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        //正则表达式的模式
        Pattern p = Pattern.compile(RULE_EMAIL);
        //正则表达式的匹配器
        Matcher m = p.matcher(emaile);
        //进行正则匹配
        return m.matches();
    }
    
    public static void main(String[] args) {
    	boolean checkEmaile = checkEmaile("377872755@qq.com");
    	System.out.println(checkEmaile);
	}
    
    
    
    
}
