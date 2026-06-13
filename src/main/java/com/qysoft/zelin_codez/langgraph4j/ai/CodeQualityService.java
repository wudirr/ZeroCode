package com.qysoft.zelin_codez.langgraph4j.ai;

import com.qysoft.zelin_codez.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;

/**
 * 代码质量检查服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface CodeQualityService {

    /**
     * 检查代码生成质量
     *
     * @param content 检查内容
     * @return 质量检查结果
     */
    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(String content);
}
