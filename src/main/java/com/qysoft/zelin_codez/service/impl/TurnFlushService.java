package com.qysoft.zelin_codez.service.impl;

import com.qysoft.zelin_codez.ai.model.TurnAccumulator;
import com.qysoft.zelin_codez.common.enums.ChatEventTypeEnum;
import com.qysoft.zelin_codez.common.enums.ChatHistoryMessageTypeEnum;
import com.qysoft.zelin_codez.common.enums.SystemRoleEnum;
import com.qysoft.zelin_codez.domain.entity.ChatEventLog;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.manager.TurnAccumulatorManager;
import com.qysoft.zelin_codez.service.ChatEventLogService;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import com.qysoft.zelin_codez.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单轮对话落库服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Service
public class TurnFlushService {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ChatEventLogService chatEventLogService;

    @Resource
    private UserService userService;

    /**
     * 正常冲洗上下文消息
     *
     * @param turnId 单轮对话id
     */
    @Transactional(rollbackFor = Exception.class)
    public void flushSuccess(String turnId) {
        TurnAccumulator accumulator = TurnAccumulatorManager.getAccumulator(turnId);
        if (accumulator == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (!accumulator.beginFlush()) {
            return;
        }
        try {
            flushTransactional(accumulator, null);
        } catch (Exception e) {
            accumulator.resetFlushFlagOrigin();
            TurnAccumulatorManager.removeAccumulator(turnId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 异常冲洗上下文消息
     *
     * @param turnId       单轮对话Id
     * @param errorMessage 错误消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void flushError(String turnId, String errorMessage) {
        TurnAccumulator accumulator = TurnAccumulatorManager.getAccumulator(turnId);
        if (accumulator == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (!accumulator.beginFlush()) {
            return;
        }
        try {
            flushTransactional(accumulator, StringUtils.defaultIfBlank(errorMessage, "unknown error"));
            TurnAccumulatorManager.removeAccumulator(turnId);
        } catch (Exception e) {
            accumulator.resetFlushFlagOrigin();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 冲洗操作
     *
     * @param accumulator  聚合上下文
     * @param errorMessage 错误消息
     */
    public void flushTransactional(TurnAccumulator accumulator, String errorMessage) {
        String userMessage = accumulator.getUserMessage();
        String assistantMessage = accumulator.assistantMessage();
        String thinkingMessage = accumulator.thinkingMessage();
        Long userId = accumulator.getUserId();
        Long appId = accumulator.getAppId();
        String turnId = accumulator.getTurnId();
        String memoryId = accumulator.getMemoryId();
        String codeGenType = accumulator.getCodeGenType();
        List<TurnAccumulator.ToolTrace> toolTraceList = accumulator.getToolTraceList();
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户Id不能为空");
        User loginUser = userService.getById(userId);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        boolean failed = StringUtils.isNotBlank(errorMessage);
        String finalAssistantMessage = assistantMessage;
        if (failed) {
            finalAssistantMessage = StringUtils.isBlank(assistantMessage)
                    ? "AI回复失败: " + errorMessage
                    : assistantMessage + "\n\nAI回复失败: " + errorMessage;
        }
        //添加到历史对话当中(ai + user)
        chatHistoryService.addChatMessage(
                appId,
                userMessage,
                ChatHistoryMessageTypeEnum.USER.getValue(),
                loginUser
        );
        chatHistoryService.addChatMessage(
                appId,
                finalAssistantMessage,
                ChatHistoryMessageTypeEnum.AI.getValue(),
                loginUser
        );
        AtomicInteger seqCount = new AtomicInteger();
        List<ChatEventLog> chatEventLogList = new ArrayList<>();
        //添加用户消息日志
        chatEventLogList.add(ChatEventLog.builder()
                .appId(appId)
                .memoryId(memoryId)
                .turnId(turnId)
                .seq(seqCount.incrementAndGet())
                .codeGenType(codeGenType)
                .role(SystemRoleEnum.USER.getValue())
                .eventType(ChatEventTypeEnum.USER_MESSAGE.getValue())
                .content(userMessage)
                .build());

        //添加AI思考信息日志
        if (StringUtils.isNotBlank(thinkingMessage)) {
            chatEventLogList.add(ChatEventLog.builder()
                    .userId(userId)
                    .appId(appId)
                    .memoryId(memoryId)
                    .turnId(turnId)
                    .seq(seqCount.incrementAndGet())
                    .codeGenType(codeGenType)
                    .role(SystemRoleEnum.ASSISTANT.getValue())
                    .eventType(ChatEventTypeEnum.THINKING_FINAL.getValue())
                    .reasoningContent(thinkingMessage)
                    .build());
        }
        //添加工具调用日志信息
        for (TurnAccumulator.ToolTrace toolTrace : toolTraceList) {
            chatEventLogList.add(ChatEventLog.builder()
                    .userId(userId)
                    .appId(appId)
                    .memoryId(memoryId)
                    .turnId(turnId)
                    .seq(seqCount.incrementAndGet())
                    .codeGenType(codeGenType)
                    .role(SystemRoleEnum.TOOL.getValue())
                    .eventType(toolTrace.getEventType())
                    .toolCallId(toolTrace.getToolCallId())
                    .toolName(toolTrace.getToolName())
                    .toolArguments(toolTrace.getToolArguments())
                    .toolResult(toolTrace.getToolResult())
                    .content(toolTrace.getContent())
                    .rawEventJson(toolTrace.getRawEventJson())
                    .build());
        }
        //添加AI回复消息日志
        if (StringUtils.isNotBlank(finalAssistantMessage)) {
            chatEventLogList.add(ChatEventLog.builder()
                    .userId(userId)
                    .appId(appId)
                    .memoryId(memoryId)
                    .turnId(turnId)
                    .seq(seqCount.incrementAndGet())
                    .codeGenType(codeGenType)
                    .role(SystemRoleEnum.ASSISTANT.getValue())
                    .eventType(ChatEventTypeEnum.ASSISTANT_FINAL.getValue())
                    .reasoningContent(StringUtils.defaultIfBlank(thinkingMessage, ""))
                    .content(finalAssistantMessage)
                    .build());
        }
        //批量添加到数据库
        boolean flag = chatEventLogService.saveBatch(chatEventLogList);
        ThrowUtils.throwIf(!flag, ErrorCode.SYSTEM_ERROR, "批量保存日志失败");
    }
}
