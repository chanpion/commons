package com.chenpp.common.bigdata.hive;

import com.alibaba.fastjson.JSONObject;
import com.chenpp.common.bigdata.hive.pool.HivePool;
import com.chenpp.common.bigdata.hive.pool.HivePoolConfig;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author April.Chen
 * @date 2024/7/4 17:52
 */
@Slf4j
public class HiveClient {

    private final HiveConf hiveConf;
    private Connection connection;
    private HivePool hivePool;
    private boolean isPool = false;

    public HiveClient(HiveConf hiveConf) {
        this.hiveConf = hiveConf;
    }

    public HiveClient(HiveConf hiveConf, HivePoolConfig poolConfig) {
        this.hiveConf = hiveConf;
        hivePool = new HivePool(hiveConf, poolConfig);
        this.isPool = true;
    }

    public Connection getConnection() {
        try {
            if (hivePool != null) {
                return hivePool.getConnection();
            }
            if (connection == null || connection.isClosed()) {
                connection = HiveUtil.getConnection(hiveConf);
            }
            return connection;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> showDatabases() {
        try {
            return HiveUtil.showDatabases(getConnection());
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public <T, R> void execute(Function<T, R> func) {
        try {

        } catch (Exception e) {
            log.error("query hive error", e);
        } finally {
            returnConnection(connection);
        }
    }


    public boolean createDatabase(String databaseName) {
        try {
            return HiveUtil.createDatabase(getConnection(), databaseName);
        } catch (Exception e) {
            log.error("query hive error", e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    public boolean dropDatabase(String databaseName) {
        try {
            return HiveUtil.dropDatabase(getConnection(), databaseName);
        } catch (Exception e) {
            log.error("query hive error", e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    public List<String> showTables(String database) {
        try {
            return HiveUtil.showTables(getConnection(), database);
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public boolean dropTable(String tableName) {
        try {
            return HiveUtil.dropTable(getConnection(), tableName);
        } catch (Exception e) {
            log.error("query hive error", e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    public List<HiveField> getTableFields(String tableName) {
        try {
            Collection<HiveField> fields = HiveUtil.getHiveFields(getConnection(), tableName);
            return new ArrayList<>(fields);
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public List<HiveField> getTableFieldsBySql(String sql) {
        try {
            Collection<HiveField> fields = HiveUtil.getHiveFieldsBySql(getConnection(), sql);
            return new ArrayList<>(fields);
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public boolean truncateTable(String tableName) {
        try {
            return HiveUtil.truncateTable(getConnection(), tableName);
        } catch (Exception e) {
            log.error("query hive error", e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    public List<JSONObject> queryTableData(String tableName) {
        try {
            return HiveUtil.executeQuery(getConnection(), "select * from " + tableName + " limit 10");
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public List<JSONObject> query(String sql) {
        try {
            return HiveUtil.executeQuery(getConnection(), sql);
        } catch (Exception e) {
            log.error("query hive error", e);
            return Collections.emptyList();
        } finally {
            returnConnection(connection);
        }
    }

    public boolean execute(String sql) {
        try {
            return HiveUtil.execute(getConnection(), sql);
        } catch (Exception e) {
            log.error("query hive error", e);
            return false;
        } finally {
            returnConnection(connection);
        }
    }

    public void returnConnection(Connection connection) {
        if (isPool) {
            hivePool.releaseConnection(connection);
        }
    }
}
