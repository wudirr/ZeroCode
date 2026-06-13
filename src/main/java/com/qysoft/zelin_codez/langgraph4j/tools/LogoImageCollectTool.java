package com.qysoft.zelin_codez.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.ImageResource;
import com.qysoft.zelin_codez.langgraph4j.model.enums.ImageCategoryEnum;
import com.qysoft.zelin_codez.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * logo图片搜集工具
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class LogoImageCollectTool {

    @Resource
    private CosManager cosManager;

    private static final String DASHBOARD_API_KEY = System.getenv("ALI-API-KEY");

    /**
     * 使用万向文生图模型生成logo图片
     *
     * @param description logo描述
     * @return 图片资源列表
     */
    @Tool("根据描述生成 Logo 设计图片,用于网站品牌标识")
    public List<ImageResource> collectLogoImage(@P("Logo 设计描述, 如名称、行业、风格等，尽量详细") String description) {
        try {
            log.info("开始生成logo图片,描述:{}", description);
            List<ImageResource> res = new ArrayList<>();
            String prompt = String.format("生成Logo,Logo中禁止出现中文,描述:%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(DASHBOARD_API_KEY)
                    .model(ImageSynthesis.Models.WANX_V1)
                    .prompt(prompt)
                    .n(1)
                    .size("1024*1024")
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            ImageSynthesisOutput output = result.getOutput();
            List<Map<String, String>> results = output.getResults();
            for (Map<String, String> itemMap : results) {
                String logoUrl = itemMap.get("url");
                //logoUrl存储在oss里面有效时间只有1天,所以我们需要将图片下载下来存储到腾讯云cos中
                File destFile = FileUtil.createTempFile("logo_", ".png", true);
                destFile = HttpUtil.downloadFileFromUrl(logoUrl, destFile);
                if (!destFile.exists() || destFile.length() == 0 || !FileUtil.getSuffix(destFile.getName()).equals("png")) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载logo图片失败");
                }
                String key = String.format("/logoImages/%s/%s", RandomUtil.randomString(5), destFile.getName());
                logoUrl = cosManager.uploadImage(key, destFile);
                res.add(ImageResource.builder()
                        .imageCategoryEnum(ImageCategoryEnum.LOGO)
                        .url(logoUrl)
                        .desc(description)
                        .build());
                FileUtil.del(destFile);
            }
            return res;
        } catch (Exception e) {
            String errorMessage = String.format("Logo生成失败,失败原因:%s,描述:%s", e.getMessage(), description);
            log.error(errorMessage);
            return List.of();
        }
    }
}
