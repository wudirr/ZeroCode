package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.json.JSONObject;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Description 文件阅读工具
 * @Author wudi
 * @Date 2026/6/3 14:53
 **/
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    /**
     * 文件阅读工具
     *
     * @param relativeFilePath 相对路径
     * @param appId            应用id
     * @return 阅读信息
     */
    @Tool("根据指定的文件路径,阅读对应的文件")
    public String readFile(@P("文件相对路径") String relativeFilePath, @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String fileName = String.format("%s_%s", "vue_project", appId.toString());
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, fileName);
                path = projectPath.resolve(relativeFilePath);
            }
            if (!Files.exists(path)) {
                return "警告-文件不存在,读取文件失败,相对路径: " + relativeFilePath;
            }
            if (!Files.isRegularFile(path)) {
                return "警告-读取的文件不是一个普通的文件,读取文件失败,相对路径: " + relativeFilePath;
            }
            String message = String.format("读取文件内容成功,相对路径:%s,内容:%s", relativeFilePath, Files.readString(path));
            log.info("读取文件成功,相对路径: {}", relativeFilePath);
            return message;
        } catch (Exception e) {
            String errorMessage = String.format("读取文件失败,相对路径:%s,失败原因:%s", relativeFilePath, e.getMessage());
            log.error(errorMessage);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getToolDesc() {
        return "读取文件";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", this.getToolDesc(), relativeFilePath);
    }
}
