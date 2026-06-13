package com.qysoft.zelin_codez.langgraph4j.model;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文对象,作为工作流的状态传输
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkFlowContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态信息的key
     */
    public static final String WORK_FLOW_CONTEXT_KEY = "workFlowContext";

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 初始提示词
     */
    private String initPrompt;

    /**
     * 图片列表字符串
     */
    private String imageListStr;

    /**
     * 图片资源集合
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum codeGenTypeEnum;

    /**
     * 生成的代码目录
     */
    private String generatorCodeDir;

    /**
     * 构建后的代码目录
     */
    private String buildCodeDir;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 质量检查结果
     */
    private QualityResult qualityResult;

    /**
     * =========== 上下文操作方法 ============
     */

    /**
     * 获取上下文对象
     *
     * @param state 当前状态
     * @return 上下文对象
     */
    public static WorkFlowContext getContext(MessagesState<String> state) {
        return (WorkFlowContext) state.data().get(WORK_FLOW_CONTEXT_KEY);
    }

    /**
     * 保存上下文对象
     *
     * @param workFlowContext 上下文对象
     * @return 状态中存储的信息
     */
    public static Map<String, Object> saveContext(WorkFlowContext workFlowContext) {
        //将数据存储到map当中返回就算是将数据存储到状态当中了
        return Map.of(WORK_FLOW_CONTEXT_KEY, workFlowContext);
    }
}
