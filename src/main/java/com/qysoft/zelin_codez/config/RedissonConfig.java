package com.qysoft.zelin_codez.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置类
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.password}")
    private String redisPass;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.database}")
    private Integer redisDataBase;

    /**
     * redisson客户端配置
     *
     * @return redisson客户端
     */
    @Bean
    public RedissonClient redissonClient() {
        String address = "redis://" + redisHost + ":" + redisPort;
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDataBase)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(1)
                .setIdleConnectionTimeout(30000)
                .setConnectTimeout(5000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
        if (StringUtils.isNotBlank(redisPass)) {
            singleServerConfig.setPassword(redisPass);
        }
        return Redisson.create(config);
    }
}
