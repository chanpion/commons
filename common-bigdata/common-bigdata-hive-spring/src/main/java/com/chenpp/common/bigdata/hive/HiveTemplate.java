package com.chenpp.common.bigdata.hive;

import com.chenpp.common.bigdata.hive.pool.HivePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/7/4 10:41
 */
@Slf4j
public class HiveTemplate {

    private final HivePool hivePool;

    private final HiveClient hiveClient;

    public HiveTemplate(HiveProperties hiveProperties) {
        this.hivePool = new HivePool(hiveProperties, hiveProperties.getPool());
        this.hiveClient = new HiveClient(hiveProperties, hiveProperties.getPool());
    }

    public List<String> showDatabases() {
        printPoolStatus();
        return hiveClient.showDatabases();
    }

    @PreDestroy
    public void shutdown() {
        hivePool.close();
    }

    public void printPoolStatus() {
        GenericObjectPool<Connection> pool = hivePool.getPool();
        log.info("pool active: {}, idle: {}, waiter: {}", pool.getNumActive(), pool.getNumIdle(), pool.getNumWaiters());
    }
}