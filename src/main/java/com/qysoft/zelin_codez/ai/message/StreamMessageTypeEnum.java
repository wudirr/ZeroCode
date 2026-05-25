package com.qysoft.zelin_codez.ai.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("ai_response", "AI回复"),

    TOOL_REQUEST("tool_request","工具请求"),

    TOOL_EXECUTED("tool_executed","工具调用结果");

    private final String value;

    private final String text;

    public static StreamMessageTypeEnum getByValue(String value){
        for (StreamMessageTypeEnum streamMessageTypeEnum : StreamMessageTypeEnum.values()) {
            if(streamMessageTypeEnum.value.equals(value)){
                return streamMessageTypeEnum;
            }
        }
        return null;
    }
}
