package com.qkwl.service.entrust.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qkwl.common.dto.news.FArticle;

/**
 * 新闻-数据库访问接口
 * @author hwj
 */
@Mapper
public interface FArticleMapper {
	
	/**
	 * 根据关键词查询
	 * @param keyword 关键字
	 * @return 
	 */
	List<FArticle> searchByKeyword(@Param("keyword") String keyword);

}