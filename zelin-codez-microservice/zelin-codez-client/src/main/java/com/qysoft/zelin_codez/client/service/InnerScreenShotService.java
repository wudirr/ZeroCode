package com.qysoft.zelin_codez.client.service;

import com.qysoft.zelin_codez.model.entity.App;

/**
 * 内部截图服务接口
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface InnerScreenShotService {

    /**
     * 异步截图服务
     *
     * @param vistUrl 访问url
     * @param app     应用对象
     */
    void generateScreenShotAsync(String vistUrl, App app);
}
