package com.qysoft.zelin_codez.client.service;

import com.qysoft.zelin_codez.model.entity.App;

import java.io.Serializable;

/**
 * 内部应用服务接口
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface InnerAppService {

    App getById(Serializable id);

    boolean updateById(App app);
}
