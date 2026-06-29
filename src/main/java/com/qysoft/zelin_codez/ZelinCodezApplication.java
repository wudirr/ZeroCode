package com.qysoft.zelin_codez;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = RedisEmbeddingStoreAutoConfiguration.class)
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@MapperScan("com.qysoft.zelin_codez.mapper")
public class ZelinCodezApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZelinCodezApplication.class, args);
    }

}
