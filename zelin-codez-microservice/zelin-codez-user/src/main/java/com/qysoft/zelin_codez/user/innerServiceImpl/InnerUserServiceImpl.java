package com.qysoft.zelin_codez.user.innerServiceImpl;

import com.qysoft.zelin_codez.client.service.InnerUserService;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.vo.user.UserVO;
import com.qysoft.zelin_codez.user.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }
}
