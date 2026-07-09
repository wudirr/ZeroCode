package com.qysoft.zelin_codez.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 上下文压缩配置
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "context-compaction")
@Data
public class ContextCompactionProperties {

    /**
     * 触发上下文压缩的最大阈值
     * 建议设置为模型总上下文窗口的80%
     */
    private int tokenThreshold;

    /**
     * 送给摘要大模型的最大字符数
     */
    private int maxSummaryInputChars;
}
