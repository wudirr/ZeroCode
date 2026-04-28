package com.qysoft.zelin_codez.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.domain.form.app.AppQueryRequest;
import com.qysoft.zelin_codez.domain.vo.app.AppQueryVO;
import com.qysoft.zelin_codez.domain.vo.app.AppVO;

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
}
