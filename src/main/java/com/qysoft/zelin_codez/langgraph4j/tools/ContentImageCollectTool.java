package com.qysoft.zelin_codez.langgraph4j.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qysoft.zelin_codez.langgraph4j.model.ImageResource;
import com.qysoft.zelin_codez.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容图片搜集工具
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class ContentImageCollectTool {

    private static final String REQUEST_URL = "https://api.pexels.com/v1/search";

    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    /**
     * 从pexels网站中根据提示词搜索对应的内容图片
     *
     * @param query 搜索关键词
     * @return 图片资源列表
     */
    @Tool("搜集内容相关的图片,用于网站内容展示")
    public List<ImageResource> collectContentImage(@P("搜索关键词") String query) {
        log.info("开始搜索内容图片,搜索关键词:{}", query);
        int searchCount = 12;
        try (HttpResponse response = HttpRequest.get(REQUEST_URL)
                .header("Authorization", pexelsApiKey)
                .form("query", query)
                .form("per_page", searchCount)
                .form("page", 1)
                .execute()) {
            List<ImageResource> imageResourceList = new ArrayList<>();
            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");
                for (int i = 0; i < photos.size(); i++) {
                    JSONObject photoJson = photos.getJSONObject(i);
                    String mediumUrl = photoJson.getJSONObject("src").getStr("medium");
                    imageResourceList.add(ImageResource.builder()
                            .imageCategoryEnum(ImageCategoryEnum.CONTENT)
                            .desc(photoJson.getStr("alt", query))
                            .url(mediumUrl)
                            .build());
                }
            }
            return imageResourceList;
        } catch (Exception e) {
            String errorMessage = String.format("搜索内容图片失败,失败原因:%s,搜索关键词:%s", e.getMessage(), query);
            log.error(errorMessage);
            return List.of();
        }
    }
}
