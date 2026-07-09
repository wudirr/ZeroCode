package com.qysoft.zelin_codez.model.annotations;


import com.qysoft.zelin_codez.model.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流类型
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 空窗时间内能够取到的最大令牌数[限流速率]
     */
    int rate() default 10;

    /**
     * 空窗时间[单位时间/s]
     */
    int rateInterval() default 1;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁,请稍后再试";
}
