package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class ExitTool extends BaseTool {

    /**
     * 退出工具调用工具
     *
     * @return 停止使用工具命令
     */
    @Tool("当任务已经完成或者不需要调用工具的时候,可以使用这个工具,结束工具调用")
    public String exit() {
        log.info("AI请求使用结束工具调用");
        return "不要继续使用工具,可以直接退出工具调用了";
    }

    @Override
    public String getToolName() {
        return "exit";
    }

    @Override
    public String getToolDesc() {
        return "结束工具调用";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        return this.getToolDesc();
    }

}
