package com.qkwl.common.util;

/**
 * 账号相关工具
 */
public class AccountUtil {

	/**
	 * 使账号模糊
	 */
	public static String blurAccount(String account) {
		int allLength = account.length();
		if (account.substring(allLength-3).equals("com")) {
			int indexOf = account.indexOf("@");
			String front = account.substring(0, indexOf);
			String behind = account.substring(indexOf);
			int length = front.length();
			String forthStr = front.substring(0, length-3);
			account = forthStr + "***" + behind;
		} else {
			account = account.substring(0, 3) + "****" +account.substring(allLength-4);
		}
		return account;
	}
	
}
