package com.qkwl.common.dto.user;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.qkwl.common.dto.Enum.UserStatusEnum;

/**
 * 用户
 * @author LY
 */
public class FUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	// 主键Id
	private Integer fid;
	// 显示ID
	private Integer fshowid;
	//推荐码
	private String fintrocode;
	// 登录名
	private String floginname;
	// 昵称
	private String fnickname;
	// 登录密码
	private String floginpassword;
	// 交易密码
	private String ftradepassword;
	// 绑定手机
	private String ftelephone;
	// 绑定邮箱
	private String femail;
	// 真实姓名
	private String frealname;
	// 身份证号
	private String fidentityno;
	// 实名认证类型
	private Integer fidentitytype;
	// 谷歌验证密匙
	private String fgoogleauthenticator;
	// 谷歌验证URL
	private String fgoogleurl;
	// 用户状态
	private Integer fstatus;
	private String fstatus_s;
	// 是否实名认证
	private Boolean fhasrealvalidate;
	// 实名认证时间
	private Date fhasrealvalidatetime;
	// 是否绑定手机
	private Boolean fistelephonebind;
	// 是否绑定邮箱
	private Boolean fismailbind;
	// 是否绑定谷歌
	private Boolean fgooglebind;
	// 是否视频认证
	private Boolean isVideo;
	// 视频认证时间
	private Date videoTime;
	// 最后更新时间
	private Date fupdatetime;
	// 手机区号
	private String fareacode;
	// 乐观锁
	private Integer version;
	// 推荐人ID
	private Integer fintrouid;
	// 推荐人数
	private Integer finvalidateintrocount;
	// 是否允许人民币提现
	private Integer fiscny;
	private String fiscny_s;
	// 是否允许虚拟币提现
	private Integer fiscoin;
	private String fiscoin_s;
	// 生日
	private Date fbirth;
	// 最后登录时间
	private Date flastlogintime;
	// 注册时间
	private Date fregistertime;
	// 修改交易密码的时间
	private Date ftradepwdtime;
	// 杆杠锁定
	private Integer fleverlock;
	// QQ登录ID
	private String fqqopenid;
	// 微信登录ID
	private String funionid;
	// 劵商ID
	private Integer fagentid;
	// 迁移账户原UID
	private Integer folduid;
	// 账号来源平台
	private Integer fplatform;
	
	//用户收藏交易，JSONArray
	private String fFavoriteTradeList;
	
	private JSONArray tradeList;
	
	private String photo;
	
	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	/**
	 * user表新增字段 
	 * 2018年9月17日16:58:12
	 * @return
	 */
	//是否开启手机验证
	private boolean isOpenPhoneValidate;
	//是否开启谷歌验证
	private boolean isOpenGoogleValidate;
	//是否开启邮箱验证
	private boolean isOpenEmailValidate;
	//是否修改过nickname
	private boolean isHavedModNickname;
	
	
	
	public boolean isOpenPhoneValidate() {
		return isOpenPhoneValidate;
	}

	public void setOpenPhoneValidate(boolean isOpenPhoneValidate) {
		this.isOpenPhoneValidate = isOpenPhoneValidate;
	}

	public boolean isOpenGoogleValidate() {
		return isOpenGoogleValidate;
	}

	public void setOpenGoogleValidate(boolean isOpenGoogleValidate) {
		this.isOpenGoogleValidate = isOpenGoogleValidate;
	}

	public boolean isOpenEmailValidate() {
		return isOpenEmailValidate;
	}

	public void setOpenEmailValidate(boolean isOpenEmailValidate) {
		this.isOpenEmailValidate = isOpenEmailValidate;
	}

	public JSONArray getTradeList() {
		return tradeList;
	}

	public void setTradeList(JSONArray tradeList) {
		this.tradeList = tradeList;
	}

	/********** 扩展字段 ***********/
	// IP
	private String ip;
	// 积分
	private Long score;
	// 等级
	private int level;
	
	private Long flastip;

	public Integer getFid() {
		return fid;
	}

	public void setFid(Integer fid) {
		this.fid = fid;
	}

	public Integer getFshowid() {
		return fshowid;
	}

	public void setFshowid(Integer fshowid) {
		this.fshowid = fshowid;
	}

	public String getFintrocode() {
		return fintrocode;
	}

	public void setFintrocode(String fintrocode) {
		this.fintrocode = fintrocode;
	}

	public String getFloginname() {
		return floginname;
	}

	public void setFloginname(String floginname) {
		this.floginname = floginname == null ? null : floginname.trim();
	}

	public String getFnickname() {
		return fnickname;
	}

	public void setFnickname(String fnickname) {
		this.fnickname = fnickname == null ? null : fnickname.trim();
	}

	public String getFloginpassword() {
		return floginpassword;
	}

	public void setFloginpassword(String floginpassword) {
		this.floginpassword = floginpassword == null ? null : floginpassword.trim();
	}

	public String getFtradepassword() {
		return ftradepassword;
	}

	public void setFtradepassword(String ftradepassword) {
		this.ftradepassword = ftradepassword == null ? null : ftradepassword.trim();
	}

	public String getFtelephone() {
		return ftelephone;
	}

	public void setFtelephone(String ftelephone) {
		this.ftelephone = ftelephone == null ? null : ftelephone.trim();
	}

	public String getFemail() {
		return femail;
	}

	public void setFemail(String femail) {
		this.femail = femail == null ? null : femail.trim();
	}

	public String getFrealname() {
		return frealname;
	}

	public void setFrealname(String frealname) {
		this.frealname = frealname == null ? null : frealname.trim();
	}

	public String getFidentityno() {
		return fidentityno;
	}

	public void setFidentityno(String fidentityno) {
		this.fidentityno = fidentityno == null ? null : fidentityno.trim();
	}

	public Integer getFidentitytype() {
		return fidentitytype;
	}

	public void setFidentitytype(Integer fidentitytype) {
		this.fidentitytype = fidentitytype;
	}

	public String getFgoogleauthenticator() {
		return fgoogleauthenticator;
	}

	public void setFgoogleauthenticator(String fgoogleauthenticator) {
		this.fgoogleauthenticator = fgoogleauthenticator == null ? null : fgoogleauthenticator.trim();
	}

	public String getFgoogleurl() {
		return fgoogleurl;
	}

	public void setFgoogleurl(String fgoogleurl) {
		this.fgoogleurl = fgoogleurl == null ? null : fgoogleurl.trim();
	}

	public Integer getFstatus() {
		return fstatus;
	}

	public void setFstatus(Integer fstatus) {
		this.fstatus = fstatus;
	}

	public Boolean getFhasrealvalidate() {
		return fhasrealvalidate;
	}

	public void setFhasrealvalidate(Boolean fhasrealvalidate) {
		this.fhasrealvalidate = fhasrealvalidate;
	}

	public Date getFhasrealvalidatetime() {
		return fhasrealvalidatetime;
	}

	public void setFhasrealvalidatetime(Date fhasrealvalidatetime) {
		this.fhasrealvalidatetime = fhasrealvalidatetime;
	}

	public Boolean getFistelephonebind() {
		return fistelephonebind;
	}

	public void setFistelephonebind(Boolean fistelephonebind) {
		this.fistelephonebind = fistelephonebind;
	}

	public Boolean getFismailbind() {
		return fismailbind;
	}

	public void setFismailbind(Boolean fismailbind) {
		this.fismailbind = fismailbind;
	}

	public Boolean getFgooglebind() {
		return fgooglebind;
	}

	public void setFgooglebind(Boolean fgooglebind) {
		this.fgooglebind = fgooglebind;
	}

	public Date getFupdatetime() {
		return fupdatetime;
	}

	public void setFupdatetime(Date fupdatetime) {
		this.fupdatetime = fupdatetime;
	}

	public String getFareacode() {
		return fareacode;
	}

	public void setFareacode(String fareacode) {
		this.fareacode = fareacode == null ? null : fareacode.trim();
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getFintrouid() {
		return fintrouid;
	}

	public void setFintrouid(Integer fintrouid) {
		this.fintrouid = fintrouid;
	}

	public Integer getFinvalidateintrocount() {
		return finvalidateintrocount;
	}

	public void setFinvalidateintrocount(Integer finvalidateintrocount) {
		this.finvalidateintrocount = finvalidateintrocount;
	}

	public Integer getFiscny() {
		return fiscny;
	}

	public void setFiscny(Integer fiscny) {
		this.fiscny = fiscny;
	}

	public Integer getFiscoin() {
		return fiscoin;
	}

	public void setFiscoin(Integer fiscoin) {
		this.fiscoin = fiscoin;
	}

	public Date getFbirth() {
		return fbirth;
	}

	public void setFbirth(Date fbirth) {
		this.fbirth = fbirth;
	}

	public Date getFlastlogintime() {
		return flastlogintime;
	}

	public void setFlastlogintime(Date flastlogintime) {
		this.flastlogintime = flastlogintime;
	}

	public Date getFregistertime() {
		return fregistertime;
	}

	public void setFregistertime(Date fregistertime) {
		this.fregistertime = fregistertime;
	}

	public Integer getFleverlock() {
		return fleverlock;
	}

	public void setFleverlock(Integer fleverlock) {
		this.fleverlock = fleverlock;
	}

	public String getFqqopenid() {
		return fqqopenid;
	}

	public void setFqqopenid(String fqqopenid) {
		this.fqqopenid = fqqopenid == null ? null : fqqopenid.trim();
	}

	public String getFunionid() {
		return funionid;
	}

	public void setFunionid(String funionid) {
		this.funionid = funionid == null ? null : funionid.trim();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public String getFstatus_s() {
		if(this.fstatus!=null) {
			return UserStatusEnum.getEnumString(this.fstatus);
		}
		return null;
	}

	public void setFstatus_s(String fstatus_s) {
		this.fstatus_s = fstatus_s;
	}

	public String getFiscny_s() {
		if(this.fiscny != null) {
			return UserStatusEnum.getEnumString(this.fiscny);
		}
		return null;
	}

	public void setFiscny_s(String fiscny_s) {
		this.fiscny_s = fiscny_s;
	}

	public String getFiscoin_s() {
		if(this.fiscoin != null) {
			return UserStatusEnum.getEnumString(this.fiscoin);
		}
		return null;
	}

	public void setFiscoin_s(String fiscoin_s) {
		this.fiscoin_s = fiscoin_s;
	}

	public Integer getFagentid() {
		return fagentid;
	}

	public void setFagentid(Integer fagentid) {
		this.fagentid = fagentid;
	}

	public Long getFlastip() {
		return flastip;
	}

	public void setFlastip(Long flastip) {
		this.flastip = flastip;
	}

	public Date getFtradepwdtime() {
		return ftradepwdtime;
	}

	public void setFtradepwdtime(Date ftradepwdtime) {
		this.ftradepwdtime = ftradepwdtime;
	}

	public Integer getFolduid() {
		return folduid;
	}

	public void setFolduid(Integer folduid) {
		this.folduid = folduid;
	}

	public Integer getFplatform() {
		return fplatform;
	}

	public void setFplatform(Integer fplatform) {
		this.fplatform = fplatform;
	}

	public Boolean getIsVideo() {
		return isVideo;
	}

	public void setIsVideo(Boolean isVideo) {
		this.isVideo = isVideo;
	}

	public Date getVideoTime() {
		return videoTime;
	}

	public void setVideoTime(Date isVideoTime) {
		this.videoTime = isVideoTime;
	}

	public String getfFavoriteTradeList() {
		return fFavoriteTradeList;
	}

	public void setfFavoriteTradeList(String fFavoriteTradeList) {
		this.fFavoriteTradeList = fFavoriteTradeList;
	}

	public boolean getIsHavedModNickname() {
		return isHavedModNickname;
	}

	public void setIsHavedModNickname(boolean isHavedModNickname) {
		this.isHavedModNickname = isHavedModNickname;
	}

	public void setHavedModNickname(boolean isHavedModNickname) {
		this.isHavedModNickname = isHavedModNickname;
	}

	@Override
	public String toString() {
		return "FUser [fid=" + fid + ", fshowid=" + fshowid + ", fintrocode=" + fintrocode + ", floginname="
				+ floginname + ", fnickname=" + fnickname + ", floginpassword=" + floginpassword + ", ftradepassword="
				+ ftradepassword + ", ftelephone=" + ftelephone + ", femail=" + femail + ", frealname=" + frealname
				+ ", fidentityno=" + fidentityno + ", fidentitytype=" + fidentitytype + ", fgoogleauthenticator="
				+ fgoogleauthenticator + ", fgoogleurl=" + fgoogleurl + ", fstatus=" + fstatus + ", fstatus_s="
				+ fstatus_s + ", fhasrealvalidate=" + fhasrealvalidate + ", fhasrealvalidatetime="
				+ fhasrealvalidatetime + ", fistelephonebind=" + fistelephonebind + ", fismailbind=" + fismailbind
				+ ", fgooglebind=" + fgooglebind + ", isVideo=" + isVideo + ", videoTime=" + videoTime
				+ ", fupdatetime=" + fupdatetime + ", fareacode=" + fareacode + ", version=" + version + ", fintrouid="
				+ fintrouid + ", finvalidateintrocount=" + finvalidateintrocount + ", fiscny=" + fiscny + ", fiscny_s="
				+ fiscny_s + ", fiscoin=" + fiscoin + ", fiscoin_s=" + fiscoin_s + ", fbirth=" + fbirth
				+ ", flastlogintime=" + flastlogintime + ", fregistertime=" + fregistertime + ", ftradepwdtime="
				+ ftradepwdtime + ", fleverlock=" + fleverlock + ", fqqopenid=" + fqqopenid + ", funionid=" + funionid
				+ ", fagentid=" + fagentid + ", folduid=" + folduid + ", fplatform=" + fplatform
				+ ", fFavoriteTradeList=" + fFavoriteTradeList + ", tradeList=" + tradeList + ", photo=" + photo
				+ ", isOpenPhoneValidate=" + isOpenPhoneValidate + ", isOpenGoogleValidate=" + isOpenGoogleValidate
				+ ", isOpenEmailValidate=" + isOpenEmailValidate + ", isHavedModNickname=" + isHavedModNickname
				+ ", ip=" + ip + ", score=" + score + ", level=" + level + ", flastip=" + flastip + "]";
	}
	
}