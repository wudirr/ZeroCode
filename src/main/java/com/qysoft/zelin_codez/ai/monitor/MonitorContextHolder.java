package com.qysoft.zelin_codez.ai.monitor;

/**
 * 观测上下文执行器
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> contextHolder = new ThreadLocal<>();

    /**
     * 设置上下文
     *
     * @param context 上下文对象
     */
    public static void saveContext(MonitorContext context) {
        if (contextHolder.get() != null) {
            return;
        }
        contextHolder.set(context);
    }

    /**
     * 获取上下文
     *
     * @return 上下文对象
     */
    public static MonitorContext getContext() {
        return contextHolder.get();
    }

    /**
     * 移除上下文对象
     */
    public static void removeContext() {
        if (contextHolder.get() == null) return;
        contextHolder.remove();
    }
}
