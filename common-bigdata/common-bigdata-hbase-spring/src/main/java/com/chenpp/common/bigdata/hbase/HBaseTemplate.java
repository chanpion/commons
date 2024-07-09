package com.chenpp.common.bigdata.hbase;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author April.Chen
 * @date 2024/7/5 11:37
 */
@Slf4j
public class HBaseTemplate {
    private final HBaseProperties hbaseProperties;
    private Configuration configuration;
    private Connection connection;

    public HBaseTemplate(HBaseProperties hBaseProperties) {
        this.hbaseProperties = hBaseProperties;
        try {
            this.configuration = HBaseUtil.getConfiguration(hBaseProperties);
            connection = HBaseUtil.getConnection(hBaseProperties);
        } catch (IOException e) {
            log.error("build hbase configuration error", e);
            throw new RuntimeException(e);
        }
    }

    public HbaseRow getRow(String tableName, String rowKey) {
        try {
            return HBaseUtil.getOneRow(connection, tableName, rowKey);
        } catch (IOException e) {
            log.error("hbase get row error", e);
            return null;
        }
    }

    public Set<HbaseRow> scan(String tableName) {
        try {
            return HBaseUtil.scan(hbaseProperties, tableName);
        } catch (Exception e) {
            log.error("hbase scan error", e);
            return Collections.emptySet();
        }
    }
}
