package com.qysoft.zelin_codez.domain.form.user;
import com.mybatisflex.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description 用户保存请求
 * @Author wudi
 * @Date 2026/4/7 11:29
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveRequest {

    /**
     * 账号
     */
    private String userAccount;

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
