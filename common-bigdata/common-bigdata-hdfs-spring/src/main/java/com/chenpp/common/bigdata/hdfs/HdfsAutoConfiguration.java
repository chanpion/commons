package com.chenpp.common.bigdata.hdfs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author April.Chen
 * @date 2024/7/2 15:55
 */
@ConditionalOnProperty(name = "common.bigdata.hdfs.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(HdfsProperties.class)
public class HdfsAutoConfiguration {

    private final HdfsProperties hdfsProperties;

    public HdfsAutoConfiguration(@Autowired HdfsProperties hdfsProperties) {
        this.hdfsProperties = hdfsProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public HdfsTemplate hdfsTemplate() {
        return new HdfsTemplate(hdfsProperties);
    }

}
