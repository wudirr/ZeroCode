package com.qysoft.zelin_codez.app.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 上下文压缩服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface ContextCompactionService {

    /**
     * Layer2 上下文超过设置的阈值时自动压缩
     *
     * @param chatMemory chatMemory
     * @param memoryId   记忆id
     * @return 压缩结果
     */
    boolean autoCompactedIfNeeded(MessageWindowChatMemory chatMemory, String memoryId);

    /**
     * Layer3 手动压缩上下文
     *
     * @param memoryId 记忆id
     * @return 压缩结果
     */
    boolean forceCompacted(String memoryId);
}
