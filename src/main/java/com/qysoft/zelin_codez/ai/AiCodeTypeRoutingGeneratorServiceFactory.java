package com.qysoft.zelin_codez.ai;

import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
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

    /**
     * 获取AI生成代码类型服务实例
     *
     * @return 实例对象
     */
    @Bean
    public AiCodeTypeRoutingGeneratorService getAiCodeTypeRoutingGeneratorService() {
        return aiCodeTypeRoutingGeneratorService();
    }

    /**
     * 创建AI生成代码类型服务实例
     *
     * @return 实例对象
     */
    private AiCodeTypeRoutingGeneratorService aiCodeTypeRoutingGeneratorService() {
        OpenAiChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", OpenAiChatModel.class);
        return AiServices.builder(AiCodeTypeRoutingGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }
}
