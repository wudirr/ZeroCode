package com.qysoft.zelin_codez.langgraph4j.node;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.core.build.VueProjectBuilder;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

/**
 * 构建项目节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class BuildProjectNode {

    /**
     * 执行构建项目
     *
     * @return 异步生成结果
     */
    public static AsyncNodeAction<MessagesState<String>> buildProject() {
        return AsyncNodeAction.node_async(state -> {
            try {
                log.info("开始执行构建项目操作");
                WorkFlowContext context = WorkFlowContext.getContext(state);
                String generatorCodeDir = context.getGeneratorCodeDir();
                CodeGenTypeEnum codeGenTypeEnum = context.getCodeGenTypeEnum();
                File workFile = new File(generatorCodeDir);
                String buildProjectDir = generatorCodeDir;
                //构建vue项目
                if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
                    VueProjectBuilder vueProjectBuilder = new VueProjectBuilder();
                    boolean flag = vueProjectBuilder.installAndBuildVueProject(workFile);
                    if (flag) {
                        log.info("vue项目构建成功,构建目录:{}", workFile.getAbsolutePath());
                        File buildFile = new File(workFile, "dist");
                        buildProjectDir = buildFile.getAbsolutePath();
                    }
                }
                context.setBuildCodeDir(buildProjectDir);
                context.setCurrentStep("构建项目");
                log.info("构建项目完成,构建项目目录:{}", buildProjectDir);
                return WorkFlowContext.saveContext(context);
            } catch (Exception e) {
                log.error("执行构建项目操作失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        });
    }
}
