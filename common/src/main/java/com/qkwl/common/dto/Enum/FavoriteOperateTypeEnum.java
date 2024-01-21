package com.qkwl.common.dto.Enum;

public enum FavoriteOperateTypeEnum {

	ADD(1, "增加"),		
	GET(0, "查询"),
	REMOVE(-1,"删除");
	
	private Integer code;
	private String value;
	
	private FavoriteOperateTypeEnum(int code, String value) {
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
	
	public static String getTypeValueByCode(Integer code) {
		for (FavoriteOperateTypeEnum e : FavoriteOperateTypeEnum.values()) {
			if (e.getCode().equals(code)) {
				return e.getValue().toString();
			}
		}
		return null;
	}
}
