package com.chenpp.common.bigdata.hbase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author April.Chen
 * @date 2024/7/5 11:37
 */
@ConditionalOnProperty(prefix = "common.bigdata.hbase", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(HBaseProperties.class)
public class HBaseAutoConfiguration {

    private final HBaseProperties hbaseProperties;

    public HBaseAutoConfiguration(@Autowired HBaseProperties hbaseProperties) {
        this.hbaseProperties = hbaseProperties;
    }

    @Bean
    public HBaseTemplate hbaseTemplate() {
        return new HBaseTemplate(hbaseProperties);
    }
}
