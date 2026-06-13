package com.qysoft.zelin_codez.langgraph4j.node;

import cn.hutool.core.io.FileUtil;
import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.ai.CodeQualityService;
import com.qysoft.zelin_codez.langgraph4j.model.QualityResult;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * 代码质量检查节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class CodeQualityCheckNode {

    /**
     * 需要检查的文件扩展名
     */
    private static final Set<String> CHECK_EXTENSIONS = Set.of(
            ".html", ".htm", ".css", ".js", ".json", ".vue", ".ts", ".jsx", ".tsx"
    );

    /**
     * 代码质量检查
     *
     * @return 异步生成结果
     */
    public static AsyncNodeAction<MessagesState<String>> checkCodeQuality() {
        return AsyncNodeAction.node_async(state -> {
            WorkFlowContext context = WorkFlowContext.getContext(state);
            log.info("开始代码质量检查");
            QualityResult qualityResult;
            try {
                //获取代码内容
                String codeContent = readAndConcatenateCode(context.getBuildCodeDir());
                if (StringUtils.isBlank(codeContent)) {
                    log.warn("代码内容为空");
                    qualityResult = QualityResult.builder()
                            .isValid(false)
                            .suggestions(List.of("代码内容为空,请生成完整的代码内容"))
                            .errors(List.of("代码内容为空,网页生成失败"))
                            .build();
                } else {
                    CodeQualityService codeQualityService = SpringContextUtil.getBean(CodeQualityService.class);
                    qualityResult = codeQualityService.checkCodeQuality(codeContent);
                    log.info("代码质量检查完成,是否通过:{}", qualityResult.isValid());
                }
            } catch (Exception e) {
                log.error("代码质量检查异常", e);
                qualityResult = QualityResult.builder()
                        .isValid(true)
                        .build();
            }
            context.setQualityResult(qualityResult);
            log.info("代码质量检查完成,上下文:{}", context);
            return WorkFlowContext.saveContext(context);
        });
    }

    /**
     * 读取并且拼接代码
     *
     * @param dirPath 代码目录
     * @return 拼接后的代码
     */
    private static String readAndConcatenateCode(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码目录不存在或者不是一个文件夹");
        }
        StringBuilder concatenateCode = new StringBuilder("生成的代码文件和代码内容:\n");
        //遍历文件夹
        FileUtil.walkFiles(dirFile, file -> {
            //检查文件是否正确
            if (!file.exists() || file.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件不存在或者文件内容为空");
            }
            //检查文件是否需要检查
            if (shouldSkip(file, dirFile)) {
                return;
            }
            //检查是否代码文件
            if (codeFile(file)) {
                String relativeFilePath = FileUtil.subPath(dirFile.getAbsolutePath(), file.getAbsolutePath());
                concatenateCode.append("# 代码文件: ").append(relativeFilePath).append("\n\n");
                //获取文件内容
                String fileContent = FileUtil.readUtf8String(file);
                concatenateCode.append(fileContent).append("\n\n");
            }
        });
        return concatenateCode.toString();
    }

    /**
     * 检查是否是代码文件
     *
     * @param file 检查的文件
     * @return 检查结果
     */
    private static boolean codeFile(File file) {
        String fileName = file.getName();
        return CHECK_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * 检查文件是否需要跳过
     *
     * @param file    检查的文件
     * @param dirFile 项目根目录
     * @return 检查结果
     */
    private static boolean shouldSkip(File file, File dirFile) {
        //获取文件相对根目录的位置
        String relativePath = FileUtil.subPath(dirFile.getAbsolutePath(), file.getAbsolutePath());
        if (file.getName().startsWith(".")) {
            return true;
        }
        return relativePath.contains("node_modules" + File.separator) ||
                relativePath.contains("dist" + File.separator) ||
                relativePath.contains("target" + File.separator) ||
                relativePath.contains(".git" + File.separator);
    }
}
