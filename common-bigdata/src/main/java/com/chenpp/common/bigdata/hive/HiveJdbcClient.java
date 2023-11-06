package com.chenpp.common.bigdata.hive;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;

/**
 * @author April.Chen
 * @date 2023/11/6 4:44 下午
 **/
public class HiveJdbcClient {
    private static Logger logger = LoggerFactory.getLogger(HiveJdbcClient.class);

    private static final String HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    private Connection getConnection(HiveConf hiveJdbcConf) throws SQLException, ClassNotFoundException {
        Class.forName(HIVE_DRIVER);
        if ("KERBEROS".equalsIgnoreCase(hiveJdbcConf.getAuth())) {
            return DriverManager.getConnection(hiveJdbcConf.getJdbcUrl(), "", "");
        } else {
            return DriverManager.getConnection(hiveJdbcConf.getJdbcUrl(), hiveJdbcConf.getUsername(), hiveJdbcConf.getPassword());
        }
    }

    public List<HiveField> getHiveFieldBySql(HiveConf hiveConf, String sql) {
        List<HiveField> hiveFields = new ArrayList<>();

        // 加载Hive JDBC驱动
        try (Connection connection = getConnection(hiveConf);
             Statement statement = connection.createStatement()) {
            if (!sql.contains("limit")) {
                sql = sql.replace(";", "");
                sql = sql + " limit 1";
            }
            logger.info("connection_sql: {}", sql);
            ResultSet resultSet = statement.executeQuery(sql);
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
        } catch (Exception e) {
            logger.error("hive query error", e);
            return null;
        }
    }

    public List<HiveField> getHiveFieldByTable(HiveConf hiveConf, String table) {
        List<HiveField> hiveFields = new ArrayList<>();
        List<String> ptsFields = new ArrayList<>();

        String sql = String.format("describe extended %s", table);

        String partitionedInfo = "Partition Information";
        String tableInfo = "Detailed Table Information";
        String colNameInfo = "col_name";
        boolean enteredPartitionFields = false;
        // 加载Hive JDBC驱动
        try (Connection connection = getConnection(hiveConf);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    String colName = resultSet.getString("col_name");
                    String dataType = resultSet.getString("data_type");
                    String comment = resultSet.getString("comment");

                    logger.info("hive field info: {}\t{}\t{}", colName, dataType, comment);

                    if (StringUtils.contains(colName, partitionedInfo)) {
                        enteredPartitionFields = true;
                        continue;
                    }
                    if (StringUtils.isAllBlank(colName, dataType)) {
                        continue;
                    }
                    //遇到Detailed Table Information直接中断
                    if (StringUtils.contains(colName, tableInfo)) {
                        break;
                    }
                    if (enteredPartitionFields) {
                        if (StringUtils.contains(colName, colNameInfo) || StringUtils.isBlank(colName)) {
                            continue;
                        }
                        ptsFields.add(colName);
                    } else {
                        hiveFields.add(new HiveField(colName, dataType, comment));
                    }
                }
            }

            hiveFields.forEach(item -> {
                if (ptsFields.contains(item.getName())) {
                    item.setPartition(true);
                }
            });
            return hiveFields;
        } catch (Exception e) {
            logger.error("hive query error", e);
            return null;
        }
    }


    public List<JSONObject> getHiveDataByTable(HiveConf hiveConf, String table) {
        List<JSONObject> hiveDataList = new ArrayList<>();
        // 加载Hive JDBC驱动
        try (Connection connection = getConnection(hiveConf);
             Statement statement = connection.createStatement()) {
            String sql = String.format("select * from %s limit 100", table);
            ResultSet resultSet = statement.executeQuery(sql);
            HiveQueryResultSet hs = (HiveQueryResultSet) resultSet;
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

    public void query(HiveConf hiveConf, String sql) {
        try (Connection connection = getConnection(hiveConf);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt(1) + "-------" + rs.getString(2));
            }
        } catch (Exception e) {
            logger.error("hive query error", e);
        }
    }
}
