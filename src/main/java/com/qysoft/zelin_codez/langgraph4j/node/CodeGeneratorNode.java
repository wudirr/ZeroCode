package com.qysoft.zelin_codez.langgraph4j.node;

import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import com.qysoft.zelin_codez.core.AiCodeGeneratorFacade;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.QualityResult;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.Duration;

/**
 * 代码生成节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class CodeGeneratorNode {

    /**
     * 执行代码生成
     *
     * @return 异步生成结果
     */
    public static AsyncNodeAction<MessagesState<String>> generateCode() {
        return AsyncNodeAction.node_async(state -> {
            try {
                log.info("开始执行生成代码操作");
                WorkFlowContext context = WorkFlowContext.getContext(state);
                String enhancedPrompt = getUserMessage(context);
                CodeGenTypeEnum codeGenTypeEnum = context.getCodeGenTypeEnum();
                //暂时先将appId设置为0
                Long appId = 116L;
                AiCodeGeneratorFacade facade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
                Flux<String> resFlux = facade.generateAndSaveCodeStream(enhancedPrompt, codeGenTypeEnum, appId);
                //阻塞等待流式结果
                resFlux.blockLast(Duration.ofMinutes(10));
                //构建代码生成目录
                String codeGeneratorDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeGenTypeEnum.getValue() + "_" + appId;
                context.setGeneratorCodeDir(codeGeneratorDir);
                context.setCurrentStep("生成代码");
                log.info("生成代码完成,代码生成目录:{}", codeGeneratorDir);
                return WorkFlowContext.saveContext(context);
            } catch (Exception e) {
                log.error("执行代码生成操作失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        });
    }

    /**
     * 获取用户消息
     *
     * @param context 上下文对象
     * @return 用户消息
     */
    private static String getUserMessage(WorkFlowContext context) {
        String userMessage = context.getEnhancedPrompt();
        QualityResult qualityResult = context.getQualityResult();
        //检查代码质量是否通过
        if (isQualityCheckFailed(qualityResult)) {
            //这个时候直接拼接报错信息
            userMessage = concatenateErrorMessage(qualityResult);
        }
        return userMessage;
    }

    /**
     * 拼接错误信息
     *
     * @param qualityResult 代码质量检查结果
     * @return 错误信息
     */
    private static String concatenateErrorMessage(QualityResult qualityResult) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        errorMessageBuilder.append("代码质量检查未通过,请根据以下建议进行修改:").append("\n\n");
        qualityResult.getErrors().forEach(error -> {
            errorMessageBuilder.append("- ").append(error).append("\n");
        });
        if (!qualityResult.getSuggestions().isEmpty()) {
            //如果建议列表不为空
            errorMessageBuilder.append("\n").append("请根据以下的建议,进行修改:").append("\n");
            qualityResult.getSuggestions().forEach(suggestion -> {
                errorMessageBuilder.append("- ").append(suggestion).append("\n");
            });
        }
        errorMessageBuilder.append("\n请根据上述问题和建议重新生成代码,确保修复所有的问题");
        return errorMessageBuilder.toString();
    }

    /**
     * 代码质量检查是否失败
     *
     * @param qualityResult 质量检查结果
     * @return 判断情况
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.isValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }
}
