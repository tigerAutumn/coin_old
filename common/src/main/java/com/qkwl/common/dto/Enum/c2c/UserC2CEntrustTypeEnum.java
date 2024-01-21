package com.qkwl.common.dto.Enum.c2c;

public enum UserC2CEntrustTypeEnum {
	recharge(1, "充值"),
    withdraw(2, "提现");
    private Integer code;
    private String value;

    private UserC2CEntrustTypeEnum(Integer code, String value) {
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
        for (UserC2CEntrustTypeEnum e: UserC2CEntrustTypeEnum.values()) {
            if (e.getCode().equals(type)) {
                return e.getValue();
            }
        }
        return "";
    }
}
