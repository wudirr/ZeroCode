package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.core.collection.CollectionUtil;
import com.qysoft.zelin_codez.common.constant.RedisConstant;
import com.qysoft.zelin_codez.common.enums.PlanStatusEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 计划追踪器
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class PlanTracker {

    static final int MAX_ITEM_COUNT = 10;

    static final int MAX_TOOL_EXECUTED_COUNT = 3;

    final Map<String, String> renderMap = new HashMap<>();

    @PostConstruct
    void init() {
        renderMap.put(PlanStatusEnum.PENDING.getValue(), "[ ]");
        renderMap.put(PlanStatusEnum.IN_PROGRESS.getValue(), "[>]");
        renderMap.put(PlanStatusEnum.COMPLETED.getValue(), "[x]");
    }

    @Resource
    private RedissonClient redissonClient;

    /**
     * 执行计划
     *
     * @param items 计划item集合
     * @return 字符串
     */
    public String updatePlan(Long appId, List<PlanItem> items) {
        if (items.size() > MAX_ITEM_COUNT) {
            return String.format("计划数目不能超过10个,当前计划数目为%s", items.size());
        }
        //存储计划项的id,用来统计每一个计划的依赖计划是否存在
        Set<String> ids = items.stream().map(PlanItem::id).collect(Collectors.toSet());
        for (PlanItem planItem : items) {
            List<String> deps = planItem.deps();
            if (CollectionUtil.isNotEmpty(deps)) {
                for (String dep : deps) {
                    if (!ids.contains(dep)) {
                        String warnMessage = String.format("计划%s依赖的计划id为%s的计划不存在,请重试", planItem.item(), dep);
                        log.warn(warnMessage);
                        return warnMessage;
                    }
                }
            }
        }
        //检查正在执行的任务计划所依赖的计划是否已完成
        Map<String, List<PlanItem>> planItemMap = items.stream().collect(Collectors.groupingBy(PlanItem::id));
        for (PlanItem planItem : items) {
            if (PlanStatusEnum.IN_PROGRESS.getValue().equals(planItem.getStatus())) {
                //这个时候需要检查依赖的任务是否完成
                if (CollectionUtil.isNotEmpty(planItem.deps())) {
                    for (String dep : planItem.deps()) {
                        if (!PlanStatusEnum.COMPLETED.getValue().equals(planItemMap.get(dep).getFirst().status())) {
                            String warnMessage = String.format("计划%s依赖的计划id为%s的计划还没有完成,请重试", planItem.item(), dep);
                            log.warn(warnMessage);
                            return warnMessage;
                        }
                    }
                }
            }
        }
        //将状态保存到redis当中
        PlanState planState = new PlanState(items, 0);
        saveState(appId, planState);
        log.info("计划更新, appId = {},共{}项", appId, items.size());
        return renderPlan(planState);
    }

    /**
     * 渲染计划
     *
     * @param planState 计划状态
     * @return 渲染列表
     */
    private String renderPlan(PlanState planState) {
        List<PlanItem> items = planState.getItems();
        if (CollectionUtil.isEmpty(items)) {
            return "暂无执行的计划";
        }
        StringBuilder sb = new StringBuilder();
        int done = 0;
        for (PlanItem planItem : items) {
            String marker = renderMap.get(planItem.status());
            if (planItem.getStatus().equals(PlanStatusEnum.COMPLETED.getValue())) done++;
            sb.append("\n").append(marker).append(" #").append(planItem.id()).append(" ").append(planItem.item()).append("\n");
            if (CollectionUtil.isNotEmpty(planItem.deps())) {
                sb.append("依赖计划(").append(String.join(",", planItem.deps())).append(")\n");
            }
        }
        sb.append("任务计划执行情况(").append(done).append("/").append(items.size()).append(")已完成");
        return sb.toString();
    }

    /**
     * 记录一次非工具调用情况
     *
     * @param appId 应用id
     * @return 调用情况
     */
    public String onPlanExecuted(Long appId) {
        //加载计划状态
        PlanState planState = loadState(appId);
        Integer nagCount = planState.getNagCount();
        nagCount++;
        planState.setNagCount(nagCount);
        if (nagCount <= MAX_TOOL_EXECUTED_COUNT) {
            return "";
        }
        String message = String.format("""
                        <reminder>已经连续调用%s次工具没有更新任务执行计划(使用updatePlan工具),\
                        计划执行情况%s,建议使用`updatePlan`工具更新任务进度</reminder>
                        """,
                nagCount, renderPlan(planState));
        saveState(appId, planState);
        return message;
    }

    /**
     * 根据appId将状态保存到redis当中
     *
     * @param appId
     */
    private void saveState(Long appId, PlanState planState) {
        //TTL为一小时
        final int TTL = 1;
        String key = RedisConstant.PLAN_STATE_KEY_PREFIX + appId.toString();
        redissonClient.getBucket(key).set(planState, Duration.ofHours(TTL));
    }

    /**
     * 加载计划状态
     *
     * @param appId 应用id
     * @return 计划状态
     */
    private PlanState loadState(Long appId) {
        String key = RedisConstant.PLAN_STATE_KEY_PREFIX + appId.toString();
        return (PlanState) redissonClient.getBucket(key).get();
    }

    @Data
    public static class PlanItem {

        private String item;

        private String id;

        private String status;

        private List<String> deps;

        public PlanItem(String item, String id, String status, List<String> deps) {
            this.item = item;
            this.id = id;
            this.status = status;
            this.deps = deps;
        }

        public String id() {
            return id == null ? "" : id;
        }

        public String status() {
            return status == null ? PlanStatusEnum.PENDING.getValue() : status;
        }

        public String item() {
            return item == null ? "" : item;
        }

        public List<String> deps() {
            return CollectionUtil.isEmpty(deps) ? List.of() : deps;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PlanState {

        private List<PlanItem> items;

        /**
         * 记录当前是第几次执行到当前任务
         */
        private Integer nagCount = 0;
    }
}
