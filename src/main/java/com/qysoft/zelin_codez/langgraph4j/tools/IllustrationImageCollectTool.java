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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Logo图片搜集工具
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class IllustrationImageCollectTool {

    private static final String REQUEST_URL = "https://undraw.co/_next/data/nS41BRGVYK4TTVjGNap_q/search/%s.json?term=%s";

    /**
     * 从undraw网站中搜索插画图片
     *
     * @param query 搜索关键词
     * @return 图片资源列表
     */
    @Tool("搜索插画图片,用于网站美化和装饰")
    public List<ImageResource> collectIllustrationImage(@P("插画搜索关键词,使用最直接的一个英文单词表示") String query) {
        log.info("开始搜索插画图片,搜索关键词:{}", query);
        String url = String.format(REQUEST_URL, query, query);
        List<ImageResource> res = new ArrayList<>();
        try (HttpResponse response = HttpRequest.get(url)
                .timeout(30000)
                .setConnectionTimeout(30000)
                .setReadTimeout(30000)
                .execute()) {
            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body()).getJSONObject("pageProps");
                JSONArray initialResults = result.getJSONArray("initialResults");
                for (int i = 0; i < initialResults.size(); i++) {
                    JSONObject initialResult = initialResults.getJSONObject(i);
                    String mediaUrl = initialResult.getStr("media");
                    String title = initialResult.getStr("title", query);
                    res.add(ImageResource.builder()
                            .imageCategoryEnum(ImageCategoryEnum.ILLUSTRATION)
                            .url(mediaUrl)
                            .desc(title)
                            .build());
                }
            }
            return res;
        } catch (Exception e) {
            String errorMessage = String.format("搜索插画图片失败,失败原因:%s,搜索关键词:%s", e.getMessage(), query);
            log.error(errorMessage, e);
            return List.of();
        }
    }
}
