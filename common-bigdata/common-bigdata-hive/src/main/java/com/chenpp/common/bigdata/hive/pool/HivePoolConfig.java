package com.chenpp.common.bigdata.hive.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.sql.Connection;

/**
 * @author April.Chen
 * @date 2024/7/4 10:00
 */
public class HivePoolConfig extends GenericObjectPoolConfig<Connection> {
    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    public HivePoolConfig() {
        // 最大连接数
        this.setMaxTotal(CPU_NUM * 2);
        // 最大空闲连接数
        this.setMaxIdle(10);
        // 最小空闲连接数
        this.setMinIdle(5);
        // 获取连接前测试
        this.setTestOnBorrow(true);
        // 空闲时测试连接是否有效
        this.setTestWhileIdle(true);
    }

}
