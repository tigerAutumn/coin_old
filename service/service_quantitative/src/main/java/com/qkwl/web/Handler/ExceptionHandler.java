package com.qkwl.web.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.framework.redis.RedisHelper;


@Component("exceptionResolver")
public class ExceptionHandler implements HandlerExceptionResolver {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 异常错误页
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ex.printStackTrace();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("localeStr", "cn");
        modelAndView.addObject("staticurl", redisHelper.getSystemArgs("staticurl"));
        modelAndView.setViewName("front/error/error");
        return modelAndView;
    }

}
