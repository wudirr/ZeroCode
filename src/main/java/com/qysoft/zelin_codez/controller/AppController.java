package com.qysoft.zelin_codez.controller;

import cn.hutool.core.io.FileUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.qysoft.zelin_codez.ai.AiCodeTypeRoutingGeneratorService;
import com.qysoft.zelin_codez.ai.AiCodeTypeRoutingGeneratorServiceFactory;
import com.qysoft.zelin_codez.common.DeleteRequest;
import com.qysoft.zelin_codez.common.Result;
import com.qysoft.zelin_codez.common.annotation.AuthCheck;
import com.qysoft.zelin_codez.common.annotation.RateLimit;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.common.enums.RateLimitType;
import com.qysoft.zelin_codez.common.enums.UserRoleEnum;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.app.*;
import com.qysoft.zelin_codez.domain.vo.app.AppVO;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.service.AppService;
import com.qysoft.zelin_codez.service.ProjectDownLoadService;
import com.qysoft.zelin_codez.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 应用 控制层。
 *
 * @author wudi
 */
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownLoadService projectDownLoadService;

    @Resource
    private AiCodeTypeRoutingGeneratorServiceFactory aiCodeTypeRoutingGeneratorServiceFactory;

    /**
     * 用户创建应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    @PostMapping("add")
    public Result<Long> addApp(@RequestBody @Valid AppAddRequest appAddRequest, HttpServletRequest request) {
        log.info("用户创建应用:{}", appAddRequest);
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        AiCodeTypeRoutingGeneratorService aiCodeTypeRoutingGeneratorService = aiCodeTypeRoutingGeneratorServiceFactory.getAiCodeTypeRoutingGeneratorService();
        CodeGenTypeEnum codeGenTypeEnum = aiCodeTypeRoutingGeneratorService.routeCodeGenType(appAddRequest.getInitPrompt());
        //校验代码生成类型是否正确
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持该类型");
        app.setCodeGenType(codeGenTypeEnum.getValue());
        //默认取提示词的前12位作为应用名称
        String appName = appAddRequest.getInitPrompt().substring(0, Math.min(appAddRequest.getInitPrompt().length(), 12));
        app.setAppName(appName);
        app.setUserId(loginUser.getId());
        ThrowUtils.throwIf(!appService.save(app), ErrorCode.OPERATION_ERROR, "创建失败");
        return Result.success(app.getId());
    }

    /**
     * 用户根据id编辑自己的应用（仅允许修改应用名称）
     *
     * @param appEditRequest
     * @param request
     * @return
     */
    @PutMapping("edit")
    public Result<Boolean> editApp(@RequestBody @Valid AppEditRequest appEditRequest, HttpServletRequest request) {
        log.info("用户编辑应用:{}", appEditRequest);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(appEditRequest.getId() == null || appEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appEditRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 仅本人可以操作
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限编辑该应用");
        // 仅允许修改应用名称
        validAppName(appEditRequest.getAppName());
        app.setAppName(appEditRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        return Result.success(appService.updateById(app));
    }

    /**
     * 用户根据id删除自己的应用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("delete")
    public Result<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        log.info("用户删除应用:{}", deleteRequest);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 仅本人可以操作
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限删除该应用");
        return Result.success(appService.removeById(deleteRequest.getId()));
    }

    /**
     * 用户根据id查询应用详情
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("getInfo/{id}")
    public Result<AppVO> getAppInfo(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 仅本人或管理员可查看详情
        boolean isAdmin = UserRoleEnum.ADMIN.getRole().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isAdmin && !app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限查看该应用");
        return Result.success(appService.getAppVO(app));
    }

    /**
     * 用户分页查询自己创建的应用（限制条数不超过20）
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("my/page")
    public Result<Page<AppVO>> listMyApp(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int pageSize = appQueryRequest.getPageSize();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        int pageNum = appQueryRequest.getPageNum();
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), appService.getQueryWrapper(appQueryRequest));
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return Result.success(appVOPage);
    }

    /**
     * 分页查询精选应用（公开，按优先级排序）
     *
     * @param appQueryRequest
     * @return
     */
    @PostMapping("featured/page")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.qysoft.zelin_codez.common.utils.ObjectKeyUtil).buildKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10"
    )
    public Result<Page<AppVO>> listFeaturedApp(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageSize = appQueryRequest.getPageSize();
        // 防止爬虫，限制查询条数不能超过20条
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.FORBIDDEN_ERROR, "每页查询条数不能超过20条");
        int pageNum = appQueryRequest.getPageNum();
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 精选应用按优先级降序排列
        queryWrapper.orderBy("priority", false);
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setTotalPage(appPage.getTotalPage());
        appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return Result.success(appVOPage);
    }

    /**
     * 设置应用为精选应用
     *
     * @param id
     * @return
     */
    @GetMapping("/priority/{id}")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> setAppToFeatured(@PathVariable Long id) {
        //校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        App updateApp = new App();
        updateApp.setId(id);
        updateApp.setPriority(AppConstant.GOOD_APP_PRIORITY);
        return Result.success(appService.updateById(updateApp));
    }

    /**
     * 管理员根据id更新任意应用
     *
     * @param appUpdateRequest
     * @return
     */
    @PutMapping("update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> updateApp(@RequestBody @Valid AppUpdateRequest appUpdateRequest) {
        log.info("管理员更新应用:{}", appUpdateRequest);
        ThrowUtils.throwIf(appUpdateRequest.getId() == null || appUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        validAppName(appUpdateRequest.getAppName());
        BeanUtils.copyProperties(appUpdateRequest, app);
        return Result.success(appService.updateById(app));
    }

    /**
     * 管理员根据id删除任意应用
     *
     * @param deleteRequest
     * @return
     */
    @DeleteMapping("remove")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> removeApp(@RequestBody DeleteRequest deleteRequest) {
        log.info("管理员删除应用:{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return Result.success(appService.removeById(deleteRequest.getId()));
    }

    /**
     * 管理员根据id查询应用详情
     *
     * @param id
     * @return
     */
    @GetMapping("get/{id}")
    @AuthCheck(mustRole = "admin")
    public Result<AppVO> getApp(@PathVariable Long id) {
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return Result.success(appService.getAppVO(app));
    }

    /**
     * 管理员分页查询应用（无条数限制）
     *
     * @param appQueryRequest
     * @return
     */
    @PostMapping("page")
    @AuthCheck(mustRole = "admin")
    public Result<Page<AppVO>> selectPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), appService.getQueryWrapper(appQueryRequest));
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize);
        appVOPage.setTotalPage(appPage.getTotalPage());
        appVOPage.setTotalRow(appPage.getTotalRow());
        appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return Result.success(appVOPage);
    }

    /**
     * 生成AI应用接口
     *
     * @param appId              应用Id
     * @param userMessage        用户消息
     * @param httpServletRequest 请求封装类
     * @return 流式输出
     * @annotation rateLimit 限流注解
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId, @RequestParam String userMessage, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        return appService.chatToGenCode(appId, userMessage, loginUser);
    }

    /**
     * 部署应用
     *
     * @param appDeployRequest   应用部署请求
     * @param httpServletRequest 请求封装类
     * @return 部署地址
     */
    @PostMapping("/deploy")
    public Result<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() <= 0, ErrorCode.NOT_LOGIN_ERROR);
        return Result.success(appService.deployApp(appDeployRequest, loginUser));
    }

    /**
     * 下载代码接口
     *
     * @param appId               应用id
     * @param httpServletRequest  http请求封装类
     * @param httpServletResponse http响应封装类
     */
    @GetMapping("/download/{appId}")
    public void downloadApp(@PathVariable Long appId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() <= 0, ErrorCode.NOT_LOGIN_ERROR);
        //构建项目路径
        String fileName = String.format("%s_%s", app.getCodeGenType(), appId);
        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + fileName;
        File projectFile = FileUtil.file(projectPath);
        if (!projectFile.exists() || !projectFile.isDirectory()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目文件不存在");
        }
        String downloadFileName = appId.toString();
        projectDownLoadService.downloadProjectAsZip(projectPath, downloadFileName, httpServletResponse);
    }

    /**
     * 校验应用名称
     *
     * @param appName
     */
    private static void validAppName(String appName) {
        ThrowUtils.throwIf(StringUtils.isNotBlank(appName) && appName.length() > 50, ErrorCode.PARAMS_ERROR, "应用名称长度不能超过50");
    }
}
