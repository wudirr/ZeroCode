package com.qysoft.zelin_codez.ai.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description 工具调用结果消息
 * @Author wudi
 * @Date 2026/5/25 11:32
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolExecutedRequestMessage extends StreamMessage {

    private String id;

    private String name;

    private String arguments;

    private String result;

    public ToolExecutedRequestMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
        this.arguments = toolExecution.request().arguments();
        this.result = toolExecution.result();
    }
}
