package com.qysoft.zelin_codez.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 聊天事件类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum ChatEventTypeEnum {

    USER_MESSAGE("USER_MESSAGE", "用户消息"),

    ASSISTANT_MESSAGE("ASSISTANT_MESSAGE", "AI回复消息"),

    TOOL_REQUEST("TOOL_REQUEST", "工具调用请求"),

    TOOL_RESULT("TOOL_RESULT", "工具调用结果"),

    THINKING_FINAL("THINKING_FINAL", "最终思考结果"),

    ASSISTANT_FINAL("ASSISTANT_FINAL", "最终回复结果");

    private final String value;

    private final String desc;

    public static ChatEventTypeEnum getByValue(String value) {
        if (StringUtils.isBlank(value)) return null;
        for (ChatEventTypeEnum eventTypeEnum : ChatEventTypeEnum.values()) {
            if (eventTypeEnum.getValue().equals(value)) {
                return eventTypeEnum;
            }
        }
        return null;
    }
}
