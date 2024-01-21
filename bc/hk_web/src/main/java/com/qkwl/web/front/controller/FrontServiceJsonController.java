package com.qkwl.web.front.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.common.PageHelper;
import com.qkwl.common.dto.news.FArticle;
import com.qkwl.common.dto.news.FArticleType;
import com.qkwl.common.dto.web.FSystemLan;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.i18n.LuangeHelper;
import com.qkwl.common.rpc.article.ArticleService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ReturnResult;
import com.qkwl.web.front.controller.base.JsonBaseController;
import com.qkwl.web.utils.WebConstant;

@Controller
public class FrontServiceJsonController extends JsonBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(FrontServiceJsonController.class);

    @Autowired
    private RedisHelper redisHelper;
    
    @Autowired
    private ArticleService articleService;

    /**
     * 通知列表
     */
    @ResponseBody
    @RequestMapping("/notice/index_json")
    public ReturnResult ourService(
            @RequestParam(required = false, defaultValue = "5") Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {// 12,5,5
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        
        id=5;
        
        //System.out.println(LuangeHelper.getLan(request));
        if (systemLan == null) {
            return ReturnResult.FAILUER("");
        }

        FArticleType farticletype = redisHelper.getArticleType(id, systemLan.getFid());
        if (farticletype == null) {

            return ReturnResult.FAILUER("");
        }
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , 2, farticletype.getFid(), currentPage,
                WebConstant.BCAgentId);

        int total = redisHelper.getArticlesPageCont(systemLan.getFid() ,2, farticletype.getFid(), WebConstant.BCAgentId);
        String pagin = PageHelper.generatePagin(total / 10 + (total % 10 == 0 ? 0 : 1), currentPage,
                "/notice/index.html?id=" + id + "&");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pagin", pagin);
        jsonObject.put("farticles", farticles);
        jsonObject.put("id", id);
        return ReturnResult.SUCCESS(jsonObject);
    }

    
    /**
     * 帮助中心
     * 
     * source 1.pc端 2.app端
     * 
     */
    @ResponseBody
    @RequestMapping("/notice/help/index")
    public ReturnResult helpIndex(@RequestParam(required = false, defaultValue = "1") Integer source) throws Exception {
    	try {
    		HttpServletRequest request = sessionContextUtils.getContextRequest();
            FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
            if (systemLan == null) {
                return ReturnResult.FAILUER("");
            }
            List<FArticleType> articleTypeList = redisHelper.getArticleTypeList(systemLan.getFid());
            if (articleTypeList == null) {
                return ReturnResult.FAILUER("");
            }
            JSONArray jsonArray = new JSONArray();
            for (FArticleType fArticleType : articleTypeList) { 
            	JSONObject jsonObject = new JSONObject();
            	jsonObject.put("typeId", fArticleType.getFtypeid());
            	jsonObject.put("title", fArticleType.getFname());
            	jsonObject.put("logo", fArticleType.getImgUrl());
            	List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , source, fArticleType.getFid(), 1, WebConstant.BCAgentId);
            	if(farticles != null) {
            		JSONArray ja = new JSONArray();
            		for (FArticle fArticle : farticles) {
            		JSONObject jo = new JSONObject();
            		jo.put("articleId", fArticle.getFid());
            		jo.put("childrenTitle", fArticle.getFtitle());
            		ja.add(jo);
    				}
            		jsonObject.put("list", ja);
            	}
            	jsonArray.add(jsonObject);
    		}
            return ReturnResult.SUCCESS(jsonArray);
		} catch (Exception e) {
			logger.error("访问 /notice/help/index 异常",e);
		}
    	return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
    }
    
    /**
     * 通过文章类型获取文章列表
     * 
     * source 1.pc端 2.app端
     * type   新闻类型
     * currentPage 当前页
     * 
     */
    @ResponseBody
    @RequestMapping("/notice/help/type")
    public ReturnResult helpType(
    		@RequestParam(required = false, defaultValue = "1") Integer source,
    		@RequestParam(required = false, defaultValue = "5") Integer type,
    		@RequestParam(required = false, defaultValue = "1") Integer currentPage
    		) throws Exception {
    	try {
    		HttpServletRequest request = sessionContextUtils.getContextRequest();
            FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
            if (systemLan == null) {
                return ReturnResult.FAILUER("");
            }
            int articlesPageCont = redisHelper.getArticlesPageCont(systemLan.getFid(), source, type, WebConstant.BCAgentId);
            List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , source, type, currentPage, WebConstant.BCAgentId);
            JSONObject jo = new JSONObject();
            jo.put("total", articlesPageCont);
            jo.put("page", currentPage);
            jo.put("list", farticles);
            return ReturnResult.SUCCESS(jo);
		} catch (Exception e) {
			logger.error("访问 /notice/help/type 异常",e);
		}
    	return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
    }
    
    /**
     * 通过文章id获取文章
     * 
     * source 1.pc端 2.app端
     * type   新闻类型
     * currentPage 当前页
     * 
     */
    @ResponseBody
    @RequestMapping("/notice/help/detail")
    public ReturnResult helpDetail(@RequestParam(required = true) Integer id) throws Exception {
    	try {
    		FArticle articleById = redisHelper.getArticleById(id);
            if(articleById == null) {
            	return ReturnResult.FAILUER("");
            }
            return ReturnResult.SUCCESS(articleById);
		} catch (Exception e) {
			logger.error("访问 /notice/help/detail 异常",e);
		}
    	return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
    }
    
    /**
     * 搜索文章
     */
    @ResponseBody
    @RequestMapping("/notice/search_article_json")
    public ReturnResult searchArticle(
    		@RequestParam(required = true)String keyword ,
    		@RequestParam(required = false,defaultValue = "1")Integer page
    		) throws Exception {
    	try {
    		PageInfo<FArticle> searchArticle = articleService.searchArticle(keyword, page, Constant.webPageSize);
            return ReturnResult.SUCCESS(searchArticle);
		} catch (Exception e) {
			logger.error("访问 /notice/help/detail 异常",e);
		}
    	return ReturnResult.FAILUER(GetR18nMsg("com.public.error.10000"));
    }
    
    /**
     * 通知详情
     */
    @ResponseBody
    @RequestMapping("/notice/detail_json")
    public ReturnResult article(Integer id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        FArticle farticle = redisHelper.getArticleById(systemLan.getFid() ,2, id, WebConstant.BCAgentId);
        if (farticle == null) {
            return ReturnResult.FAILUER("");
			/*modelAndView.setViewName("redirect:/notice/index.html");
			return modelAndView;*/
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("farticle", farticle);
        return ReturnResult.SUCCESS(jsonObject);
    }
    


    /**
     * app页面
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/service/appnews_json")
    public ReturnResult AppNews(
            @RequestParam(required = false, defaultValue = "2") Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) {
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        int pagesize = 10;
        FArticleType farticletype = redisHelper.getArticleType(id, 1);
        List<FArticle> farticles = redisHelper.getArticles(systemLan.getFid() , 1, farticletype.getFid(), currentPage, WebConstant.BCAgentId);
        int total = redisHelper.getArticlesPageCont(systemLan.getFid(),1, farticletype.getFid(), WebConstant.BCAgentId);
        int totalpage = total / pagesize + (total % pagesize == 0 ? 0 : 1);
        int nextpage = 0;
        if (currentPage <= totalpage - 1) {
            nextpage = currentPage + 1;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("farticles", farticles);
        jsonObject.put("nextpage", nextpage);
        jsonObject.put("id", id);
        return ReturnResult.SUCCESS(jsonObject);
    }

    /**
     * app页面详情
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/service/appnew_json")
    public ReturnResult AppNew(int id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        HttpServletRequest request = sessionContextUtils.getContextRequest();
        FSystemLan systemLan = redisHelper.getLanguageType(LuangeHelper.getLan(request));
        FArticle farticle = redisHelper.getArticleById(systemLan.getFid() ,1, id, WebConstant.BCAgentId);
        if (farticle == null) {
            return ReturnResult.FAILUER("");
			/*modelAndView.setViewName("redirect:/service/appnews.html");
			return modelAndView;*/
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("farticle", farticle);
        return ReturnResult.SUCCESS(jsonObject);
    }

}
