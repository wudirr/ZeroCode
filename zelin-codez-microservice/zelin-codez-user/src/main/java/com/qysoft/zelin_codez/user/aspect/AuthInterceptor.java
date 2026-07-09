package com.qysoft.zelin_codez.user.aspect;

import com.qysoft.zelin_codez.common.exception.BusinessException;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.common.exception.ThrowUtils;
import com.qysoft.zelin_codez.model.annotations.AuthCheck;
import com.qysoft.zelin_codez.model.entity.User;
import com.qysoft.zelin_codez.model.enums.UserRoleEnum;
import com.qysoft.zelin_codez.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description 统一权限校验切面
 * @Author wudi
 * @Date 2026/4/7 18:17
 **/
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint proceedingJoinPoint, AuthCheck authCheck) throws Throwable {
        //获取请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum userRoleEnum = UserRoleEnum.getByValue(loginUser.getUserRole());
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NOT_LOGIN_ERROR);
        //校验权限
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustUserRoleEnum = UserRoleEnum.getByValue(mustRole);
        ThrowUtils.throwIf(mustUserRoleEnum == null, ErrorCode.SYSTEM_ERROR);
        if (mustUserRoleEnum.getRole().equals("admin") && !userRoleEnum.getRole().equals("admin")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (userRoleEnum.getRole().equals(UserRoleEnum.BAN.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return proceedingJoinPoint.proceed();
    }
}
