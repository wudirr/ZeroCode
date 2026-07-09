package com.qysoft.zelin_codez.client.service;

import com.qysoft.zelin_codez.common.constants.UserConstant;
import com.qysoft.zelin_codez.common.exception.BusinessException;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 内部用户服务接口
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface InnerUserService {

    /**
     * 获取当前登录用户(提供默认实现方法,防止httpServletRequest序列化出现问题导致服务调用失败)
     *
     * @param httpServletRequest
     * @return
     */
    static User getLoginUser(HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getSession().getAttribute(UserConstant.SALT + UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null || loginUser.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    /**
     * 根据id获取用户
     *
     * @param id id
     * @return User对象
     */
    User getById(Serializable id);

    /**
     * 根据id集合获取用户
     *
     * @param ids id集合
     * @return User对象集合
     */
    List<User> listByIds(Collection<? extends Serializable> ids);

    /**
     * 将User对象转化成VO对象
     *
     * @param user 用户对象
     * @return
     */
    UserVO getUserVO(User user);
}
