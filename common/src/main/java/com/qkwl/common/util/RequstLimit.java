package com.qkwl.common.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequstLimit {
	/**
	*
	* 允许访问的次数，默认值MAX_VALUE
	*/
  int count() default 10;

  /**
    *
    * 时间段，单位为毫秒，默认值一分钟
    */
  long time() default 60000;
}
