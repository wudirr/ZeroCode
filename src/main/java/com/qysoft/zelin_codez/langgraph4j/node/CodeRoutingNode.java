package com.qysoft.zelin_codez.langgraph4j.node;

import com.qysoft.zelin_codez.ai.AiCodeTypeRoutingGeneratorService;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

/**
 * 代码路由节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class CodeRoutingNode {

    /**
     * 执行智能路由
     *
     * @return 异步生成结果
     */
    public static AsyncNodeAction<MessagesState<String>> codeRouting() {
        return AsyncNodeAction.node_async(state -> {
            try {
                log.info("开始执行智能路由操作");
                WorkFlowContext context = WorkFlowContext.getContext(state);
                //获取初始提示词
                String initPrompt = context.getInitPrompt();
                AiCodeTypeRoutingGeneratorService aiCodeTypeRoutingGeneratorService = SpringContextUtil.getBean(AiCodeTypeRoutingGeneratorService.class);
                CodeGenTypeEnum codeGenTypeEnum = aiCodeTypeRoutingGeneratorService.routeCodeGenType(initPrompt);
                if (codeGenTypeEnum == null || CodeGenTypeEnum.getEnumByValue(codeGenTypeEnum.getValue()) == null) {
                    //采用降级策略
                    codeGenTypeEnum = CodeGenTypeEnum.HTML;
                }
                context.setCodeGenTypeEnum(codeGenTypeEnum);
                context.setCurrentStep("智能路由");
                log.info("智能路由完成,选择生成类型:{}", codeGenTypeEnum);
                return WorkFlowContext.saveContext(context);
            } catch (Exception e) {
                log.error("智能路由失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        });
    }
}
