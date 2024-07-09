package com.chenpp.common.bigdata.hbase;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author April.Chen
 * @date 2024/7/5 11:36
 */
@ConfigurationProperties(prefix = "common.bigdata.hbase")
public class HBaseProperties extends HBaseConf{
}
