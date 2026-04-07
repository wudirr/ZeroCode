package com.qysoft.zelin_codez.domain.vo.user;

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
 * @Description 用户查询VO模型
 * @Author wudi
 * @Date 2026/4/7 11:32
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryVO implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;
}
