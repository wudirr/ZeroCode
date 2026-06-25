package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Description 文件编辑工具
 * @Author wudi
 * @Date 2026/6/3 14:53
 **/
@Slf4j
@Component
public class FileEditTool extends BaseTool {

    @Resource
    private PlanTracker planTracker;

    /**
     * 修改文件工具
     *
     * @param relativeFilePath 文件相对路径
     * @param oldContent       修改之前的内容
     * @param newContent       修改之后的内容
     * @param appId            应用id
     * @return 修改信息
     */
    @Tool("修改指定路径的文件内容")
    public String editFile(@P("文件相对路径") String relativeFilePath,
                           @P("修改之前的内容") String oldContent,
                           @P("修改的新内容") String newContent,
                           @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String fileName = String.format("%s_%s", "vue_project", appId.toString());
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, fileName);
                path = projectPath.resolve(relativeFilePath);
            }
            if (!Files.exists(path)) {
                return "警告-文件不存在,修改文件失败,相对路径: " + relativeFilePath;
            }
            if (!Files.isRegularFile(path)) {
                return "警告-修改的文件不是一个普通的文件,修改文件失败,相对路径: " + relativeFilePath;
            }
            String content = Files.readString(path);
            if (!content.contains(oldContent)) {
                return "修改的内容不存在,相对路径: " + relativeFilePath;
            }
            //替换内容
            String replaceContent = content.replace(oldContent, newContent);
            if (replaceContent.equals(content)) {
                return "文件修改后的内容与原文件相同,相对路径: " + relativeFilePath;
            }
            Files.writeString(path, replaceContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件修改成功,相对路径: {}", relativeFilePath);
            String message = planTracker.onPlanExecuted(appId);
            return String.format("文件修改成功,相对路径: %s,修改后的内容: %s" + (StringUtils.isNotBlank(message) ? message : ""), relativeFilePath, replaceContent);
        } catch (Exception e) {
            String errorMessage = String.format("修改文件失败,相对路径:%s,失败原因:%s", relativeFilePath, e.getMessage());
            log.error(errorMessage);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "editFile";
    }

    @Override
    public String getToolDesc() {
        return "修改文件";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        return String.format("""
                [工具调用] %s %s
                                
                替换前:
                ``` %s
                %s
                ```
                                
                替换后:
                ``` %s
                %s
                ```
                """, this.getToolDesc(), relativeFilePath, suffix, oldContent, suffix, newContent);
    }
}
