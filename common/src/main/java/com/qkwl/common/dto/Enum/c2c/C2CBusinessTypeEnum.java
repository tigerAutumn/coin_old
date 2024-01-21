package com.qkwl.common.dto.Enum.c2c;

public enum C2CBusinessTypeEnum {
	recharge(1, "充值"),
    withdraw(2, "提现"),
	rechargeAndWithdraw(3,"充值和提现");
    private Integer code;
    private String value;

    private C2CBusinessTypeEnum(Integer code, String value) {
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
        for (C2CBusinessTypeEnum e: C2CBusinessTypeEnum.values()) {
            if (e.getCode().equals(type)) {
                return e.getValue();
            }
        }
        return "";
    }

}
