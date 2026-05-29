package com.qysoft.zelin_codez.service;

import com.qysoft.zelin_codez.domain.entity.App;

public interface ScreenShotService {

    /**
     * 生成并且上传封面图片
     *
     * @param url 网页url
     * @return 封面图片的cos地址
     */
    String generateAndUploadScreenShot(String url);

    /**
     * 异步生成图片
     *
     * @param url 网页url
     */
    void generateScreenShotAsync(String url, App app);
}
