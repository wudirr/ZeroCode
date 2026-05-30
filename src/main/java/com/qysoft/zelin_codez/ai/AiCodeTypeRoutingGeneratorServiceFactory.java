package com.qysoft.zelin_codez.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 代码类型推断AI服务制造工厂
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class AiCodeTypeRoutingGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI生成代码类型服务实例
     *
     * @return 实例对象
     */
    @Bean
    public AiCodeTypeRoutingGeneratorService aiCodeTypeRoutingGeneratorService() {
        return AiServices.builder(AiCodeTypeRoutingGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }
}
