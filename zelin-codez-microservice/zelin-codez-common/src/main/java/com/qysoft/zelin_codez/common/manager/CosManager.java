package com.qysoft.zelin_codez.common.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qysoft.zelin_codez.common.config.CosConfig;
import com.qysoft.zelin_codez.common.exception.BusinessException;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.common.exception.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Description cos管理器
 * @Author wudi
 * @Date 2026/5/29 15:46
 **/
@Component
@ConditionalOnBean(COSClient.class)
@Slf4j
public class CosManager {

    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传图片服务
     *
     * @param key  桶存储键
     * @param file 图片文件
     * @return 上传结果
     */
    public String uploadImage(String key, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, file);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            ThrowUtils.throwIf(putObjectResult == null, ErrorCode.SYSTEM_ERROR, "上传图片失败");
            String url = String.format("%s%s", cosConfig.getHost(), key);
            log.info("上传图片:{}成功,路径为:{}", file.getName(), url);
            return url;
        } catch (Exception e) {
            log.error("上传图片到cos失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        }

    }
}
