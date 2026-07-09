package com.qysoft.zelin_codez.core.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qysoft.zelin_codez.ai.message.*;
import com.qysoft.zelin_codez.ai.model.TurnAccumulator;
import com.qysoft.zelin_codez.ai.tools.BaseTool;
import com.qysoft.zelin_codez.ai.tools.ToolManager;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.core.build.VueProjectBuilder;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.manager.TurnAccumulatorManager;
import com.qysoft.zelin_codez.service.impl.TurnFlushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
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
@Component
public class JsonMessageStreamHandler {

    @Resource
    private ToolManager toolManager;

    @Resource
    private TurnFlushService turnFlushService;

    /**
     * 消息处理主方法
     *
     * @param appId  应用Id
     * @param turnId 单轮对话聚合Id
     * @param result 结果
     * @return 处理后的流式输出
     */
    public Flux<ServerSentEvent<String>> handler(Long appId, String turnId, Flux<String> result) {
        Set<String> toolIds = new HashSet<>();
        TurnAccumulator accumulator = TurnAccumulatorManager.getAccumulator(turnId);
        ThrowUtils.throwIf(accumulator == null, ErrorCode.SYSTEM_ERROR);
        return result.map(flunk -> {
            String message = jsonMessageHandler(flunk, toolIds, accumulator);
            if (message == null) {
                Map<String, String> flunkMap = Map.of(
                        "d", "解析消息失败",
                        "error", "true"
                );
                String flunkJson = JSONUtil.toJsonStr(flunkMap);
                return ServerSentEvent.<String>builder()
                        .event("error")
                        .data(flunkJson)
                        .build();
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
                turnFlushService.flushSuccess(turnId);
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
        }).doOnError(e -> {
            //异步保存聊天记录
            CompletableFuture.runAsync(() -> {
                turnFlushService.flushError(turnId, e.getMessage());
            }).exceptionally(errorMessage -> {
                log.error("保存历史聊天记录失败", errorMessage);
                return null;
            });
        });
    }

    /**
     * JSON消息处理器
     *
     * @param flunk   上游返回的StremMessage的JSON字符串
     * @param toolIds 工具id集合
     * @return 字符串
     */
    private String jsonMessageHandler(String flunk, Set<String> toolIds, TurnAccumulator accumulator) {
        StreamMessage streamMessage = JSONUtil.toBean(flunk, StreamMessage.class);
        StreamMessageTypeEnum streamMessageTypeEnum = StreamMessageTypeEnum.getByValue(streamMessage.getType());
        ThrowUtils.throwIf(streamMessageTypeEnum == null, "不支持的消息类型");
        switch (streamMessageTypeEnum) {
            case THINKING_CONTENT -> {
                ThinkingMessage thinkingMessage = JSONUtil.toBean(flunk, ThinkingMessage.class);
                String data = thinkingMessage.getData();
                accumulator.appendThinkingMessage(data);
                //返回JSON格式的数据,实时流式返回数据
                return JSONUtil.createObj()
                        .set("type", StreamMessageTypeEnum.THINKING_CONTENT.getValue())
                        .set("data", data)
                        .toString();
            }
            case AI_RESPONSE -> {
                AiResponseMessage aiResponseMessage = JSONUtil.toBean(flunk, AiResponseMessage.class);
                String data = aiResponseMessage.getData();
                accumulator.appendAssistantMessage(data);
//                stringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolExecutionRequestMessage toolExecutionRequestMessage = JSONUtil.toBean(flunk, ToolExecutionRequestMessage.class);
                String toolId = toolExecutionRequestMessage.getId();
                if (StringUtils.isNotBlank(toolId) && !toolIds.contains(toolId)) {
                    //这个时候将这个工具调用请求的id添加进来,说明是一次新的工具调用请求
                    toolIds.add(toolId);
                    String toolName = toolExecutionRequestMessage.getName();
                    String toolRequestResult = toolManager.getTool(toolName).getToolRequestResult();
                    accumulator.addToolRequest(toolId, toolName, toolExecutionRequestMessage.getArguments(), toolRequestResult, flunk);
                    accumulator.appendAssistantMessage(toolRequestResult);
                    return toolRequestResult;
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
                    //构建返回结果
                    String toolName = toolExecutedRequestMessage.getName();
                    if ("updatePlan".equals(toolName)) {
                        //手动构建Json数据返回给前端,用户数据渲染
                        accumulator.addToolResult(toolExecutedRequestMessage.getId(), toolName, arguments, toolExecutedRequestMessage.getResult(), "", flunk);
                        return JSONUtil.createObj()
                                .set("type", "tool_request")
                                .set("name", "updatePlan")
                                .set("result", toolExecutedRequestMessage.getResult())
                                .set("arguments", arguments)
                                .toString();
                    }
                    BaseTool tool = toolManager.getTool(toolName);
                    String result = tool.getToolRequestResponse(jsonObject);
                    String output = String.format("\n\n%s\n\n", result);
//                    stringBuilder.append(output);
                    accumulator.addToolResult(toolExecutedRequestMessage.getId(), toolName, arguments, toolExecutedRequestMessage.getResult(), output, flunk);
                    accumulator.appendAssistantMessage(output);
                    return output;
                } catch (Exception e) {
                    log.error("解析工具调用结果失败", e);
                }
            }
        }
        return null;
    }
}
