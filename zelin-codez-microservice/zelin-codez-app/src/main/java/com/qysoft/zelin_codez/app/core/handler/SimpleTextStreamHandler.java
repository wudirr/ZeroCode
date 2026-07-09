package com.qysoft.zelin_codez.app.core.handler;

import cn.hutool.json.JSONUtil;
import com.qysoft.zelin_codez.app.service.ChatHistoryService;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.enums.ChatHistoryMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Description 简单文本流式处理器
 * @Author wudi
 * @Date 2026/5/25 22:23
 **/
@Slf4j
@Component
public class SimpleTextStreamHandler {

    public Flux<ServerSentEvent<String>> handler(Long appId, User loginUser, Flux<String> result, ChatHistoryService chatHistoryService) {
        StringBuilder stringBuilder = new StringBuilder();
        return result.map(flunk -> {
            Map<String, String> flunkMap = Map.of("d", flunk);
            stringBuilder.append(flunk);
            String flunkJson = JSONUtil.toJsonStr(flunkMap);
            return ServerSentEvent.<String>builder()
                    .data(flunkJson)
                    .build();
        }).concatWith(Mono.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()
        )).doOnComplete(() -> {
            //异步保存聊天记录
            CompletableFuture.runAsync(() -> {
                String message = stringBuilder.toString();
                String messageType = ChatHistoryMessageTypeEnum.AI.getValue();
                chatHistoryService.addChatMessage(appId, message, messageType, loginUser);
            }).exceptionally(e -> {
                log.error("保存历史聊天记录失败", e);
                return null;
            });
        });
    }
}
