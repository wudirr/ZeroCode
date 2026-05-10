package com.qysoft.zelin_codez.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private String password;

    private int port;

    private static final String REDIS_MEMORY_PREFIX = "codez:chat:memory:";

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        long ttl = 3600L;
        return RedisChatMemoryStore.builder()
                .host(host)
                .password(password)
                .port(port)
                .ttl(ttl)
                .prefix(REDIS_MEMORY_PREFIX)
                .build();
    }
}
