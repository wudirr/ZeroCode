package com.qysoft.zelin_codez.core.handler;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @Description 流式消息处理执行器
 * @Author wudi
 * @Date 2026/5/26 11:49
 **/
@Component
public class StreamMessageHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    @Resource
    private SimpleTextStreamHandler simpleTextStreamHandler;

    /**
     * 消息处理器
     *
     * @param appId              应用id
     * @param loginUser          登录用户
     * @param result             流式结果
     * @param chatHistoryService 聊天历史服务
     * @param codeGenTypeEnum    代码类型
     * @return 封装后的流式结果
     */
    public Flux<ServerSentEvent<String>> messageHandler(Long appId, User loginUser, Flux<String> result, ChatHistoryService chatHistoryService, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum) {
            case HTML, MULTI_FILE -> {
                return simpleTextStreamHandler.handler(appId, loginUser, result, chatHistoryService);
            }
            case VUE_PROJECT -> {
                return jsonMessageStreamHandler.handler(appId, loginUser, result, chatHistoryService);
            }
        }
        return null;
    }
}
