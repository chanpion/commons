package com.chenpp.common.bigdata.hive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author April.Chen
 * @date 2024/7/4 10:40
 */
@ConditionalOnProperty(name = "common.bigdata.hive.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(HiveProperties.class)
public class HiveAutoConfiguration {

    private final HiveProperties hiveProperties;

    public HiveAutoConfiguration(@Autowired HiveProperties hiveProperties) {
        this.hiveProperties = hiveProperties;
    }

    @Bean
    HiveTemplate hiveTemplate() {
        return new HiveTemplate(hiveProperties);
    }
}
