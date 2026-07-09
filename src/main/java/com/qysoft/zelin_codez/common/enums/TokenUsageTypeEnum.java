package com.qysoft.zelin_codez.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Token使用类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum TokenUsageTypeEnum {

    INPUT("input", "输入"),

    OUTPUT("output", "输出"),

    TOTAL("total", "总消耗");

    private final String value;

    private final String desc;
}
