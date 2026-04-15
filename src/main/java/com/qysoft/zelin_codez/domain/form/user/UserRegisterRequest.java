package com.qysoft.zelin_codez.domain.form.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description 用户注册请求
 * @Author wudi
 * @Date 2026/4/7 11:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest implements Serializable {
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

    /**
     * 重复填写的密码
     */
    private String checkPassword;
}
