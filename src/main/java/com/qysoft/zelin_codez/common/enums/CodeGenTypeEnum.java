package com.qysoft.zelin_codez.common.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodeGenTypeEnum {

    HTML("原生 HTML 模式", "html"),

    MULTI_FILE("原生多文件模式", "multi_file"),

    VUE_PROJECT("VUE工程模式","vue_project");

    private final String text;

    private final String value;

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

