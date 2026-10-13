package com.qysoft.zelin_codez.manager;

import com.qysoft.zelin_codez.common.constant.RedisConstant;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class TurnCancellationManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 取消当前轮次的对话, 并将取消状态保存在Redis当中
     *
     * @param appId 应用Id
     * @return 是否取消成功
     */
    public boolean cancelTurn(Long appId) {
        try {
            String key = RedisConstant.CANCEL_STATE_KEY_PREFIX + appId.toString();
            redissonClient.getBucket(key).set(true);
        } catch (Exception e) {
            log.error("取消当前轮次对话失败, appId: {}", appId, e);
            return false;
        }
        return true;
    }

    /**
     * 判断当前应用是否取消对话
     *
     * @param appId 应用Id
     * @return 是否取消
     */
    public boolean isCancelled(Long appId) {
        String key = RedisConstant.CANCEL_STATE_KEY_PREFIX + appId.toString();
        return Boolean.TRUE.equals(redissonClient.getBucket(key).get());
    }

    /**
     * 清除当前应用的取消状态
     *
     * @param appId 应用Id
     */
    public void clearCancelState(Long appId) {
        try {
            String key = RedisConstant.CANCEL_STATE_KEY_PREFIX + appId.toString();
            redissonClient.getBucket(key).delete();
        } catch (Exception e) {
            log.error("清除当前应用的取消状态失败, appId: {}", appId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "清除当前应用的取消状态失败");
        }
    }
}
