package com.qysoft.zelin_codez.domain.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description 用户登录VO模型
 * @Author wudi
 * @Date 2026/4/7 11:32
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;
    /**
     * 账号
     */
    private String userAccount;

    private String userName;

}
