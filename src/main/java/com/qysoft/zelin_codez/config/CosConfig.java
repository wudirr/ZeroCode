package com.qysoft.zelin_codez.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 腾讯云cos配置类
 * @Author wudi
 * @Date 2026/5/29 15:20
 **/
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosConfig {

    private String secretId;

    private String secretKey;

    private String bucket;

    private String region;

    private String host;

    @Bean
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region cosConfigRegion = new Region(region);
        ClientConfig clientConfig = new ClientConfig(cosConfigRegion);
        return new COSClient(cred, clientConfig);
    }
}
