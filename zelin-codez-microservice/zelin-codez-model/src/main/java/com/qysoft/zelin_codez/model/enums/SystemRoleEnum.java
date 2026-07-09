package com.qysoft.zelin_codez.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 系统角色枚举类
 */
@Getter
@RequiredArgsConstructor
public enum SystemRoleEnum {

    USER("user", "用户"),

    ASSISTANT("assistant", "AI"),

    TOOL("tool", "工具");

    private final String value;

    private final String desc;

    public static SystemRoleEnum getByValue(String value) {
        if (StringUtils.isBlank(value)) return null;

        for (SystemRoleEnum systemRoleEnum : SystemRoleEnum.values()) {
            if (value.equals(systemRoleEnum.getValue())) {
                return systemRoleEnum;
            }
        }
        return null;
    }
}
