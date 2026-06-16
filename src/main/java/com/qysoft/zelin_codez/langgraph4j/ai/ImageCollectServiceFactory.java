package com.qysoft.zelin_codez.langgraph4j.ai;

import com.qysoft.zelin_codez.langgraph4j.tools.ArchitectureImageCollectTool;
import com.qysoft.zelin_codez.langgraph4j.tools.ContentImageCollectTool;
import com.qysoft.zelin_codez.langgraph4j.tools.IllustrationImageCollectTool;
import com.qysoft.zelin_codez.langgraph4j.tools.LogoImageCollectTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集服务创建工厂
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ImageCollectServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ContentImageCollectTool contentImageCollectTool;

    @Resource
    private IllustrationImageCollectTool illustrationImageCollectTool;

    @Resource
    private ArchitectureImageCollectTool architectureImageCollectTool;

    @Resource
    private LogoImageCollectTool logoImageCollectTool;

    @Bean
    public ImageCollectService imageCollectService() {
        return AiServices.builder(ImageCollectService.class)
                .chatModel(chatModel)
                .tools(contentImageCollectTool,
                        illustrationImageCollectTool,
                        architectureImageCollectTool,
                        logoImageCollectTool)
                .build();
    }
}
