package com.qysoft.zelin_codez.ai.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型指标收集器
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Component
public class AiModelMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Counter> REQUEST_COUNTER_MAP = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Counter> ERROR_COUNTER_MAP = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Counter> TOKEN_USAGE_COUNTER_MAP = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Timer> RESPONSE_TIME_MAP = new ConcurrentHashMap<>();

    /**
     * 记录请求次数指标
     *
     * @param userId        用户id
     * @param appId         应用id
     * @param modelName     模型名称
     * @param requestStatus 请求状态
     */
    public void recordRequest(Long userId, Long appId, String modelName, String requestStatus) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, requestStatus);
        Counter counter = REQUEST_COUNTER_MAP.computeIfAbsent(key, k ->
                Counter.builder("ai_model_request_count")
                        .description("AI模型请求次数")
                        .tag("user_id", userId.toString())
                        .tag("app_id", appId.toString())
                        .tag("model_name", modelName)
                        .tag("request_type", requestStatus)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录错误次数指标
     *
     * @param userId       用户id
     * @param appId        应用id
     * @param modelName    模型名称
     * @param errorMessage 错误信息
     */
    public void recordError(Long userId, Long appId, String modelName, String errorMessage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);
        Counter counter = ERROR_COUNTER_MAP.computeIfAbsent(key, k ->
                Counter.builder("ai_model_error_count")
                        .description("AI模型错误次数")
                        .tag("user_id", userId.toString())
                        .tag("app_id", appId.toString())
                        .tag("model_name", modelName)
                        .tag("error_message", StringUtils.isBlank(errorMessage) ? "unknown error" : errorMessage)
                        .register(meterRegistry));
        counter.increment();
    }

    /**
     * 统计token使用总量
     *
     * @param userId         用户id
     * @param appId          应用id
     * @param modelName      模型名称
     * @param tokenUsageType token使用类型
     * @param tokenUsage     token使用量
     */
    public void recordTokenUsage(Long userId, Long appId, String modelName, String tokenUsageType, Long tokenUsage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenUsageType);
        Counter counter = TOKEN_USAGE_COUNTER_MAP.computeIfAbsent(key, k ->
                Counter.builder("ai_model_token_usage")
                        .description("AI模型Token使用量")
                        .tag("user_id", userId.toString())
                        .tag("app_id", appId.toString())
                        .tag("model_name", modelName)
                        .tag("token_usage_type", tokenUsageType)
                        .register(meterRegistry));
        counter.increment(Double.parseDouble(tokenUsage.toString()));
    }

    /**
     * 记录响应时间指标
     *
     * @param userId    用户id
     * @param appId     应用id
     * @param modelName 模型名称
     * @param time      响应时间
     */
    public void recordResponseTime(Long userId, Long appId, String modelName, Duration time) {
        String key = String.format("%s_%s_%s", userId, appId, modelName);
        Timer timer = RESPONSE_TIME_MAP.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_time")
                        .description("AI模型响应时间")
                        .tag("user_id", userId.toString())
                        .tag("app_id", appId.toString())
                        .tag("model_name", modelName)
                        .register(meterRegistry));
        timer.record(time);
    }
}
