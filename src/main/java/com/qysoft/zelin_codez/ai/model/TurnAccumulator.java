package com.qysoft.zelin_codez.ai.model;

import com.qysoft.zelin_codez.common.enums.ChatEventTypeEnum;
import com.qysoft.zelin_codez.common.enums.SystemRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单轮对话信息上下文
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Getter
public class TurnAccumulator {

    private final Long userId;

    private final Long appId;

    private final String memoryId;

    private final String turnId;

    private final String codeGenType;

    private final String userMessage;

    private final StringBuilder assistantMessageBuilder = new StringBuilder();

    private final StringBuilder thinkingMessageBuilder = new StringBuilder();

    private final List<ToolTrace> toolTraceList = new ArrayList<>();

    private boolean flushing;

    public TurnAccumulator(Long userId, Long appId, String memoryId, String turnId, String codeGenType, String userMessage) {
        this.userId = userId;
        this.appId = appId;
        this.memoryId = memoryId;
        this.turnId = turnId;
        this.codeGenType = codeGenType;
        this.userMessage = userMessage;
    }

    /**
     * 开始冲洗当前上下文将信息落库[如果有线程冲洗过了将flushing变成true了就不用再次冲洗]
     *
     * @return 是否可以冲洗
     */
    public synchronized boolean beginFlush() {
        if (flushing) {
            return false;
        }
        flushing = true;
        return true;
    }

    /**
     * 重置冲洗标志
     */
    public synchronized void resetFlushFlagOrigin() {
        flushing = false;
    }

    /**
     * AI消息拼接执行器
     *
     * @param message 消息内容
     */
    public synchronized void appendAssistantMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            this.assistantMessageBuilder.append(message);
        }
    }

    /**
     * AI思考消息拼接执行器
     *
     * @param message 消息内容
     */
    public synchronized void appendThinkingMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            this.thinkingMessageBuilder.append(message);
        }
    }

    /**
     * 获取AI消息
     *
     * @return AI消息
     */
    public synchronized String assistantMessage() {
        return this.assistantMessageBuilder.toString();
    }

    /**
     * 获取AI思考消息
     *
     * @return AI思考消息
     */
    public synchronized String thinkingMessage() {
        return this.thinkingMessageBuilder.toString();
    }

    /**
     * 添加工具调用请求
     *
     * @param toolCallId    工具调用Id
     * @param toolName      工具名称
     * @param toolArguments 工具参数
     * @param content       调用内容
     * @param rawEventJson  原始事件Json
     */
    public synchronized void addToolRequest(String toolCallId, String toolName, String toolArguments, String content, String rawEventJson) {
        toolTraceList.add(ToolTrace.request(toolCallId, toolName, toolArguments, content, rawEventJson));
    }

    /**
     * 添加工具调用结果
     *
     * @param toolCallId    工具调用Id
     * @param toolName      工具名称
     * @param toolArguments 工具参数
     * @param toolResult    工具调用结果
     * @param content       调用内容
     * @param rawEventJson  原始事件Json
     */
    public synchronized void addToolResult(String toolCallId, String toolName, String toolArguments, String toolResult, String content, String rawEventJson) {
        toolTraceList.add(ToolTrace.result(toolCallId, toolName, toolArguments, toolResult, content, rawEventJson));
    }

    @Getter
    @AllArgsConstructor
    public static class ToolTrace {

        private final String eventType;

        private final String role;

        private final String toolCallId;

        private final String toolName;

        private final String toolArguments;

        private final String toolResult;

        private final String content;

        private final String rawEventJson;

        /**
         * 构建请求工具调用追溯对象
         *
         * @param toolCallId    工具调用Id
         * @param toolName      工具名称
         * @param toolArguments 工具参数
         * @param content       调用内容
         * @param rawEventJson  原始事件Json
         * @return 工具调用请求追溯对象
         */
        public static ToolTrace request(String toolCallId, String toolName, String toolArguments,
                                        String content, String rawEventJson) {
            return new ToolTrace(ChatEventTypeEnum.TOOL_REQUEST.getValue(), SystemRoleEnum.ASSISTANT.getValue(),
                    toolCallId, toolName, toolArguments, "", content, rawEventJson);
        }

        /**
         * 构建工具调用结果追溯对象
         *
         * @param toolCallId    工具调用Id
         * @param toolName      工具名称
         * @param toolArguments 工具参数
         * @param toolResult    工具调用结果
         * @param content       调用内容
         * @param rawEventJson  原始事件Json
         * @return 工具调用结果追溯对象
         */
        public static ToolTrace result(String toolCallId, String toolName, String toolArguments,
                                       String toolResult, String content, String rawEventJson) {
            return new ToolTrace(ChatEventTypeEnum.TOOL_RESULT.getValue(), SystemRoleEnum.TOOL.getValue(), toolCallId, toolName, toolArguments, toolResult, content, rawEventJson);
        }
    }
}
