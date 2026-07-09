package com.qysoft.zelin_codez.app.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.app.mapper.ChatHistoryMapper;
import com.qysoft.zelin_codez.app.service.AppService;
import com.qysoft.zelin_codez.app.service.ChatHistoryService;
import com.qysoft.zelin_codez.common.exception.BusinessException;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.common.exception.ThrowUtils;
import com.qysoft.zelin_codez.model.entity.App;
import com.qysoft.zelin_codez.model.entity.ChatHistory;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.enums.ChatHistoryMessageTypeEnum;
import com.qysoft.zelin_codez.model.enums.UserRoleEnum;
import com.qysoft.zelin_codez.model.form.history.ChatHistoryQueryRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author wudi
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppService appService;

    @Override
    public Boolean addChatMessage(Long appId, String message, String messageType, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(messageType) || ChatHistoryMessageTypeEnum.getEnumByValue(messageType) == null, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(loginUser.getId())
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("id", id, id != null)
                .like("message", message, StringUtils.isNotBlank(message))
                .eq("messageType", messageType, StringUtils.isNotBlank(messageType))
                .eq("appId", appId, appId != null)
                .eq("userId", userId, userId != null);
        if (!StringUtils.isBlank(sortField)) {
            queryWrapper.orderBy(sortField, sortOrder.equals("asc"));
        } else {
            //默认按照排序时间降序
            queryWrapper.orderBy("createTime", false);
        }
        queryWrapper.lt("createTime", lastCreateTime, lastCreateTime != null);
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        if (!isCreator && !loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        chatHistoryQueryRequest.setAppId(appId);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount) {
        try {
            //校验参数
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
            //查询聊天历史
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("appId", appId)
                    .orderBy("createTime", false)
                    .limit(1, maxCount)
                    .select("message", "messageType");
            List<ChatHistory> chatHistoryList = this.list(queryWrapper);
            if (CollectionUtil.isEmpty(chatHistoryList)) {
                return 0;
            }
            //因为查询出来的时间是降序的,但是按照加载顺序需要反转一下
            chatHistoryList = chatHistoryList.reversed();
            //清理内存
            chatMemory.clear();
            int loadCount = 0;
            for (ChatHistory item : chatHistoryList) {
                if (item.getMessageType().equals(ChatHistoryMessageTypeEnum.USER.getValue())) {
                    chatMemory.add(UserMessage.from(item.getMessage()));
                } else if (item.getMessageType().equals(ChatHistoryMessageTypeEnum.AI.getValue())) {
                    chatMemory.add(AiMessage.from(item.getMessage()));
                }
                loadCount++;
            }
            log.info("加载历史成功,加载数量:{}", loadCount);
            return loadCount;
        } catch (Exception e) {
            log.error("加载历史失败", e);
            return 0;
        }
    }
}
