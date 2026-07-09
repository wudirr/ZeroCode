package com.qysoft.zelin_codez.ai.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description 工具调用请求消息封装类
 * @Author wudi
 * @Date 2026/5/25 11:27
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolExecutionRequestMessage extends StreamMessage {

    private String id;

    private String name;

    private String arguments;

    public ToolExecutionRequestMessage(ToolExecutionRequest toolExecutionRequest){
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}
