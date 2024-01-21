package com.qkwl.service.entrust.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.news.FArticle;
import com.qkwl.common.rpc.article.ArticleService;
import com.qkwl.service.entrust.dao.FArticleMapper;

/**
 * 委单接口实现
 *
 * @author hwj
 */
@Service("articleService")
public class ArticleServiceImpl implements ArticleService {

    
    private Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    
    @Autowired
    private FArticleMapper articleMapper;

	@Override
	public PageInfo<FArticle> searchArticle(String keyword,Integer pageNum,Integer pageSize) {
		try {
			PageHelper.startPage(pageNum, pageSize);
			List<FArticle> searchByKeyword = articleMapper.searchByKeyword(keyword);
			PageInfo<FArticle> pageInfo = new PageInfo<FArticle>(searchByKeyword);
			return pageInfo;
		} catch (Exception e) {
			logger.error("searchArticleu异常，参数：{keyword:"+keyword+"}",e);
		}
		return null;
	}

}