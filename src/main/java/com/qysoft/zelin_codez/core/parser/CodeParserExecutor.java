package com.qysoft.zelin_codez.core.parser;

import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 代码解析器执行器
 * @Author wudi
 * @Date 2026/4/27 15:02
 **/
public class CodeParserExecutor {

    private static final CodeParserAdapter HTML_CODE_PARSER = new HtmlCodeParser();

    private static final CodeParserAdapter MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 代码解析器
     *
     * @param content 代码解析内容
     * @param codeGenTypeEnum 代码生成类型
     * @return Object
     */
    public static Object coderParser(String content, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(StringUtils.isBlank(content) || codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.codeParser(content);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.codeParser(content);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }
}
