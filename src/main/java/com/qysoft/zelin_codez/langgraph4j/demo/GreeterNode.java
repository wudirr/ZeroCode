package com.qysoft.zelin_codez.langgraph4j.demo;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/**
 * 打招呼结点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class GreeterNode implements NodeAction<SimpleState> {

    /**
     * 结点执行的操作
     *
     * @param simpleState 结点状态
     * @return 结点执行完之后的状态
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> apply(SimpleState simpleState) throws Exception {
        System.out.println("GreeterNode start apply:" + simpleState.messages());
        return Map.of(SimpleState.MESSAGE_KEY, "Hello from GreeterNode!");
    }
}
