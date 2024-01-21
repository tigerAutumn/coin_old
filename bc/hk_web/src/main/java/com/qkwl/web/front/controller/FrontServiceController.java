package com.qkwl.web.front.controller;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.qkwl.web.front.controller.base.WebBaseController;
import com.qkwl.web.utils.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.util.Utils;
import com.qkwl.common.dto.common.PageHelper;
import com.qkwl.common.dto.news.FArticle;
import com.qkwl.common.dto.news.FArticleType;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;

@Controller
public class FrontServiceController extends WebBaseController {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 通知列表
     */
    @RequestMapping("/notice/index")
    public ModelAndView ourService(
            @RequestParam(required = false, defaultValue = "2") Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {// 12,5,5
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        ModelAndView modelAndView = new ModelAndView();
        if (systemLan == null) {
            modelAndView.setViewName("redirect:/");
            return modelAndView;
        }
        FArticleType farticletype = redisHelper.getArticleType(id, systemLan.getFid());
        if (farticletype == null) {
            modelAndView.setViewName("redirect:/");
            return modelAndView;
        }
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , 2, farticletype.getFid(), currentPage,
                WebConstant.BCAgentId);

        int total = redisHelper.getArticlesPageCont(systemLan.getFid() , 2, farticletype.getFid(), WebConstant.BCAgentId);
        String pagin = PageHelper.generatePagin(total / 10 + (total % 10 == 0 ? 0 : 1), currentPage,
                "/notice/index.html?id=" + id + "&");
        modelAndView.addObject("pagin", pagin);
        modelAndView.addObject("farticles", farticles);
        modelAndView.addObject("id", id);
        modelAndView.setViewName("front/service/index");
        return modelAndView;
    }

    /**
     * 通知详情
     */
    @RequestMapping("/notice/detail")
    public ModelAndView article(int id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        FArticle farticle = redisHelper.getArticleById(systemLan.getFid() ,2, id, WebConstant.BCAgentId);
        if (farticle == null) {
            modelAndView.setViewName("redirect:/notice/index.html");
            return modelAndView;
        }
        modelAndView.addObject("farticle", farticle);
        modelAndView.setViewName("front/service/article");
        return modelAndView;
    }

    /**
     * app页面
     *
     * @return
     */
    @RequestMapping(value = "/service/appnews")
    public ModelAndView AppNews(
            @RequestParam(required = false, defaultValue = "2") Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        ModelAndView modelAndView = new ModelAndView();
        int pagesize = 10;
        FArticleType farticletype = redisHelper.getArticleType(id, 1);
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , 1, farticletype.getFid(), currentPage, WebConstant.BCAgentId);
        int total = redisHelper.getArticlesPageCont(systemLan.getFid() ,1, farticletype.getFid(), WebConstant.BCAgentId);
        int totalpage = total / pagesize + (total % pagesize == 0 ? 0 : 1);
        int nextpage = 0;
        if (currentPage <= totalpage - 1) {
            nextpage = currentPage + 1;
        }

        modelAndView.addObject("farticles", farticles);
        modelAndView.addObject("nextpage", nextpage);
        modelAndView.addObject("id", id);
        modelAndView.setViewName("front/service/appnews");
        return modelAndView;
    }

    /**
     * app页面详情
     *
     * @return
     */
    @RequestMapping("/service/appnew")
    public ModelAndView AppNew(int id) throws Exception {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        ModelAndView modelAndView = new ModelAndView();
        FArticle farticle = redisHelper.getArticleById(systemLan.getFid() , 1, id, WebConstant.BCAgentId);
        if (farticle == null) {
            modelAndView.setViewName("redirect:/service/appnews.html");
            return modelAndView;
        }
        modelAndView.addObject("farticle", farticle);
        modelAndView.setViewName("front/service/appnew");
        return modelAndView;
    }

    /**
     * app页面加载更多
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/service/appnewmore")
    public String AppNewsMore(
            @RequestParam(required = false, defaultValue = "2") Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer cur) {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        int pagesize = 10;
        FArticleType farticletype = redisHelper.getArticleType(id, 1);
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() ,1, farticletype.getFid(), cur, WebConstant.BCAgentId);
        int total = redisHelper.getArticlesPageCont(systemLan.getFid() ,1, farticletype.getFid(), WebConstant.BCAgentId);
        int totalpage = total / pagesize + (total % pagesize == 0 ? 0 : 1);
        int nextpage = 0;
        if (cur <= totalpage - 1) {
            nextpage = cur + 1;
        }
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (FArticle farticle : farticles) {
            JSONObject item = new JSONObject();
            item.put("id", farticle.getFid());
            item.put("title", farticle.getFtitle());
            item.put("content", farticle.getFkeyword());
            item.put("img", farticle.getFindeximg() == null ? (redisHelper.getSystemArgs("staticurl") + "/front/images/service/defaultImg.png") : farticle.getFindeximg());
            item.put("date", Utils.dateFormat(new Timestamp(farticle.getFcreatedate().getTime()), "yyyy.MM.dd"));
            jsonArray.add(item);
        }
        jsonObject.put("items", jsonArray);
        jsonObject.put("nextpage", nextpage);
        jsonObject.put("code", 0);
        return jsonObject.toString();
    }
}
