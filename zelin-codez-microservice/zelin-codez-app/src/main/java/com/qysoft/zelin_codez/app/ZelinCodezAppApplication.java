package com.qysoft.zelin_codez.app;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication(exclude = RedisEmbeddingStoreAutoConfiguration.class)
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@MapperScan("com.qysoft.zelin_codez.app.mapper")
@ComponentScan(basePackages = {"com.qysoft.zelin_codez"})
public class ZelinCodezAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZelinCodezAppApplication.class, args);
    }
}
