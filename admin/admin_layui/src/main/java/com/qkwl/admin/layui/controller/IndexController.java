package com.qkwl.admin.layui.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.qkwl.admin.layui.base.WebBaseController;
import com.qkwl.admin.layui.utils.GeetestConfig;
import com.qkwl.admin.layui.utils.MQSend;
import com.qkwl.admin.layui.utils.WebConstant;
import com.qkwl.common.Enum.redis.RedisTypeEnum;
import com.qkwl.common.Enum.validate.BusinessTypeEnum;
import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.Enum.validate.PlatformEnum;
import com.qkwl.common.Enum.validate.SendTypeEnum;
import com.qkwl.common.dto.Enum.LimitTypeEnum;
import com.qkwl.common.dto.Enum.LogAdminActionEnum;
import com.qkwl.common.dto.admin.FAdmin;
import com.qkwl.common.dto.coin.SystemTradeType;
import com.qkwl.common.exceptions.BCException;
import com.qkwl.common.framework.limit.LimitHelper;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.framework.validate.ValidateHelper;
import com.qkwl.common.framework.validate.ValidationCheckHelper;
import com.qkwl.common.google.GoogleAuth;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.admin.IAdminManageService;
import com.qkwl.common.rpc.market.IMarketService;
import com.qkwl.common.rpc.redis.IRedisService;
import com.qkwl.common.util.GeetestLib;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.common.util.Utils;

/**
 * 主页
 */
