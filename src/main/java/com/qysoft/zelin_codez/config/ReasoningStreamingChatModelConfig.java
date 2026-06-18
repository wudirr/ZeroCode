package com.qysoft.zelin_codez.config;

import com.qysoft.zelin_codez.ai.listeners.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

/**
 * @Description 流式推理模型配置
 * @Author wudi
 * @Date 2026/5/21 10:18
 **/
@Configuration
@ConfigurationProperties("langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Long timeOut;

    private Integer maxTokens;

    private Boolean logRequests;

    private Boolean logResponses;

    /**
     * 流式推理大模型配置
     *
     * @return 流式推理大模型
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofMillis(timeOut))
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .listeners(List.of(new AiModelMonitorListener()))
                .build();
    }
}
