package com.qysoft.zelin_codez.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 请求状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum RequestStatusEnum {

    STARTED("started", "开始"),

    SUCCESS("success", "成功");

    private final String value;

    private final String desc;
}
