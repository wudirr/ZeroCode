package com.qysoft.zelin_codez.langgraph4j.demo;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class SimpleGraphApp {
    public static void main(String[] args) throws Exception {
        GreeterNode greeterNode = new GreeterNode();
        ResponseNode responseNode = new ResponseNode();
        StateGraph<SimpleState> stateGraph = new StateGraph<>(SimpleState.SCHEMA, SimpleState::new)
                .addNode("greeterNode", node_async(greeterNode))
                .addNode("responseNode", node_async(responseNode))
                .addEdge(START, "greeterNode")
                .addEdge("greeterNode", "responseNode")
                .addEdge("responseNode", END);
        //获取编译后的状态图(为了检查当前的图是否存在问题,比如是否存在孤立结点)
        CompiledGraph<SimpleState> compiledGraph = stateGraph.compile();
        GraphRepresentation graphRepresentation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID, "simpleGraphApp");
        System.out.println("MERMAID结果: " + graphRepresentation);
        //异步执行获取流式结果
        for (NodeOutput<SimpleState> item : compiledGraph.stream(Map.of(SimpleState.MESSAGE_KEY, "Let's Begin!"))) {
            System.out.println(item);
        }
    }
}
