package com.qysoft.zelin_codez.core.saver;

import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;

import java.io.File;

/**
 * @Description 代码保存执行器
 * @Author wudi
 * @Date 2026/4/27 15:48
 **/
public class CodeFileSaverExecutor {

    private static final CodeFileSaverTemplate<HtmlCodeResult> HTML_CODE_SAVER = new HtmlCodeSaver();

    private static final CodeFileSaverTemplate<MultiFileCodeResult> MULTI_FILE_CODE_SAVER = new MutiFileCodeSaver();

    /**
     * 保存代码
     *
     * @param codeResult 代码执行结果
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件
     */
    public static File saveCode(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeResult == null || codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_SAVER.codeFileSaver((HtmlCodeResult) codeResult);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVER.codeFileSaver((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR);
        };
    }
}
