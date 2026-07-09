package com.qysoft.zelin_codez.ai.service;

import com.qysoft.zelin_codez.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码类型生成判断服务
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public interface AiCodeTypeRoutingGeneratorService {

    /**
     * 判断代码类型
     *
     * @param userMessage 用户消息
     * @return 代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userMessage);
}
