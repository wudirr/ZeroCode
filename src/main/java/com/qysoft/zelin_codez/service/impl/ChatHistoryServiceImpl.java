package com.qysoft.zelin_codez.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.common.enums.ChatHistoryMessageTypeEnum;
import com.qysoft.zelin_codez.common.enums.UserRoleEnum;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.entity.ChatHistory;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.history.ChatHistoryQueryRequest;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.mapper.ChatHistoryMapper;
import com.qysoft.zelin_codez.service.AppService;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author wudi
 */
@Service
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
}
