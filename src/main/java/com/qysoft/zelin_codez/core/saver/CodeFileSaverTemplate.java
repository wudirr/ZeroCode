package com.qysoft.zelin_codez.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Description 代码文件保存模板
 * @Author wudi
 * @Date 2026/4/27 15:07
 **/
public abstract class CodeFileSaverTemplate<T> {

    protected static final String FILE_SAVER_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 代码文件保存
     *
     * @param t 代码生成结果
     * @return 文件
     */
    protected final File codeFileSaver(T t,Long appId){
        //校验参数
        validCodeResult(t);
        //构建唯一目录
        String dirName = buildUniqueDir(getCodeGenType(),appId);
        //保存代码文件
        saveCodeResult(t,dirName);
        return new File(dirName);
    }

    /**
     * 保存代码文件,交给子类自己实现
     *
     * @param t 代码生成结果
     * @param dirName 文件夹目录
     */
    protected abstract void saveCodeResult(T t,String dirName);

    protected abstract String getCodeGenType();

    protected void validCodeResult(T t){
        if (t == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }


    /**
     * 根据文件名称创建文件: tmp/code_output/type_雪花id
     *
     * @param codeGenType 代码生成类型
     * @return 文件目录
     */
    protected final String buildUniqueDir(String codeGenType,Long appId) {
        String uniqueDirName = StrUtil.format("{}_{}",codeGenType, appId.toString());
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
    protected final void write2File(String dirName, String fileName, String content) {
        String codePath = dirName + File.separator + fileName;
        FileUtil.writeString(content, codePath, StandardCharsets.UTF_8);
    }
}
