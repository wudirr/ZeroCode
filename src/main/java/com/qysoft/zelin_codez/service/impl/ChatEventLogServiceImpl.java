package com.qysoft.zelin_codez.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.domain.entity.ChatEventLog;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.mapper.ChatEventLogMapper;
import com.qysoft.zelin_codez.service.ChatEventLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天事件日志 服务层实现。
 *
 * @author wudi
 */
@Service
public class ChatEventLogServiceImpl extends ServiceImpl<ChatEventLogMapper, ChatEventLog> implements ChatEventLogService {

    @Override
    public List<ChatEventLog> listEventByMemoryId(String memoryId, int limit) {
        ThrowUtils.throwIf(StringUtils.isBlank(memoryId), ErrorCode.PARAMS_ERROR, "memoryId不能为空");
        final int FINAL_LIMIT = Math.max(1, Math.min(limit, 500));
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("memoryId", memoryId)
                .orderBy("createTime", false)
                .limit(0, FINAL_LIMIT);
        List<ChatEventLog> res = this.list(queryWrapper);
        return res.reversed();
    }

    @Override
    public List<ChatEventLog> listEventByTurnId(Long turnId) {
        ThrowUtils.throwIf(turnId == null || turnId <= 0, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("turnId", turnId);
        return this.list(queryWrapper);
    }
}
