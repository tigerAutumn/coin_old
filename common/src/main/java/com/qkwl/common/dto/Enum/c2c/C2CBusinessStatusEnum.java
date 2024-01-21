package com.qkwl.common.dto.Enum.c2c;

public enum C2CBusinessStatusEnum {
	Freeze(0, "禁用"),
	Normal(1, "启用");
    private Integer code;
    private String value;

    private C2CBusinessStatusEnum(Integer code, String value) {
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

    public static String getValueByCode(Integer type){
        for (C2CBusinessStatusEnum e: C2CBusinessStatusEnum.values()) {
            if (e.getCode().equals(type)) {
                return e.getValue();
            }
        }
        return "";
    }

}
