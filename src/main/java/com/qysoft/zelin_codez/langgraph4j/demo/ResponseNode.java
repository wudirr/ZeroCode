package com.qysoft.zelin_codez.langgraph4j.demo;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

/**
 * 响应结点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class ResponseNode implements NodeAction<SimpleState> {

    /**
     * 结点执行的操作
     *
     * @param simpleState 结点状态
     * @return 结点执行完之后的状态
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> apply(SimpleState simpleState) throws Exception {
        System.out.println("ResponseNode start apply:" + simpleState.messages());
        List<String> messages = simpleState.messages();
        if (messages.contains("Hello from GreeterNode!")) {
            return Map.of(SimpleState.MESSAGE_KEY, "Acknowledged greeting!");
        }
        return Map.of(SimpleState.MESSAGE_KEY, "No greeting found.");
    }
}
