package com.qkwl.common.dto.Enum.c2c;

public enum UserC2CEntrustStatusEnum {
	processing(1, "处理中"),
	success(2, "已完成"),
	cancel(3, "已撤销"),
	close(4,"已关闭"),
	wait(5,"待确认");
    private Integer code;
    private String value;

    private UserC2CEntrustStatusEnum(Integer code, String value) {
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
        for (UserC2CEntrustStatusEnum e: UserC2CEntrustStatusEnum.values()) {
            if (e.getCode().equals(type)) {
                return e.getValue();
            }
        }
        return "";
    }
}
