package com.qysoft.zelin_codez.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.entity.User;
import com.qysoft.zelin_codez.domain.form.app.AppQueryRequest;
import com.qysoft.zelin_codez.domain.vo.app.AppQueryVO;
import com.qysoft.zelin_codez.domain.vo.app.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author wudi
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用VO
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用VO列表
     *
     * @param records
     * @return
     */
    List<AppVO> getAppVOList(List<App> records);

    /**
     * 获取应用查询VO
     *
     * @param app
     * @return
     */
    AppQueryVO getAppQueryVO(App app);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用并且生成对应的代码
     *
     * @param userMessage 用户消息
     * @param appId 应用标识
     * @param loginUser 登录用户
     * @return 流式输出
     */
    Flux<String> chat2GenCode(String userMessage, Long appId, User loginUser);
}
