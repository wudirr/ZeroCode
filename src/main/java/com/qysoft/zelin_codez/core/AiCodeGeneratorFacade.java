package com.qysoft.zelin_codez.core;

import com.qysoft.zelin_codez.ai.AiCodeGeneratorService;
import com.qysoft.zelin_codez.ai.AiCodeGeneratorServiceFactory;
import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.core.parser.CodeParserExecutor;
import com.qysoft.zelin_codez.core.saver.CodeFileSaverExecutor;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * @Description 零代码生成服务门面类
 * @Author wudi
 * @Date 2026/4/27 11:15
 **/
@Component
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 生成代码并且保存代码
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage) || codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.saveCode(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.saveCode(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }

    /**
     * 生成代码并且保存代码(流式输出)
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage) || codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML ->
                    processCodeStream(aiCodeGeneratorService.generateHtmlCodeStream(userMessage), codeGenTypeEnum, appId);
            case MULTI_FILE ->
                    processCodeStream(aiCodeGeneratorService.generateMultiFileCodeStream(userMessage), codeGenTypeEnum, appId);
            case VUE_PROJECT ->
                    processCodeStream(aiCodeGeneratorService.generateVueProjectCodeStream(appId,userMessage), codeGenTypeEnum, appId);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }

    private Flux<String> processCodeStream(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            return result;
        }
        StringBuilder stringBuilder = new StringBuilder();
        return result.doOnNext(stringBuilder::append).doOnComplete(() -> {
            CompletableFuture.runAsync(() -> {
                try {
                    String content = stringBuilder.toString();
                    Object codeResult = CodeParserExecutor.coderParser(content, codeGenTypeEnum);
                    //保存代码
                    File file = CodeFileSaverExecutor.saveCode(codeResult, codeGenTypeEnum, appId);
                    log.info("保存html文件成功,文件路径:{}", file.getAbsolutePath());
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
                }
            }).exceptionally(e -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
            });
        });
    }
}
