package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class UpdatePlanTool extends BaseTool {

    @Resource
    private PlanTracker planTracker;

    /**
     * 创建或更新任务计划
     *
     * @param items 计划项
     * @param appId 应用id
     * @return 更新计划结果
     */
    @Tool("创建或更新任务计划。在开始生成项目或执行修改前，先用此工具列出计划；每完成一个阶段后更新进度。支持通过 deps 声明任务依赖关系。")
    public String updatePlan(@P("计划条目的 JSON 数组，每个条目包含 id（编号）、text（任务描述）、status（pending/in_progress/completed）、deps（依赖的任务 id 列表，可选）")
                             List<PlanItem> items,
                             @MemoryId Long appId) {
        if (CollectionUtil.isEmpty(items)) {
            return "计划条目数量不能为空,请重试";
        }
        try {
            List<PlanTracker.PlanItem> planItems = new ArrayList<>();
            for (PlanItem planItem : items) {
                PlanTracker.PlanItem item = new PlanTracker.PlanItem(planItem.item(), planItem.id(), planItem.status(), planItem.deps());
                planItems.add(item);
            }
            return planTracker.updatePlan(appId, planItems);
        } catch (Exception e) {
            String errorMessage = String.format("更新计划失败,错误信息: %s", e.getMessage());
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "updatePlan";
    }

    @Override
    public String getToolDesc() {
        return "更新计划";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        return "[计划更新完成]";
    }

    /**
     * 用于langchain4j结构化输入参数
     *
     * @param item
     * @param id
     * @param status
     * @param deps
     */
    public record PlanItem(String item, String id, String status, List<String> deps) {
    }
}
