package com.qysoft.zelin_codez.ai.tools;

import com.qysoft.zelin_codez.manager.TurnCancellationManager;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * 工具取消装饰器(使用动态代理实现)
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Component
public class ToolCancellationDecorator {

    private static final String CANCELED_TOOL_RESULT = "[用户已手动停止本次生成] 请立即调用 exit 工具结束任务,"
            + "并用一两句话总结已完成的工作。不要再调用任何其他工具。";

    @Resource
    private TurnCancellationManager turnCancellationManager;

    /**
     * 将所有的BaseTool进行一层装饰代理
     *
     * @param tools 所有的工具
     * @return 装饰后的工具
     */
    public Object[] decorate(BaseTool[] tools) {
        return Arrays.stream(tools).map(tool -> {
            return Proxy.newProxyInstance(
                    tool.getClass().getClassLoader(),
                    tool.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        // 装饰方法
                        if (isToolMethod(method, args)) {
                            return CANCELED_TOOL_RESULT;
                        }
                        return method.invoke(tool, args);
                    }
            );
        }).toArray();
    }

    /**
     * 判断当前方式是否是工具方法
     *
     * @param method 装饰的方法
     * @param args   工具参数
     * @return 是否是工具方法
     */
    private boolean isToolMethod(Method method, Object[] args) {
        if (!method.isAnnotationPresent(Tool.class)) {
            return false;
        }
        Long appId = null;
        for (Object arg : args) {
            if (arg instanceof Long id) {
                appId = id;
                if (turnCancellationManager.isCancelled(appId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
