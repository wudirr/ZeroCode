package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.core.io.FileUtil;
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
import java.nio.file.StandardOpenOption;

/**
 * @Description 文件写入到指定路径的工具
 * @Author wudi
 * @Date 2026/5/21 10:10
 **/
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    @Tool("将文件写入指定的路径")
    public String writeFile(@P("文件相对路径") String relativeFilePath,
                            @P("写入的代码内容") String content,
                            @ToolMemoryId Long memoryId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String dirName = "vue_project_" + memoryId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, dirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            Path parentPath = path.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            //将代码写入文件(创建,如果文件存在创建并覆盖文件)
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件写入成功,路径为: {}", relativeFilePath);
            return "文件写入成功,路径为: " + relativeFilePath;
        } catch (Exception e) {
            String errorMessage = "文件写入失败,相对路径:" + relativeFilePath + ",失败原因:" + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getToolDesc() {
        return "写入文件";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """, this.getToolDesc(), relativeFilePath, suffix, content);
    }
}
