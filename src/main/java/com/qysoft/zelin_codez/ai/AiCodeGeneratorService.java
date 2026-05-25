package com.qysoft.zelin_codez.ai;

import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
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

    /**
     * 生成Vue工程代码(流式输出)
     *
     * @param memoryId 记忆id(因为我们需要将这个记忆id传递给工具调用,所以需要给记忆id放到上下文中,这个时候创建AI服务的时候就必须使用chatMemoryProvider,使用这个就必须要在方法里面加上memoryId)
     * @param userMessage 用户消息
     * @return 流式输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-vue-project-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId Long memoryId, @UserMessage String userMessage);
}
