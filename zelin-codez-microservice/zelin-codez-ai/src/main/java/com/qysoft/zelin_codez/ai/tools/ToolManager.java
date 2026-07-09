package com.qysoft.zelin_codez.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 工具管理器
 * @Author wudi
 * @Date 2026/6/3 17:06
 **/
@Component
public class ToolManager {

    @Resource
    private BaseTool[] tools;

    private static final Map<String, BaseTool> TOOL_MAP = new HashMap<>();

    /**
     * 初始化工厂map
     *
     */
    @PostConstruct
    public void initMap() {
        for (BaseTool tool : tools) {
            TOOL_MAP.put(tool.getToolName(), tool);
        }
    }

    /**
     * 根据名称对应的工具
     *
     * @param toolName 工具名称
     * @return 工具
     */
    public BaseTool getTool(String toolName) {
        if (!TOOL_MAP.containsKey(toolName)) {
            return null;
        }
        return TOOL_MAP.get(toolName);
    }

    /**
     * 获取所有工具
     *
     * @return 所有工具
     */
    public BaseTool[] getTools() {
        if (tools != null) {
            return this.tools;
        }
        return new BaseTool[]{};
    }
}
