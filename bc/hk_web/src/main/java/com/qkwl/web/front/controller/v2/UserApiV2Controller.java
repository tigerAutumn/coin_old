package com.qkwl.web.front.controller.v2;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.dto.Enum.IdentityStatusEnum;
import com.qkwl.common.dto.Enum.LogUserActionEnum;
import com.qkwl.common.dto.Enum.UserLoginType;
import com.qkwl.common.dto.common.DeviceInfo;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserIdentity;
import com.qkwl.common.dto.user.LoginResponse;
import com.qkwl.common.dto.user.RequestUserInfo;
import com.qkwl.common.dto.validate.ValidateParamInfo;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.oss.OssHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.http.HttpUtil;
import com.qkwl.common.match.MathUtils;
import com.qkwl.common.redis.RedisConstant;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.common.rpc.user.IUserIdentityService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.rpc.v2.UserSecurityService;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.DateUtils;
import com.qkwl.common.util.PhoneValiUtil;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;
import com.qkwl.web.config.FaceIdProperties;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

@Controller
public class UserApiV2Controller extends JsonBaseController{
	
	private static final Logger logger = LoggerFactory.getLogger(UserApiV2Controller.class);
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private RedisHelper redisHelper;
	
	@Autowired
	private IUserIdentityService userIdentityService;

	@Autowired
	private UserSecurityService userSecurityService;
	@Autowired
	private IUserWalletService userWalletService;
	
	@Autowired
    private FaceIdProperties faceIdProperties;
	
	@Autowired
	private OssHelper ossHelper;
	
	//新版获取实名认证接口token
	@ResponseBody
	@RequestMapping(value = "/v2/getValidateToken")
	public ReturnResult getValidateToken() {
		FUser user = getUser();
		
		if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1000"));
        }
		
		//如果用户实名认证过则返回错误
		if(user.getFhasrealvalidate()) {
			return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
		}
		
		String loginToken = null;
		String webToken = sessionContextUtils.getContextToken("token");
	    String appToken = sessionContextUtils.getContextApiToken();
		
		if(StringUtils.isNotEmpty(webToken)) {
			loginToken = webToken;
		}else {
			loginToken = appToken;
		}
		
		Map<String, String> param = new HashMap<>();
    	param.put("api_key", faceIdProperties.getApi_key());
    	param.put("api_secret", faceIdProperties.getApi_secret());
    	param.put("return_url", faceIdProperties.getReturn_url()+"?biz_no="+user.getFid());
    	param.put("notify_url", faceIdProperties.getNotify_url()+"?biz_no="+user.getFid()+"&token="+loginToken);
    	param.put("biz_no", user.getFid()+"");
    	param.put("comparison_type", faceIdProperties.getComparison_type());
    	param.put("idcard_mode", faceIdProperties.getIdcard_mode());
    	param.put("web_title", faceIdProperties.getIdcard_mode());
		
