package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * @Description 文件基类[文件抽象类]
 * @Author wudi
 * @Date 2026/6/3 16:27
 **/
public abstract class BaseTool {

    /**
     * 读取工具名称
     *
     * @return 工具名称
     */
    public abstract String getToolName();

    /**
     * 读取工具描述
     *
     * @return 工具描述
     */
    public abstract String getToolDesc();

    /**
     * 获取工具执行信息
     *
     * @return 执行信息
     */
    public String getToolRequestResult() {
        return String.format("\n\n[选择工具] %s\n\n", this.getToolDesc());
    }

    /**
     * 获取工具执行结果
     *
     * @param arguments 工具执行参数
     * @return 执行结果
     */
    public abstract String getToolRequestResponse(JSONObject arguments);
}
