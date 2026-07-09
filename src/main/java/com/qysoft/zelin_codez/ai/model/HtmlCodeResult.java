package com.qysoft.zelin_codez.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @Description HTML代码生成结果
 * @Author wudi
 * @Date 2026/4/27 10:18
 **/
@Data
@Description("生成HTML代码文件的结果")
public class HtmlCodeResult {

    @Description("html代码部分")
    private String htmlCode;

    @Description("代码描述")
    private String description;
}