    	//判断用户限制，每天只能认证3次
    	String key = "faceId_token_limit_"+user.getFid();
        //从redis获取count
        String countStr = redisHelper.get(key);
        if(StringUtils.isNotEmpty(countStr)) {
        	int count = Integer.parseInt(countStr);
            
            if(count > 3) {
            	return ReturnResult.FAILUER("每天只能实名认证三次！");
            }
        }
		//请求旷视API获取token
		String resultStr = HttpUtil.sendPost(faceIdProperties.getToken_url(), param);
		JSONObject result = JSONObject.parseObject(resultStr);
		String token = result.getString("token");
		redisHelper.setRedisData("faceId_token_"+user.getFid(), result);
		return ReturnResult.SUCCESS(200,"https://api.megvii.com/faceid/lite/do?token="+token);
	}
	
	//新版获取实名认证接口token
	@ResponseBody
	@RequestMapping(value = "/v2/returnUrl")
	public void returnUrl(String biz_no, HttpServletResponse response) {
		try{
			String successHtml = "<!DOCTYPE html>\r\n" + 
					"<html>\r\n" + 
					"<head>\r\n" + 
					"<meta charset=\"UTF-8\">\r\n" + 
					"<title>热币网</title>\r\n" + 
					"<style type=\"text/css\">\r\n" + 
					"	.face-id-request{text-align:center;font-size:2.5em;padding:50px;font-family:Arial}\r\n" + 
					"</style>\r\n" + 
					"</head>\r\n" + 
					"<body>\r\n" + 
					"<div class=\"face-id-request\">\r\n" + 
					"    <p>您的实名验证已成功提交。</p>\r\n" + 
					"    <p>请返回 我的账户 页面查看验证结果。</p>\r\n" + 
					"</div>\r\n" + 
					"</body>\r\n" + 
					"</html>";
			
			String cancelHtml = "<!DOCTYPE html>\r\n" + 
					"<html>\r\n" + 
					"<head>\r\n" + 
					"<meta charset=\"UTF-8\">\r\n" + 
					"<title>热币网</title>\r\n" + 
					"<style type=\"text/css\">\r\n" + 
					"	.face-id-request{text-align:center;font-size:2.5em;padding:50px;font-family:Arial}\r\n" + 
					"</style>\r\n" + 
					"</head>\r\n" + 
					"<body>\r\n" + 
					"<div class=\"face-id-request\">\r\n" + 
					"    <p>您的实名验证提交失败。</p>\r\n" + 
					"    <p>请尝试刷新并重新提交。</p>\r\n" + 
					"</div>\r\n" + 
					"</body>\r\n" + 
					"</html>";
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");

			String resultStr = redisHelper.getRedisData("faceId_token_"+biz_no);
			if(StringUtils.isEmpty(biz_no) || StringUtils.isEmpty(resultStr)) {
				response.getWriter().write(cancelHtml);
				response.flushBuffer();
				return;
			}
			
			JSONObject result = JSONObject.parseObject(resultStr);
			String biz_id = result.getString("biz_id");
			
			String url = faceIdProperties.getResult_url()+"?api_key="+faceIdProperties.getApi_key()
			+"&api_secret="+faceIdProperties.getApi_secret()+"&biz_id="+biz_id;
			String getResultStr = HttpUtil.sendGet(url);
			
			JSONObject getResult = JSONObject.parseObject(getResultStr);
			
			if(getResult != null) {
				//判断实名认证状态，如果不为ok的话则是正在进行中或者
				String status = getResult.getString("status");
				if(StringUtils.isNotEmpty(status)) {
					//如果是在进行中PROCESSING状态
					if(status.equals("OK")) {
						response.getWriter().write(successHtml);
						response.flushBuffer();
						return;
					}else if(status.equals("CANCELLED") || status.equals("FAILED")) {
						//返回错误页面
						response.getWriter().write(cancelHtml);
						response.flushBuffer();
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//新版获取实名认证接口token
	@ResponseBody
	@RequestMapping(value = "/v2/getValidateStatus")
	public ReturnResult getValidateStatus() {
		FUser user = getUser();
		try{
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1000"));
	        }
			
			// 用户实名认证状态key
			String statusKey = "faceId_result_" + user.getFid();
			
			String resultStr = redisHelper.getRedisData("faceId_token_"+user.getFid());
			if(StringUtils.isEmpty(resultStr)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
			JSONObject result = JSONObject.parseObject(resultStr);
			String biz_id = result.getString("biz_id");
			
			String url = faceIdProperties.getResult_url()+"?api_key="+faceIdProperties.getApi_key()
			+"&api_secret="+faceIdProperties.getApi_secret()+"&biz_id="+biz_id;
			String getResultStr = HttpUtil.sendGet(url);
			
			JSONObject getResult = JSONObject.parseObject(getResultStr);
			
			if(getResult != null) {
				//判断实名认证状态，如果不为ok的话则是正在进行中或者
				String status = getResult.getString("status");
				if(StringUtils.isNotEmpty(status)) {
					//如果是在进行中PROCESSING状态
					if(status.equals("OK")) {
						//如果验证流程走完了，则需要保存用户实名认证信息
						String resultString = redisHelper.getRedisData(statusKey);
						JSONObject object = JSONObject.parseObject(resultString);
						int code = object.getIntValue("code");
						String msg = object.getString("msg");
						
						if(code == 200) {
							return ReturnResult.SUCCESS();
						}else {
							return ReturnResult.FAILUER(msg);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ReturnResult.FAILUER(301,"认证进行中！");
	}
	
	@ResponseBody
	@RequestMapping(value = "/v2/notifyValidate")
	public void notifyValidate(String biz_no, HttpServletResponse response) throws Exception {
		try {
			// 用户实名认证状态key
			String statusKey = "faceId_result_" + biz_no;

			String resultStr = redisHelper.getRedisData("faceId_token_" + biz_no);
			if (StringUtils.isEmpty(resultStr)) {
				System.out.println("resultStr-----"+resultStr);
				return;
			}

			JSONObject result = JSONObject.parseObject(resultStr);
			String biz_id = result.getString("biz_id");
//			String biz_id = "1539846653,e756cc9a-81a7-490b-8afe-b5ed7d7d0607";

			String url = faceIdProperties.getResult_url() + "?api_key=" + faceIdProperties.getApi_key() + "&api_secret="
					+ faceIdProperties.getApi_secret() + "&biz_id=" + biz_id;
//					+ "&return_image=4";
			String getResultStr = HttpUtil.sendGet(url);

			JSONObject getResult = JSONObject.parseObject(getResultStr);

			System.out.println("getResult.size：-----"+getResult.size());
			String key = "faceId_token_limit_" + biz_no;
			if (getResult != null) {
				// 判断实名认证状态，如果不为ok的话则是正在进行中或者
				String status = getResult.getString("status");
				if (StringUtils.isNotEmpty(status)) {
					// 如果是在进行中PROCESSING状态
					if (status.equals("OK")) {
						// 如果验证流程走完了，则需要保存用户实名认证信息
						// 获取verify_result ，判断error_message
						// 1、判断error_message是否为空
						JSONObject verify_result = getResult.getJSONObject("verify_result");
						if (verify_result != null) {
							String error_message = verify_result.getString("error_message");
							// 如果error_message为空则说明没有任何错误，
							if (StringUtils.isEmpty(error_message)) {
								// 获取result_faceid
								JSONObject result_faceid = verify_result.getJSONObject("result_faceid");
								if (result_faceid != null) {
									String confidenceStr = result_faceid.getString("confidence");
									Float confidence = Float.valueOf(confidenceStr);
									// 分数大于60
									if (confidence.compareTo(60f) > 0 || confidence.compareTo(60f) == 0) {
										JSONObject idcard_info = getResult.getJSONObject("idcard_info");
										if (idcard_info != null) {
											// 查重
											JSONObject front_side = idcard_info.getJSONObject("front_side");
											if (front_side != null) {
												JSONObject ocr_result = front_side.getJSONObject("ocr_result");
												if (ocr_result != null) {
													String name = ocr_result.getString("name");
													String id_card_number = ocr_result.getString("id_card_number");
													int id = Integer.parseInt(biz_no);

													FUserIdentity identityParam = userIdentityService
															.selectByCode(id_card_number);
													if (identityParam != null) {
														int sec = DateUtils.getOffSeconds_abs(new Date(),DateUtils.getCurrentDay());
														redisHelper.getIncrByKey(key,sec);

														JSONObject resultObj = new JSONObject();
														resultObj.put("code", 300);
														resultObj.put("msg", "身份证号码已被实名验证，不能重复验证，请注意您的信息安全！");
														redisHelper.setRedisData(statusKey, resultObj);
														return;
													}

													FUser param = new FUser();
													param.setFidentityno(id_card_number);
													List<FUser> users = userService.selectUserListByParam(param);
													if (users != null && users.size() > 0) {
														int sec = DateUtils.getOffSeconds_abs(new Date(),DateUtils.getCurrentDay());
														redisHelper.getIncrByKey(key,sec);

														JSONObject resultObj = new JSONObject();
														resultObj.put("code", 300);
														resultObj.put("msg", "身份证号码已被实名验证，不能重复验证，请注意您的信息安全！");
														redisHelper.setRedisData(statusKey, resultObj);
														return;
													}

													FUserIdentity identity = new FUserIdentity();
													identity.setFuid(id);
													identity.setFname(name);
													identity.setFtype(0);
													identity.setFcountry("中国大陆(China)");
													identity.setFcode(id_card_number);
													identity.setFstatus(IdentityStatusEnum.PASS.getCode());
													identity.setFcreatetime(new Date());
													identity.setIp(getIpAddr());
													
													
//													//上传 图片到OSS
//													JSONObject images = getResult.getJSONObject("images");
//													
//													System.out.println("images：-----"+images.size());
//													
//													String image_best = images.getString("image_best");
//													String image_idcard_back = images.getString("image_idcard_back");
//													String image_idcard_front = images.getString("image_idcard_front");
//													Base64 base64 = new Base64();
//													
//													//设置文件夹前缀
//													String keyPrix = "hotcoin/"+id;
//													System.out.println("前缀：-----"+keyPrix);
//													byte[] image_best_byte = base64.decode(image_best);
//													String image_best_result = ossHelper.uploadValidateFile(image_best_byte, keyPrix);
//													
//													System.out.println("image_best_result：-----"+image_best_result);
//													byte[] image_idcard_back_byte = base64.decode(image_idcard_back);
//													String image_idcard_back_result = ossHelper.uploadValidateFile(image_idcard_back_byte, keyPrix);
//													
//													System.out.println("image_idcard_back_result：-----"+image_idcard_back_result);
//													byte[] image_idcard_front_byte = base64.decode(image_idcard_front);
//													String image_idcard_front_result = ossHelper.uploadValidateFile(image_idcard_front_byte, keyPrix);
//													
//													System.out.println("image_idcard_front_result：-----"+image_idcard_front_result);
//													identity.setImageBest(image_best_result);
//													identity.setImageIdcardBack(image_idcard_back_result);
//													identity.setImageIdcardFront(image_idcard_front_result);
													identity.setBizId(biz_id);
													identity.setNewValidate(true);
													userIdentityService.updateNormalIdentity(identity);

													FUser user = userService.selectUserById(id);
													user.setFhasrealvalidate(true);
													user.setFidentityno(id_card_number);
													user.setFidentitytype(0);
													user.setFrealname(name);
													user.setFhasrealvalidatetime(Utils.getTimestamp());
													userService.updateByPrimaryKey(user);
													updateUserInfo(user);
													
													JSONObject resultObj = new JSONObject();
													resultObj.put("code", 200);
													resultObj.put("msg", "成功");
													redisHelper.setRedisData(statusKey, resultObj);
												}
											}
										}
									} else {
										int sec = DateUtils.getOffSeconds_abs(new Date(),DateUtils.getCurrentDay());
										redisHelper.getIncrByKey(key,sec);

										JSONObject resultObj = new JSONObject();
										resultObj.put("code", 300);
										resultObj.put("msg", "实名认证未通过！");
										redisHelper.setRedisData(statusKey, resultObj);
										return;
									}
								}
							} else {
								int sec = DateUtils.getOffSeconds_abs(new Date(),DateUtils.getCurrentDay());
								redisHelper.getIncrByKey(key,sec);

								JSONObject resultObj = new JSONObject();
								resultObj.put("code", 300);
								resultObj.put("msg", "实名认证未通过！");
								redisHelper.setRedisData(statusKey, resultObj);
								return;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 新版登录
	 */
	@ResponseBody
	@RequestMapping(value = "/v1/login")
	public ReturnResult login_v1(
			@RequestParam(required = false, defaultValue = "0") Integer type,
			@RequestParam(required = true) String loginName,
			@RequestParam(required = true) String password
	)throws Exception {
		// 获取IP地址
		String ip = getIpAddr();

		if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password)){
			return ReturnResult.FAILUER(GetR18nMsg("user.login.error.1000"));
		}
		
		DeviceInfo deviceInfo = getDeviceInfo();
		
		// 登录参数
		RequestUserInfo requestUserInfo = new RequestUserInfo();
		requestUserInfo.setFloginname(loginName);
		requestUserInfo.setType(type);
		requestUserInfo.setFagentid(WebConstant.BCAgentId);
		requestUserInfo.setPlatform(PlatformEnum.BC);
		requestUserInfo.setDeviceInfo(deviceInfo);
		// 登录
		try {
			requestUserInfo.setFloginpassword(password);
			Result result = userService.updateCheckLogin(requestUserInfo, UserLoginType.getUserLoginTypeByCode(deviceInfo.getPlatform()), ip, super.getLanEnum());
			if(result.getCode() == 200){
				LoginResponse login = (LoginResponse) result.getData();
				// 设置登录成功的Token
				sessionContextUtils.addContextToken("token",login.getToken());
				//返回对象去除谷歌
				login.getUserinfo().setFgoogleauthenticator("");
				login.getUserinfo().setFgoogleurl("");
				return ReturnResult.SUCCESS(ReturnResult.SUCCESS, login);
			} else if(result.getCode() > 200 && result.getCode() < 1000){
				return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
			} else if(result.getCode() >= 1000 && result.getCode() < 10000){
				return ReturnResult.FAILUER(GetR18nMsg("user.login.error." + result.getCode(), result.getData()));
			} else{
				return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
			}
		} catch (Exception e) {
			logger.error("用户登录异常:",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10004"));
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/v2/logout")
    public ReturnResult logout() throws Exception {
        // 删除登陆中的用户
        super.deleteUserInfo();
        super.deleteApiUserInfo();
        return ReturnResult.SUCCESS();
    }
	
	/**
	 * 注册
	 */
	@ResponseBody
	@RequestMapping(value = "/v1/register")
	public ReturnResult regIndex_v1(
			@RequestParam(required = false, defaultValue = "0") String password,
			@RequestParam(required = false, defaultValue = "0") String regName,
			@RequestParam(required = false, defaultValue = "0") Integer regType,
			@RequestParam(required = false, defaultValue = "0") String pcode,
			@RequestParam(required = false, defaultValue = "0") String ecode,
			@RequestParam(required = false, defaultValue = "86") String areaCode,
			@RequestParam(required = false, defaultValue = "") String intro_user,
			@RequestParam(required = false, defaultValue = "") String nickname
			) throws Exception {
		// 获取IP
		String ip = getIpAddr();
		
		//获取设备信息
		DeviceInfo deviceInfo = getDeviceInfo();
		
		// 区号
		areaCode = areaCode.replace("+", "");
		// 检测密码强度
		if (!password.matches(Constant.passwordReg)) {
			return ReturnResult.FAILUER(-10, GetR18nMsg("com.validate.error.11009"));
		}
		// 检测开放注册
		String isOpenReg = redisHelper.getSystemArgs("isOpenReg").trim();
		if (!isOpenReg.equals("1")) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11014"));
		}

		RequestUserInfo userInfo = new RequestUserInfo();
		// 推广推荐ID
		if(StringUtils.isEmpty(intro_user)) {
			if(redisHelper.getSystemArgs(ArgsConstant.ISMUSTINTROL).equals("1"))
				return ReturnResult.FAILUER(GetR18nMsg("user.reg.error.1006"));
		}else{
			userInfo.setIntrocode(intro_user);
		}
		userInfo.setFareacode(areaCode);
		if(regType == 0){
			userInfo.setCode(pcode);
		}else{
			userInfo.setCode(ecode);
		}
		userInfo.setFloginname(regName);
		userInfo.setType(regType);
		userInfo.setFagentid(WebConstant.BCAgentId);
		userInfo.setFloginpassword(Utils.MD5(password));
		userInfo.setPlatform(PlatformEnum.BC);
		userInfo.setIp(ip);
		userInfo.setNickname(nickname);
		try {
			Result result = this.userService.insertRegister(userInfo, UserLoginType.getUserLoginTypeByCode(deviceInfo.getPlatform()));
			if(result.getCode() == 200){
				LoginResponse login = (LoginResponse) result.getData();
				// 设置登录成功的Token
                sessionContextUtils.addContextToken("token", login.getToken());
				return ReturnResult.SUCCESS();
			} else if(result.getCode() > 200 && result.getCode() < 1000){
				return ReturnResult.FAILUER(GetR18nMsg("common.error." + result.getCode()));
			} else if(result.getCode() >= 1000 && result.getCode() < 10000){
				return ReturnResult.FAILUER(GetR18nMsg("user.reg.error." + result.getCode()));
			} else{
				return ReturnResult.FAILUER(GetR18nMsg("com.error." + result.getCode(), result.getData()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户注册异常："+e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
	}

	
	/**
	 * 获取用户安全信息
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/getSecuritySetting")
	public ReturnResult getSecuritySetting(String loginName) throws Exception {
		FUser user = getUser();
		
		if (user == null) {
			if(StringUtils.isNotEmpty(loginName)) {
				//当用户等于空的时候则使用loginName查询用户，查询为空的话则报错
				FUser param = new FUser();
				param.setFloginname(loginName);
				user = userService.selectUserByParam(param);
				if(user == null) {
					return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
				}
			}else {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
        }
		
		FUser fuser = userService.selectUserById(user.getFid());
		updateUserInfo(fuser);
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("isOpenEmailValidate", fuser.isOpenEmailValidate());
		jsonObject.put("isOpenGoogleValidate", fuser.isOpenGoogleValidate());
		jsonObject.put("isOpenPhoneValidate", fuser.isOpenPhoneValidate());
		jsonObject.put("email", fuser.getFemail());
		jsonObject.put("phone", fuser.getFtelephone());
		return ReturnResult.SUCCESS(jsonObject);
	}
	
	public static void main(String[] args) {
		System.out.println();
	}
	
	/**
	 * 获取用户安全设置信息
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/userSecurity")
	public ReturnResult userSecurity() throws Exception {
		try {
			FUser userInfo = getCurrentUserInfoByToken();
			if(userInfo != null) {
				FUser fuser = userService.selectUserById(userInfo.getFid());
				updateUserInfo(fuser);

				FUserIdentity identity = userIdentityService.selectByUser(fuser.getFid());
				String device_name = Constant.GoogleAuthName + "--" + fuser.getFloginname();

				boolean isBindGoogle = fuser.getFgooglebind() == null ? false : fuser.getFgooglebind();
				boolean isBindTelephone = fuser.getFistelephonebind() == null ? false : fuser.getFistelephonebind();
				boolean isEmail = fuser.getFismailbind() == null ? false : fuser.getFismailbind();
				boolean isTradePass = fuser.getFtradepassword() != null && !fuser.getFtradepassword().equals("");
				boolean isLoginPass = fuser.getFloginpassword() != null && !fuser.getFloginpassword().equals("");
				boolean isIdentity = identity != null && identity.getFstatus() == 1;

				int bindcount = 0;
				if (isBindGoogle) {
					bindcount++;
				}
				if (isBindTelephone) {
					bindcount++;
				}
				if (isEmail) {
					bindcount++;
				}
				if (isIdentity){
					bindcount++;
				}

				String loginName = Utils.formatloginName(fuser.getFloginname());
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("loginName", loginName);
				jsonObject.put("device_name", device_name);
				jsonObject.put("securityLevel", bindcount);
				jsonObject.put("isBindGoogle", isBindGoogle);
				jsonObject.put("isBindTelephone", isBindTelephone);
				jsonObject.put("isBindEmail", isEmail);
				jsonObject.put("isTradePass", isTradePass);
				jsonObject.put("isLoginPass", isLoginPass);
				jsonObject.put("isIdentity", isIdentity);
				jsonObject.put("phone", fuser.getFtelephone());
				jsonObject.put("email", fuser.getFemail());
				jsonObject.put("isOpenEmailValidate", fuser.isOpenEmailValidate());
				jsonObject.put("isOpenGoogleValidate", fuser.isOpenGoogleValidate());
				jsonObject.put("isOpenPhoneValidate", fuser.isOpenPhoneValidate());
				return ReturnResult.SUCCESS(jsonObject);
			}else {
				return ReturnResult.FAILUER("请登录！");
			}
			
		} catch (Exception e) {
			logger.error("请求 /v2/userSecurity 错误" , e);
			return ReturnResult.FAILUER("系统异常");
		}
	}
	
	/**
	 * 设置用户安全信息
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/setSecurity")
	public ReturnResult setSecurity(
			@RequestParam(required = true) Boolean isOpenEmailValidate,
			@RequestParam(required = true) Boolean isOpenGoogleValidate,
			@RequestParam(required = true) Boolean isOpenPhoneValidate,
			@RequestParam(required = false, defaultValue = "0") String phoneCode,
			@RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode
			) throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			if(!isOpenEmailValidate && !isOpenGoogleValidate && !isOpenPhoneValidate ) {
				return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1023"));
			}
			FUser fuser = userService.selectUserById(user.getFid());
			
			if(isOpenEmailValidate) {
				if(!fuser.getFismailbind() || StringUtils.isEmpty(fuser.getFemail())) {
					return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1020"));
				}
			}
			if(isOpenGoogleValidate) {
				if(!fuser.getFgooglebind()) {
					return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1021"));
				}
			}
			if(isOpenPhoneValidate) {
				if(!fuser.getFistelephonebind() ||  StringUtils.isEmpty(fuser.getFtelephone())) {
					return ReturnResult.FAILUER(super.GetR18nMsg("user.security.error.1022"));
				}
			}
			LogUserActionEnum action = LogUserActionEnum.MODIFY_SECURITY_SETTINGS;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setEmailCode(emailCode);
			paramInfo.setBusinessType(BusinessTypeEnum.SMS_SEND_CODE);
			paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);
	
			fuser.setFupdatetime(Utils.getTimestamp());
			fuser.setIp(getIpAddr());
			fuser.setOpenEmailValidate(isOpenEmailValidate);
			fuser.setOpenGoogleValidate(isOpenGoogleValidate);
			fuser.setOpenPhoneValidate(isOpenPhoneValidate);
			
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				updateUserInfo(fuser);
				return ReturnResult.SUCCESS(GetR18nMsg("com.public.succeed.10000"));
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("访问/v2/setSecurity异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
		
	}
	
	/**
	 * 修改谷歌验证
	 * 第一步，获取新的谷歌验证码
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/get_google")
	public ReturnResult getGoogle() throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			LogUserActionEnum action = LogUserActionEnum.GET_GOOGLE;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			Result result = this.userSecurityService.updateUserSecurityInfo(user, paramInfo, action, null);
			if(result.getSuccess()) {
				Map<String, String> map = (Map<String, String>) result.getData();
				String wechatGoogleUrl = "";
				String systemArgs = redisHelper.getSystemArgs(ArgsConstant.WECHAR_GOOGLE_URL);
				if(!StringUtils.isEmpty(systemArgs)) {
					wechatGoogleUrl = systemArgs.replace("#googleUrl#", map.get("url"));
				}
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("code", 0);
				jsonObject.put("qecode", map.get("url"));
				jsonObject.put("totpKey", map.get("secret"));
				//塞redis
				redisHelper.set(RedisConstant.UPDATE_GOOGLE+user.getFid(), jsonObject.toJSONString(), 5*60);
				
				jsonObject.put("wechatGoogleUrl", wechatGoogleUrl);
				return ReturnResult.SUCCESS(jsonObject);
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.validate.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("获取新的谷歌验证码异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
		
	}
	
	/**
	 * 修改谷歌验证
	 * 第二步，验证新的验证码是否有误
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/validate_google")
	public ReturnResult validateGoogle(			
			@RequestParam(required = false, defaultValue = "0") String googleCode
			) throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			String string = redisHelper.get(RedisConstant.UPDATE_GOOGLE+user.getFid());
			if(StringUtils.isEmpty(string)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			JSONObject parseObject = JSON.parseObject(string);
			
			LogUserActionEnum action = LogUserActionEnum.VALIDATE_GOOGLE;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setTotpKey(parseObject.getString("totpKey"));
			Result result = this.userSecurityService.updateUserSecurityInfo(user, paramInfo, action, null);
			if(result.getSuccess()) {
				//如果确认修改无误则刷到缓存
				parseObject.put("code", 1);
				redisHelper.set(RedisConstant.UPDATE_GOOGLE+user.getFid(), parseObject.toJSONString(), 5*60);
				return ReturnResult.SUCCESS(GetR18nMsg("common.succeed.200"));
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.validate.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("验证新的验证码是否有误异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
		
	}
	
	
	
	/**
	 * 修改谷歌验证
	 * 第三步，绑定新的谷歌验证
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/update_google")
	public ReturnResult updateGoogle(
			@RequestParam(required = false, defaultValue = "0") String phoneCode,
			@RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode
			) throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			if(user.isOpenEmailValidate() && StringUtils.isEmpty(emailCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10122"));
			}
			if(user.isOpenGoogleValidate() && StringUtils.isEmpty(googleCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10115"));
			}
			if(user.isOpenPhoneValidate() && StringUtils.isEmpty(phoneCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10114"));
			}
			
			String string = redisHelper.get(RedisConstant.UPDATE_GOOGLE+user.getFid());
			if(StringUtils.isEmpty(string)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			JSONObject parseObject = JSON.parseObject(string);
			
			//如果没有操作到第二步，则返回错误
			if(parseObject.getInteger("code") != 1) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}

			LogUserActionEnum action = LogUserActionEnum.BIND_NEW_GOOGLE;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setEmailCode(emailCode);
			paramInfo.setBusinessType(BusinessTypeEnum.SMS_SEND_CODE);
			paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);
			
			user.setFgoogleauthenticator(parseObject.getString("totpKey"));
			user.setFgoogleurl(parseObject.getString("qecode"));
			
			Result result = this.userSecurityService.updateUserSecurityInfo(user, paramInfo, action, null);
			if(result.getSuccess()) {
				redisHelper.remove(RedisConstant.UPDATE_GOOGLE+user.getFid());
				updateUserInfo(user);
				return ReturnResult.SUCCESS(GetR18nMsg("common.succeed.200"));
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("绑定新的谷歌验证异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
		
	}
	
	
	/**
	 * 修改手机，第一步，验证新手机验证码
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/validate_phone")
	public ReturnResult validatePhone(
			@RequestParam(required = true) String phone,
			@RequestParam(required = true) String area,
			@RequestParam(required = true) String phoneCode
			) throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			if(StringUtils.isEmpty(phoneCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10114"));
			}
			if(PhoneValiUtil.checkPhoneNumber(phone, area));
			LogUserActionEnum action = LogUserActionEnum.VALIDATE_PHONE;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setBusinessType(BusinessTypeEnum.SMS_SEND_CODE);
			paramInfo.setPhone(phone);
			paramInfo.setAreaCode(area);
			
			user.setFtelephone(phone);
			user.setFareacode(area);
			Result result = this.userSecurityService.updateUserSecurityInfo(user, paramInfo, action, null);
			if(result.getSuccess()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("phone", phone);
				jsonObject.put("area", area);
				redisHelper.set(RedisConstant.UPDATE_PHONE+user.getFid(), jsonObject.toJSONString(), 5*60);
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.validate.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("第一步，验证新手机验证码异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
	}
	
	/**
	 * 修改手机，第二步，绑定新手机
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/update_phone")
	public ReturnResult updatePhone(
			@RequestParam(required = false, defaultValue = "0") String phoneCode,
			@RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode
			) throws Exception {
		try {
			FUser user = getUser();
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			if(user.isOpenEmailValidate() && StringUtils.isEmpty(emailCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10122"));
			}
			if(user.isOpenGoogleValidate() && StringUtils.isEmpty(googleCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10115"));
			}
			if(user.isOpenPhoneValidate() && StringUtils.isEmpty(phoneCode)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.error.10114"));
			}
			
			String string = redisHelper.get(RedisConstant.UPDATE_PHONE+user.getFid());
			if(StringUtils.isEmpty(string)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
			}
			
			JSONObject parseObject = JSON.parseObject(string);

			LogUserActionEnum action = LogUserActionEnum.BIND_NEW_PHONE;
			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(getIpAddr());
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setEmailCode(emailCode);
			paramInfo.setBusinessType(BusinessTypeEnum.SMS_SEND_CODE);
			paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);
			
			user.setFtelephone(parseObject.getString("phone"));
			user.setFareacode(parseObject.getString("area"));
			
			Result result = this.userSecurityService.updateUserSecurityInfo(user, paramInfo, action, null);
			if(result.getSuccess()) {
				redisHelper.remove(RedisConstant.UPDATE_PHONE+user.getFid());
				updateUserInfo(user);
				return ReturnResult.SUCCESS(GetR18nMsg("common.succeed.200"));
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.validate.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("第二步，绑定新手机异常",e);
			return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
		}
		
	}
	
	/**
	 * 修改登录和交易密码
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/modifyPwd")
	public ReturnResult modifyPwd(
			@RequestParam(required = true) String newPwd,
			@RequestParam(required = true) String originPwd,
			@RequestParam(required = true) String reNewPwd,
			@RequestParam(required = false, defaultValue = "0") String phoneCode,
			@RequestParam(required = false, defaultValue = "0") Integer pwdType,
			@RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode)throws Exception{
		
		HttpServletRequest request = sessionContextUtils.getContextRequest();
		FUser fuser = getCurrentUserInfoByToken();
		
		if (!newPwd.equals(reNewPwd)) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11010"));
		}
		// 检测密码强度
		if(pwdType == 0) {
			if (!newPwd.matches(Constant.passwordReg)) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
		}else {
			if (newPwd.length() < 6) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
		}
		
		LogUserActionEnum action = LogUserActionEnum.MODIFY_LOGINPWD;
		BusinessTypeEnum msgType = BusinessTypeEnum.SMS_MODIFY_LOGIN_PASSWORD;
		//是否需要发送短信提醒
		if (pwdType == 0) {
			// 修改登录密码
			fuser.setFloginpassword(Utils.MD5(newPwd));
		} else {
			// 修改交易密码
			action = LogUserActionEnum.BIND_TRADEPWD;
			
			//如果交易密码不为空则是修改交易密码
			if (fuser.getFtradepassword() != null && fuser.getFtradepassword().trim().length() > 0) {
				action = LogUserActionEnum.MODIFY_TRADEPWD;
				fuser.setFtradepwdtime(Utils.getTimestamp());
			}
			fuser.setFtradepassword(Utils.MD5(newPwd));
			//发送短信的类型
			msgType = BusinessTypeEnum.SMS_MODIFY_TRADE_PASSWORD;
		}

		String ip = Utils.getIpAddr(request);

		ValidateParamInfo paramInfo = new ValidateParamInfo();
		paramInfo.setCode(phoneCode);
		paramInfo.setTotpCode(googleCode);
		paramInfo.setIp(ip);
		paramInfo.setPlatform(PlatformEnum.BC);
		paramInfo.setBusinessType(msgType);
		paramInfo.setLocale(getLanEnum());
		paramInfo.setOriginLoginPwd(Utils.MD5(originPwd));
		paramInfo.setEmailCode(emailCode);
		paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);

		fuser.setFupdatetime(Utils.getTimestamp());
		fuser.setIp(ip);
		try {
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				if(action == LogUserActionEnum.MODIFY_LOGINPWD){
					deleteUserInfo();
				} else {
					updateUserInfo(fuser);
				}
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("modifyPwd error {}",e);
		}
		return ReturnResult.FAILUER(GetR18nMsg("common.error.400"));
	}
	
	/**
	 * 修改登录和交易密码,APP端需要签名
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/modifyPwdSign")
	public ReturnResult modifyPwdSign(
			@RequestParam(required = true) String newPwd,
			@RequestParam(required = true) String originPwd,
			@RequestParam(required = true) String reNewPwd,
			@RequestParam(required = false, defaultValue = "0") String phoneCode,
			@RequestParam(required = false, defaultValue = "0") Integer pwdType,
			@RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode)throws Exception{
		
		HttpServletRequest request = sessionContextUtils.getContextRequest();
		FUser fuser = getCurrentUserInfoByToken();
		
		if (!newPwd.equals(reNewPwd)) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11010"));
		}
		// 检测密码强度
		if(pwdType == 0) {
			if (!newPwd.matches(Constant.passwordReg)) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
		}else {
			if (newPwd.length() < 6) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
		}
		
		LogUserActionEnum action = LogUserActionEnum.MODIFY_LOGINPWD;
		BusinessTypeEnum msgType = BusinessTypeEnum.SMS_MODIFY_LOGIN_PASSWORD;
		//是否需要发送短信提醒
		if (pwdType == 0) {
			// 修改登录密码
			fuser.setFloginpassword(Utils.MD5(newPwd));
		} else {
			// 修改交易密码
			action = LogUserActionEnum.BIND_TRADEPWD;
			
			//如果交易密码不为空则是修改交易密码
			if (fuser.getFtradepassword() != null && fuser.getFtradepassword().trim().length() > 0) {
				action = LogUserActionEnum.MODIFY_TRADEPWD;
				fuser.setFtradepwdtime(Utils.getTimestamp());
			}
			fuser.setFtradepassword(Utils.MD5(newPwd));
			//发送短信的类型
			msgType = BusinessTypeEnum.SMS_MODIFY_TRADE_PASSWORD;
		}

		String ip = Utils.getIpAddr(request);

		ValidateParamInfo paramInfo = new ValidateParamInfo();
		paramInfo.setCode(phoneCode);
		paramInfo.setTotpCode(googleCode);
		paramInfo.setIp(ip);
		paramInfo.setPlatform(PlatformEnum.BC);
		paramInfo.setBusinessType(msgType);
		paramInfo.setLocale(getLanEnum());
		paramInfo.setOriginLoginPwd(Utils.MD5(originPwd));
		paramInfo.setEmailCode(emailCode);
		paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);

		fuser.setFupdatetime(Utils.getTimestamp());
		fuser.setIp(ip);
		try {
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				if(action == LogUserActionEnum.MODIFY_LOGINPWD){
					deleteUserInfo();
					deleteApiUserInfo();
				} else {
					updateUserInfo(fuser);
				}
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("modifyPwd error {}",e);
		}
		return ReturnResult.FAILUER(GetR18nMsg("common.error.400"));
	}
	
	/**
	 * 设置交易密码
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/setTradePwd")
	public ReturnResult setTradePwd(
			@RequestParam(required = true) String newPwd,
			@RequestParam(required = true) String reNewPwd)throws Exception{
		
		HttpServletRequest request = sessionContextUtils.getContextRequest();
		FUser fuser = getCurrentUserInfoByToken();
		
		if (!newPwd.equals(reNewPwd)) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11010"));
		}
		// 检测密码强度
		if (newPwd.length() < 6) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
		}
		
		LogUserActionEnum action = LogUserActionEnum.BIND_TRADEPWD;
		BusinessTypeEnum msgType = BusinessTypeEnum.SMS_MODIFY_TRADE_PASSWORD;
		//是否需要发送短信提醒
		//修改交易密码
		
		fuser.setFtradepassword(Utils.MD5(newPwd));
		//发送短信的类型

		String ip = Utils.getIpAddr(request);
		ValidateParamInfo paramInfo = new ValidateParamInfo();
		paramInfo.setIp(ip);
		paramInfo.setPlatform(PlatformEnum.BC);
		paramInfo.setBusinessType(msgType);
		paramInfo.setLocale(getLanEnum());
		paramInfo.setEmailBusinessType(BusinessTypeEnum.EMAIL_SEND_CODE);

		fuser.setIp(ip);
		try {
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				updateUserInfo(fuser);
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("setTradePwd error {}",e);
		}
		return ReturnResult.FAILUER(GetR18nMsg("common.error.400"));
	}
	
	/**
	 * 设置更改昵称
	 */
	@ResponseBody
	@RequestMapping(value = "/v2/update_nickname")
	public ReturnResult updateNickName(
			@RequestParam(required = false) String nickname) throws Exception {
		try {
			FUser user = getUser();
			
			if (user == null) {
	            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
	        }
			
			FUser fuser = userService.selectUserById(user.getFid());
			if(fuser.getIsHavedModNickname()) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.validate.error.11021"));
			}
			
			FUser param = new FUser();
			param.setFnickname(nickname);
			if(userService.selectIsExistByParam(param)) {
				return ReturnResult.FAILUER(super.GetR18nMsg("com.validate.error.11022"));
			}
			
			fuser.setFnickname(nickname);
			fuser.setIsHavedModNickname(true);
			fuser.setFupdatetime(new Date());
			Integer updateByPrimaryKey = userService.updateByPrimaryKey(fuser);
			if(updateByPrimaryKey != null  && updateByPrimaryKey != 0 ) {
				updateUserInfo(fuser);
				return ReturnResult.SUCCESS(GetR18nMsg("common.succeed.200"));
			}
		} catch (Exception e) {
			logger.error("/v2/update_nickname异常",e);
		}
		return ReturnResult.FAILUER(GetR18nMsg("common.error.400"));
	}
	
	/**
     * 获取用户余额
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/v2/balance")
    public ReturnResult balance() throws Exception {
        FUser user = getUser();
        if (user == null) {
            return ReturnResult.FAILUER("请重新登录");
        }

        Integer fid = user.getFid();
        JSONObject result = new JSONObject();
        try {
        	List<UserCoinWallet> userCoinWallets = userWalletService.listUserCoinWallet(fid);
            Iterator<UserCoinWallet> iterator = userCoinWallets.iterator();
            while (iterator.hasNext()) {
                UserCoinWallet wallet = (UserCoinWallet) iterator.next();
                if (!redisHelper.hasCoinId(wallet.getCoinId())) {
                    iterator.remove();
                }
            }
            BigDecimal totalAssets = getTotalAssets(userCoinWallets);
            BigDecimal btcPrice = redisHelper.getLastPrice(8);
            BigDecimal btcAssets = MathUtils.div(totalAssets, btcPrice);
            
            result.put("netAssets", getNetAssets(userCoinWallets));
            result.put("totalAssets", totalAssets);
            result.put("btcAssets", btcAssets);
            result.put("userWalletList", userCoinWallets);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
        }
        return ReturnResult.SUCCESS(result);
    }
    
    
    /**
     * 找回密码第一步：验证账号以及图片验证码
     */
    @ResponseBody
    @RequestMapping("/v2/resetLoginPwdCheck")
    public ReturnResult resetLoginPwdCheck(
    		@RequestParam(required = true, defaultValue = "0") String loginName,
            @RequestParam(required = true, defaultValue = "0") String imgCode,
            @RequestParam(required = false) String imgToken) 
    {
        if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(imgCode)) {
            return ReturnResult.FAILUER(GetR18nMsg("user.security.error.300"));
        }

        FUser param = new FUser();
        param.setFloginname(loginName);
        FUser fuser = this.userService.selectUserByParam(param);
        // 没找到用户
        if (fuser == null) {
            return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1000"));
        }

        String session_code = redisHelper.getRedisData(imgToken);
		if ((session_code == null) || (!imgCode.equalsIgnoreCase(session_code))) {
			return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11004"));
		}
		
		String redisKey = setRedisData("resetToken", loginName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resetToken", redisKey);
        return ReturnResult.SUCCESS(jsonObject);
    }
    
    /**
     * 找回密码第一步：验证账号以及图片验证码
     */
    @ResponseBody
    @RequestMapping("/v2/resetLoginPwd")
    public ReturnResult resetLoginPwd(
    		@RequestParam(required = true, defaultValue = "0") String loginName,
    		@RequestParam(required = true, defaultValue = "0") String newPwd,
            @RequestParam(required = true, defaultValue = "0") String reNewPwd,
            @RequestParam(required = true, defaultValue = "0") String resetToken,
            @RequestParam(required = false, defaultValue = "0") String phoneCode,
            @RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode) 
    {
    	HttpServletRequest request = sessionContextUtils.getContextRequest();
    	FUser param = new FUser();
		try {
			
			param.setFloginname(loginName);
	    	FUser fuser = this.userService.selectUserByParam(param);
	    	
	    	// 没找到用户
	    	if (fuser == null) {
	    		return ReturnResult.FAILUER(GetR18nMsg("user.security.error.1000"));
	    	}
	    	
	    	if (!newPwd.equals(reNewPwd)) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11010"));
			}
			// 检测密码强度
			if (!newPwd.matches(Constant.passwordReg)) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
			
			LogUserActionEnum action = LogUserActionEnum.FIND_LOGINPASS;
			BusinessTypeEnum smsType = BusinessTypeEnum.SMS_FIND_PHONE_PASSWORD;
			
			BusinessTypeEnum emailType = BusinessTypeEnum.EMAIL_SEND_CODE;
			//修改登录密码
			fuser.setFloginpassword(Utils.MD5(newPwd));
			

			String ip = Utils.getIpAddr(request);

			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(ip);
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setBusinessType(smsType);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setEmailCode(emailCode);
			paramInfo.setEmailBusinessType(emailType);
			fuser.setFupdatetime(Utils.getTimestamp());
			fuser.setIp(ip);
			
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				deleteUserInfo();
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("modifyPwd error {}",e);
		}
        
        return ReturnResult.SUCCESS();
    }
    
    
    /**
     * 找回资金密码第一步：验证账号以及图片验证码
     */
    @ResponseBody
    @RequestMapping("/v2/resetTradePwdCheck")
    public ReturnResult resetTradePwdCheck(
    		@RequestParam(required = false, defaultValue = "0") Integer type,
			@RequestParam(required = true) String loginName,
			@RequestParam(required = true) String password) 
    {
        FUser fuser = getUser();
        // 没找到用户
        if (fuser == null) {
        	return ReturnResult.FAILUER("请登录！");
        }
        
        
        FUser checkUser = new FUser();
        if (StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password)) {
            return ReturnResult.FAILUER("登录名或密码不能为空");
        } else {
            if (type == 0) {
            	checkUser.setFtelephone(loginName);
            } else {
            	checkUser.setFemail(loginName);
            }
            List<FUser> list = userService.selectUserListByParam(checkUser);
            if (list == null || list.size() <= 0) {
            	return ReturnResult.FAILUER("用户不存在");
            }
        }
		
		String redisKey = setRedisData("resetToken", fuser.getFloginname());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resetToken", redisKey);
        return ReturnResult.SUCCESS(jsonObject);
    }
    
    /**
     * 找回密码第一步：验证账号以及图片验证码
     */
    @ResponseBody
    @RequestMapping("/v2/resetTradePwd")
    public ReturnResult resetTradePwd(
    		@RequestParam(required = true, defaultValue = "0") String newPwd,
            @RequestParam(required = true, defaultValue = "0") String reNewPwd,
            @RequestParam(required = true, defaultValue = "0") String resetToken,
            @RequestParam(required = false, defaultValue = "0") String phoneCode,
            @RequestParam(required = false, defaultValue = "0") String googleCode,
			@RequestParam(required = false, defaultValue = "0") String emailCode) 
    {
    	HttpServletRequest request = sessionContextUtils.getContextRequest();
		try {
			
			FUser fuser = getUser();
	        // 没找到用户
	        if (fuser == null) {
	        	return ReturnResult.FAILUER("请登录！");
	        }
	    	
	    	if (!newPwd.equals(reNewPwd)) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11010"));
			}
			// 检测密码强度
			if (newPwd.length() < 6) {
				return ReturnResult.FAILUER(GetR18nMsg("com.validate.error.11009"));
			}
			
			LogUserActionEnum action = LogUserActionEnum.FIND_TRADE_PWD;
			BusinessTypeEnum smsType = BusinessTypeEnum.SMS_FIND_PHONE_PASSWORD;
			
			BusinessTypeEnum emailType = BusinessTypeEnum.EMAIL_SEND_CODE;
			String ip = Utils.getIpAddr(request);

			ValidateParamInfo paramInfo = new ValidateParamInfo();
			paramInfo.setCode(phoneCode);
			paramInfo.setTotpCode(googleCode);
			paramInfo.setIp(ip);
			paramInfo.setPlatform(PlatformEnum.BC);
			paramInfo.setBusinessType(smsType);
			paramInfo.setLocale(getLanEnum());
			paramInfo.setEmailCode(emailCode);
			paramInfo.setEmailBusinessType(emailType);
			
			fuser.setIp(ip);
			fuser.setFtradepassword(Utils.MD5(newPwd));
			fuser.setFtradepwdtime(Utils.getTimestamp());
			fuser.setFupdatetime(Utils.getTimestamp());
			
			Result result = this.userSecurityService.updateUserSecurityInfo(fuser, paramInfo, action, null);
			if(result.getSuccess()) {
				//更新redis中的用户信息
				updateUserInfo(fuser);
				return ReturnResult.SUCCESS();
			} else if(result.getCode() >= 10000){
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("com.error." + result.getCode(), result.getData()));
			} else {
				return ReturnResult.FAILUER(result.getCode(),GetR18nMsg("user.security.error." + result.getCode()));
			}
		} catch (Exception e) {
			logger.error("modifyPwd error {}",e);
		}
        return ReturnResult.SUCCESS();
    }
    
    /**
     * 返回用户信息
     */
    @ResponseBody
    @RequestMapping("/v2/getUserInfo")
    public ReturnResult getUserInfo() 
    {
		try {
			FUser fuser = getUser();
	        // 没找到用户
	        if (fuser == null) {
	        	return ReturnResult.FAILUER("请登录！");
	        }
	        //谷歌验证不能返回
			fuser.setFgoogleauthenticator("");
			fuser.setFgoogleurl("");
			return ReturnResult.SUCCESS(fuser);
		} catch (Exception e) {
			logger.error("modifyPwd error {}",e);
		}
		return ReturnResult.FAILUER("系统繁忙，请稍后再试");
    }
    
    
    /**
     * 修改头像
     * @param 
     * @return map
     */
	@RequestMapping(value = "/v2/editPhoto", method = RequestMethod.POST)
	@ResponseBody
	public ReturnResult editPhoto(HttpServletRequest request,MultipartFile file) {
    	logger.info("/editPhoto:param:");
		logger.info("/editPhoto:queryString"+request.getQueryString());

        FUser user = getUser();
        
        if (user == null) {
            return ReturnResult.FAILUER(super.GetR18nMsg("com.public.error.10001"));
        }
        
		if(file!=null){
			logger.debug("用户修改照片: " + request.getQueryString());
			String fileName = file.getOriginalFilename();
			logger.debug("fileName="+fileName);
	        String fileType =file.getContentType();
	        logger.debug("fileType="+fileType);

			if (fileName != null
					&& (fileName.endsWith(".jpg") || fileName.endsWith(".gif") || fileName
							.endsWith(".png")||fileName.endsWith(".jpeg"))) {
				try {
					String keyPrix = "hotcoin/photo";
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM");
					String forMatDate = formatter.format(new Date());
					String ossKey = keyPrix+"/"+forMatDate+"/";
					logger.info("oss 存储路径："+ossKey);
			        
			        String result = ossHelper.uploadFile(file, ossKey);
			        FUser fuser = userService.selectUserById(user.getFid());
			        if (StringUtils.isNotEmpty(result)) {
			        	fuser.setPhoto(result);
						fuser.setFupdatetime(new Date());
						Integer updateByPrimaryKey = userService.updateByPrimaryKey(fuser);
						if(updateByPrimaryKey != null  && updateByPrimaryKey != 0 ) {
							updateUserInfo(fuser);
							return ReturnResult.SUCCESS(200,result);
						}
			        } else {
			        	return ReturnResult.FAILUER("上传失败");
			        }
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					logger.error("请求路径："+request.getRequestURL()+"系统异常："+e.getMessage());
					logger.error(e.getMessage(), e);
				}
			} else {
				return ReturnResult.FAILUER("上传文件出错，只能上传 *.jpg , *.gif,*.png,*.jpeg");
			}
		}else{
	        // 返回信息status为1，表示成功
			return ReturnResult.FAILUER("参数错误");
		}
		return ReturnResult.FAILUER("上传失败");
	}
}
