package com.qysoft.zelin_codez.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.user.UploadAvatarRequest;
import com.qysoft.zelin_codez.domain.form.user.UserLoginRequest;
import com.qysoft.zelin_codez.domain.form.user.UserRegisterRequest;
import com.qysoft.zelin_codez.domain.vo.user.UploadAvatarVO;
import com.qysoft.zelin_codez.domain.vo.user.UserLoginVO;
import com.qysoft.zelin_codez.domain.vo.user.UserVO;
import com.qysoft.zelin_codez.mapper.UserMapper;
import com.qysoft.zelin_codez.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 服务层实现。
 *
 * @author wudi
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Override
    public Long registerUser(UserRegisterRequest userRegisterRequest) {
        return 0L;
    }

    @Override
    public UserLoginVO loginUser(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return null;
    }

    @Override
    public String encryptPassword(String password) {
        return "";
    }

    @Override
    public UserLoginVO getUserLoginVO(User user) {
        return null;
    }

    @Override
    public UserVO getUserVO(User user) {
        return null;
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        return null;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> records) {
        return List.of();
    }

    @Override
    public UploadAvatarVO uploadAvatar(MultipartFile file, UploadAvatarRequest request) {
        return null;
    }
}
