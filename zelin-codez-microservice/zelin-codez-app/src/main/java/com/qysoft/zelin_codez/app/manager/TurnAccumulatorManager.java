package com.qysoft.zelin_codez.app.manager;

import com.qysoft.zelin_codez.ai.model.TurnAccumulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单轮对话聚合管理器
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class TurnAccumulatorManager {

    private static final Map<String, TurnAccumulator> turnAccumulatorMap = new ConcurrentHashMap<>();

    /**
     * 开始上下文聚合
     *
     * @param userId      用户id
     * @param appId       应用id
     * @param memoryId    记忆id
     * @param turnId      单轮对话id
     * @param userMessage 用户消息
     * @param codeGenType 代码生成类型
     */
    public static void startTurn(Long userId, Long appId, String memoryId, String turnId, String userMessage, String codeGenType) {
        TurnAccumulator turnAccumulator = new TurnAccumulator(userId, appId, memoryId, turnId, codeGenType, userMessage);
        turnAccumulatorMap.put(turnId, turnAccumulator);
    }

    /**
     * 获取上下文聚合类
     *
     * @param turnId 单轮对话Id
     * @return TurnAccumulator
     */
    public static TurnAccumulator getAccumulator(String turnId) {
        if (turnId != null) {
            return turnAccumulatorMap.get(turnId);
        }
        return null;
    }

    /**
     * 移除上下文聚合类[防止OOM]
     *
     * @param turnId 单轮对话Id
     */
    public static void removeAccumulator(String turnId) {
        if (turnId != null) {
            turnAccumulatorMap.remove(turnId);
        }
    }
}
