package com.qysoft.zelin_codez.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 流式推理模型配置
 * @Author wudi
 * @Date 2026/5/21 10:18
 **/
@Configuration
@ConfigurationProperties("langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        final String MODEL_NAME = "glm-5.1";
        final int MAX_TOKENS = 50000;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(MODEL_NAME)
                .maxTokens(MAX_TOKENS)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
