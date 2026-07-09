package com.qysoft.zelin_codez.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

/**
 * 代码类型路由大模型配置类
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConfigurationProperties("langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Long timeOut;

    private Integer maxTokens;

    private Boolean logRequests;

    private Boolean logResponses;

    /**
     * 路由代码生成类型大模型配置
     *
     * @return 路由代码生成类型大模型
     */
    @Bean
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofMillis(timeOut))
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
