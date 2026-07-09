package com.qysoft.zelin_codez.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableDubbo
@MapperScan("com.qysoft.zelin_codez.user.mapper")
public class ZelinCodezUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZelinCodezUserApplication.class, args);
    }
}
