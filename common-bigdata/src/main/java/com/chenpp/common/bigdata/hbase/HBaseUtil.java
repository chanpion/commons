package com.chenpp.common.bigdata.hbase;

import com.chenpp.common.security.KerberosUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2023/11/6 10:34 上午
 **/
public class HBaseUtil {
    private final static Logger logger = LoggerFactory.getLogger(HBaseUtil.class);

    private static final Map<String, Connection> HBASE_CONN_POOL_MAP = new ConcurrentHashMap<>();

    public static Connection getConnection(HBaseConf hbaseConf) throws IOException {
        return connect(hbaseConf);
    }

    public static Configuration getConfiguration(HBaseConf hbaseConf) throws IOException {
        String hbaseZnode = hbaseConf.getHbaseZnode();
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", hbaseConf.getHbaseHost());
        conf.set("hbase.client.retries.number", "2");
        conf.setInt("hbase.client.operation.timeout", 1000);
        conf.set("zookeeper.recovery.retry", "2");
        if (StringUtils.isNotEmpty(hbaseZnode)) {
            conf.set("zookeeper.znode.parent", hbaseZnode);
        }
        if (StringUtils.isNotBlank(hbaseConf.getHbasePort())) {
            conf.set("hbase.zookeeper.property.clientPort", hbaseConf.getHbasePort());
        }
        if (hbaseConf.getKerberosEnable()) {
            conf.set("hadoop.security.authentication", "kerberos");
            conf.set("hbase.security.authentication", "kerberos");
            conf.set("keytab.file", hbaseConf.getKeytabPath());
            conf.set("kerberos.principal", hbaseConf.getPrincipal());
            conf.set("hbase.regionserver.kerberos.principal", hbaseConf.getRegionServerPrincipal());
            conf.set("hbase.master.kerberos.principal", hbaseConf.getMasterPrincipal());

        } else {
            conf.set("hadoop.security.authentication", "simple");
            conf.set("hbase.security.authentication", "simple");
        }
        //检查配置
        HBaseAdmin.available(conf);
        return conf;
    }

    public static Connection connect(HBaseConf hbaseConf) throws IOException {
        String hbaseQuorum = hbaseConf.getHbaseHost();
        String hbaseZnode = hbaseConf.getHbaseZnode();

        String key = getKey(hbaseQuorum, hbaseZnode);
        Connection connection = HBASE_CONN_POOL_MAP.get(key);
        if (connection == null || connection.isClosed()) {
            synchronized (HBASE_CONN_POOL_MAP) {
                connection = HBASE_CONN_POOL_MAP.get(key);
                if (connection == null || connection.isClosed()) {
                    Configuration conf = getConfiguration(hbaseConf);
                    if (hbaseConf.getKerberosEnable()) {
                        connection = ConnectionFactory.createConnection(conf, KerberosUtil.getAuthenticatedUser(hbaseConf.getKrb5Path(), hbaseConf.getKeytabPath(), hbaseConf.getPrincipal()));
                    } else {
                        connection = ConnectionFactory.createConnection(conf);
                    }
                    HBASE_CONN_POOL_MAP.put(key, connection);
                }
            }
        }
        return connection;
    }

    private static String getKey(String zkQuorum, String hbaseZnode) {
        if (StringUtils.isEmpty(hbaseZnode)) {
            hbaseZnode = "";
        }
        return zkQuorum + hbaseZnode;
    }


    public static boolean tableExists(String tableName, HBaseConf hbaseConf) throws IOException {
        Connection connection = connect(hbaseConf);
        TableName tableNameWithNs = TableName.valueOf(String.format("%s:%s", hbaseConf.getHbaseNs(), tableName));
        return connection.getAdmin().tableExists(tableNameWithNs);
    }

    public static void createTable(HBaseConf hbaseConf, String table, String... columnFamily) throws IOException {
        Connection connection = connect(hbaseConf);
        Admin admin = connection.getAdmin();

        // 1. 判断表是否存在
        if (admin.tableExists(TableName.valueOf(table))) {
            return;
        }

        // 2. 构建表描述构建器
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(table));
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = Arrays.stream(columnFamily)
                .map(cf -> ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build())
                .collect(Collectors.toList());
        // 3. 添加列蔟
        tableDescriptorBuilder.setColumnFamilies(columnFamilyDescriptors);
        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();

