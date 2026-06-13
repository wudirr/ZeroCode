package com.qysoft.zelin_codez.langgraph4j;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.QualityResult;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import com.qysoft.zelin_codez.langgraph4j.node.*;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 代码生成工作流
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class CodeGenWorkFlow {

    /**
     * 编译工作流图
     *
     * @return 编译后的工作流图
     */
    public CompiledGraph<MessagesState<String>> getCompiledGraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("image_controller", ImageCollectNode.collectImage())
                    .addNode("prompt_enhancer", PromptEnhancerNode.promptEnhancer())
                    .addNode("router", CodeRoutingNode.codeRouting())
                    .addNode("code_generator", CodeGeneratorNode.generateCode())
                    .addNode("code_check", CodeQualityCheckNode.checkCodeQuality())
                    .addNode("project_builder", BuildProjectNode.buildProject())
                    .addEdge(START, "image_controller")
                    .addEdge("image_controller", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_check")
                    .addConditionalEdges("code_check", AsyncEdgeAction.edge_async(this::routeAfterQualityCheck), Map.of("fail", "code_generator", "build", "project_builder", "skip", END))
                    .addEdge("project_builder", END)
                    .compile();
        } catch (Exception e) {
            log.error("编译工作流图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编译工作流图失败");
        }
    }

    /**
     * 根据代码质量检查结果进行一个循环
     *
     * @param stringMessagesState 节点状态信息
     * @return 构建或者跳过
     */
    private String routeAfterQualityCheck(MessagesState<String> stringMessagesState) {
        WorkFlowContext context = WorkFlowContext.getContext(stringMessagesState);
        QualityResult qualityResult = context.getQualityResult();
        if (!qualityResult.isValid()) {
            return "fail";
        }
        return routeBuildOrSkip(stringMessagesState);
    }

    /**
     * 根据状态检查是否需要构建项目 build -> 构建 skip -> 跳过
     *
     * @param stringMessagesState 节点状态信息
     * @return 构建或者跳过
     */
    private String routeBuildOrSkip(MessagesState<String> stringMessagesState) {
        final String BUILD = "build";
        final String SKIP = "skip";
        WorkFlowContext context = WorkFlowContext.getContext(stringMessagesState);
        CodeGenTypeEnum codeGenTypeEnum = context.getCodeGenTypeEnum();
        if (codeGenTypeEnum == null || CodeGenTypeEnum.getEnumByValue(codeGenTypeEnum.getValue()) == null) {
            return SKIP;
        }
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            return BUILD;
        }
        return SKIP;
    }

    /**
     * 执行工作流
     *
     * @param initPrompt 初始提示词
     * @return 最终的状态上下文对象
     */
    public WorkFlowContext executeWorkFlow(String initPrompt) {
        log.info("开始执行生成代码工作流,初始内容:{}", initPrompt);
        CompiledGraph<MessagesState<String>> compiledGraph = getCompiledGraph();
        GraphRepresentation codeGenWorkFlow = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID, "codeGenWorkFlow");
        log.info("mermaid结果:\n{}", codeGenWorkFlow);
        //构建初始状态
        WorkFlowContext context = WorkFlowContext.builder()
                .currentStep("初始化")
                .initPrompt(initPrompt)
                .build();
        WorkFlowContext finalContext = null;
        log.info("开始执行工作流");
        int stepCount = 1;
        for (NodeOutput<MessagesState<String>> item : compiledGraph.stream(Map.of(WorkFlowContext.WORK_FLOW_CONTEXT_KEY, context))) {
            log.info("===== 执行工作流第{}步完成 ====", stepCount++);
            //获取执行的状态
            WorkFlowContext currentContext = WorkFlowContext.getContext(item.state());
            if (currentContext != null) {
                log.info("当前步骤上下文:{}", currentContext);
                finalContext = currentContext;
            }
        }
        return finalContext;
    }
}
