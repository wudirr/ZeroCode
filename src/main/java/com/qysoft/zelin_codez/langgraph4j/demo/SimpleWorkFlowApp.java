package com.qysoft.zelin_codez.langgraph4j.demo;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 简单的工作流应用
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class SimpleWorkFlowApp {

    /**
     * 封装运行节点的方法
     *
     * @param message 消息
     * @return AsyncNodeAction
     */
    static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
        return node_async(state -> {
            log.info("执行节点:{}", message);
            return Map.of("messages", message);
        });
    }

    public static void main(String[] args) throws Exception {
        CompiledGraph<MessagesState<String>> compiledGraph = new MessagesStateGraph<String>()
                .addNode("image_controller", makeNode("获取图片素材"))
                .addNode("prompt_enhancer", makeNode("提示词增强"))
                .addNode("router", makeNode("智能路由"))
                .addNode("code_generator", makeNode("生成网站"))
                .addNode("project_builder", makeNode("构建项目"))
                .addEdge(START, "image_controller")
                .addEdge("image_controller", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "project_builder")
                .addEdge("project_builder", END)
                .compile();
        GraphRepresentation simpleWorkFlow = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID, "simpleWorkFlow");
        log.info("mermaid结果:\n{}", simpleWorkFlow);
        int stepCount = 1;
        for (NodeOutput<MessagesState<String>> messages : compiledGraph.stream(Map.of("messages", "Let's Begin!"))) {
            log.info("----- 第{}步完成", stepCount++);
            log.info("执行步骤:{}", messages);
        }
        log.info("工作流完成!");
    }
}