        // 4. 创建表
        admin.createTable(tableDescriptor);
    }

    public static void deleteTable(HBaseConf hbaseConf, String tableName) throws IOException {
        Connection connection = connect(hbaseConf);
        Admin admin = connection.getAdmin();
        TableName table = TableName.valueOf(tableName);
        // 1. 判断表是否存在
        if (admin.tableExists(table)) {
            // 2. 禁用表
            admin.disableTable(table);
            // 3. 删除表
            admin.deleteTable(table);
        }
        admin.close();
    }

    public static void add(HBaseConf hbaseConf, String tableName, String rowKey, String columnFamily, String column, String value) throws IOException {
        Connection connection = connect(hbaseConf);
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(columnFamily)
                , Bytes.toBytes(column)
                , Bytes.toBytes(value));
        table.put(put);
        table.close();
    }

    public static void add(HBaseConf hbaseConf, String tableName, String rowKey, String columnFamily, Map<String, Object> data) throws IOException {
        Connection connection = connect(hbaseConf);
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        data.forEach((k, v) -> put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(k), Bytes.toBytes(v.toString())));
        table.put(put);
        table.close();
    }

    public static void deleteRow(HBaseConf hbaseConf, String tableName, String rowKey) throws IOException {
        Connection connection = connect(hbaseConf);
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        }
    }

    public static List<String> queryFields(String tableNameWithNs, HBaseConf hbaseConf) throws Exception {
        try {
            Connection connection = connect(hbaseConf);
            Table table = connection.getTable(TableName.valueOf(tableNameWithNs));

            Scan scan = new Scan();
            scan.setMaxResultSize(5 * 1024);
            scan.setOneRowLimit();

            ResultScanner rs = table.getScanner(scan);

            Set<String> res = new HashSet<>();
            for (Result r : rs) {
                List<String> qualifier = r.listCells().stream()
                        .map(cell -> Bytes.toString(CellUtil.cloneQualifier(cell)))
                        .collect(Collectors.toList());
                res.addAll(qualifier);
            }
            return new ArrayList<>(res);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public static HbaseRow getOneRow(HBaseConf hbaseConf, String tableName, String rowKey, String... columnFamily) throws IOException {
        HbaseRow row;
        if (StringUtils.isBlank(rowKey) || StringUtils.isEmpty(hbaseConf.getHbaseHost()) || StringUtils.isEmpty(hbaseConf.getHbasePort())) {
            return null;
        }
        Connection connection = connect(hbaseConf);
        if (connection == null) {
            return null;
        }
        if (!tableName.contains(".")) {
            tableName = String.format("%s:%s", hbaseConf.getHbaseNs(), tableName);
        }
        TableName queryTable = TableName.valueOf(tableName);

        try (Admin admin = connection.getAdmin()) {
            if (!admin.tableExists(queryTable)) {
                logger.info("hbase table does not exist.,table name:{}", queryTable);
                return null;
            }
        }

        try (Table table = connection.getTable(queryTable)) {
            Get get = new Get(rowKey.getBytes());
            if (columnFamily != null) {
                Arrays.stream(columnFamily).forEach(cf -> get.addFamily(Bytes.toBytes(cf)));
            }
            Result result = table.get(get);
            row = parseData(result);
        }
        return row;
    }

    public static Set<HbaseRow> scan(HBaseConf hbaseConf, String tableName) throws IOException {
        Connection connection = getConnection(hbaseConf);
        Table table = connection.getTable(TableName.valueOf(tableName));

        Scan scan = new Scan();
        scan.setMaxResultSize(5 * 1024);

        ResultScanner rs = table.getScanner(scan);
        Set<HbaseRow> res = new HashSet<>();
        for (Result r : rs) {
            HbaseRow row = parseData(r);
            res.add(row);
        }
        return res;
    }


    public static HbaseRow parseData(Result result) {
        HbaseRow row = new HbaseRow();
        String rowKey = Bytes.toString(result.getRow());
        List<Cell> cellList = result.listCells();
        List<HBaseCell> dataList = cellList.stream().map(cell -> {
            HBaseCell hBaseCell = new HBaseCell();
            String cf = Bytes.toString(CellUtil.cloneFamily(cell));
            String col = Bytes.toString(CellUtil.cloneQualifier(cell));
            String val = Bytes.toString(CellUtil.cloneValue(cell));
            hBaseCell.setColumnFamily(cf);
            hBaseCell.setColumn(col);
            hBaseCell.setValue(val);
            return hBaseCell;
        }).collect(Collectors.toList());
        row.setRowKey(rowKey);
        row.setCells(dataList);
        return row;
    }


    public static void closeAllConnection() {
        HBASE_CONN_POOL_MAP.forEach((k, v) -> {
            try {
                if (v != null && !v.isClosed()) {
                    v.close();
                }
            } catch (IOException ignore) {
            }
        });
    }
}
