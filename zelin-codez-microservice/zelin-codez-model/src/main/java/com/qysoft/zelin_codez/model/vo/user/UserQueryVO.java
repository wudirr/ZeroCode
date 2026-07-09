package com.qysoft.zelin_codez.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description 用户查询VO模型
 * @Author wudi
 * @Date 2026/4/7 11:32
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 用户简介
     */
    private String userProfile;
}
