package com.chenpp.common.bigdata.hive.pool;

import com.chenpp.common.bigdata.hive.HiveConf;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * @author April.Chen
 * @date 2024/7/4 10:10
 */
public class HivePool {

    private GenericObjectPool<Connection> pool;

    public HivePool(HiveConf hiveConf, HivePoolConfig poolConfig) {
        HivePoolFactory poolFactory = new HivePoolFactory(hiveConf);
        pool = new GenericObjectPool<>(poolFactory, poolConfig);
    }

    public Connection getConnection() throws Exception {
        return pool.borrowObject();
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.returnObject(conn);
        }
    }

    public void close() {
        if (pool != null) {
            pool.close();
        }
    }

    public GenericObjectPool<Connection> getPool() {
        return pool;
    }
}
