package com.qysoft.zelin_codez.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 代码生成模式枚举
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum CodeGenModeEnum {

    CLASSICAL("classical", "传统模式"),

    WORK_FLOW("work_flow", "工作流模式");

    private final String value;

    private final String desc;

    public static CodeGenModeEnum getByValue(String value) {
        if (StringUtils.isBlank(value)) return null;
        for (CodeGenModeEnum codeGenModeEnum : CodeGenModeEnum.values()) {
            if (value.equals(codeGenModeEnum.getValue())) {
                return codeGenModeEnum;
            }
        }
        return null;
    }
}
