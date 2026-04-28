package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.app.AppQueryRequest;
import com.qysoft.zelin_codez.domain.vo.app.AppQueryVO;
import com.qysoft.zelin_codez.domain.vo.app.AppVO;
import com.qysoft.zelin_codez.domain.vo.user.UserVO;
import com.qysoft.zelin_codez.mapper.AppMapper;
import com.qysoft.zelin_codez.service.AppService;
import com.qysoft.zelin_codez.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

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
            if(CollectionUtil.isNotEmpty(userVOs)){
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
        queryWrapper.eq("priority",priority);
        queryWrapper.like("appName", appName, StringUtils.isNotBlank(appName));
        queryWrapper.eq("codeGenType", codeGenType, StringUtils.isNotBlank(codeGenType));
        queryWrapper.eq("userId", userId, userId != null);
        queryWrapper.orderBy(sortField, StringUtils.isNotBlank(sortOrder) && sortOrder.equals("asc"));
        return queryWrapper;
    }
}
