package com.qysoft.zelin_codez.core.saver;

import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import io.micrometer.common.util.StringUtils;

/**
 * @Description 多文件代码保存器
 * @Author wudi
 * @Date 2026/4/27 15:46
 **/
public class MutiFileCodeSaver extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected void saveCodeResult(MultiFileCodeResult multiFileCodeResult, String dirName) {
        if (StringUtils.isNotBlank(multiFileCodeResult.getHtmlCode())) {
            write2File(dirName, "index.html", multiFileCodeResult.getHtmlCode());
        }
        if (StringUtils.isNotBlank(multiFileCodeResult.getJsCode())) {
            write2File(dirName, "script.js", multiFileCodeResult.getJsCode());
        }
        if (StringUtils.isNotBlank(multiFileCodeResult.getCssCode())) {
            write2File(dirName, "style.css", multiFileCodeResult.getCssCode());
        }
    }

    @Override
    protected String getCodeGenType() {
        return CodeGenTypeEnum.MULTI_FILE.getValue();
    }
}
