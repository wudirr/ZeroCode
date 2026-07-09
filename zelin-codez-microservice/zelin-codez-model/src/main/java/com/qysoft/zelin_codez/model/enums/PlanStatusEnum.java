package com.qysoft.zelin_codez.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@RequiredArgsConstructor
public enum PlanStatusEnum {

    PENDING("pending", "等待中"),

    IN_PROGRESS("in_progress", "进行中"),

    COMPLETED("completed", "已完成");

    private final String value;

    private final String desc;

    public static PlanStatusEnum fromValue(String value) {
        if (StringUtils.isBlank(value)) return null;
        for (PlanStatusEnum planStatusEnum : PlanStatusEnum.values()) {
            if (value.equals(planStatusEnum.getValue())) {
                return planStatusEnum;
            }
        }
        return null;
    }
}
