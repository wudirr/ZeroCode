package com.qysoft.zelin_codez.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface ImageCollectService {

    /**
     * 收集图片AI服务
     *
     * @param prompt 提示词
     * @return 收集的图片列表字符串
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImage(@UserMessage String prompt);
}
