package com.qkwl.web.front.controller.v2;

import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.Enum.validate.SendTypeEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.validate.ValidateParamInfo;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.rpc.v2.UserSecurityService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.front.controller.ValidateImage.VerifyCodeUtils;
import com.qkwl.web.front.controller.base.JsonBaseController;

import java.io.ByteArrayOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ValidateV2Controller extends JsonBaseController {
	private static final Logger logger = LoggerFactory.getLogger(ValidateV2Controller.class);
	@Autowired
	private IUserService userService;
	@Autowired
	private ValidateHelper validateHelper;
	@Autowired
	private UserSecurityService userSecurityV2Service;
	@Autowired
	private RedisHelper redisHelper;

	@ResponseBody
    @RequestMapping("/v2/validateImage")
    public ReturnResult getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 生成图片
        int w = 100, h = 39;
        VerifyCodeUtils.outputImage(w, h, out, verifyCode);
        
        byte[] bytes = out.toByteArray();
        
        String base64 = Base64.encodeBase64String(bytes);
        super.deletRedisData("CHECKCODE");
        String token = super.setRedisData("CHECKCODE", verifyCode);
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", base64);
        jsonObject.put("imgToken", token);
        
        return ReturnResult.SUCCESS(jsonObject);
    }
	
	
	@ResponseBody
	@RequestMapping({ "/v2/sendPhone"})
	public ReturnResult sendPhone(@RequestParam(required = false, defaultValue = "0") String phone,
			@RequestParam(required = false) String area,
			@RequestParam(required = false) String imgCode,
			@RequestParam(required = true) int type,
			@RequestParam(required = false) String imgToken) throws Exception {
		if ((phone.equals("0"))) {
			return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1024"));
		}
		
		FUser user = getUser();
		if(user == null) {
			FUser param = new FUser();
			param.setFtelephone(StringUtils.isEmpty(phone)?"":phone);
			user = userService.selectUserByParam(param);
			if(user == null) {
				if(StringUtils.isNotEmpty(imgCode)) {
					String session_code = redisHelper.getRedisData(imgToken);
					if ((session_code == null) || (!imgCode.equalsIgnoreCase(session_code))) {
						return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11004"));
					}
				}else {
					return ReturnResult.FAILUER(401,"请登录!");
				}
			}
		}
		
		//登录用户不需要输入区号
		if(user != null) {
			if(StringUtils.isEmpty(area) || area.equals("0")) {
				if(StringUtils.isNotEmpty(user.getFareacode())) {
					area = user.getFareacode();
				}else {
					area = "86";
				}
			}
		}
		try {
			boolean isSuccess = this.validateHelper.smsValidateCode(Integer.valueOf(0), area, phone,
					SendTypeEnum.SMS_TEXT.getCode(), PlatformEnum.BC.getCode(),
					type, super.getLanEnum().getCode());
			if (!isSuccess) {
				return ReturnResult.FAILUER("send failure");
			}
		} catch (BCException e) {
			logger.error(e.getMessage());
			return ReturnResult.FAILUER("send error ");
		}
		return ReturnResult.SUCCESS();
	}

	public static void main(String[] args) {
		System.out.println("w604111589@163.com".matches(Constant.EmailReg));
	}
	
	@ResponseBody
	@RequestMapping({ "/v2/sendEmail" })
	public ReturnResult sendEmail(@RequestParam(required = true) String email,
			@RequestParam(required = false) String imgCode,
			@RequestParam(required = true) int type,
			@RequestParam(required = false) String imgToken) throws Exception {
		HttpServletRequest request = this.sessionContextUtils.getContextRequest();
		if (!email.matches(Constant.EmailReg)) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11007"));
		}
		
		FUser user = getUser();
		if(user == null) {
			FUser param = new FUser();
			param.setFemail(StringUtils.isEmpty(email)?"":email);
			user = userService.selectUserByParam(param);
			if(user == null) {
				if(StringUtils.isNotEmpty(imgCode)) {
					String session_code = redisHelper.getRedisData(imgToken);
					if ((session_code == null) || (!imgCode.equalsIgnoreCase(session_code))) {
						return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11004"));
					}
				}else {
					return ReturnResult.FAILUER(401,"请登录!");
				}
			}
		}
		
		boolean emailvalidate = this.validateHelper
				.mailOverdueValidate(email, PlatformEnum.BC, BusinessTypeEnum.getBusinessTypeByCode(type)).booleanValue();
		if (emailvalidate) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11006"));
		}
		boolean result = false;
		try {
			result = this.validateHelper.mailSendCode(email, PlatformEnum.BC,type, super.getLanEnum(),
					 Utils.getIpAddr(request)).booleanValue();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			result = false;
		}
		if (result) {
			return ReturnResult.SUCCESS();
		}
		return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1016"));
	}
	
	//发送邮箱验证码
	@ResponseBody
	@RequestMapping({ "/v2/send_email_bind_code"})
	public ReturnResult sendEmailCode(@RequestParam(required = true) String email) throws Exception {
		try {
			FUser user = getUser();
			
			if(user == null) {
				return ReturnResult.FAILUER("请登录！");
			}
			
			if(user.getFismailbind()) {
				//客户已绑定邮箱
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11020"));
			}
			if (!email.matches(Constant.EmailReg)) {
				//邮箱错误
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11007"));
			}
			
			user.setFemail(email);
			user.setFismailbind(false);
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);
			paramInfo.setLocale(getLanEnum());
			
			Result result = userSecurityV2Service.updateUserSecurityInfo(user, paramInfo, LogUserActionEnum.ADD_MAIL, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				updateUserInfo(user);
				return ReturnResult.SUCCESS(super.GetR18nMsg("common.succeed.200"));
			}   else if (result.getCode() >= 10000) {
	            return ReturnResult.FAILUER(GetR18nMsg("com.validate.error." + result.getCode(), result.getData()));
	        } else {
	            return ReturnResult.FAILUER(GetR18nMsg("user.security.error." + result.getCode()));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10001"));
	}
	
	//通过验证码绑定邮箱
	@ResponseBody
	@RequestMapping({ "/v2/email_bind"})
	public ReturnResult emailBind(@RequestParam(required = true) String code) throws Exception {
		
		FUser user = getUser();
		
		if(user == null) {
			return ReturnResult.FAILUER("请登录！");
		}
		
		if(user.getFismailbind()) {
			//客户已绑定邮箱
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11020"));
		}
		
		user.setFismailbind(true);
		ValidateParamInfo paramInfo = new ValidateParamInfo();
		paramInfo.setIp(getIpAddr());
		paramInfo.setPlatform(PlatformEnum.BC);
		paramInfo.setBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);
		paramInfo.setLocale(getLanEnum());
		paramInfo.setEmailCode(code);
		
		Result result = userSecurityV2Service.updateUserSecurityInfo(user, paramInfo, LogUserActionEnum.BIND_MAIL, null);
		if(result.getSuccess()) {
			//更新redis中的用户信息
			updateUserInfo(user);
			return ReturnResult.SUCCESS(super.GetR18nMsg("common.succeed.200"));
		}   else if (result.getCode() >= 10000) {
            return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
        } else {
            return ReturnResult.FAILUER(GetR18nMsg("user.security.error." + result.getCode()));
        }
	}
	
	@ResponseBody
    @RequestMapping("/v2/send_phone_bind_code")
    public ReturnResult postPhone(@RequestParam(required = false, defaultValue = "0") String phone,
                                  @RequestParam(required = false, defaultValue = "0") String area) throws Exception {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FUser fuser = getCurrentUserInfoByToken();
        
        if(fuser == null) {
			return ReturnResult.FAILUER("请登录！");
		}
        
        if (phone.equals("0")) {
            //判断手机是否为空
            return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1003"));
        }
        fuser.setFtelephone(phone);

        String ip = Utils.getIpAddr(request);

        ValidateParamInfo paramInfo = new ValidateParamInfo();
        paramInfo.setIp(ip);

        paramInfo.setPlatform(PlatformEnum.BC);
        paramInfo.setBusinessType(BusinessTypeEnum.SMS_BING_MOBILE);
        paramInfo.setLocale(super.getLanEnum());
        paramInfo.setAreaCode(area);
        paramInfo.setPhone(phone);

        Result validateResult = this.userSecurityV2Service.updateUserSecurityInfo(fuser, paramInfo, LogUserActionEnum.ADD_PHONE, null);
        if (!validateResult.getSuccess()) {
            return ReturnResult.FAILUER(validateResult.getCode(),GetR18nMsg("user.security.error." + validateResult.getCode()));
        }
        //验证短信已经发送，请及时验证！
        return ReturnResult.SUCCESS();
    }

    @ResponseBody
    @RequestMapping("/v2/phone_bind")
    public ReturnResult bindPhone(@RequestParam(required = false, defaultValue = "0") String phone,
                                  @RequestParam(required = false, defaultValue = "0") String area,
                                  @RequestParam(required = false, defaultValue = "0") String code) throws Exception {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FUser fuser = getCurrentUserInfoByToken();
        
        if(fuser == null) {
			return ReturnResult.FAILUER("请登录！");
		}
        
        if (phone.equals("0")) {
            //判断手机是否为空
            return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1024"));
        }
        fuser.setFtelephone(phone);
        fuser.setFareacode(area);
        String ip = Utils.getIpAddr(request);
        fuser.setIp(ip);
        ValidateParamInfo paramInfo = new ValidateParamInfo();
        paramInfo.setIp(ip);
        paramInfo.setPlatform(PlatformEnum.BC);
        paramInfo.setBusinessType(BusinessTypeEnum.SMS_BING_MOBILE);
        paramInfo.setLocale(super.getLanEnum());
        paramInfo.setSecondAreaCode(area);
        paramInfo.setSecondPhone(phone);
        paramInfo.setSecondCode(code);
        paramInfo.setCode(code);
        fuser.setFistelephonebind(true);
        
        Result validateResult = this.userSecurityV2Service.updateUserSecurityInfo(fuser, paramInfo,
                LogUserActionEnum.BIND_PHONE, null);
        if (validateResult.getSuccess()) {
            return ReturnResult.SUCCESS();
        }else if(validateResult.getCode() >= 10000){
			return ReturnResult.FAILUER(validateResult.getCode(),GetR18nMsg("com.validate.error." + validateResult.getCode(), validateResult.getData()));
		} else {
			return ReturnResult.FAILUER(validateResult.getCode(),GetR18nMsg("user.security.error." + validateResult.getCode()));
		}
        //验证短信已经发送，请及时验证！
    }
}
