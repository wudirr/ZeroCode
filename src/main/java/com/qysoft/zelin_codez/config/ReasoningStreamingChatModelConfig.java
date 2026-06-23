package com.qysoft.zelin_codez.config;

import com.qysoft.zelin_codez.ai.listeners.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("${langchain4j.open-ai.reasoning-return-thinking: false}")
    private Boolean reasoningReturnThinking;

    @Value("${langchain4j.open-ai.reasoning-send-thinking: false}")
    private Boolean reasoningSendThinking;

    @Value("${langchain4j.open-ai.reasoning-thinking-field: reasoning_content}")
    private String reasoningThinkingField;

    @Value("${langchain4j.open-ai.reasoning-thinking-type}")
    private String reasoningThinkingType;

    @Value("${langchain4j.open-ai.reasoning-effort}")
    private String reasoningEffort;


    /**
     * 流式推理大模型配置[增加开启思考开关选项]
     *
     * @return 流式推理大模型
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofMillis(timeOut))
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .returnThinking(reasoningReturnThinking)
                .sendThinking(reasoningSendThinking, reasoningThinkingField)
                .listeners(List.of(new AiModelMonitorListener()));
        if (StringUtils.isNotBlank(reasoningEffort)) {
            builder.reasoningEffort(reasoningEffort);
        }
        if (StringUtils.isNotBlank(reasoningThinkingType) && reasoningThinkingType.equals("enabled")) {
            Map<String, Object> customParameters = new HashMap<>();
            customParameters.put("thinking", Map.of("type", reasoningThinkingType));
            builder.customParameters(customParameters);
        }
        return builder.build();
    }
}
