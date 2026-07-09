package com.qysoft.zelin_codez.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.common.constants.UserConstant;
import com.qysoft.zelin_codez.common.exception.BusinessException;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.common.exception.ThrowUtils;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.enums.UserRoleEnum;
import com.qysoft.zelin_codez.model.form.user.UploadAvatarRequest;
import com.qysoft.zelin_codez.model.form.user.UserLoginRequest;
import com.qysoft.zelin_codez.model.form.user.UserQueryRequest;
import com.qysoft.zelin_codez.model.form.user.UserRegisterRequest;
import com.qysoft.zelin_codez.model.vo.user.UploadAvatarVO;
import com.qysoft.zelin_codez.model.vo.user.UserLoginVO;
import com.qysoft.zelin_codez.model.vo.user.UserQueryVO;
import com.qysoft.zelin_codez.model.vo.user.UserVO;
import com.qysoft.zelin_codez.user.mapper.UserMapper;
import com.qysoft.zelin_codez.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 服务层实现。
 *
 * @author wudi
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //加密盐值
    public static final String SALT = "qysoft";

    @Resource
    private UserMapper userMapper;

    @Override
    public Long registerUser(UserRegisterRequest userRegisterRequest) {
        //校验参数
        validParam(userRegisterRequest);
        User user = new User();
        userRegisterRequest.setUserPassword(encryptPassword(userRegisterRequest.getUserPassword()));
        BeanUtils.copyProperties(userRegisterRequest, user);
        user.setUserName(UserConstant.DEFAULT_USER_NAME);
        user.setUserProfile(UserConstant.DEFAULT_PROFILE);
        //判断用户是否存在
        String userAccount = userRegisterRequest.getUserAccount();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        User tempUser = userMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(tempUser != null && tempUser.getId() > 0, ErrorCode.PARAMS_ERROR, "用户已存在");
        //插入用户
        boolean save = save(user);
        ThrowUtils.throwIf(!save, ErrorCode.PARAMS_ERROR, "注册失败");
        return user.getId();
    }

    @Override
    public UserLoginVO loginUser(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null || StringUtils.isBlank(userLoginRequest.getUserAccount()) || StringUtils.isBlank(userLoginRequest.getUserPassword()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userLoginRequest.getUserAccount().length() < 4 || userLoginRequest.getUserAccount().length() > 15, ErrorCode.PARAMS_ERROR, "账户名异常");
        ThrowUtils.throwIf(userLoginRequest.getUserPassword().length() < 8 || userLoginRequest.getUserPassword().length() > 20, ErrorCode.PARAMS_ERROR, "密码异常");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //查询用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword(userPassword));
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或者密码输入错误");
        if (user.getUserRole().equals(UserRoleEnum.BAN.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户被封禁");
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, user);
        return getUserLoginVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null || user.getId() < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (user.getUserRole().equals(UserRoleEnum.BAN.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户被封禁");
        }
        //查询数据库,防止数据不一致
        user = getById(user.getId());
        return user;
    }

    /**
     * md5密码加密
     *
     * @param password
     * @return
     */
    @Override
    public String encryptPassword(String password) {
        return DigestUtil.md5Hex((SALT + password).getBytes());
    }

    @Override
    public UserLoginVO getUserLoginVO(User user) {
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setId(user.getId());
        userLoginVO.setUserAccount(user.getUserAccount());
        return userLoginVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (user == null || user.getId() < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return true;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        return records.stream().map(this::getUserVO).toList();
    }

    @Override
    public UploadAvatarVO uploadAvatar(MultipartFile file, UploadAvatarRequest request) {
        // TODO 上传用户头像
        return null;
    }

    @Override
    public UserQueryVO getUserQueryVO(User user) {
        if (user == null) {
            return null;
        }
        UserQueryVO userQueryVO = new UserQueryVO();
        BeanUtils.copyProperties(user, userQueryVO);
        return userQueryVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        queryWrapper.eq("id", id, id != null);
        queryWrapper.eq("userAccount", userAccount, StringUtils.isNotBlank(userAccount));
        queryWrapper.eq("userName", userName, StringUtils.isNotBlank(userName));
        queryWrapper.eq("userRole", userRole, StringUtils.isNotBlank(userRole));
        queryWrapper.orderBy(sortField, StringUtils.isNotBlank(sortOrder) && sortOrder.equals("asc"));
        return queryWrapper;
    }

    private static void validParam(UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(StringUtils.isBlank(userRegisterRequest.getUserAccount()) || StringUtils.isBlank(userRegisterRequest.getUserPassword()) || StringUtils.isBlank(userRegisterRequest.getCheckPassword()), ErrorCode.PARAMS_ERROR, "请填写正确信息");
        ThrowUtils.throwIf(!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword()), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        ThrowUtils.throwIf(userRegisterRequest.getUserAccount().length() < 4 || userRegisterRequest.getUserAccount().length() > 15, ErrorCode.PARAMS_ERROR, "账户长度为8-15位");
        ThrowUtils.throwIf(userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getUserPassword().length() > 20, ErrorCode.PARAMS_ERROR, "账户密码为1-20位");
    }
}
