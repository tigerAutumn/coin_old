package com.qkwl.common.dto.user;

import java.util.Date;

/**
 * 用户收藏trade对象
 * @author hwj
 */
public class FUserFavoriteTrade {
	
    private Integer id;

    //用户id
    private Integer fuid; 
    
    //用户收藏id的listjson
    private String ffavoritetradelist; 

    //创建时间
    private Date fcreatetime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFuid() {
        return fuid;
    }

    public void setFuid(Integer fuid) {
        this.fuid = fuid;
    }

    public String getFfavoritetradelist() {
        return ffavoritetradelist;
    }

    public void setFfavoritetradelist(String ffavoritetradelist) {
        this.ffavoritetradelist = ffavoritetradelist == null ? null : ffavoritetradelist.trim();
    }

    public Date getFcreatetime() {
        return fcreatetime;
    }

    public void setFcreatetime(Date fcreatetime) {
        this.fcreatetime = fcreatetime;
    }
}