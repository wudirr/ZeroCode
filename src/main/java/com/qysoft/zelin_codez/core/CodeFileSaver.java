package com.qysoft.zelin_codez.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.qysoft.zelin_codez.ai.model.HtmlCodeResult;
import com.qysoft.zelin_codez.ai.model.MultiFileCodeResult;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Description 代码文件保存器
 * @Author wudi
 * @Date 2026/4/27 10:52
 **/
@Deprecated
public class CodeFileSaver {

    //1.定义代码文件存放目录
    private static final String FILE_SAVER_ROOT_DIR = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output";

    /**
     * 保存单文件代码
     *
     * @param htmlCodeResult 代码生成结果
     * @return 文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult){
        String dirName = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        if (StringUtils.isBlank(dirName)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        write2File(dirName, "index.html", htmlCodeResult.getHtmlCode());
        return new File(dirName);
    }

    /**
     * 保存多文件代码
     *
     * @param multiFileCodeResult 代码生成结果
     * @return 文件
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String dirName = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        if (StringUtils.isBlank(dirName)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        write2File(dirName, "index.html", multiFileCodeResult.getHtmlCode());
        write2File(dirName, "script.js", multiFileCodeResult.getJsCode());
        write2File(dirName, "style.css", multiFileCodeResult.getCssCode());
        return new File(dirName);
    }

    /**
     * 根据文件名称创建文件: tmp/code_output/type_雪花id
     *
     * @param codeGenType 代码生成类型
     * @return 文件目录
     */
    private static String buildUniqueDir(String codeGenType) {
        String uniqueDirName = StrUtil.format("{}_{}",codeGenType, IdUtil.getSnowflakeNextIdStr());
        String dirName = FILE_SAVER_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirName);
        return dirName;
    }

    /**
     * 将代码写入文件
     *
     * @param dirName 文件目录名称
     * @param fileName 文件名称
     * @param content 写入内容
     */
    private static void write2File(String dirName, String fileName, String content) {
        String codePath = dirName + File.separator + fileName;
        FileUtil.writeString(content, codePath, StandardCharsets.UTF_8);
    }
}
