package com.qkwl.common.dto.Enum;

public enum UserBankInfoDefaultEnum {
	FALSE(0, "非默认"),
	TRUE(1, "默认");

	private Integer code;
	private Object value;

	private UserBankInfoDefaultEnum(Integer code, Object value) {
		this.code = code;
		this.value = value;
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
}
