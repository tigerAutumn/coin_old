package com.qkwl.common.rpc.article;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.qkwl.common.dto.news.FArticle;

/**
 * 文章接口
 * @author hwj
 */
public interface ArticleService {

	
	/**
	 * 按关键词搜索文章
	 * @param keyword 关键词
	 * @return FArticle
	 */
	PageInfo<FArticle> searchArticle(String keyword,Integer pageNum,Integer pageSize);
	
}
