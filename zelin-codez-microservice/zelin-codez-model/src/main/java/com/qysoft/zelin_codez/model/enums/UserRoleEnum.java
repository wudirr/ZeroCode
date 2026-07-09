package com.qysoft.zelin_codez.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {
    BAN("ban", "封禁"),
    USER("user", "用户"),
    ADMIN("admin", "管理员");

    private String role;

    private String message;

    private UserRoleEnum(String role, String message) {
        this.role = role;
        this.message = message;
    }

    public static UserRoleEnum getByValue(String role) {
        if (StringUtils.isBlank(role)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getRole().equals(role)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
