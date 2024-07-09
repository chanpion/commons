package com.chenpp.common.bigdata.hdfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author April.Chen
 * @date 2024/7/2 15:28
 */
@ConfigurationProperties(prefix = "common.bigdata.hdfs")
public class HdfsProperties extends HdfsConf {
}
