package com.qysoft.zelin_codez.langgraph4j.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 代码检查服务实例工厂
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class CodeQualityServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 代码质量检查服务
     *
     * @return 代码质量检查服务实例
     */
    @Bean
    public CodeQualityService codeQualityService() {
        return AiServices.builder(CodeQualityService.class)
                .chatModel(chatModel)
                .build();
    }
}