@Controller
public class IndexController extends WebBaseController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private IAdminManageService adminManageService;
    @Autowired
    private IMarketService marketService;
    @Autowired
    private IRedisService redisService;
    @Autowired
    private MQSend mqSend;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private LimitHelper limitHelper;
    @Autowired
    private ValidateHelper validateHelper;
    @Autowired
    private ValidationCheckHelper validationCheckHelper;

    /**
     * 首页
     */
    @RequestMapping("/index")
    public ModelAndView index() {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FAdmin admin = (FAdmin) request.getSession().getAttribute("login_admin");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("loginAdmin", admin);
        modelAndView.setViewName("comm/index");
        return modelAndView;
    }

    /**
     * 登录页面
     */
    @RequestMapping("/admin/login")
    public ModelAndView login() throws Exception {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        String requestURI = request.getRequestURI();
        System.out.println("url = "+requestURI);
        System.out.println("scheme:"+request.getScheme());
        System.out.println("Protocol:"+request.getProtocol());
        System.out.println("X-Forwarded-Proto:"+request.getHeader("X-Forwarded-Proto"));
        System.out.println("X-Forwarded-For:"+request.getHeader("X-Forwarded-Por"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("comm/login");
        return modelAndView;
    }
    
    @ResponseBody
    @RequestMapping("/admin/sendSMS")
    public JSONObject bindPhone(@RequestParam(required = true) String loginName) throws Exception {
        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(loginName)) {
        	result.put("msg", "请先输入账号！");
        	result.put("code", "-1");
            return result;
        }
        
        // 获取当前登录的用户名
		FAdmin filterFAdmin = new FAdmin();
		filterFAdmin.setFname(loginName);
		FAdmin admin = adminManageService.selectAdminByProperty(filterFAdmin);
        
		if (admin == null) {
			result.put("msg", "输入的账号有误！");
        	result.put("code", "-1");
            return result;
	    }else {
	    	String phone = admin.getPhone();
	    	//如果登录用户不需要验证码登录则跳过
	    	if(StringUtils.isNotEmpty(phone)) {
	    		boolean sendSmsResult= this.validateHelper.smsValidateCode(admin.getFid(), "86", admin.getPhone(), 
            			SendTypeEnum.SMS_TEXT.getCode(), PlatformEnum.BC.getCode(), 
            			BusinessTypeEnum.SMS_ADMIN_LOGIN.getCode(), LocaleEnum.ZH_CN.getCode());
	                if (!sendSmsResult){
	                	result.put("msg", "短信发送失败！");
	                	result.put("code", "-1");
	                    return result;
	                }else {
	                	result.put("code", "0");
	                    return result;
	                }
	    	}
	    }
        //验证短信已经发送，请及时验证！
		result.put("msg", "短信发送失败！");
    	result.put("code", "-1");
        return result;
    }

    /**
     * 登录跳转
     */
    @RequestMapping("/admin/submitlogin")
    public ModelAndView submitLogin(@RequestParam(required = true) String name,
                                    @RequestParam(required = true) String password, @RequestParam(required = true) String googlecode,
                                    @RequestParam(required = false) String phoneCode
                                    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        if (name == null || "".equals(name.trim()) || password == null || "".equals(password.trim())) {
            modelAndView.addObject("error", "请输入帐号，密码");
            modelAndView.setViewName("/comm/login");
            return modelAndView;
        } else {
            String ip = request.getRemoteAddr();
            // 登录错误次数
            if (!limitHelper.checkLimit(ip, LimitTypeEnum.AdminLogin.getCode())) {
                modelAndView.addObject("error", "连续登录错误10次，为安全起见，禁止登录2小时！");
                modelAndView.setViewName("/comm/login");
                return modelAndView;
            }
            // 图片验证码
            Integer code = validateCaptcha();
            if (!code.equals(1)) {
                modelAndView.addObject("error", "请通过验证码！");
                modelAndView.setViewName("/comm/login");
                return modelAndView;
            }
            // 登录判断
            Subject admin = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(name, Utils.MD5(password));
            token.setRememberMe(true);
            Boolean resultFage = false;
            String resultStr = "";
            try {
                // 登录
                admin.login(token);
                FAdmin fadmin = (FAdmin) (request.getSession().getAttribute("login_admin"));
                // 登录日志
                try {
                    mqSend.SendAdminAction(fadmin.getFagentid(), fadmin.getFid(), LogAdminActionEnum.LOGIN, ip);
                } catch (BCException e) {
                    System.out.println("登陆成功日志写入队列失败！");
                    e.printStackTrace();
                }
                if (fadmin.getFgooglebind() && fadmin.getFgooglevalidate()) {
                    if (googlecode == null || "".equals(googlecode.trim())) {
                        resultStr = "请输入谷歌身份验证！";
                    } else {
                        if (GoogleAuth.auth(Long.parseLong(googlecode), fadmin.getFgoogleauthenticator())) {
                            resultFage = true;
                        } else {
                            resultStr = "谷歌身份验证失败";
                        }
                    }
                } else {
                    resultFage = true;
                }
                
                //紧急版本，新增短信验证码功能，手机号需要在数据库手动添加，
                if(StringUtils.isNotEmpty(fadmin.getPhone())) {
                	if(StringUtils.isEmpty(phoneCode)) {
                		resultStr = "请输入短信验证码";
	                	resultFage = false;
                	}else {
                	  String realIp = Utils.getIpAddr(request);
                  	  //验证手机验证码功能
  	            	  Result validateResult = validationCheckHelper.getPhoneCodeCheck("86",fadmin.getPhone(),
  	            			  phoneCode, BusinessTypeEnum.SMS_ADMIN_LOGIN.getCode(), realIp,PlatformEnum.BC.getCode());
  	                  if(!validateResult.getSuccess()){
  	                	  resultStr = "短信验证码错误";
  	                	  resultFage = false;
  	                  }else {
  	                	  resultFage = true;
  	                  }
                	}
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                resultStr = e.getMessage();
            }
            if (!resultFage) {
                token.clear();
                limitHelper.updateLimit(ip, LimitTypeEnum.AdminLogin.getCode());
                modelAndView.addObject("error", resultStr);
                modelAndView.setViewName("/comm/login");
                return modelAndView;
            }
            limitHelper.deleteLimit(ip, LimitTypeEnum.AdminLogin.getCode());
        }
        modelAndView.setViewName("redirect:/index.html");
        return modelAndView;
    }


    public Integer validateCaptcha() {
        GeetestLib gtSdk = new GeetestLib(GeetestConfig.getGeetest_id(), GeetestConfig.getGeetest_key(),
                GeetestConfig.isnewfailback());

        String challenge = sessionContextUtils.getContextRequest().getParameter(GeetestLib.fn_geetest_challenge);
        String validate = sessionContextUtils.getContextRequest().getParameter(GeetestLib.fn_geetest_validate);
        String seccode = sessionContextUtils.getContextRequest().getParameter(GeetestLib.fn_geetest_seccode);

        //从session中获取gt-server状态
        int gt_server_status_code = (Integer) sessionContextUtils.getContextRequest().getSession().getAttribute(gtSdk.gtServerStatusSessionKey);

        //从session中获取userid
        String userid = (String) sessionContextUtils.getContextRequest().getSession().getAttribute("userid");

        Integer gtResult = 0;

        if (gt_server_status_code == 1) {
            //gt-server正常，向gt-server进行二次验证
            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, userid);
        } else {
            // gt-server非正常情况下，进行failback模式验证
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
        }
        return gtResult;
    }

    /**
     * 退出登陆
     */
    @RequestMapping("admin/logout")
    public ModelAndView loginOut() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        Subject admin = SecurityUtils.getSubject();
        admin.getSession().removeAttribute("permissions");
        admin.logout();
        modelAndView.setViewName("redirect:/admin/login.html");
        return modelAndView;
    }


    /**
     * 重置行情数据页面
     */
    @RequestMapping("admin/restMarket")
    public ModelAndView restMarket() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("comm/restMarket");
        List<SystemTradeType> tradeTypes = redisHelper.getTradeTypeList(WebConstant.BCAgentId);
        Map<Integer, String> typeMap = redisHelper.getCoinTypeNameMap();
        typeMap.put(0, "全部");
        for (SystemTradeType tradeType : tradeTypes) {
            typeMap.put(tradeType.getId(), tradeType.getSellName());
        }
        modelAndView.addObject("typeMap", typeMap);
        return modelAndView;
    }

    /**
     * 重置Redis
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/goDealRedis")
    public ModelAndView goDealRedis(
            @RequestParam(value = "url", required = true) String url
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(url);
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(0, "全部");
        for (RedisTypeEnum e : RedisTypeEnum.values()) {
            typeMap.put(e.getCode(), e.getValue());
        }
        modelAndView.addObject("typeMap", typeMap);
        return modelAndView;
    }

    /**
     * 重置Redis数据
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/resetRedisData")
    @ResponseBody
    public ReturnResult resetRedisData(
            @RequestParam(value = "type", required = true) Integer type
    ) throws Exception {
        redisService.resetRedis(type);
        return ReturnResult.SUCCESS("重置成功");
    }

    /**
     * 清空Redis数据
     * @return
     * @throws Exception
     */
    @RequestMapping("admin/clearRedisData")
    @ResponseBody
    public ReturnResult clearRedisData(
            @RequestParam(value = "type", required = true) Integer type
    ) throws Exception {
        redisService.clearRedis(type);
        return ReturnResult.SUCCESS("清空成功");
    }
}
