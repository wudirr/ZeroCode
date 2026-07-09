package com.qysoft.zelin_codez.common.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * set注入上下文对象
     *
     * @param applicationContext 上下文对象
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取IoC容器中指定类型的bean
     *
     * @param clazz 字节码对象
     * @param <T>   bean对象的类型
     * @return bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取IoC容器中指定名称的bean
     *
     * @param name bean名称
     * @return bean对象
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 获取IoC容器中指定名称和类型的bean
     *
     * @param name  bean名称
     * @param clazz bean类型
     * @param <T>   泛型
     * @return bean对象的类型
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
}
