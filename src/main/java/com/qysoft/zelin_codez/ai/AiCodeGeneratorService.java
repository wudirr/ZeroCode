package com.qysoft.zelin_codez.ai;

import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * @Description 零代码生成服务
 * @Author wudi
 * @Date 2026/4/27 10:17
 **/
public interface AiCodeGeneratorService {

    /**
     * 生成单文件html代码(采用结构化输出)
     *
     * @param userMessage 用户输入
     * @return 单文件代码生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码(采用结构化输出)
     *
     * @param userMessage 用户输入
     * @return 多文件代码生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成单文件html代码(流式输出)
     *
     * @param userMessage 用户输入
     * @return 单文件代码生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码(流式输出)
     *
     * @param userMessage 用户输入
     * @return 多文件代码生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
