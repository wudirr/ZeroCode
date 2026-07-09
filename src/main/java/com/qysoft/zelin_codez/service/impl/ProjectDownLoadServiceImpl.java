package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.service.ProjectDownLoadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
public class ProjectDownLoadServiceImpl implements ProjectDownLoadService {

    /**
     * 下载项目过滤的文件
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 下载项目过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        try {
            // 基础校验
            ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
            File projectDir = new File(projectPath);
            ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.NOT_FOUND_ERROR, "项目目录不存在");
            ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "指定路径不是目录");
            log.info("开始打包下载项目: {} -> {}.zip", projectPath, downloadFileName);
            // 设置 HTTP 响应头
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition",
                    String.format("attachment; filename=\"%s.zip\"", downloadFileName));
            // 定义文件过滤器
            FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());

            // 使用 Hutool 的 ZipUtil 直接将过滤后的目录压缩到响应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            log.info("项目打包下载完成: {}", downloadFileName);
        } catch (Exception e) {
            log.error("项目打包下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包下载失败");
        }
    }


    private boolean isPathAllowed(Path projectPath, Path fullPath) {
        Path relativizePath = projectPath.relativize(fullPath);
        for (Path path : relativizePath) {
            String fileName = path.toString();
            //检查文件名
            if (IGNORED_NAMES.contains(fileName)) {
                return false;
            }
            //检查文件扩展名
            if (IGNORED_EXTENSIONS.contains(fileName)) {
                return false;
            }
        }
        return true;
    }
}
