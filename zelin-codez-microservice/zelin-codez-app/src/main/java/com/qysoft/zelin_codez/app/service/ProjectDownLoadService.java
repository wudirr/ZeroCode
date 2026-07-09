package com.qysoft.zelin_codez.app.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface ProjectDownLoadService {

    /**
     * 下载项目并且压缩成一个zip压缩包
     *
     * @param projectPath         项目路径
     * @param downLoadFileName    下载文件名
     * @param httpServletResponse http响应对象
     */
    void downloadProjectAsZip(String projectPath, String downLoadFileName, HttpServletResponse httpServletResponse);
}
