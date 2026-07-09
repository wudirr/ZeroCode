package com.qysoft.zelin_codez.app.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qysoft.zelin_codez.model.entity.ChatHistory;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.form.history.ChatHistoryQueryRequest;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author wudi
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存聊天信息
     *
     * @param appId       应用Id
     * @param message     消息内容
     * @param messageType 消息类型
     * @param loginUser   登录用户
     * @return 保存结果
     */
    Boolean addChatMessage(Long appId, String message, String messageType, User loginUser);

    /**
     * 根据应用Id删除聊天信息
     *
     * @param appId
     * @return 删除结果
     */
    Boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询对象
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 游标查询聊天记录
     *
     * @param appId          应用id
     * @param pageSize       分页大小
     * @param lastCreateTime 最后一次查询时间
     * @param loginUser      登录用户
     * @return 历史记录分页对象
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 加载聊天历史到内存
     *
     * @param appId      应用id
     * @param chatMemory 聊天记忆对象
     * @param maxCount   读取的最大的数量
     * @return 加载数量
     */
    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);
}
