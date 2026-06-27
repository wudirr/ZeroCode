package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.qysoft.zelin_codez.config.ContextCompactionProperties;
import com.qysoft.zelin_codez.service.ContextCompactionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
public class ContextCompactionServiceImpl implements ContextCompactionService {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ContextCompactionProperties contextCompactionProperties;

    @Resource
    private ChatMemoryStore redisChatMemoryStore;

    private static final String COMPACT_MARKER = "[对话已压缩完成,完整记录保存在事件日志中]";

    private static final String SUMMARY_PROMPT = """
            请总结以下对话的关键信息，确保包含：
            1) 已完成的操作（创建/修改/删除了哪些文件），每次修改必须记录【修改前的原始值 → 修改后的新值】的完整对照，
               例如：「将首页标题从『静夜思』修改为『测试』」，而不是只写「将标题修改为『测试』」。
               这一点非常重要，因为用户可能需要回退到之前的状态，必须保留原始值。
            2) 当前项目状态（项目结构、关键组件）
            3) 用户的核心需求和偏好
            4) 尚未完成的任务或待解决的问题
            5) 关键数据的变更历史链（按时间顺序），包括文本内容、配置参数、样式属性等的前后值

            要求：
            - 对于每一处内容修改，必须同时保留修改前和修改后的值，确保可回溯可回退
            - 使用「A → B」格式记录变更对照
            - 简洁但不遗漏任何修改细节，使用中文回复

            对话内容：
            """;

    @Override
    public boolean autoCompactedIfNeeded(MessageWindowChatMemory chatMemory, String memoryId) {
        List<ChatMessage> messages = chatMemory.messages();
        if (CollectionUtil.isEmpty(messages)) return false;
        int estimatedTokens = estimatedTokens(messages);
        if (estimatedTokens <= contextCompactionProperties.getTokenThreshold()) {
            return false;
        }
        log.warn("上下文消息超过设置阈值,开始自动进行上下文压缩, memoryId = {}", memoryId);
        return doCompact(chatMemory, memoryId);
    }

    @Override
    public boolean forceCompacted(String memoryId) {
        try {
            List<ChatMessage> messages = redisChatMemoryStore.getMessages(memoryId);
            if (messages.isEmpty() || messages.size() < 5) {
                log.warn("上下文消息过短,不需要进行压缩,memoryId = {}", memoryId);
                return false;
            }
            log.info("开始手动进行上下文压缩, memoryId = {}", memoryId);
            String summaryContext = generateSummary(messages);
            List<ChatMessage> messageList = List.of(UserMessage.from(COMPACT_MARKER + "\n\n" + summaryContext));
            redisChatMemoryStore.updateMessages(memoryId, messageList);
            log.info("手动压缩上下文成功,memoryId = {},content = {}", memoryId, messageList);
            return true;
        } catch (Exception e) {
            String message = String.format("手动压缩上下文失败,errorMessage = %s,memoryId = %s", e.getMessage(), memoryId);
            log.warn(message);
            return false;
        }
    }

    /**
     * 对上下文进行压缩操作
     *
     * @param chatMemory chatMemory
     * @return 压缩结果
     */
    private boolean doCompact(MessageWindowChatMemory chatMemory, String memoryId) {
        try {
            List<ChatMessage> messages = chatMemory.messages();
            String summaryContext = generateSummary(messages);
            chatMemory.clear();
            chatMemory.add(UserMessage.from(COMPACT_MARKER + "\n\n" + summaryContext));
            log.info("上下文压缩完成, memoryId = {}, summary = {}", memoryId, summaryContext);
            return true;
        } catch (Exception e) {
            String message = String.format("上下文压缩失败,errorMessage = %s,memoryId = %s", e.getMessage(), memoryId);
            log.warn(message);
            return false;
        }
    }

    /**
     * 获取上下文摘要
     *
     * @param messages 消息列表
     * @return 摘要内容
     */
    private String generateSummary(List<ChatMessage> messages) {
        StringBuilder summaryAppender = new StringBuilder();
        for (ChatMessage message : messages) {
            summaryAppender.append(formatMessage(message)).append("\n");
        }
        String summary = summaryAppender.toString();
        if (summary.length() > contextCompactionProperties.getMaxSummaryInputChars()) {
            summary = summary.substring(summary.length() - contextCompactionProperties.getMaxSummaryInputChars());
        }
        //将摘要消息喂给大模型进行总结
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(SUMMARY_PROMPT + summary)).build();
        ChatResponse chatResponse = chatModel.chat(chatRequest);
        String summaryContext = chatResponse.aiMessage().text();
        if (StringUtils.isBlank(summaryContext)) {
            summaryContext = "无法生成摘要，请查看事件日志获取完整记录。";
        }
        return summaryContext;
    }

    /**
     * 将chatMessage进行格式化,转化成普通的纯文本
     *
     * @param message 消息内容
     * @return 格式化后的纯文本
     */
    private String formatMessage(ChatMessage message) {
        if (message instanceof UserMessage userMessage) {
            return "[用户]: " + userMessage.singleText();
        } else if (message instanceof AiMessage aiMessage) {
            if (aiMessage.hasToolExecutionRequests()) {
                return "[AI 工具调用]: " + aiMessage.toolExecutionRequests().toString();
            }
            return "[AI]: " + (StringUtils.isBlank(aiMessage.text()) ? "" : aiMessage.text());
        } else if (message instanceof ToolExecutionResultMessage toolExecutionResultMessage) {
            String text = toolExecutionResultMessage.text();
            String toolName = toolExecutionResultMessage.toolName();
            if (StringUtils.isNotBlank(text) && text.length() > 2000) {
                //如果是读取文件,删除文件,修改文件需要展示前后修改的完整结果
                if ("deleteFile".equals(toolName) || "editFile".equals(toolName) || "writeFile".equals(toolName)) {
                    text = extractModificationDetails(text);
                } else {
                    text = text.substring(0, 1500) + "[...截断]";
                }
            }
            return "[工具" + toolName + "调用结果]: " + text;

        }
        return message.toString();
    }

    /**
     * 提取某些工具执行结果的细节信息
     *
     * @param text 执行结果信息
     * @return 执行的细节消息
     */
    private String extractModificationDetails(String text) {
        StringBuilder detailsAppender = new StringBuilder();
        int idx = text.indexOf("oldContent");
        if (idx != -1) {
            int end = Math.min(idx + 1500, text.length());
            detailsAppender.append("[修改前后的内容]: ").append(text, idx, end).append("|");
        }
        idx = text.indexOf("newContent");
        if (idx != -1) {
            int end = Math.min(idx + 1500, text.length());
            detailsAppender.append(text, idx, end);
        }
        idx = text.indexOf("relativeFilePath");
        if (idx != -1) {
            int end = Math.min(idx + 200, text.length());
            detailsAppender.insert(0, "[文件路径: ]" + text.substring(idx, end) + "|");
        }
        //如果前面的内容都没有找到直接截取原内容
        if (detailsAppender.isEmpty()) {
            detailsAppender.append(text, 0, 1500).append("...[截断]");
        }
        return detailsAppender.toString();
    }

    /**
     * 计算上下午消息中占用的token
     *
     * @param messages 消息列表
     * @return 占用token数
     */
    private int estimatedTokens(List<ChatMessage> messages) {
        int estimatedTokens = 0;
        for (ChatMessage message : messages) {
            estimatedTokens += message.toString().length();
        }
        return estimatedTokens;
    }
}
