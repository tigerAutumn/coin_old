package com.qkwl.service.user.impl;

import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.Enum.validate.SendTypeEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.ScoreTypeEnum;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.log.FLogUserAction;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.validate.ValidateParamInfo;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.framework.mq.ScoreHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.google.GoogleAuth;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.v2.UserSecurityService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.service.user.dao.FLogUserActionMapper;
import com.qkwl.service.user.dao.FUserMapper;
import com.qkwl.service.user.utils.MQSend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangchen on 2017-06-22.
 */
@Service("userSecurityV2Service")
public class UserSecurityV2ServiceImpl implements UserSecurityService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserSecurityV2ServiceImpl.class);

    @Autowired
    private MQSend mqSend;
    @Autowired
    private ScoreHelper scoreHelper;
    @Autowired
    private FUserMapper userMapper;
    @Autowired
    private FLogUserActionMapper logUserActionMapper;
    @Autowired
    private ValidationCheckHelper validationCheckHelper;
    @Autowired
    private ValidateHelper validateHelper;

    /**
     * 验证参数合法性
     *
     * @param user      当前数据库的用户信息
     * @param validateParam 前台传过来的用户信息
     * @param action    验证类型
     * @return Result   返回结果<br/>
     * 200 : 成功
     * 1000: 用户不存在！<br/>
     * 1001: 邮箱已绑定！<br/>
     * 1002: 邮箱地址已绑定，请更换邮箱地址！<br/>
     * 1003: 邮箱地址不能为空！<br/>
     * 1004: 手机已绑定！<br/>
     * 1006: 手机号已存在！<br/>
     * 1007: 谷歌已绑定！<br/>
     * 1008: 参数不合法！<br/>
     * 1009: 您没有绑定手机或谷歌验证暂不允许修改密码。<br/>
     * 1010: 登录密码不允许与交易密码一致！<br/>
     * 1011: 请先完成实名认证！<br/>
     * 1012: 请输入身份证号！<br/>
     * 1013: 身份证号码错误！<br/>
     * 1014: 交易密码不允许与登录密码一致<br/>
     * 1015: 5分钟内只能发送一次邮件！<br/>
     * 1016: 邮件发送失败，请稍后再试！<br/>
     * 1017= 验证邮件已发送，请及时验证！<br/>
     * 1018: 原始密码错误！<br/>
     * 1019: 短信发送失败！<br/>
     */
    @Override
    public Result updateUserSecurityInfo(FUser user, ValidateParamInfo validateParam, LogUserActionEnum action, ScoreTypeEnum scoreType){
    	try {
    		FUser userInfo = userMapper.selectByPrimaryKey(user.getFid());
            //参数验证
            Result validateResult = validateSecurityParam(userInfo, user, action);
            if (!validateResult.getSuccess()) {
                return validateResult;
            }

            Result codeResult = validateCode(userInfo,action,validateParam);
            if (!codeResult.getSuccess()) {
                return codeResult;
            }

            Boolean isSendAction = true;
            Object resultData = null;
            switch (action) {
                case ADD_PHONE:
                    userInfo.setFareacode(user.getFareacode());
                    userInfo.setFtelephone(user.getFtelephone());
                    userInfo.setFistelephonebind(false);
                    boolean sendSmsResult= this.validateHelper.smsValidateCode(userInfo.getFid(),
                            userInfo.getFareacode(),userInfo.getFtelephone(), SendTypeEnum.SMS_TEXT.getCode(),
                            PlatformEnum.BC.getCode(), BusinessTypeEnum.SMS_BING_MOBILE.getCode(),validateParam.getLocale().getCode());
                    if (!sendSmsResult){
                        return Result.failure(1019, "短信发送失败！");
                    }
                    break;
                case APP_ADD_EMAIL:
                    userInfo.setFemail(user.getFemail());
                    userInfo.setFismailbind(false);
                    boolean _emailvalidate = this.validateHelper.mailOverdueValidate(user.getFemail(),
                            validateParam.getPlatform(), validateParam.getBusinessType());
                    if (_emailvalidate) {
                        //5分钟内只能发送一次邮件！
                        return Result.failure(1015, "5分钟内只能发送一次邮件！");
                    }

                    boolean _result = this.validateHelper.mailSendCode(user.getFemail(),validateParam.getPlatform(),
                            validateParam.getBusinessType().getCode(),validateParam.getLocale(),validateParam.getIp());
                    if (!_result) {
                        //邮件发送失败，请稍后再试！
                        return Result.failure(1018, "邮件发送失败！");
                    }
                    break;
                case ADD_MAIL:
                    userInfo.setFemail(user.getFemail());
                    userInfo.setFismailbind(false);
                    boolean emailvalidate = this.validateHelper.mailOverdueValidate(user.getFemail(),
                            validateParam.getPlatform(), validateParam.getBusinessType());
                    if (emailvalidate) {
                        //5分钟内只能发送一次邮件！
                        return Result.failure(1015, "5分钟内只能发送一次邮件！");
                    }
                    boolean result = this.validateHelper.mailSendCode(user.getFemail(), validateParam.getPlatform(),validateParam.getBusinessType().getCode(), validateParam.getLocale(), validateParam.getIp());
                    
                    if (!result) {
                        //邮件发送失败，请稍后再试！
                        return Result.failure(1018, "邮件发送失败！");
                    }
                    break;
                case APP_BIND_EMAIL:
                	break;
                case BIND_MAIL:
                    userInfo.setFismailbind(true);
                    break;
                case BIND_PHONE:
                    userInfo.setFtelephone(user.getFtelephone());
                    userInfo.setFistelephonebind(true);
                    userInfo.setFareacode(user.getFareacode());
                    break;
                case MODIFY_PHONE:
                    userInfo.setFtelephone(user.getFtelephone());
                    userInfo.setFareacode(user.getFareacode());
                    if (!userInfo.getFloginname().matches(Constant.EmailReg)) {
                        userInfo.setFloginname(user.getFtelephone());
                        userInfo.setFnickname(user.getFtelephone());
                    }
                    break;
                case MODIFY_GOOGLE:
                    Map<String, String> map = GoogleAuth.genSecret(userInfo.getFloginname().toString());
                    userInfo.setFgoogleauthenticator(map.get("secret"));
                    userInfo.setFgoogleurl(map.get("url"));
                    isSendAction = false;
                    resultData = map;
                    break;
                case BIND_GOOGLE:
                    userInfo.setFgooglebind(true);
                    break;
                case MODIFY_LOGINPWD:
                    userInfo.setFloginpassword(user.getFloginpassword());
                    break;
                case BIND_TRADEPWD:
                	userInfo.setFtradepwdtime(Utils.getTimestamp());
                    userInfo.setFtradepassword(user.getFtradepassword());
                    break;
                case MODIFY_TRADEPWD:
                    userInfo.setFtradepassword(user.getFtradepassword());
                    userInfo.setFtradepwdtime(Utils.getTimestamp());
                    break;
                case MODIFY_SECURITY_SETTINGS:
                	userInfo.setOpenEmailValidate(user.isOpenEmailValidate());
                	userInfo.setOpenGoogleValidate(user.isOpenGoogleValidate());
                	userInfo.setOpenPhoneValidate(user.isOpenPhoneValidate());
                	break;
                case FIND_LOGINPASS:
                	userInfo.setFloginpassword(user.getFloginpassword());
                	break;
                case FIND_TRADE_PWD:
                    userInfo.setFtradepassword(user.getFtradepassword());
                    userInfo.setFtradepwdtime(Utils.getTimestamp());
                    break;
                case GET_GOOGLE:
                	 Map<String, String> map1 = GoogleAuth.genSecret(userInfo.getFloginname().toString());
                     userInfo.setFgoogleauthenticator(map1.get("secret"));
                     userInfo.setFgoogleurl(map1.get("url"));
                     return Result.success("成功",map1);
                case VALIDATE_GOOGLE:
                    return Result.success("成功");
                case BIND_NEW_GOOGLE:
	                userInfo.setFgoogleauthenticator(user.getFgoogleauthenticator());
	                userInfo.setFgoogleurl(user.getFgoogleurl());
	                break;
                case VALIDATE_PHONE:
                	return Result.success("成功");
                case BIND_NEW_PHONE:
                	userInfo.setFareacode(user.getFareacode());
                	userInfo.setFtelephone(user.getFtelephone());
                	break;
    			default:
    				break;

            }
            userInfo.setFupdatetime(Utils.getTimestamp());
            int result = userMapper.updateSecurityByPrimaryKey(userInfo);
            if (result <= 0) {

                return Result.failure("修改用户信息失败");
            }
            if (action != null && isSendAction) {
                // MQ_USER_ACTION
                mqSend.SendUserAction(userInfo.getFagentid(), userInfo.getFid(), action, user.getIp());
            }
            if (scoreType != null) {
                //增加积分
                scoreHelper.SendUserScore(userInfo.getFid(), BigDecimal.ZERO, scoreType.getCode(), scoreType.getValue().toString());
            }

            //发送短信
            if(validateResult.getIsSendMsg() && userInfo.getFistelephonebind()){
                BusinessTypeEnum businessTypeEnum = null;
                switch (validateParam.getBusinessType()){
                    case SMS_MODIFY_LOGIN_PASSWORD:
                        businessTypeEnum = BusinessTypeEnum.SMS_MODIFY_LOGIN_REMIND;
                    case SMS_MODIFY_TRADE_PASSWORD:
                        businessTypeEnum = BusinessTypeEnum.SMS_MODIFY_TRADE_REMIND;
    				default:
    					break;
                }
                if(businessTypeEnum != null){
                    validateHelper.smsSensitiveInfo(userInfo.getFareacode(), userInfo.getFtelephone(), validateParam.getLocale().getCode(),validateParam.getPlatform().getCode(),
                            businessTypeEnum.getCode());
                }
            }

            return Result.success("成功",resultData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result.failure("");
    }


    private Result validateCode(FUser user, LogUserActionEnum action, ValidateParamInfo validateParam){
        switch (action) {
            case ADD_PHONE:
            case ADD_MAIL:
            case APP_ADD_EMAIL:
                break;
            case APP_BIND_EMAIL:
                return validationCheckHelper.getEmailCodeCheck(user.getFemail(), validateParam.getUuid(),
                        BusinessTypeEnum.EMAIL_APP_BIND.getCode() ,
                        validateParam.getIp() , validateParam.getPlatform().getCode());
            case BIND_MAIL:
            	return validationCheckHelper.getEmailCodeCheck(user.getFemail(), validateParam.getEmailCode(), BusinessTypeEnum.EMAIL_SEND_CODE.getCode(), validateParam.getIp(), validateParam.getPlatform().getCode());
            case MODIFY_PHONE:
                Result resultCode = validationCheckHelper.getChangeCheck(user, validateParam.getCode(), BusinessTypeEnum.SMS_UNBIND_MOBILE.getCode(), validateParam.getTotpCode(), validateParam.getIp(), validateParam.getPlatform().getCode());
                if(!resultCode.getSuccess()){
                    return resultCode;
                }
            case BIND_PHONE:
                Result secondResult = validationCheckHelper.getPhoneCodeCheck(validateParam.getSecondAreaCode(), validateParam.getSecondPhone(),
                        validateParam.getSecondCode(), BusinessTypeEnum.SMS_BING_MOBILE.getCode(), validateParam.getIp(), validateParam.getPlatform().getCode());
                if(!secondResult.getSuccess()){
                    if(user.getFistelephonebind()) {
                        if (secondResult.getCode().equals(10004))//更换的手机号码错误多次
                            secondResult.setCode(10014);
                        else if (secondResult.getCode().equals(10005))//更换的手机短信验证码错误
                            secondResult.setCode(10015);
                    }
                }
                return secondResult;
            case MODIFY_GOOGLE:
            	return validateUserSercuritySetting(user,validateParam);
            case BIND_GOOGLE:
                return validationCheckHelper.getGoogleCodeCheck(validateParam.getTotpKey(), validateParam.getTotpCode(), validateParam.getIp());
            case MODIFY_LOGINPWD:
                if(!StringUtils.isEmpty(validateParam.getOriginLoginPwd()) && !user.getFloginpassword().equals(validateParam.getOriginLoginPwd())){
                    return Result.failure(1018, "原始密码错误！");
                }
                return validateUserSercuritySetting(user,validateParam);
            case BIND_TRADEPWD:
            	break;
            case MODIFY_TRADEPWD:
            	if(!StringUtils.isEmpty(validateParam.getOriginLoginPwd()) && !user.getFtradepassword().equals(validateParam.getOriginLoginPwd())){
                    return Result.failure(1018, "原始密码错误！");
                }
                return validateUserSercuritySetting(user,validateParam);
            case MODIFY_SECURITY_SETTINGS:
                return validateUserSercuritySetting(user,validateParam);
            case FIND_LOGINPASS:
                return validateUserSercuritySetting(user,validateParam);
            case FIND_TRADE_PWD:
                return validateUserSercuritySetting(user,validateParam);
            case VALIDATE_GOOGLE:
                return validationCheckHelper.getGoogleCodeCheck(validateParam.getTotpKey(), validateParam.getTotpCode(), validateParam.getIp());
            case BIND_NEW_GOOGLE:
                return validateUserSercuritySetting(user,validateParam);
            case VALIDATE_PHONE:
            	 Result secondResult1 = validationCheckHelper.getPhoneCodeCheck(validateParam.getAreaCode(), validateParam.getPhone(),
                         validateParam.getCode(), validateParam.getBusinessType().getCode(), validateParam.getIp(), validateParam.getPlatform().getCode());
                 if(!secondResult1.getSuccess()){
                     if(user.getFistelephonebind()) {
                         if (secondResult1.getCode().equals(10004))//更换的手机号码错误多次
                             secondResult1.setCode(10014);
                         else if (secondResult1.getCode().equals(10005))//更换的手机短信验证码错误
                             secondResult1.setCode(10015);
                     }
                 }
                 return secondResult1;
            case BIND_NEW_PHONE:
            	 return validateUserSercuritySetting(user,validateParam);
            default:
                break;
        }
        return Result.success();
    }

    /**
     * 验证参数合法性
     *
     * @param user      当前数据库的用户信息
     * @param paramUser 前台传过来的用户信息
     * @param action    验证类型
     * @return Result   返回结果<br/>
     * 200 : 成功
     * 1000: 用户不存在！<br/>
     * 1001: 邮箱已绑定！<br/>
     * 1002: 邮箱地址已绑定，请更换邮箱地址！<br/>
     * 1003: 邮箱地址不能为空！<br/>
     * 1004: 手机已绑定！<br/>
     * 1006: 手机号已存在！<br/>
     * 1007: 谷歌已绑定！<br/>
     * 1008: 参数不合法！<br/>
     * 1009: 您没有绑定手机或谷歌验证暂不允许修改密码。<br/>
     * 1010: 登录密码不允许与交易密码一致！<br/>
     * 1011: 请先完成实名认证！<br/>
     * 1012: 请输入身份证号！<br/>
     * 1013: 身份证号码错误！<br/>
     * 1014: 交易密码不允许与登录密码一致<br/>
     *
     */
    private Result validateSecurityParam(FUser user, FUser paramUser, LogUserActionEnum action) {
        if (user == null) {
            return Result.failure(1000, "用户不存在！");
        }
        Map<String,Object> searchUser = new HashMap<String,Object>();
        searchUser.put("notUid",user.getFid());
        Integer userCount = 0;
        switch (action) {
            case ADD_PHONE:
                if (user.getFistelephonebind()){
                    return Result.failure(1004,"手机已绑定! ");
                }
                searchUser.put("ftelephone",paramUser.getFtelephone());
                userCount = userMapper.getUserCountByParam(searchUser);
                if (userCount != null && userCount > 0){
                    return Result.failure(1004, "手机已绑定!");
                }
                break;
            case APP_ADD_EMAIL:
            case ADD_MAIL:
                if (user.getFismailbind()) {
                    return Result.failure(1001, "邮箱已绑定！");
                }
                //验证邮件地址是否被使用
                searchUser.put("femail",paramUser.getFemail());
                userCount = userMapper.getUserCountByParam(searchUser);
                if(userCount != null && userCount == 1) {
                	if(!paramUser.getFemail().equals(user.getFemail())) {
                		return Result.failure(1002, "邮箱地址已绑定，请更换邮箱地址！");
                	}
                }else if (userCount != null && userCount > 1) {
                	return Result.failure(1002, "邮箱地址已绑定，请更换邮箱地址！");
                }
                break;
            case APP_BIND_EMAIL:
            	break;
            case BIND_MAIL:
                if (user.getFemail() == null) {
                    return Result.failure(1003, "邮箱地址不能为空");
                }
                if (user.getFismailbind()) {
                    return Result.failure(1001, "邮箱已绑定");
                }
                break;
            case BIND_PHONE:
                if (user.getFistelephonebind()) {
                    return Result.failure(1004, "手机已绑定");
                }
                //break;
            case MODIFY_PHONE:
                //验证手机号码是否被使用
                searchUser.put("ftelephone",paramUser.getFtelephone());
                userCount = userMapper.getUserCountByParam(searchUser);
                if (userCount != null && userCount > 0) {
                    return Result.failure(1006, "手机号已存在！");
                }
                break;
            case MODIFY_GOOGLE:
                /*if (user.getFgooglebind()) {
                    return Result.failure(1007, "谷歌已绑定");
                }*/
                break;
            case BIND_GOOGLE:
	        	if (user.getFgooglebind()) {
	                return Result.failure(1007, "谷歌已绑定");
	            }
                if (!(!user.getFgooglebind() && paramUser.getFgoogleauthenticator().equals(user.getFgoogleauthenticator())
                        && !paramUser.getFgoogleauthenticator().trim().equals(""))) {
                    return Result.failure(1008, "参数不合法");
                }
                break;
            case MODIFY_LOGINPWD:
                return validateLoginPassword(user,paramUser);
            case MODIFY_TRADEPWD:
                return validateTradePassword(user,paramUser,action);
            case BIND_TRADEPWD:
                // 绑定交易密码
                if (!StringUtils.isEmpty(paramUser.getFtradepassword()) && user.getFloginpassword().equals(paramUser.getFtradepassword())) {
                    return Result.failure(1014, "交易密码不允许与登录密码一致！");
                }
                break;
            case FIND_TRADE_PWD:
            	// 找回交易密码
                if (!StringUtils.isEmpty(paramUser.getFtradepassword()) && user.getFloginpassword().equals(paramUser.getFtradepassword())) {
                    return Result.failure(1014, "交易密码不允许与登录密码一致！");
                }
                break;
            case VALIDATE_PHONE:
            	searchUser.put("ftelephone",paramUser.getFtelephone());
                userCount = userMapper.getUserCountByParam(searchUser);
                if (userCount != null && userCount > 0){
                    return Result.failure(1004, "手机已绑定!");
                }
                break;
            case BIND_NEW_PHONE:
            	searchUser.put("ftelephone",paramUser.getFtelephone());
                userCount = userMapper.getUserCountByParam(searchUser);
                if (userCount != null && userCount > 0){
                    return Result.failure(1004, "手机已绑定!");
                }
                break;
			default:
				break;
	        }
        return Result.success();
    }
    
    /**
     * 验证登陆密码的参数
     * @param user
     * @param paramUser
     * @return
     */
    private Result validateLoginPassword(FUser user, FUser paramUser){

        // 修改登录密码
        if (!StringUtils.isEmpty(paramUser.getFtradepassword()) && user.getFtradepassword().equals(paramUser.getFloginpassword())) {
            return Result.failure(1010, "登录密码不允许与交易密码一致！");
        }
        boolean isSendMsg = false;
        if (user.getFloginpassword() != null && "86".equals(user.getFareacode())) {
            isSendMsg = true;
        }
        return Result.success(isSendMsg);
    }

    /**
     * 验证交易密码的参数
     * @param user
     * @param paramUser
     * @param action
     * @return
     */
    private Result validateTradePassword(FUser user, FUser paramUser, LogUserActionEnum action){
        //初次设置交易密码的情况，不进行下面验证
        if(action.getCode().equals(LogUserActionEnum.BIND_TRADEPWD.getCode())){
            return Result.success();
        }
        // 修改交易密码
        if (!StringUtils.isEmpty(paramUser.getFtradepassword()) && user.getFloginpassword().equals(paramUser.getFtradepassword())) {
            return Result.failure(1014, "交易密码不允许与登录密码一致！");
        }
        boolean isSendMsg = false;
        if (user.getFtradepassword() != null) {
            isSendMsg = true;
        }
        return Result.success(isSendMsg);
    }

    /**
     * 重置登录密码
     * @param user              用户信息
     * @param validateParam     验证信息
     * @param findType          找回密码的类型
     * 200 : 成功
     * 1000: 该密码找回链接已过期！<br/>
     * 1001: 登录密码不允许与交易密码一致！<br/>
     * 1002: 手机号码不存在，请确认您的账号！<br/>
     * 1003: 未知错误！<br/>
     * 1004: 找回密码失败，请稍后再试！<br/>
     */
    @Override
    public Result restLoginPasword(FUser user, ValidateParamInfo validateParam, Integer findType) {
        FUser userInfo = null;
        if(findType.equals(1)){
            userInfo = userMapper.selectByPrimaryKey(user.getFid());
            if (userInfo == null) {
                return Result.failure(1000,"该密码找回链接已过期！");
            }
            if (user.getFloginpassword().equals(userInfo.getFtradepassword())) {
                return Result.failure(1001,"登录密码不允许与交易密码一致！");
            }
            Result checkResult = validationCheckHelper.getChangeCheck(userInfo, validateParam.getCode(), validateParam.getBusinessType().getCode(),
                    validateParam.getTotpCode(), validateParam.getIp(), validateParam.getPlatform().getCode());
            if (!checkResult.getSuccess()) {
                return checkResult;
            }
        } else{
            userInfo = new FUser();
            userInfo.setFtelephone(user.getFtelephone());
            List<FUser> fusers = this.userMapper.getUserListByParam(userInfo);
            if (fusers == null || fusers.size() < 1) {
                return Result.failure(1002,"手机号码不存在，请确认您的账号！");
            }
            userInfo = fusers.get(0);
            if (user.getFloginpassword().equals(userInfo.getFtradepassword())) {
                return Result.failure(1001,"登录密码不允许与交易密码一致！");
            }

            // 验证谷歌验证码
            if (userInfo.getFgooglebind()) {
                Result checkResult = validationCheckHelper.getGoogleCodeCheck(userInfo.getFgoogleauthenticator(), validateParam.getTotpCode(), validateParam.getIp());
                if (!checkResult.getSuccess()) {
                    return checkResult;
                }
            }
        }
        userInfo.setFloginpassword(user.getFloginpassword());
        userInfo.setFupdatetime(Utils.getTimestamp());
        if(userMapper.updateSecurityByPrimaryKey(userInfo) <= 0){
            return Result.failure(1004,"找回密码失败，请稍后再试！");
        }
        return Result.success();
    }

    /**
     * @param uid 用户ID
     * @param page 分页实体对象
     * @param logType 日志记录类型<br/>
     *            logType = 1 登陆日志
     *            logType = 2 安全设置日志
     * @return Pagination<FLogUserAction>
     */
    @Override
    public Pagination<FLogUserAction> listSettingLogByUser(Integer uid, Pagination<FLogUserAction> page, Integer logType) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("offset", page.getOffset());
        map.put("limit", page.getPageSize());
        map.put("fuid", uid);
        if(logType.equals(1)){
            map.put("ftype", LogUserActionEnum.LOGIN.getCode());
        } else {
            map.put("fmintype", LogUserActionEnum.LOGIN.getCode());
            map.put("fmaxtype", LogUserActionEnum.BIND_IDCARD.getCode());
        }

        int count = logUserActionMapper.countListByUser(map);
        if(count > 0) {
            List<FLogUserAction> logUserList = logUserActionMapper.selectByType(map);
            page.setData(logUserList);
        }
        page.setTotalRows(count);
        page.generate();
        return page;
    }
    
    
    /**
     * 验证用户组合验证
     * @param user
     * @param paramUser
     * @param action
     * @return
     */
	private Result validateUserSercuritySetting(FUser user, ValidateParamInfo validateParam) {
		if (user != null) {
			// 如果用户开启了邮箱验证
			if (user.isOpenEmailValidate()) {
				Result result = validationCheckHelper.getEmailCodeCheck(user.getFemail(), validateParam.getEmailCode(),
						validateParam.getEmailBusinessType().getCode(), validateParam.getIp(),
						validateParam.getPlatform().getCode());
				if (!result.getSuccess()) {
					return result;
				}
			}

			// 如果用户开启了手机验证
			if (user.isOpenPhoneValidate()) {
				Result checkResult = validationCheckHelper.getPhoneCodeCheck(user.getFareacode(),
						user.getFtelephone(), validateParam.getCode(),
						validateParam.getBusinessType().getCode(), validateParam.getIp(),
						validateParam.getPlatform().getCode());
				if (!checkResult.getSuccess()) {
					return checkResult;
				}
			}

			// 如果用户开启了谷歌验证码验证
			if (user.isOpenGoogleValidate()) {
				Result checkResult = validationCheckHelper.getGoogleCodeCheck(user.getFgoogleauthenticator(),
						validateParam.getTotpCode(), validateParam.getIp());
				if (!checkResult.getSuccess()) {
					return checkResult;
				}
			}
		}
		return Result.success();
	}


}
