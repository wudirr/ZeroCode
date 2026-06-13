package com.qysoft.zelin_codez.langgraph4j.node;

import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

/**
 * 提示词增强节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class PromptEnhancerNode {

    /**
     * 执行提示词增强
     *
     * @return 异步生成结果
     */
    public static AsyncNodeAction<MessagesState<String>> promptEnhancer() {
        return AsyncNodeAction.node_async(state -> {
            try {
                log.info("开始执行提示词增强操作");
                WorkFlowContext context = WorkFlowContext.getContext(state);
                String imageListStr = context.getImageListStr();
                String enhancerPrompt = context.getInitPrompt() + imageListStr;
                context.setEnhancedPrompt(enhancerPrompt);
                context.setCurrentStep("提示词增强");
                log.info("执行提示词增强操作成功,内容为:{}", enhancerPrompt);
                return WorkFlowContext.saveContext(context);
            } catch (Exception e) {
                log.error("执行增强提示词失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        });
    }
}
