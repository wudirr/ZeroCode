package com.qysoft.zelin_codez.screenshot.innerServiceImpl;

import com.qysoft.zelin_codez.client.service.InnerScreenShotService;
import com.qysoft.zelin_codez.model.entity.App;
import com.qysoft.zelin_codez.screenshot.service.ScreenShotService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@DubboService
public class InnerScreenShotServiceImpl implements InnerScreenShotService {

    @Resource
    private ScreenShotService screenShotService;

    @Override
    public void generateScreenShotAsync(String vistUrl, App app) {
        screenShotService.generateScreenShotAsync(vistUrl, app);
    }
}
