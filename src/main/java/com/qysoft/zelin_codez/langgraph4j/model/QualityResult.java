package com.qysoft.zelin_codez.langgraph4j.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 质量检查模型
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityResult {

    @Description("是否通过")
    private boolean isValid;

    @Description("错误描述信息")
    private List<String> errors;

    @Description("建议信息列表")
    private List<String> suggestions;
}
