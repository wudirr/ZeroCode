package com.qysoft.zelin_codez.ai.config;

import com.qysoft.zelin_codez.ai.stores.CompactingChatMemoryStore;
import com.qysoft.zelin_codez.ai.stores.SanitizingChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
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
    public ChatMemoryStore redisChatMemoryStore() {
        long ttl = 3600L;
        ChatMemoryStore redisChatMemoryStore = RedisChatMemoryStore.builder()
                .host(host)
                .password(password)
                .port(port)
                .ttl(ttl)
                .prefix(REDIS_MEMORY_PREFIX)
                .build();
        //装饰链模式(原始消息存储 -> 清洗消息存储 -> 压缩消息存储)
        return new CompactingChatMemoryStore(new SanitizingChatMemoryStore(redisChatMemoryStore));
    }
}
