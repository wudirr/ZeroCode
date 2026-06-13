package com.qysoft.zelin_codez.langgraph4j.demo;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class SimpleState extends AgentState {

    /**
     * 消息key
     */
    public static final String MESSAGE_KEY = "message";

    /**
     * 定义schema存储结点的状态(本质上就是一个map)
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(MESSAGE_KEY, Channels.appender(ArrayList::new));

    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    /**
     * 获取消息列表
     *
     * @return 消息列表
     */
    public List<String> messages() {
        return this.<List<String>>value(MESSAGE_KEY).orElse(List.of());
    }
}
