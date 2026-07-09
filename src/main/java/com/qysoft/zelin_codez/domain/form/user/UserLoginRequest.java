package com.qysoft.zelin_codez.domain.form.user;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 用户登录请求
 * @Author wudi
 * @Date 2026/4/7 11:30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
