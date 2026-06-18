package com.qysoft.zelin_codez.ai.listeners;

import com.qysoft.zelin_codez.ai.monitor.AiModelMetricsCollector;
import com.qysoft.zelin_codez.ai.monitor.MonitorContext;
import com.qysoft.zelin_codez.ai.monitor.MonitorContextHolder;
import com.qysoft.zelin_codez.common.enums.RequestStatusEnum;
import com.qysoft.zelin_codez.common.enums.TokenUsageTypeEnum;
import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.output.TokenUsage;

import java.time.Duration;
import java.time.Instant;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class AiModelMonitorListener implements ChatModelListener {

    private static final String REQUEST_START_TIME_KEY = "requestStartTime";

    private static final String CONTEXT_KEY = "monitorContext";

    private final AiModelMetricsCollector aiModelMetricsCollector = SpringContextUtil.getBean("aiModelMetricsCollector", AiModelMetricsCollector.class);

    private MonitorContext TMP_CONTEXT;

    private boolean CONTEXT_CACHED = false;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        //监听请求,获取数据
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        MonitorContext context = MonitorContextHolder.getContext();
        if (context != null && !CONTEXT_CACHED) {
            TMP_CONTEXT = context;
            CONTEXT_CACHED = true;
        } else {
            //兜底处理,如果在多轮对话中切换线程,也可以拿到上下文对象[使用没有切换线程之前的上下文对象]
            context = TMP_CONTEXT;
        }
        requestContext.attributes().putIfAbsent(CONTEXT_KEY, context);
        ChatRequest chatRequest = requestContext.chatRequest();
        ChatRequestParameters parameters = chatRequest.parameters();
        //埋点,记录请求次数
        aiModelMetricsCollector.recordRequest(context.getUserId(), context.getAppId(), parameters.modelName(), RequestStatusEnum.STARTED.getValue());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        //拿到上下文对象
        MonitorContext monitorContext = (MonitorContext) responseContext.attributes().get(CONTEXT_KEY);
        Long userId = monitorContext.getUserId();
        Long appId = monitorContext.getAppId();
        //获取响应信息
        TokenUsage tokenUsage = responseContext.chatResponse().tokenUsage();
        String modelName = responseContext.chatRequest().parameters().modelName();
        //记录响应时间
        recordResponseTime((Instant) responseContext.attributes().get(REQUEST_START_TIME_KEY), userId, appId, modelName);
        //记录响应次数
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, RequestStatusEnum.SUCCESS.getValue());
        //记录token使用情况
        recordTokenUsage(userId, appId, modelName, tokenUsage);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext monitorContext = (MonitorContext) errorContext.attributes().get(CONTEXT_KEY);
        Long userId = monitorContext.getUserId();
        Long appId = monitorContext.getAppId();
        String modelName = errorContext.chatRequest().parameters().modelName();
        String errorMessage = errorContext.error().getMessage();
        //记录错误次数
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
    }

    /**
     * 记录响应时间
     *
     * @param startTime 开始时间
     */
    private void recordResponseTime(Instant startTime, Long userId, Long appId, String modelName) {
        Instant endTime = Instant.now();
        Duration responseTime = Duration.between(startTime, endTime);
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }

    /**
     * 记录token使用情况
     *
     * @param userId    用户id
     * @param appId     应用id
     * @param modelName 模型名称
     */
    private void recordTokenUsage(Long userId, Long appId, String modelName, TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            Integer inputTokenUsage = tokenUsage.inputTokenCount();
            Integer outputTokenUsage = tokenUsage.outputTokenCount();
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, TokenUsageTypeEnum.INPUT.getValue(), Long.valueOf(inputTokenUsage));
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, TokenUsageTypeEnum.OUTPUT.getValue(), Long.valueOf(outputTokenUsage));
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, TokenUsageTypeEnum.TOTAL.getValue(), Long.valueOf(tokenUsage.totalTokenCount()));
        }
    }
}
