package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.json.JSONObject;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Description 文件删除工具
 * @Author wudi
 * @Date 2026/6/3 14:52
 **/
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    @Resource
    private PlanTracker planTracker;

    /**
     * 重要的文件列表
     */
    private static final String[] importantFiles = {
            "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml", "vite.config.js", "vite.config.ts", "vue.config.js",
            "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
            "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
    };

    /**
     * 删除文件工具
     *
     * @param relativeFilePath 文件相对路径
     * @param appId            应用id
     * @return 删除文件信息
     */
    @Tool("删除指定的文件工具")
    public String deleteFile(@P("文件的相对路径") String relativeFilePath, @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String fileName = String.format("%s_%s", "vue_project", appId.toString());
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, fileName);
                path = projectPath.resolve(relativeFilePath);
            }
            if (!Files.exists(path)) {
                return "警告-文件不存在,删除文件失败,相对路径: " + relativeFilePath;
            }
            if (!Files.isRegularFile(path)) {
                return "警告-删除的文件不是一个普通的文件,删除文件失败,相对路径: " + relativeFilePath;
            }
            //安全检查,防止删除重要的文件
            File file = path.toFile();
            boolean flag = isImportantFile(file.getName());
            if (flag) {
                return "警告-该文件不允许删除,删除文件失败,相对路径: " + relativeFilePath;
            }
            Files.delete(path);
            log.info("删除文件:{}成功", relativeFilePath);
            String message = planTracker.onPlanExecuted(appId);
            return "删除文件成功,相对路径: " + relativeFilePath + (StringUtils.isNotBlank(message) ? message : "");
        } catch (Exception e) {
            String errorMessage = String.format("删除文件失败,相对路径:%s,失败原因:%s", relativeFilePath, e.getMessage());
            log.error(errorMessage);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要的文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    private boolean isImportantFile(String fileName) {
        for (String importantFile : importantFiles) {
            if (importantFile.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getToolDesc() {
        return "删除文件";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", this.getToolDesc(), relativeFilePath);
    }
}
