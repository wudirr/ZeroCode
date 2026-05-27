package com.qysoft.zelin_codez.core.handler;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * @Description 流式消息处理执行器
 * @Author wudi
 * @Date 2026/5/26 11:49
 **/
public class StreamMessageHandlerExecutor {

    public static Flux<ServerSentEvent<String>> messageHandler(Long appId, User loginUser, Flux<String> result, ChatHistoryService chatHistoryService, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum){
            case HTML,MULTI_FILE -> {
                SimpleTextStreamHandler simpleTextStreamHandler = new SimpleTextStreamHandler();
                return simpleTextStreamHandler.handler(appId, loginUser, result, chatHistoryService);
            }
            case VUE_PROJECT -> {
                JsonMessageStreamHandler jsonMessageStreamHandler = new JsonMessageStreamHandler();
                return jsonMessageStreamHandler.handler(appId, loginUser, result, chatHistoryService);
            }
        }
        return null;
    }
}
