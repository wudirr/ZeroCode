package com.qysoft.zelin_codez.app.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 聊天记忆回放服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface ChatMemoryReplayService {

    /**
     * 事件重放
     *
     * @param memoryId   记忆id
     * @param chatMemory 聊天记忆对象
     * @param maxEvents  回放事件数量的最大值,用来控制上下文产生的噪声
     * @return 回放事件数量
     */
    int rebuildFromEvent(String memoryId, MessageWindowChatMemory chatMemory, int maxEvents);
}
