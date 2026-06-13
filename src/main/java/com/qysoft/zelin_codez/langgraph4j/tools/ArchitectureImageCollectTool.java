package com.qysoft.zelin_codez.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.model.ImageResource;
import com.qysoft.zelin_codez.langgraph4j.model.enums.ImageCategoryEnum;
import com.qysoft.zelin_codez.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 架构图搜集工具
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class ArchitectureImageCollectTool {

    @Resource
    private CosManager cosManager;

    /**
     * 指定.puppeteerrc.cjs文件的路径[文件里面存储的JSON结构指定了chrome浏览器的路径]
     * (如果安装mermaid-cli工具没有正确安装chrome浏览器时使用)
     */
    @Value("${puppeteer.puppeteerrcCjs.path}")
    private String cjsPath;

    /**
     * 根据mermaid代码生成架构图
     *
     * @param mermaidCode mermaid代码
     * @param description 架构图描述
     * @return 图片资源列表
     */
    @Tool("将 Mermaid 代码转化成加购图片,用于展示系统结构和技术关系")
    public List<ImageResource> collectArchitectureImage(@P("mermaid代码") String mermaidCode,
                                                        @P("架构图描述") String description) {
        try {
            log.info("开始生成架构图,mermaid代码:{}", mermaidCode);
            List<ImageResource> res = new ArrayList<>();
            //将mermaid转化成svg图片
            File architectureSvgFile = Mermaid2Svg(mermaidCode);
            if (architectureSvgFile == null) return List.of();
            //将图片上传cos
            String key = String.format("/architectureImage/%s/%s",
                    RandomUtil.randomString(5),
                    architectureSvgFile.getName());
            String imageUrl = cosManager.uploadImage(key, architectureSvgFile);
            res.add(ImageResource.builder()
                    .imageCategoryEnum(ImageCategoryEnum.ARCHITECTURE)
                    .url(imageUrl)
                    .desc(description)
                    .build());
            //清除本地文件
            FileUtil.del(architectureSvgFile);
            return res;
        } catch (Exception e) {
            String errorMessage = String.format("架构图生成失败,失败原因:%s,mermaid代码:%s", e.getMessage(), mermaidCode);
            log.error(errorMessage);
            return List.of();
        }
    }

    /**
     * 利用窗口cli命令将mermaid转化成svg图片
     *
     * @param mermaidCode mermaid代码
     * @return svg图片文件
     */
    private File Mermaid2Svg(String mermaidCode) {
        if (StringUtils.isBlank(mermaidCode)) {
            return null;
        }
        //创建文件
        File mermaidFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, mermaidFile);
        File svgFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
        //构建命令
        String baseCommand = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
        String command = String.format("%s -i %s -o %s -b transparent -p %s",
                baseCommand,
                mermaidFile.getAbsolutePath(),
                svgFile.getAbsolutePath(),
                cjsPath);
        //执行命令
        RuntimeUtil.execForStr(command);
        if (!svgFile.exists() || svgFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "mermaid转化svg图片失败");
        }
        //清除输入文件
        FileUtil.del(mermaidFile);
        return svgFile;
    }
}
