package com.qkwl.common.dto.Enum;

public enum USDTCollectStatusEnum {
	UNCOLLECT(0, "未收集"),		
	FINISHED(1, "已结束"); 	

	private Integer code;
	private String value;
	
	USDTCollectStatusEnum(int code, String value) {
		this.code = code;
		this.value = value;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static USDTCollectStatusEnum getEnumByCode(Integer code) {
		for (USDTCollectStatusEnum entrustsource : USDTCollectStatusEnum.values()) {
			if (entrustsource.getCode().equals(code)) {
				return entrustsource;
			}
		}
		return null;
	}
}
