package com.qkwl.web.front.controller;


import javax.servlet.http.Cookie;

import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.rpc.user.IUserSecurityService;
import com.qkwl.web.front.controller.base.WebBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.log.FLogUserAction;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserDTO;
import com.qkwl.common.dto.user.FUserIdentity;
import com.qkwl.common.rpc.user.IUserIdentityService;
import com.qkwl.common.rpc.user.IUserService;


@Controller
public class FrontUserController extends WebBaseController {

    private static final Logger logger = LoggerFactory.getLogger(FrontUserController.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private IUserSecurityService userSecurityService;
    @Autowired
    private IUserIdentityService userIdentityService;

    /**
     * 登录
     */
    @RequestMapping(value = "/user/login")
    public ModelAndView login(@RequestParam(required = false, defaultValue = "") String forwardUrl) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("forwardUrl", forwardUrl);
        modelAndView.setViewName("front/user/user_login");
        return modelAndView;
    }

    /**
     * 手机注册
     */
    @RequestMapping(value = "/user/phonereg")
    public ModelAndView regPhone() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("areaCodes", LuangeHelper.parseCity());
        modelAndView.addObject("defaultAreaName", "中国");
        modelAndView.addObject("defaultAreaCode", "86");
        modelAndView.setViewName("front/user/user_register_phone");
        return modelAndView;
    }

    /**
     * 手机注册
     */
    @RequestMapping(value = "/user/intro")
    public ModelAndView regIntro(Integer intro) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("areaCodes", LuangeHelper.parseCity());
        modelAndView.addObject("defaultAreaName", "中国");
        modelAndView.addObject("defaultAreaCode", "86");
        modelAndView.addObject("intro", intro);
        modelAndView.setViewName("front/user/user_register_intro");
        return modelAndView;
    }

    /**
     * 注册
     */
    @RequestMapping(value = "/user/register")
    public ModelAndView register(@RequestParam(required = false, defaultValue = "") String uid) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        // 推荐注册
        FUser intro = null;
        try {
            if (uid.isEmpty()) {
                Cookie cs[] = sessionContextUtils.getContextRequest().getCookies();
                if (cs != null && cs.length > 0) {
                    for (Cookie cookie : cs) {
                        if (cookie.getName().endsWith("r")) {
                            intro = this.userService.selectUserByShowId(Integer.parseInt(cookie.getValue()));
                            break;
                        }
                    }
                }
            } else {
                intro = this.userService.selectUserByShowId(Integer.parseInt(uid));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        if (intro != null) {
            modelAndView.addObject("intro", intro.getFshowid());
        }
        modelAndView.setViewName("front/user/user_register");
        return modelAndView;
    }

    /**
     * 退出
     */

    @RequestMapping(value = "/user/logout")
    public ModelAndView logout() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        // 删除登陆中的用户
        super.deleteUserInfo();
        modelAndView.setViewName("redirect:/index.html");
        return modelAndView;
    }

    /**
     * 安全设置
     */
    @RequestMapping(value = "/user/security")
    public ModelAndView userSecurity() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        FUser userInfo = getCurrentUserInfoByToken();
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
        if (isTradePass) {
            bindcount++;
        }
        if (isLoginPass) {
            bindcount++;
        }
        if (isIdentity) {
            bindcount++;
        }

        String loginName = Utils.formatloginName(fuser.getFloginname());
        int securityLevel = 1;
        if (bindcount >= 2) {
            securityLevel = 2;
        }
        if (fuser.getFemail() != null && !fuser.getFemail().equals("")) {
            String[] emails = fuser.getFemail().split("@");
            String emaString = "";
            if (emails[0].length() > 3) {
                emaString = emails[0].substring(0, 3) + "****@" + emails[1];
            } else {
                emaString = emails[0].substring(0, 1) + "****@" + emails[1];
            }
            modelAndView.addObject("emaString", emaString);
        }
        if (fuser.getFtelephone() != null && !fuser.getFtelephone().equals("")) {
            String phoneString = fuser.getFtelephone();
            phoneString = phoneString.substring(0, 3) + "****" + phoneString.substring(7);
            modelAndView.addObject("phoneString", "+" + fuser.getFareacode() + " " + phoneString);
        }
        modelAndView.addObject("bindcount", bindcount);
        modelAndView.addObject("loginName", loginName);
        modelAndView.addObject("device_name", device_name);
        modelAndView.addObject("securityLevel", securityLevel);
        modelAndView.addObject("bindLogin", !StringUtils.isEmpty(fuser.getFloginpassword()));
        modelAndView.addObject("bindTrade", !StringUtils.isEmpty(fuser.getFtradepassword()));
        modelAndView.addObject("fuser", ModelMapperUtils.mapper(fuser, FUserDTO.class));
        modelAndView.addObject("identity", identity);
        modelAndView.setViewName("/front/user/user_security");
        return modelAndView;
    }

    /**
     * 登录日志
     */
    @RequestMapping(value = "/user/user_loginlog")
    public ModelAndView userLoginlog(@RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        int pagesize = 10;
        FUser userInfo = getCurrentUserInfoByToken();

        Pagination<FLogUserAction> flogs = new Pagination<FLogUserAction>(currentPage, pagesize, "/user/user_loginlog.html?");
        flogs = userSecurityService.listSettingLogByUser(userInfo.getFid(), flogs, 1);

        modelAndView.addObject("flogs", flogs);
        modelAndView.addObject("pagin", flogs.getPagin());
        modelAndView.setViewName("/front/user/user_loginlog");
        return modelAndView;
    }

    /**
     * 安全设置日志
     */
    @RequestMapping(value = "/user/user_settinglog")
    public ModelAndView userSettinglog(@RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        int pagesize = 10;
        ModelAndView modelAndView = new ModelAndView();
        FUser userInfo = getCurrentUserInfoByToken();

        Pagination<FLogUserAction> flogs = new Pagination<FLogUserAction>(currentPage, pagesize, "/user/user_settinglog.html?");
        flogs = userSecurityService.listSettingLogByUser(userInfo.getFid(), flogs, 2);

        modelAndView.addObject("flogs", flogs);
        modelAndView.setViewName("/front/user/user_settinglog");
        return modelAndView;
    }

    /**
     * 用户申请api的界面
     */
    @RequestMapping(value = "/user/user_auth")
    public ModelAndView userAuth(@RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        int pagesize = 10;
        ModelAndView modelAndView = new ModelAndView();
        FUser userInfo = getCurrentUserInfoByToken();

        Pagination<FLogUserAction> flogs = new Pagination<FLogUserAction>(currentPage, pagesize, "/user/user_settinglog.html?");
        flogs = userSecurityService.listSettingLogByUser(userInfo.getFid(), flogs, 2);

        modelAndView.addObject("flogs", flogs);
        modelAndView.setViewName("/front/user/user_settinglog");
        return modelAndView;
    }


}
