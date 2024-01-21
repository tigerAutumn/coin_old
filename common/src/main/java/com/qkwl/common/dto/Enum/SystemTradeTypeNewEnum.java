package com.qkwl.common.dto.Enum;

/**
 * 交易类型
 * @author hwj
 *
 */
public enum SystemTradeTypeNewEnum {

	//ETC(3,"ETH交易区"),
	//ETH(2, "BTC交易区"),
	//BTC(1, "GSET交易区");
	FAVORITE(0, "自选市场","自选"),
	GSET(1, "对GSET交易区","GSET"),
	BTC(2, "对BTC交易区","BTC"),
	ETH(3, "对ETH交易区","ETH"),
	USDT(4, "对USDT交易区","USDT");

	private Integer code;
	private Object value;
	private String symbol;

	private SystemTradeTypeNewEnum(Integer code, Object value,String symbol) {
		this.code = code;
		this.value = value;
		this.symbol = symbol;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public static String getValueByCode(Integer code) {
		for (SystemTradeTypeNewEnum coinType : SystemTradeTypeNewEnum.values()) {
			if (coinType.getCode().equals(code)) {
				return coinType.getValue().toString();
			}
		}
		return null;
	}
}
