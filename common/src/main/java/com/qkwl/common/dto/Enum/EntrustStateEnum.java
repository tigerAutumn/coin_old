package com.qkwl.common.dto.Enum;

public enum EntrustStateEnum {
	Going(1, "未成交"),				// 未成交
	PartDeal(2, "部分成交"),			// 部分成交
	AllDeal(3, "完全成交"),			// 完全成交
	WAITCancel(4, "撤单处理中"),		// 等待撤单
	Cancel(5, "已撤销"),			// 撤销
	Null(-1, "已撤销");				// 空，由于mysql查询历史委单表是使用fstatus in (1)是会触发起优化器交叉索引，为防止出现这种情况添加一个不存在的状态避免
	
	private Integer code;
	private String value;
	
	private EntrustStateEnum(int code, String value) {
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
	
	public static String getEntrustStateValueByCode(Integer code) {
		for (EntrustStateEnum entrustsource : EntrustStateEnum.values()) {
			if (entrustsource.getCode().equals(code)) {
				return entrustsource.getValue().toString();
			}
		}
		return null;
	}
}
