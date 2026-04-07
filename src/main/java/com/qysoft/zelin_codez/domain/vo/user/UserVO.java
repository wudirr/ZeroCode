package com.qysoft.zelin_codez.domain.vo.user;

import com.mybatisflex.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description 用户VO模型
 * @Author wudi
 * @Date 2026/4/7 11:32
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

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

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;
}
