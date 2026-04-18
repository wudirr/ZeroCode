package com.qysoft.zelin_codez.controller;

import com.mybatisflex.core.paginate.Page;
import com.qysoft.zelin_codez.common.DeleteRequest;
import com.qysoft.zelin_codez.common.PageRequest;
import com.qysoft.zelin_codez.common.Result;
import com.qysoft.zelin_codez.common.annotation.AuthCheck;
import com.qysoft.zelin_codez.common.constant.UserConstant;
import com.qysoft.zelin_codez.domain.form.user.*;
import com.qysoft.zelin_codez.domain.vo.user.UserLoginVO;
import com.qysoft.zelin_codez.domain.vo.user.UserQueryVO;
import com.qysoft.zelin_codez.domain.vo.user.UserVO;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author wudi
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册用户
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public Result<Long> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        log.info("用户注册:{}", userRegisterRequest);
        Long res = userService.registerUser(userRegisterRequest);
        return Result.success(res);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> loginUser(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        log.info("用户登录:{}", userLoginRequest);
        UserLoginVO userLoginVO = userService.loginUser(userLoginRequest, request);
        return Result.success(userLoginVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public Result<UserQueryVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return Result.success(userService.getUserQueryVO(user));
    }

    /**
     * 用户登出
     *
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request) {
        Boolean res = userService.logout(request);
        return Result.success(res);
    }

    /**
     * 保存用户。
     *
     * @param userSaveRequest 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> save(@RequestBody @Valid UserSaveRequest userSaveRequest) {
        //校验参数
        validParam(userSaveRequest);
        User user = new User();
        BeanUtils.copyProperties(userSaveRequest, user);
        if (StringUtils.isBlank(userSaveRequest.getUserProfile())) {
            user.setUserProfile(UserConstant.DEFAULT_PROFILE);
        }
        user.setUserPassword(userService.encryptPassword(userSaveRequest.getUserPassword()));
        return Result.success(userService.save(user));
    }

    /**
     * 根据主键删除用户。
     *
     * @param deleteRequest 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> remove(@RequestBody DeleteRequest deleteRequest) {
        //查询用户
        User user = userService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(user == null || user.getId() < 0, ErrorCode.NOT_FOUND_ERROR, "用户存在");
        return Result.success(userService.removeById(deleteRequest.getId()));
    }

    /**
     * 根据主键更新用户。
     *
     * @param userUpdateRequest 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        //查询用户
        ThrowUtils.throwIf(userUpdateRequest.getId() == null || userUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        validParam(userUpdateRequest);
        User user = userService.getById(userUpdateRequest.getId());
        ThrowUtils.throwIf(user == null || user.getId() < 0, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        if (userUpdateRequest.getUserPassword() != null) {
            if (userService.encryptPassword(userUpdateRequest.getUserPassword()).equals(user.getUserPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改密码不能与原密码相同");
            }
            //这个时候修改密码
            userUpdateRequest.setUserPassword(userService.encryptPassword(userUpdateRequest.getUserPassword()));
        }
        BeanUtils.copyProperties(userUpdateRequest, user);
        return Result.success(userService.updateById(user));
    }

    /**
     * 根据主键编辑用户。
     *
     * @param userEditRequest 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("edit")
    public Result<Boolean> edit(@RequestBody UserEditRequest userEditRequest) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        BeanUtils.copyProperties(userEditRequest, userUpdateRequest);
        return update(userUpdateRequest);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @AuthCheck(mustRole = "admin")
    public Result<List<UserVO>> list() {
        List<User> records = userService.list();
        return Result.success(userService.getUserVOList(records));
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    @AuthCheck(mustRole = "admin")
    public Result<UserVO> getInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(userService.getUserVO(user));
    }

    /**
     * 分页查询用户。
     *
     * @param userQueryRequest 用户分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @AuthCheck(mustRole = "admin")
    public Result<Page<UserVO>> page(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        //防止爬虫
        ThrowUtils.throwIf(pageSize >= 30, ErrorCode.FORBIDDEN_ERROR);
        Page<User> userPage = userService.page(new Page<>(), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(pageNum,pageSize);
        userVOPage.setTotalPage(userPage.getTotalPage());
        userVOPage.setTotalRow(userPage.getTotalRow());
        userVOPage.setRecords(userService.getUserVOList(userPage.getRecords()));
        return Result.success(userVOPage);
    }


    /**
     * 参数校验
     *
     * @param userSaveRequest 校验模型
     */
    private static void validParam(UserSaveRequest userSaveRequest) {
        ThrowUtils.throwIf(StringUtils.isBlank(userSaveRequest.getUserAccount()), ErrorCode.PARAMS_ERROR, "账户不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userSaveRequest.getUserPassword()), ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userSaveRequest.getUserName()), ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(userSaveRequest.getUserProfile()) && userSaveRequest.getUserProfile().length() > 30, ErrorCode.PARAMS_ERROR, "简介长度为1-30");
        ThrowUtils.throwIf(userSaveRequest.getUserAccount().length() < 4 || userSaveRequest.getUserAccount().length() > 15, ErrorCode.PARAMS_ERROR, "账户长度为8-15位");
        ThrowUtils.throwIf(userSaveRequest.getUserName().isEmpty() || userSaveRequest.getUserName().length() > 15, ErrorCode.PARAMS_ERROR, "用户名长度为1-15位");
        ThrowUtils.throwIf(userSaveRequest.getUserPassword().length() < 8 || userSaveRequest.getUserPassword().length() > 20, ErrorCode.PARAMS_ERROR, "账户密码为1-20位");
    }

    /**
     * 参数校验
     *
     * @param userUpdateRequest 校验模型
     */
    private static void validParam(UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(StringUtils.isNotBlank(userUpdateRequest.getUserName()) && (userUpdateRequest.getUserName().isEmpty() || userUpdateRequest.getUserName().length() > 15), ErrorCode.PARAMS_ERROR, "用户名长度为1-15位");
        ThrowUtils.throwIf(StringUtils.isNotBlank(userUpdateRequest.getUserPassword()) && (userUpdateRequest.getUserPassword().length() < 8 || userUpdateRequest.getUserPassword().length() > 20), ErrorCode.PARAMS_ERROR, "账户密码为1-20位");
        ThrowUtils.throwIf(StringUtils.isNotBlank(userUpdateRequest.getUserProfile()) && userUpdateRequest.getUserProfile().length() > 30, ErrorCode.PARAMS_ERROR, "用户简介长度不得超过30");
    }

}
