package com.qkwl.common.dto.activity;

import java.io.Serializable;
import java.util.Date;

public class ActivityConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//活动ID
	private Integer id;
	//活动名称
	private String activityName;
	//开始时间
	private Date startTime;
	//结束时间
	private Date endTime;
	//更新时间
	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
