package com.qysoft.zelin_codez.user.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.form.user.UploadAvatarRequest;
import com.qysoft.zelin_codez.model.form.user.UserLoginRequest;
import com.qysoft.zelin_codez.model.form.user.UserQueryRequest;
import com.qysoft.zelin_codez.model.form.user.UserRegisterRequest;
import com.qysoft.zelin_codez.model.vo.user.UploadAvatarVO;
import com.qysoft.zelin_codez.model.vo.user.UserLoginVO;
import com.qysoft.zelin_codez.model.vo.user.UserQueryVO;
import com.qysoft.zelin_codez.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author wudi
 */
public interface UserService extends IService<User> {
    /**
     * 注册用户
     *
     * @param userRegisterRequest
     * @return
     */
    Long registerUser(UserRegisterRequest userRegisterRequest);

    /**
     * 登录用户
     *
     * @param userLoginRequest
     * @return
     */
    UserLoginVO loginUser(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    String encryptPassword(String password);

    UserLoginVO getUserLoginVO(User user);

    UserVO getUserVO(User user);

    /**
     * 登出
     *
     * @param request
     * @return
     */
    Boolean logout(HttpServletRequest request);

    /**
     * 获取userVO列表
     *
     * @param records
     * @return
     */
    List<UserVO> getUserVOList(List<User> records);

    /**
     * 上传头像
     *
     * @param file
     * @param request
     * @return
     */
    UploadAvatarVO uploadAvatar(MultipartFile file, UploadAvatarRequest request);

    /**
     * 获取用户查询VO
     *
     * @param user
     * @return
     */
    UserQueryVO getUserQueryVO(User user);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
