package com.qkwl.web.config;

import com.qkwl.common.auth.SessionContextUtils;

import com.qkwl.common.framework.redis.RedisHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;

/**
 * Created by muji on 2017/5/24.
 */
@Configuration
public class BeanConfig {

    @Bean
    public SessionContextUtils sessionContextUtils() {
        return new SessionContextUtils();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource i18nSource = new ResourceBundleMessageSource();
        i18nSource.setBasenames("i18n/international_msg", "i18n/page_msg", "i18n/result");
        return i18nSource;
    }

    @Bean(name = "localeResolver")
    public CookieLocaleResolver cookieLocaleResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.TAIWAN);
        resolver.setCookieName("oex_lan");
        resolver.setCookieMaxAge(31536000);
        return resolver;
    }

    @Bean
    public InternalResourceViewResolver resourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setContentType("text/html;charset=utf-8");
        resolver.setPrefix("/WEB-INF/pages/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(1);
        return resolver;
    }


}
