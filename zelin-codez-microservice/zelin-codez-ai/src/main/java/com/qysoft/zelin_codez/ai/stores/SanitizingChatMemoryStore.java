package com.qysoft.zelin_codez.ai.stores;

import cn.hutool.core.collection.CollectionUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 清理后的聊天记忆存储(对记忆消息进行兜底清洗,防止非法消息导致模型返回400)
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class SanitizingChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore delegate;

    public SanitizingChatMemoryStore(ChatMemoryStore chatMemoryStore) {
        this.delegate = chatMemoryStore;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return sanitizingMessage(delegate.getMessages(memoryId), memoryId);
    }

    /**
     * 清洗消息
     *
     * @param messages 消息列表
     * @return 清洗后的消息列表
     */
    private List<ChatMessage> sanitizingMessage(List<ChatMessage> messages, Object memoryId) {
        if (CollectionUtil.isEmpty(messages)) return List.of();
        //定义一个新集合
        List<ChatMessage> sanitizedMessages = new ArrayList<>();
        for (ChatMessage chatMessage : messages) {
            if (chatMessage instanceof AiMessage aiMessage) {
                boolean hasText = hasText(aiMessage.text());
                boolean hasToolCalled = aiMessage.hasToolExecutionRequests();
                if (!hasText && !hasToolCalled) {
                    //OpenAI 兼容接口要求,Assistant至少有content或者tool_calls
                    String thinkingText = aiMessage.thinking();
                    if (hasText(thinkingText)) {
                        sanitizedMessages.add(AiMessage.builder()
                                .thinking(thinkingText)
                                .text(thinkingText)
                                .toolExecutionRequests(aiMessage.toolExecutionRequests())
                                .build());
                        log.info("清洗缺少内容的aiMessage,sanitizedMessage = {},memoryId = {}", thinkingText, memoryId);
                    } else {
                        log.warn("忽略缺少内容的Assistant信息,memoryId = {}", memoryId);
                    }
                }
            }
            sanitizedMessages.add(chatMessage);
        }
        return sanitizedMessages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        delegate.updateMessages(memoryId, list);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        delegate.deleteMessages(memoryId);
    }

    /**
     * 判断是否有文本信息
     *
     * @param text 文本内容
     * @return 判断结果
     */
    private boolean hasText(String text) {
        return text != null && !text.isEmpty();
    }
}
