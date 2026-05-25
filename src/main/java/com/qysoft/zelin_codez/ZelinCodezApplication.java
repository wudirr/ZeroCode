package com.qysoft.zelin_codez;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.qysoft.zelin_codez.mapper")
public class ZelinCodezApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZelinCodezApplication.class, args);
    }

}
