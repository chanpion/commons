package com.chenpp.common.bigdata.hive;

import com.chenpp.common.bigdata.hive.pool.HivePoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author April.Chen
 * @date 2024/7/4 10:40
 */
@ConfigurationProperties(prefix = "common.bigdata.hive")
public class HiveProperties extends HiveConf {

    private HivePoolConfig pool;


    public HivePoolConfig getPool() {
        return pool;
    }

    public void setPool(HivePoolConfig pool) {
        this.pool = pool;
    }
}
