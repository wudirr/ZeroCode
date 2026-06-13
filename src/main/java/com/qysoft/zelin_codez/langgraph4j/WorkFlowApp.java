package com.qysoft.zelin_codez.langgraph4j;

import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import com.qysoft.zelin_codez.langgraph4j.node.*;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class WorkFlowApp {
    public static void main(String[] args) throws Exception {
        CompiledGraph<MessagesState<String>> compiledGraph = new MessagesStateGraph<String>()
                .addNode("image_controller", ImageCollectNode.collectImage())
                .addNode("prompt_enhancer", PromptEnhancerNode.promptEnhancer())
                .addNode("router", CodeRoutingNode.codeRouting())
                .addNode("code_generator", CodeGeneratorNode.generateCode())
                .addNode("project_builder", BuildProjectNode.buildProject())
                .addEdge(START, "image_controller")
                .addEdge("image_controller", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "project_builder")
                .addEdge("project_builder", END)
                .compile();
        GraphRepresentation workFlowApp = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID, "workFlowApp");
        log.info("mermaid结果:\n{}", workFlowApp);
        int stepCount = 1;
        for (NodeOutput<MessagesState<String>> item : compiledGraph.stream(Map.of(WorkFlowContext.WORK_FLOW_CONTEXT_KEY, WorkFlowContext.builder().initPrompt("帮我生成一个网站").build()))) {
            log.info("----- 第{}步完成 -----", stepCount++);
            System.out.println(item);
        }
    }
}
