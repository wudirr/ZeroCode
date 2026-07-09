package com.qysoft.zelin_codez.app.innerServiceImpl;

import com.qysoft.zelin_codez.app.service.AppService;
import com.qysoft.zelin_codez.client.service.InnerAppService;
import com.qysoft.zelin_codez.model.entity.App;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@DubboService
public class InnerAppServiceImpl implements InnerAppService {

    @Resource
    private AppService appService;

    @Override
    public App getById(Serializable id) {
        return appService.getById(id);
    }

    @Override
    public boolean updateById(App app) {
        return appService.updateById(app);
    }
}
