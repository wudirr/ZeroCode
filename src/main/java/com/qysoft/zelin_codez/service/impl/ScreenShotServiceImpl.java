package com.qysoft.zelin_codez.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.qysoft.zelin_codez.common.utils.WebScreenshotUtils;
import com.qysoft.zelin_codez.domain.entity.App;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import com.qysoft.zelin_codez.manager.CosManager;
import com.qysoft.zelin_codez.service.ScreenShotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description 截图服务
 * @Author wudi
 * @Date 2026/5/29 16:02
 **/
@Service
@Slf4j
public class ScreenShotServiceImpl implements ScreenShotService {

    @Resource
    private CosManager cosManager;

    @Resource
    @Lazy
    private AppServiceImpl appService;

    @Override
    public String generateAndUploadScreenShot(String url) {
        try {
            String filePath = WebScreenshotUtils.saveWebPageScreenShot(url);
            String vistUrl = uploadPicture2Cos(filePath);
            //清理本地图片
            boolean cleanRes = clearLocalFile(filePath);
            ThrowUtils.throwIf(!cleanRes, "清理本地图片失败");
            return vistUrl;
        } catch (Exception e) {
            log.error("生成应用封面失败", e);
            return "";
        }
    }

    @Override
    public void generateScreenShotAsync(String url, App app) {
        Thread.ofVirtual().name("screenshot-thread-" + System.currentTimeMillis()).start(() -> {
            String vistUrl = generateAndUploadScreenShot(url);
            App updateApp = new App();
            updateApp.setId(app.getId());
            updateApp.setCover(vistUrl);
            boolean flag = appService.updateById(updateApp);
            ThrowUtils.throwIf(!flag, "更新应用封面失败");
        });
    }

    /**
     * 上传图片到cos
     *
     * @param localPath 图片本地的路径
     * @return 图片的url
     */
    public String uploadPicture2Cos(String localPath) {
        ThrowUtils.throwIf(StringUtils.isBlank(localPath), "图片路径为空");
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片文件不存在");
        }
        //构建存储键
        String fileName = RandomUtil.randomString(5) + "_compressed.jpg";
        String key = buildBucketKey(fileName);
        //上传文件
        return cosManager.uploadImage(key, localFile);
    }

    /**
     * 构建桶的存储key
     *
     * @param fileName 文件名称
     * @return key
     */
    private String buildBucketKey(String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 删除本地文件
     *
     * @param localPath 本地文件路径
     * @return 删除结果
     */
    public boolean clearLocalFile(String localPath) {
        return FileUtil.clean(localPath);
    }
}
