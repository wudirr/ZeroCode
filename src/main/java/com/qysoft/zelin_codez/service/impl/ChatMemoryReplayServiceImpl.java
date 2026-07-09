package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qysoft.zelin_codez.common.enums.ChatEventTypeEnum;
import com.qysoft.zelin_codez.domain.entity.ChatEventLog;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.service.ChatEventLogService;
import com.qysoft.zelin_codez.service.ChatMemoryReplayService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Service
public class ChatMemoryReplayServiceImpl implements ChatMemoryReplayService {

    @Resource
    private ChatEventLogService chatEventLogService;

    @Override
    public int rebuildFromEvent(String memoryId, MessageWindowChatMemory chatMemory, int maxEvents) {
        int replayCount = 0;
        chatMemory.clear();
        List<ChatEventLog> chatEventList = chatEventLogService.listEventByMemoryId(memoryId, maxEvents);
        for (ChatEventLog chatEventLog : chatEventList) {
            String eventType = chatEventLog.getEventType();
            ChatEventTypeEnum chatEventTypeEnum = ChatEventTypeEnum.getByValue(eventType);
            ThrowUtils.throwIf(chatEventTypeEnum == null, ErrorCode.PARAMS_ERROR);
            switch (chatEventTypeEnum) {
                case USER_MESSAGE -> {
                    String userMessage = chatEventLog.getContent();
                    if (StringUtils.isBlank(userMessage)) {
                        continue;
                    }
                    chatMemory.add(UserMessage.from(userMessage));
                    replayCount++;
                }
                case TOOL_REQUEST -> {
                    if (StrUtil.hasBlank(chatEventLog.getToolCallId(), chatEventLog.getToolName())) {
                        continue;
                    }
                    ToolExecutionRequest toolRequestMessage = ToolExecutionRequest.builder()
                            .id(chatEventLog.getToolCallId())
                            .name(chatEventLog.getToolName())
                            .arguments(chatEventLog.getToolArguments())
                            .build();
                    chatMemory.add(AiMessage.from(toolRequestMessage));
                    replayCount++;
                }
                case TOOL_RESULT -> {
                    if (StrUtil.hasBlank(chatEventLog.getToolCallId(), chatEventLog.getToolName())) {
                        continue;
                    }
                    String resultText = chatEventLog.getContent();
                    if (StringUtils.isBlank(resultText)) {
                        continue;
                    }
                    chatMemory.add(ToolExecutionResultMessage.from(chatEventLog.getToolCallId(), chatEventLog.getToolName(), resultText));
                    replayCount++;
                }
                case ASSISTANT_FINAL -> {
                    if (StringUtils.isBlank(chatEventLog.getContent())) {
                        continue;
                    }
                    AiMessage.Builder builder = AiMessage.builder()
                            .text(chatEventLog.getContent());
                    String reasoningText = chatEventLog.getReasoningContent();
                    if (StringUtils.isNotBlank(reasoningText)) {
                        builder.thinking(reasoningText);
                    }
                    chatMemory.add(builder.build());
                    replayCount++;
                }
                default -> {
                    // THINKING和ASSISTANT_MESSAGE暂时不回放
                }
            }
        }
        return replayCount;
    }
}
