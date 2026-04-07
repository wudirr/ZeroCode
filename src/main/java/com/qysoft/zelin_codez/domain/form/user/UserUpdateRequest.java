package com.qysoft.zelin_codez.domain.form.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 用户更新请求
 * @Author wudi
 * @Date 2026/4/7 11:29
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;
}
