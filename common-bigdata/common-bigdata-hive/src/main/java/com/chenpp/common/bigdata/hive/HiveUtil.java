package com.chenpp.common.bigdata.hive;

import com.alibaba.fastjson.JSONObject;
import com.chenpp.common.bigdata.security.KerberosConf;
import com.chenpp.common.bigdata.security.LoginUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.jdbc.HiveQueryResultSet;
import org.apache.hive.jdbc.HiveResultSetMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.http.client.config.AuthSchemes.KERBEROS;

/**
 * @author April.Chen
 * @date 2024/7/4 16:08
 */
public class HiveUtil {
    private static Logger logger = LoggerFactory.getLogger(HiveUtil.class);
    private static final String HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    private HiveUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Connection getConnection(HiveConf hiveJdbcConf) throws Exception {
        Class.forName(HIVE_DRIVER);
        if (KERBEROS.equalsIgnoreCase(hiveJdbcConf.getAuth())) {
            KerberosConf kerberosConf = hiveJdbcConf.getKerberosConf();
            LoginUtil.login(kerberosConf.getPrincipal(), kerberosConf.getKeytabPath(), kerberosConf.getKrb5Path());
            String username = UserGroupInformation.getCurrentUser().getUserName();
            logger.info("user: {}", username);
            return DriverManager.getConnection(hiveJdbcConf.getJdbcUrl());
        } else {
            return DriverManager.getConnection(hiveJdbcConf.getJdbcUrl(), hiveJdbcConf.getUsername(), hiveJdbcConf.getPassword());
        }
    }

    public static List<String> showDatabases(Connection connection) throws SQLException {
        String sql = "show databases";
        return executeQuery(connection, sql, rs -> {
            try {
                return rs.getString(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean createDatabase(Connection connection, String databaseName) throws SQLException {
        String sql = "create database " + databaseName;
        return execute(connection, sql);
    }

    public static boolean dropDatabase(Connection connection, String databaseName) throws SQLException {
        String sql = "drop database " + databaseName;
        return execute(connection, sql);
    }

    public static List<String> showTables(Connection connection, String database) throws SQLException {
        String sql = String.format("show tables in %s", database);
        List<JSONObject> result = HiveUtil.executeQuery(connection, sql);
        return result.stream().map(item -> item.getString("tab_name")).collect(Collectors.toList());
    }

    public static boolean dropTable(Connection connection, String tableName) throws SQLException {
        String sql = "drop table " + tableName;
        return execute(connection, sql);
    }

    public static boolean truncateTable(Connection connection, String tableName) throws SQLException {
        String sql = "truncate table " + tableName;
        return execute(connection, sql);
    }

    public static Collection<HiveField> getHiveFields(Connection connection, String tableName) throws SQLException {
        String sql = "desc extended " + tableName;
        Map<String, HiveField> fields = new HashMap<>();
        String partitionedInfo = "Partition Information";
        String tableInfo = "Detailed Table Information";
        String colNameInfo = "col_name";
        boolean enteredPartitionFields = false;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String colName = rs.getString("col_name");
                String dataType = rs.getString("data_type");
                String comment = rs.getString("comment");
                logger.info("hive field info: {}\t{}\t{}", colName, dataType, comment);
                if ("# col_name".equals(colName)) {
                    continue;
                }
                //遇到Detailed Table Information直接中断
                if (StringUtils.contains(colName, tableInfo)) {
                    break;
                }
                if (StringUtils.contains(colName, partitionedInfo)) {
                    enteredPartitionFields = true;
                    continue;
                }
                if (StringUtils.isAnyBlank(colName, dataType)) {
                    continue;
                }
                HiveField field = new HiveField(colName, dataType, comment);
                if (enteredPartitionFields) {
                    if (StringUtils.contains(colName, colNameInfo) || StringUtils.isBlank(colName)) {
                        continue;
                    }
                    field.setPartition(true);
                }
                fields.put(colName, field);
            }
            return fields.values();
        }
    }

    public static Collection<HiveField> getHiveFieldsBySql(Connection connection, String sql) throws SQLException {
        if (!sql.contains("limit")) {
            sql = sql.replace(";", "");
            sql = sql + " limit 1";
        }
        logger.info("run sql: {}", sql);
        List<HiveField> hiveFields = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet resultSet = stmt.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            logger.info("hive meta: {}", resultMetaData);
            int columnCount = resultMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String colName = resultMetaData.getColumnName(i);
                if (colName.contains(".")) {
                    colName = StringUtils.substringAfter(resultMetaData.getColumnName(i), ".");
                }
                String dataType = resultMetaData.getColumnTypeName(i);
                logger.info("hive field info: {}\t{}\t{}", colName, dataType, resultMetaData.getColumnLabel(i));
                hiveFields.add(new HiveField(colName, dataType, null));
            }
            return hiveFields;
        }
    }

    public static List<JSONObject> executeQuery(Connection connection, String sql) {
        List<JSONObject> hiveDataList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            HiveQueryResultSet hs = (HiveQueryResultSet) rs;
            HiveResultSetMetaData data = (HiveResultSetMetaData) hs.getMetaData();
            int cols = data.getColumnCount();
            while (hs.next()) {
                JSONObject dataJson = new JSONObject();
                for (int i = 1; i <= cols; i++) {
                    String colName = data.getColumnName(i);
                    Object obj = hs.getObject(colName);
                    dataJson.put(colName, obj);
                }
                hiveDataList.add(dataJson);
            }
        } catch (Exception e) {
            logger.error("hive query error", e);
        }
        return hiveDataList;
    }

    public static <R> List<R> executeQuery(Connection connection, String sql, Function<ResultSet, R> func) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<R> results = new ArrayList<>();
            while (rs.next()) {
                results.add(func.apply(rs));
            }
            return results;
        }
    }

    public static boolean execute(Connection connection, String sql) throws SQLException {
        logger.info("run sql: {}", sql);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            return stmt.execute();
        }
    }
}
