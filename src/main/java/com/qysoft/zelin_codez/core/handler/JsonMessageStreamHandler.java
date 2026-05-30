package com.qysoft.zelin_codez.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qysoft.zelin_codez.ai.message.*;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.common.enums.ChatHistoryMessageTypeEnum;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.core.build.VueProjectBuilder;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @Description JSON流式消息处理器
 * @Author wudi
 * @Date 2026/5/25 22:24
 **/
@Slf4j
public class JsonMessageStreamHandler {

    public Flux<ServerSentEvent<String>> handler(Long appId, User loginUser, Flux<String> result, ChatHistoryService chatHistoryService) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> toolIds = new HashSet<>();
        return result.map(flunk -> {
            String message = jsonMessageHandler(flunk, stringBuilder, toolIds);
            if (message == null) {
                throw new BusinessException("解析消息失败");
            }
            Map<String, String> flunkMap = Map.of("d", message);
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
            //构建vue项目
            String fileName = String.format("%s_%s", CodeGenTypeEnum.VUE_PROJECT.getValue(), appId.toString());
            String workDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + fileName;
            File workFile = new File(workDir);
            VueProjectBuilder vueProjectBuilder = new VueProjectBuilder();
            vueProjectBuilder.installAndBuildVueProjectAsync(workFile);
        });
    }

    /**
     * JSON消息处理器
     *
     * @param flunk         上游返回的StremMessage的JSON字符串
     * @param stringBuilder 字符串拼接器
     * @param toolIds       工具id集合
     * @return 字符串
     */
    private String jsonMessageHandler(String flunk, StringBuilder stringBuilder, Set<String> toolIds) {
        StreamMessage streamMessage = JSONUtil.toBean(flunk, StreamMessage.class);
        StreamMessageTypeEnum streamMessageTypeEnum = StreamMessageTypeEnum.getByValue(streamMessage.getType());
        ThrowUtils.throwIf(streamMessageTypeEnum == null, "不支持的消息类型");
        switch (streamMessageTypeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiResponseMessage = JSONUtil.toBean(flunk, AiResponseMessage.class);
                String data = aiResponseMessage.getData();
                stringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolExecutionRequestMessage toolExecutionRequestMessage = JSONUtil.toBean(flunk, ToolExecutionRequestMessage.class);
                String toolId = toolExecutionRequestMessage.getId();
                if (StringUtils.isNotBlank(toolId) && !toolIds.contains(toolId)) {
                    //这个时候将这个工具调用请求的id添加进来,说明是一次新的工具调用请求
                    toolIds.add(toolId);
                    return "\n\n[选择工具]写入文件\n\n";
                } else {
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                try {
                    ToolExecutedRequestMessage toolExecutedRequestMessage = JSONUtil.toBean(flunk, ToolExecutedRequestMessage.class);
                    String arguments = toolExecutedRequestMessage.getArguments();
                    if (StringUtils.isBlank(arguments)) return "";
                    //这个时候解析arguments,拿到里面的参数
                    JSONObject jsonObject = JSONUtil.parseObj(arguments);
                    String relativeFilePath = jsonObject.getStr("relativeFilePath");
                    //拿到后缀
                    String suffix = FileUtil.getSuffix(relativeFilePath);
                    if (StringUtils.isBlank(suffix)) return "";
                    String content = jsonObject.getStr("content");
                    if (StringUtils.isBlank(content)) return "";
                    //构建返回结果
                    String result = String.format("""
                            [工具调用]写入文件%s
                            ```%s
                            %s
                            ```
                            """, relativeFilePath, suffix, content);
                    String output = String.format("\n\n%s\n\n", result);
                    stringBuilder.append(output);
                    return output;
                } catch (Exception e) {
                    log.error("解析工具调用结果失败", e);
                }
            }
        }
        return null;
    }
}
