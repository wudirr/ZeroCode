package com.qysoft.zelin_codez.app.core.saver;

import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 单文件代码保存器
 * @Author wudi
 * @Date 2026/4/27 15:42
 **/
public class HtmlCodeSaver extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected void saveCodeResult(HtmlCodeResult htmlCodeResult, String dirName) {
        if (StringUtils.isNotBlank(htmlCodeResult.getHtmlCode())) {
            write2File(dirName, "index.html", htmlCodeResult.getHtmlCode());
        }
    }

    @Override
    protected String getCodeGenType() {
        return CodeGenTypeEnum.HTML.getValue();
    }
}
