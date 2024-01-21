package com.qkwl.web.front.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.qkwl.web.utils.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.dto.web.FAbout;
import com.qkwl.common.dto.web.FAboutType;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.web.front.controller.base.WebBaseController;

@Controller
public class FrontAboutController extends WebBaseController {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 关于我们
     */
    @RequestMapping("/about/about")
    public ModelAndView index(@RequestParam(required = false, defaultValue = "0") Integer id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
//		String uri = request.getRequestURI();
//
//		System.out.println("host = "+request.getRemoteHost());
//		System.out.println("url = "+uri);
//		System.out.println("scheme:"+request.getScheme());
//		System.out.println("Protocol:"+request.getProtocol());
        System.out.println("X-Forwarded-Proto:" + request.getHeader("X-Forwarded-Proto"));
        //System.out.println("X-Forwarded-For:"+request.getHeader("X-Forwarded-Por"));

        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        if (systemLan == null) {
            modelAndView.setViewName("redirect:/");
            return modelAndView;
        }
        // 获取当前语言下所有的about种类
        List<FAboutType> fabouttypes = redisHelper.getAboutTypeList(1, WebConstant.BCAgentId);
        // 查找当前语言下制定id下的详细信息
        FAbout fabout = redisHelper.getAbout(id, 1, WebConstant.BCAgentId);

        modelAndView.addObject("fabout", fabout);
        modelAndView.addObject("fabouttypes", fabouttypes);
        modelAndView.setViewName("front/about/index");
        return modelAndView;
    }

}
