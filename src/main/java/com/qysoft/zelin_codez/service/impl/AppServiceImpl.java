package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.ai.monitor.MonitorContext;
import com.qysoft.zelin_codez.ai.monitor.MonitorContextHolder;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.core.AiCodeGeneratorFacade;
import com.qysoft.zelin_codez.core.build.VueProjectBuilder;
import com.qysoft.zelin_codez.core.handler.StreamMessageHandlerExecutor;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.app.AppDeployRequest;
import com.qysoft.zelin_codez.domain.form.app.AppQueryRequest;
import com.qysoft.zelin_codez.domain.vo.app.AppQueryVO;
import com.qysoft.zelin_codez.domain.vo.app.AppVO;
import com.qysoft.zelin_codez.domain.vo.user.UserVO;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.manager.TurnAccumulatorManager;
import com.qysoft.zelin_codez.mapper.AppMapper;
import com.qysoft.zelin_codez.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author wudi
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;

    @Resource
    private ScreenShotService screenShotService;

    @Resource
    private StreamMessageHandlerExecutor streamMessageHandlerExecutor;

    @Resource
    private ChatEventLogService chatEventLogService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        User user = userService.getById(app.getUserId());
        appVO.setUserVO(userService.getUserVO(user));
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = records.stream().map(App::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(ids);
        Map<Long, List<UserVO>> userMap = users.stream().map(user -> userService.getUserVO(user)).collect(Collectors.groupingBy(UserVO::getId));
        return records.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            List<UserVO> userVOs = userMap.computeIfAbsent(app.getUserId(), key -> new ArrayList<>());
            if (CollectionUtil.isNotEmpty(userVOs)) {
                appVO.setUserVO(userVOs.getFirst());
            }
            return appVO;
        }).toList();
    }

    @Override
    public AppQueryVO getAppQueryVO(App app) {
        if (app == null) {
            return null;
        }
        AppQueryVO appQueryVO = new AppQueryVO();
        BeanUtils.copyProperties(app, appQueryVO);
        return appQueryVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String codeGenType = appQueryRequest.getCodeGenType();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        Integer priority = appQueryRequest.getPriority();
        queryWrapper.eq("id", id, id != null);
        queryWrapper.eq("priority", priority);
        queryWrapper.like("appName", appName, StringUtils.isNotBlank(appName));
        queryWrapper.eq("codeGenType", codeGenType, StringUtils.isNotBlank(codeGenType));
        queryWrapper.eq("userId", userId, userId != null);
        queryWrapper.orderBy(sortField, StringUtils.isNotBlank(sortOrder) && sortOrder.equals("asc"));
        return queryWrapper;
    }

    @Override
    public Flux<ServerSentEvent<String>> chatToGenCode(Long appId, String userMessage, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.PARAMS_ERROR);
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        //仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //保存用户消息
//        Boolean flag = chatHistoryService.addChatMessage(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser);
        //调用AI服务生成代码
        String memoryId = String.format("%s_%s", appId, codeGenType);
        String turnId = IdUtil.fastSimpleUUID();
        //开始聚合单轮对话上下文
        TurnAccumulatorManager.startTurn(loginUser.getId(), appId, memoryId, turnId, userMessage, codeGenType);
        MonitorContext monitorContext = new MonitorContext(loginUser.getId(), appId);
        MonitorContextHolder.saveContext(monitorContext);
        Flux<String> result = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, app.getId()).doFinally(signalType -> {
            //处理完流式响应之后清除上下文
            MonitorContextHolder.removeContext();
        });
        return streamMessageHandlerExecutor.messageHandler(appId, turnId, loginUser, result, chatHistoryService, codeGenTypeEnum);
    }

    @Override
    public String deployApp(AppDeployRequest appDeployRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        //2.检查应用是否存在,仅本人可以部署
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您没有权限部署该应用");
        }
        //3.生成部署标识,并且检查应用是否存在部署标识
        String deployKey = app.getDeployKey();
        if (StringUtils.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        //4.检查代码文件是否存在
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.NOT_FOUND_ERROR);
        File sourceDir = getSourceDir(codeGenTypeEnum, app);
        //5.构建部署目录,并且将代码文件拷贝到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        File deployDir = new File(deployDirPath);
        try {
            FileUtil.copyContent(sourceDir, deployDir, true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码拷贝失败");
        }
        //6.更新应用
        App updateApp = new App();
        updateApp.setId(app.getId());
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean flag = this.updateById(updateApp);
        ThrowUtils.throwIf(!flag, ErrorCode.SYSTEM_ERROR, "更新应用失败");
        String vistUrl = AppConstant.CODE_DEPLOY_HOST + "/" + deployKey;
        //异步生成封面
        screenShotService.generateScreenShotAsync(vistUrl, app);
        return vistUrl;
    }

    @Override
    public boolean removeById(Serializable id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Long appId = Long.parseLong(id.toString());
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        Boolean flag = chatHistoryService.deleteByAppId(appId);
        if (!flag) {
            log.error("删除历史聊天记录失败");
        }
        //删除关联的聊天事件日志
        chatEventLogService.deleteByAppId(appId);
        return super.removeById(appId);
    }

    /**
     * 构建项目原目录
     *
     * @param codeGenTypeEnum 代码生成类型
     * @param app             应用
     * @return 项目原目录
     */
    private static @NonNull File getSourceDir(CodeGenTypeEnum codeGenTypeEnum, App app) {
        String sourceDirName = codeGenTypeEnum.getValue() + "_" + app.getId().toString();
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (CodeGenTypeEnum.VUE_PROJECT == codeGenTypeEnum) {
            //这个时候需要执行构建操作
            VueProjectBuilder vueProjectBuilder = new VueProjectBuilder();
            boolean buildRes = vueProjectBuilder.installAndBuildVueProject(sourceDir);
            ThrowUtils.throwIf(!buildRes, ErrorCode.SYSTEM_ERROR, "项目构建失败");
            //检查部署目录是否存在
            File distFile = new File(sourceDir, "dist");
            if (!distFile.exists() || !distFile.isDirectory()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目构建成功,但是部署失败");
            }
            sourceDir = distFile;
            log.info("Vue项目部署成功,路径:{}", sourceDir.getAbsolutePath());
        }
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在,请先生成代码");
        }
        return sourceDir;
    }
}
