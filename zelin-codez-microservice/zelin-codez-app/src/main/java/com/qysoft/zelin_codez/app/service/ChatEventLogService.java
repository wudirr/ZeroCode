package com.qysoft.zelin_codez.app.service;

import com.mybatisflex.core.service.IService;
import com.qysoft.zelin_codez.model.entity.ChatEventLog;

import java.util.List;

/**
 * 聊天事件日志 服务层。
 *
 * @author wudi
 */
public interface ChatEventLogService extends IService<ChatEventLog> {

    /**
     * 根据memoryId回溯所有的事件
     *
     * @param memoryId 记忆id
     * @param limit    限制个数
     * @return 聊天事件日志集合
     */
    List<ChatEventLog> listEventByMemoryId(String memoryId, int limit);

    /**
     * 根据turnId回溯所有的事件
     *
     * @param turnId 单轮对话id
     * @return 聊天事件日志集合
     */
    List<ChatEventLog> listEventByTurnId(Long turnId);

    /**
     * 根据appId删除所有的事件日志
     *
     * @param appId 应用id
     * @return 删除结果
     */
    void deleteByAppId(Long appId);
}
