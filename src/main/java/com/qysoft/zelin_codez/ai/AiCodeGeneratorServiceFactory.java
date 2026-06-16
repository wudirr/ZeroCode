package com.qysoft.zelin_codez.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qysoft.zelin_codez.ai.guardrails.PromptSafetyInputGuardrail;
import com.qysoft.zelin_codez.ai.tools.ToolManager;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

/**
 * @Description 零代码生成服务制造工厂
 * @Author wudi
 * @Date 2026/4/27 10:24
 **/
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * caffeine缓存对象
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.warn("Ai服务实例被移除,appId:{}, cause:{}", key, cause);
            }).build();


    /**
     * 获取对应的AiService实例对象,不携带代码生成类型,兼容老逻辑
     *
     * @param appId 应用id
     * @return Ai服务实例
     */
    public AiCodeGeneratorService getAiService(Long appId) {
        return getAiService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 获取对应的AiService实例对象,携带代码生成类型
     *
     * @param appId           应用id
     * @param codeGenTypeEnum 代码生成类型
     * @return Ai服务实例
     */
    public AiCodeGeneratorService getAiService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = buildKey(appId, codeGenTypeEnum);
        return serviceCache.get(cacheKey, key -> createAiService(appId, codeGenTypeEnum));
    }

    private AiCodeGeneratorService createAiService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            return null;
        }
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(30)
                .build();
        //从数据库中读取历史数据刷新缓存
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 30);
        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> {
                OpenAiStreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", OpenAiStreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools((Object[]) toolManager.getTools())
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest, "ERROR EXECUTE TOOLS" + toolExecutionRequest.name()))
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }
            case HTML, MULTI_FILE -> {
                OpenAiStreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", OpenAiStreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }
            default -> throw new BusinessException("不支持的代码生成类型");
        };

    }

    private static String buildKey(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.getValue();
    }

    //@Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        OpenAiStreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", OpenAiStreamingChatModel.class);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .build();
    }
}
