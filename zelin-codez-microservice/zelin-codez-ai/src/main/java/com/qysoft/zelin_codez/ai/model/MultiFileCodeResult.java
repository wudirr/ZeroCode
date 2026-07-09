package com.qysoft.zelin_codez.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @Description 多文件代码生成结果
 * @Author wudi
 * @Date 2026/4/27 10:20
 **/
@Data
@Description("多文件代码生成结果")
public class MultiFileCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("js代码")
    private String jsCode;

    @Description("css代码")
    private String cssCode;

    @Description("代码描述")
    private String description;
}
