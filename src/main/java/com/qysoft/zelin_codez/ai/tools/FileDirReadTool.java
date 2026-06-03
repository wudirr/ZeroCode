package com.qysoft.zelin_codez.ai.tools;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.qysoft.zelin_codez.common.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * @Description 项目目录阅读工具
 * @Author wudi
 * @Date 2026/6/3 14:53
 **/
@Slf4j
@Component
public class FileDirReadTool extends BaseTool{

    /**
     * 需要忽略的文件目录和文件名
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules", ".git", "dist", "build",
            ".Ds_Store", ".env", "target", ".mvn",
            ".idea", ".vscode", "coverage");

    /**
     * 需要忽略的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(".log", ".tmp", ".cache", ".lock");

    /**
     * 读取目录结构,获取指定目录下的所有文件和子目录信息
     *
     * @param relativeDirPath 目录相对路径
     * @param appId 应用id
     * @return 读取信息
     */
    @Tool("读取目录结构,获取指定目录下的所有文件和子目录信息")
    public String readFileDir(@P("目录相对路径,为空则读取整个项目目录") String relativeDirPath, @MemoryId Long appId) {
        try{
            Path path = Paths.get(StringUtils.isBlank(relativeDirPath) ? "" : relativeDirPath);
            if (!path.isAbsolute()) {
                String fileName = String.format("%s_%s", "vue_project", appId.toString());
                Path projectPath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, fileName);
                path = projectPath.resolve(StringUtils.isBlank(relativeDirPath) ? "" : relativeDirPath);
            }
            if(!Files.exists(path)){
                return "读取文件目录失败,文件目录不存在,相对路径: " + relativeDirPath;
            }
            if(!Files.isDirectory(path)){
                return "读取文件目录失败,该文件不是一个目录,相对路径: " + relativeDirPath;
            }
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("文件目录结构:\n");
            //递归获取所有的文件
            File targetFile = path.toFile();
            List<File> files = FileUtil.loopFiles(targetFile, file -> !isIgnoredFile(file.getName()));
            if(CollectionUtil.isEmpty(files)){
                return "文件目录为空,相对路径: " + relativeDirPath;
            }
            files.stream().sorted((f1,f2) -> {
                int depth1 = getDepth(targetFile, f1);
                int depth2 = getDepth(targetFile, f2);
                if(depth1 != depth2){
                    return Integer.compare(depth1,depth2);
                }
                return f1.getPath().compareTo(f2.getPath());
            }).forEach(file -> {
                int depth = getDepth(targetFile, file);
                contentBuilder.append(" ".repeat(depth)).append(file.getName());
            });
            log.info("文件目录读取成功,相对路径:{}", relativeDirPath);
            return String.format("文件目录读取成功,相对路径:%s,读取内容:%s", relativeDirPath, contentBuilder);
        }catch(Exception e){
            String errorMessage = String.format("读取目录失败,相对路径:%s,失败原因:%s", relativeDirPath, e.getMessage());
            log.error(errorMessage);
            return errorMessage;
        }
    }

    /**
     * 判断是否是被忽略的文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public boolean isIgnoredFile(String fileName){
        return IGNORED_NAMES.contains(fileName) || IGNORED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * 获取文件相对于目录的深度
     *
     * @param targetDir 目标文件夹
     * @param fileDir 目标文件
     * @return 深度 - 1
     */
    public int getDepth(File targetDir,File fileDir){
        Path targetDirPath = targetDir.toPath();
        Path filePath = fileDir.toPath();
        return targetDirPath.relativize(filePath).getNameCount() - 1;
    }

    @Override
    public String getToolName() {
        return "readFileDir";
    }

    @Override
    public String getToolDesc() {
        return "读取文件目录结构";
    }

    @Override
    public String getToolRequestResponse(JSONObject arguments) {
        String relativeDirPath = arguments.getStr("relativeDirPath");
        return String.format("[工具调用] %s %s", this.getToolDesc(),relativeDirPath);
    }
}
