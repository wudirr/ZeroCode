package com.qysoft.zelin_codez.stores;

import cn.hutool.core.collection.CollectionUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Layer1 工具结果微压缩
 * 策略: 对于超出阈值数量的消息进行一个微压缩,保留没有超过阈值的消息原始内容
 * 内容超过一定长度的工具执行结果进行一个微压缩
 * 目的: 减少发送给LLM的历史上下文工具中占用的Token,让AI知道某些工具已经调用过了,而不是把整个结果发送给AI
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class CompactingChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore delegate;

    /**
     * 最多保留10条最近的消息,超过的部分对工具调用结果进行微压缩
     */
    private static final int KEEP_RECENT_MESSAGE = 10;

    /**
     * 对工具调用结果内容进行微压缩的阈值
     */
    private static final int MAX_RESULT_SIZE = 300;

    public CompactingChatMemoryStore(ChatMemoryStore chatMemoryStore) {
        this.delegate = chatMemoryStore;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return compactMessages(delegate.getMessages(memoryId)).messages();
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
     * 压缩并且持久化操作
     *
     * @param memoryId 记忆id
     * @return 执行结果
     */
    public void compactAndPersist(Object memoryId) {
        try {
            List<ChatMessage> messages = delegate.getMessages(memoryId);
            if (CollectionUtil.isEmpty(messages)) {
                return;
            }
            MicroCompactResult microCompactResult = compactMessages(messages);
            int compactCount = microCompactResult.compactCount();
            if (compactCount == 0) {
                return;
            }
            log.info("Layer1 微压缩成功,memoryId = {},compactCount = {}", memoryId, compactCount);
            delegate.updateMessages(memoryId, microCompactResult.messages());
        } catch (Exception e) {
            log.warn("Layer 1 微压缩失败,memoryId = {},失败原因 = {}", memoryId, e.getMessage());
        }
    }

    /**
     * 压缩消息方法[微压缩]
     *
     * @param messages 消息列表
     * @return 微压缩结果
     */
    private MicroCompactResult compactMessages(List<ChatMessage> messages) {
        if (CollectionUtil.isEmpty(messages)) {
            return new MicroCompactResult(messages, 0);
        }
        int compactCount = 0;
        //记录索引,方便后续对超出原始内容最大窗口的部分消息进行压缩
        List<Integer> messageIdx = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            messageIdx.add(i);
        }
        if (messageIdx.size() < KEEP_RECENT_MESSAGE) {
            return new MicroCompactResult(messages, 0);
        }
        messageIdx = messageIdx.subList(0, messageIdx.size() - KEEP_RECENT_MESSAGE);
        List<ChatMessage> compactMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) instanceof ToolExecutionResultMessage toolResultMessage && messageIdx.contains(i)) {
                String toolResult = toolResultMessage.text();
                if (toolResult != null && toolResult.length() > MAX_RESULT_SIZE) {
                    //这个时候对工具执行结果进行微压缩
                    toolResult = String.format("[已执行: %s]", toolResultMessage.toolName());
                    compactMessages.add(ToolExecutionResultMessage.from(toolResultMessage.id(), toolResultMessage.toolName(), toolResult));
                    compactCount++;
                } else {
                    compactMessages.add(messages.get(i));
                }
            } else {
                compactMessages.add(messages.get(i));
            }
        }
        return new MicroCompactResult(compactMessages, compactCount);
    }

    /**
     * 微压缩结果模型
     *
     * @param messages     微压缩后的消息列表
     * @param compactCount 压缩数量
     */
    public record MicroCompactResult(List<ChatMessage> messages, int compactCount) {
    }
}
